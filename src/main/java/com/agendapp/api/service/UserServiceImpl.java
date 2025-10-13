package com.agendapp.api.service;

import com.agendapp.api.controller.request.UserLoginRequest;
import com.agendapp.api.controller.request.UserRegistrationRequest;
import com.agendapp.api.controller.request.UserRequest;
import com.agendapp.api.controller.response.UserAuthResponse;
import com.agendapp.api.dto.UserDTO;
import com.agendapp.api.entity.Brand;
import com.agendapp.api.entity.Subscription;
import com.agendapp.api.entity.User;
import com.agendapp.api.exception.BusinessErrorCodes;
import com.agendapp.api.exception.BusinessRuleException;
import com.agendapp.api.repository.BrandRepository;
import com.agendapp.api.repository.UserRepository;
import com.agendapp.api.security.JWTUtils;
import com.agendapp.api.utils.GenericAppConstants;
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

    @Value("${api.base.url}")
    private String baseURL;

    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper, BCryptPasswordEncoder passwordEncoder, JWTUtils jwtUtils, BrandRepository brandRepository) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.brandRepository = brandRepository;
    }

    public UserDTO register(UserRegistrationRequest userRegistrationRequest) {
        User userEntity = modelMapper.map(userRegistrationRequest, User.class);
        userEntity.setEnabled(true);
        userEntity.setPassword(passwordEncoder.encode(userRegistrationRequest.getPassword()));
        try {
            boolean brandAlreadyExists = brandRepository.existsByName(userRegistrationRequest.getBrandName());
            if (brandAlreadyExists) {
                throw new BusinessRuleException(BusinessErrorCodes.UNIQUE_FIELD_ALREADY_EXISTS.name(),
                        Map.of("field", "brandName",
                                "value", userRegistrationRequest.getBrandName()));
            }

            Brand brand = brandRepository.save(Brand.builder()
                    .name(userRegistrationRequest.getBrandName())
                    .build());
            userEntity.setBrand(brand);

            Subscription subscription = Subscription.builder()
                    .expired(Boolean.FALSE)
                    .enabled(Boolean.TRUE)
                    .creationDateTime(LocalDateTime.now())
                    .expiration(LocalDateTime.now().plusDays(GenericAppConstants.FREE_TIER_DAYS))
                    .build();
            userEntity.setSubscription(subscription);

            User savedUser = userRepository.saveAndFlush(userEntity);
            return modelMapper.map(savedUser, UserDTO.class);
        } catch (DataIntegrityViolationException ex) {
            validateSQLConstraints(ex, userRegistrationRequest);
            throw ex;
        }
    }

    @Override
    public UserAuthResponse login(UserLoginRequest userLoginRequest) {
        User user = this.loadUserByUsername(userLoginRequest.getEmail());
        if(!passwordEncoder.matches(userLoginRequest.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        String jwt = jwtUtils.generateToken(user);
        log.info("User logged successfully");
        return UserAuthResponse.builder()
                .token(jwt)
                .id(user.getId())
                .email(user.getUsername())
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
    public UserDTO findById(UUID id) {
        User user = userRepository.findById(id.toString())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return modelMapper.map(user, UserDTO.class);
    }

    @Override
    public UserDTO update(UUID id, UserRequest request) {
        User user = userRepository.findById(id.toString())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String userIdByBrandName = userRepository.findUserIdByBrandName(request.getBrandName());

        if(userIdByBrandName != null && !userIdByBrandName.equals(user.getId())) {
            throw new BusinessRuleException(BusinessErrorCodes.UNIQUE_FIELD_ALREADY_EXISTS.name(),
                    Map.of("field", "brandName",
                            "value", request.getBrandName()));
        }

        user.setName(request.getName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());

        Brand brand = user.getBrand();
        brand.setName(request.getBrandName());
        user.setBrand(brand);
        brandRepository.save(brand);

        user = userRepository.save(user);
        return modelMapper.map(user, UserDTO.class);
    }

    @Override
    public String getPublicURL(UUID userId) {
        User user = userRepository.findById(userId.toString())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return this.baseURL + "/" + "reservas/" + user.getBrand().getName();
    }

    @Override
    public User loadUserByUsername(String email) throws UsernameNotFoundException {
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
}
