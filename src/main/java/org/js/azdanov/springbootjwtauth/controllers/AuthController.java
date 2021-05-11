package org.js.azdanov.springbootjwtauth.controllers;

import org.js.azdanov.springbootjwtauth.dto.request.LoginRequest;
import org.js.azdanov.springbootjwtauth.dto.request.SignupRequest;
import org.js.azdanov.springbootjwtauth.dto.response.JwtResponse;
import org.js.azdanov.springbootjwtauth.dto.response.MessageResponse;
import org.js.azdanov.springbootjwtauth.models.Role;
import org.js.azdanov.springbootjwtauth.models.User;
import org.js.azdanov.springbootjwtauth.models.enums.RoleEnum;
import org.js.azdanov.springbootjwtauth.repository.RoleRepository;
import org.js.azdanov.springbootjwtauth.repository.UserRepository;
import org.js.azdanov.springbootjwtauth.security.jwt.JwtUtils;
import org.js.azdanov.springbootjwtauth.security.services.UserDetailsImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder encoder, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                new JwtResponse(
                        jwt,
                        userDetails.getId(),
                        userDetails.getUsername(),
                        userDetails.getEmail(),
                        roles));
    }

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> registerUser(
            @Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail(), encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository
                    .findByName(RoleEnum.ROLE_USER)
                    .orElseThrow(getRuntimeExceptionSupplier("Error: Role is not found."));
            userRole.getUsers().add(user);
            roles.add(userRole);
        } else {
            strRoles.forEach(
                    role -> {
                        switch (role) {
                            case "admin" -> {
                                Role adminRole = roleRepository
                                        .findByName(RoleEnum.ROLE_ADMIN)
                                        .orElseThrow(getRuntimeExceptionSupplier("Error: Role is not found."));
                                adminRole.getUsers().add(user);
                                roles.add(adminRole);
                            }
                            case "moderator" -> {
                                Role modRole = roleRepository
                                        .findByName(RoleEnum.ROLE_MODERATOR)
                                        .orElseThrow(getRuntimeExceptionSupplier("Error: Role is not found."));
                                modRole.getUsers().add(user);
                                roles.add(modRole);
                            }
                            default -> {
                                Role userRole = roleRepository
                                        .findByName(RoleEnum.ROLE_USER)
                                        .orElseThrow(getRuntimeExceptionSupplier("Error: Role is not found."));
                                userRole.getUsers().add(user);
                                roles.add(userRole);
                            }
                        }
                    });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    private Supplier<RuntimeException> getRuntimeExceptionSupplier(String message) {
        return () -> new RuntimeException(message);
    }
}
