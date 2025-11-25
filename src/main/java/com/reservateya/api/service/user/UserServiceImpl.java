package com.reservateya.api.service.user;

import com.reservateya.api.controller.request.UserLoginRequest;
import com.reservateya.api.controller.request.UserRegistrationRequest;
import com.reservateya.api.controller.request.UserRequest;
import com.reservateya.api.controller.response.UserAuthResponse;
import com.reservateya.api.repository.entity.SubscriptionEntity;
import com.reservateya.api.repository.entity.UserEntity;
import com.reservateya.api.domain.User;
import com.reservateya.api.repository.entity.BrandEntity;
import com.reservateya.api.exception.BusinessErrorCodes;
import com.reservateya.api.exception.BusinessRuleException;
import com.reservateya.api.repository.BrandRepository;
import com.reservateya.api.repository.UserRepository;
import com.reservateya.api.security.JWTUtils;
import com.reservateya.api.service.payment.PaymentService;
import com.reservateya.api.utils.GenericAppConstants;
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

    @Value("${api.base.url}")
    private String baseURL;

    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper, BCryptPasswordEncoder passwordEncoder, JWTUtils jwtUtils,
                           BrandRepository brandRepository, PaymentService paymentService) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.brandRepository = brandRepository;
        this.paymentService = paymentService;
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
            String checkoutLink = paymentService.createCheckoutLink(savedUserEntity.getEmail(), savedUserEntity.getId());

            SubscriptionEntity subscriptionEntity = SubscriptionEntity.builder()
                    .expired(Boolean.FALSE)
                    .enabled(Boolean.TRUE)
                    .creationDateTime(LocalDateTime.now())
                    .expiration(LocalDateTime.now().plusDays(GenericAppConstants.FREE_TIER_DAYS))
                    .checkoutLink(checkoutLink)
                    .build();
            userEntity.setSubscriptionEntity(subscriptionEntity);

            savedUserEntity = userRepository.saveAndFlush(userEntity);

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
}
