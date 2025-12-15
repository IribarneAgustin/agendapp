package com.reservalink.api.service.user;

import com.reservalink.api.controller.request.UserLoginRequest;
import com.reservalink.api.controller.request.UserRegistrationRequest;
import com.reservalink.api.controller.request.UserRequest;
import com.reservalink.api.controller.response.UserAuthResponse;
import com.reservalink.api.repository.RecoverPasswordTokenRepository;
import com.reservalink.api.repository.entity.RecoverPasswordTokenEntity;
import com.reservalink.api.repository.entity.SubscriptionEntity;
import com.reservalink.api.repository.entity.UserEntity;
import com.reservalink.api.domain.User;
import com.reservalink.api.repository.entity.BrandEntity;
import com.reservalink.api.exception.BusinessErrorCodes;
import com.reservalink.api.exception.BusinessRuleException;
import com.reservalink.api.repository.BrandRepository;
import com.reservalink.api.repository.UserRepository;
import com.reservalink.api.security.JWTUtils;
import com.reservalink.api.service.notification.NotificationService;
import com.reservalink.api.service.payment.PaymentService;
import com.reservalink.api.utils.GenericAppConstants;
import com.reservalink.api.utils.TokenHelper;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;


@Service
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JWTUtils jwtUtils;
    private final BrandRepository brandRepository;
    private final PaymentService paymentService;
    private final NotificationService notificationService;
    private final RecoverPasswordTokenRepository recoverPasswordTokenRepository;

    @Value("${api.base.url}")
    private String baseURL;

    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper, BCryptPasswordEncoder passwordEncoder, JWTUtils jwtUtils,
                           BrandRepository brandRepository, PaymentService paymentService, NotificationService notificationService, RecoverPasswordTokenRepository recoverPasswordTokenRepository) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.brandRepository = brandRepository;
        this.paymentService = paymentService;
        this.notificationService = notificationService;
        this.recoverPasswordTokenRepository = recoverPasswordTokenRepository;
    }

    public User register(UserRegistrationRequest userRegistrationRequest) {
        UserEntity userEntity = modelMapper.map(userRegistrationRequest, UserEntity.class);
        userEntity.setEnabled(true);
        userEntity.setPassword(passwordEncoder.encode(userRegistrationRequest.getPassword()));
        try {
            boolean brandAlreadyExists = brandRepository.existsByName(userRegistrationRequest.getBrandName());
            if (brandAlreadyExists) {
                throw new BusinessRuleException(BusinessErrorCodes.UNIQUE_FIELD_ALREADY_EXISTS.name(),
                        Map.of("field", "brandName",
                                "value", userRegistrationRequest.getBrandName()));
            }

            BrandEntity brandEntity = brandRepository.save(BrandEntity.builder()
                    .name(userRegistrationRequest.getBrandName())
                    .build());
            userEntity.setBrandEntity(brandEntity);

            UserEntity savedUserEntity = userRepository.saveAndFlush(userEntity);
            String checkoutLink = paymentService.createSubscriptionCheckoutURL(savedUserEntity.getId());

            SubscriptionEntity subscriptionEntity = SubscriptionEntity.builder()
                    .expired(Boolean.FALSE)
                    .enabled(Boolean.TRUE)
                    .creationDateTime(LocalDateTime.now())
                    .expiration(LocalDateTime.now().plusDays(GenericAppConstants.FREE_TIER_DAYS))
                    .checkoutLink(checkoutLink)
                    .build();
            userEntity.setSubscriptionEntity(subscriptionEntity);

            savedUserEntity = userRepository.saveAndFlush(userEntity);
            notificationService.sendNewUserRegistered(userEntity);

            return modelMapper.map(savedUserEntity, User.class);
        } catch (DataIntegrityViolationException ex) {
            validateSQLConstraints(ex, userRegistrationRequest);
            throw ex;
        }
    }

    @Override
    public UserAuthResponse login(UserLoginRequest userLoginRequest) {
        UserEntity userEntity = this.loadUserByUsername(userLoginRequest.getEmail());
        if(!passwordEncoder.matches(userLoginRequest.getPassword(), userEntity.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        String jwt = jwtUtils.generateToken(userEntity);
        log.info("User logged successfully");
        return UserAuthResponse.builder()
                .token(jwt)
                .id(userEntity.getId())
                .email(userEntity.getUsername())
                .build();
    }

    @Override
    public String findUserIdByBrandName(String brandName) {
        String userId = userRepository.findUserIdByBrandName(brandName);
        if(userId == null) {
            throw new UsernameNotFoundException("User Not Found");
        }
        return userId;
    }

    @Override
    public User findById(UUID id) {
        UserEntity userEntity = userRepository.findById(id.toString())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return modelMapper.map(userEntity, User.class);
    }

    @Override
    public User update(UUID id, UserRequest request) {
        UserEntity userEntity = userRepository.findById(id.toString())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String userIdByBrandName = userRepository.findUserIdByBrandName(request.getBrandName());

        if(userIdByBrandName != null && !userIdByBrandName.equals(userEntity.getId())) {
            throw new BusinessRuleException(BusinessErrorCodes.UNIQUE_FIELD_ALREADY_EXISTS.name(),
                    Map.of("field", "brandName",
                            "value", request.getBrandName()));
        }

        userEntity.setName(request.getName());
        userEntity.setLastName(request.getLastName());
        userEntity.setPhone(request.getPhone());

        BrandEntity brandEntity = userEntity.getBrandEntity();
        brandEntity.setName(request.getBrandName());
        userEntity.setBrandEntity(brandEntity);
        brandRepository.save(brandEntity);

        userEntity = userRepository.save(userEntity);
        return modelMapper.map(userEntity, User.class);
    }

    @Override
    public String getPublicURL(UUID userId) {
        UserEntity userEntity = userRepository.findById(userId.toString())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return this.baseURL + "/" + "reservas/" + userEntity.getBrandEntity().getName();
    }

    @Override
    public UserEntity loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private void validateSQLConstraints(DataIntegrityViolationException ex, UserRegistrationRequest userRegistrationRequest) {
        Throwable rootCause = ex.getRootCause();
        if (rootCause instanceof SQLException) {
            SQLException sqlEx = (SQLException) rootCause;
            String message = sqlEx.getMessage().toUpperCase();
            if (message.contains("UK_USER_EMAIL")) {
                throw new BusinessRuleException(BusinessErrorCodes.UNIQUE_FIELD_ALREADY_EXISTS.name(),
                        Map.of("field", "email",
                                "value", userRegistrationRequest.getEmail()));
            }

        }

    }

    @Override
    public SubscriptionEntity findUserSubscription(UUID userId) {
        UserEntity userEntity = userRepository.findById(userId.toString())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return userEntity.getSubscriptionEntity();
    }

    @Override
    public void requestPasswordChange(String email) {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User email " + email + " not found"));

        TokenHelper.TokenPair tokenPair = TokenHelper.generate();

        RecoverPasswordTokenEntity recoverPasswordTokenEntity = RecoverPasswordTokenEntity.builder()
                .used(false)
                .expiration(LocalDateTime.now().plusMinutes(30))
                .tokenHash(tokenPair.hashedToken())
                .user(userEntity)
                .build();

        recoverPasswordTokenRepository.save(recoverPasswordTokenEntity);

        notificationService.sendResetPasswordRequest(userEntity.getEmail(), tokenPair.rawToken());
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        String tokenHash = TokenHelper.hashToken(token);

        RecoverPasswordTokenEntity tokenEntity = recoverPasswordTokenRepository.findValidToken(tokenHash, LocalDateTime.now())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token"));

        UserEntity user = tokenEntity.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.saveAndFlush(user);

        tokenEntity.setUsed(true);
        recoverPasswordTokenRepository.saveAndFlush(tokenEntity);
    }
}
