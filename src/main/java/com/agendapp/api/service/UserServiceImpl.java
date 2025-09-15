package com.agendapp.api.service;

import com.agendapp.api.controller.request.UserLoginRequest;
import com.agendapp.api.controller.request.UserRegistrationRequest;
import com.agendapp.api.controller.response.UserAuthResponse;
import com.agendapp.api.dto.UserDTO;
import com.agendapp.api.entity.User;
import com.agendapp.api.repository.UserRepository;
import com.agendapp.api.security.JWTUtils;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JWTUtils jwtUtils;

    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper, BCryptPasswordEncoder passwordEncoder, JWTUtils jwtUtils) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    public UserDTO register(UserRegistrationRequest userRegistrationRequest) {
        User userEntity = modelMapper.map(userRegistrationRequest, User.class);
        userEntity.setActive(true);
        userEntity.setPassword(passwordEncoder.encode(userRegistrationRequest.getPassword()));
        return modelMapper.map(userRepository.save(userEntity), UserDTO.class);
    }

    @Override
    public UserAuthResponse login(UserLoginRequest userLoginRequest) {
        UserDetails userDetails = this.loadUserByUsername(userLoginRequest.getEmail());
        if(!passwordEncoder.matches(userLoginRequest.getPassword(), userDetails.getPassword())) {
            throw new UsernameNotFoundException("Invalid credentials");
        }
        String jwt = jwtUtils.generateToken(userDetails);
        return UserAuthResponse.builder()
                .token(jwt)
                .email(userDetails.getUsername())
                .build();
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
