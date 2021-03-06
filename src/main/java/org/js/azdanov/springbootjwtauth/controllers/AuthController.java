package org.js.azdanov.springbootjwtauth.controllers;

import org.js.azdanov.springbootjwtauth.dto.request.LoginRequest;
import org.js.azdanov.springbootjwtauth.dto.request.SignupRequest;
import org.js.azdanov.springbootjwtauth.dto.request.TokenRefreshRequest;
import org.js.azdanov.springbootjwtauth.dto.response.JwtResponse;
import org.js.azdanov.springbootjwtauth.dto.response.MessageResponse;
import org.js.azdanov.springbootjwtauth.dto.response.TokenRefreshResponse;
import org.js.azdanov.springbootjwtauth.models.RefreshToken;
import org.js.azdanov.springbootjwtauth.models.Role;
import org.js.azdanov.springbootjwtauth.models.User;
import org.js.azdanov.springbootjwtauth.models.enums.RoleEnum;
import org.js.azdanov.springbootjwtauth.repository.RoleRepository;
import org.js.azdanov.springbootjwtauth.repository.UserRepository;
import org.js.azdanov.springbootjwtauth.security.exception.TokenRefreshException;
import org.js.azdanov.springbootjwtauth.security.jwt.JwtUtils;
import org.js.azdanov.springbootjwtauth.security.services.RefreshTokenService;
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
    private final RefreshTokenService refreshTokenService;

    public AuthController(
        AuthenticationManager authenticationManager,
        UserRepository userRepository,
        RoleRepository roleRepository,
        PasswordEncoder encoder,
        JwtUtils jwtUtils,
        RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
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

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        return ResponseEntity.ok(
            new JwtResponse(
                jwt,
                refreshToken.getToken(),
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<TokenRefreshResponse> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
            .map(refreshTokenService::verifyExpiration)
            .map(RefreshToken::getUser)
            .map(user -> {
                String token = jwtUtils.generateWithSubject(user.getUsername());
                return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
            })
            .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                "Refresh token is not in database!"));
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
        Set<Role> roles = strRoles
            .stream()
            .map(role -> switch (role) {
                case "admin" -> assignRole(user, RoleEnum.ROLE_ADMIN);
                case "moderator" -> assignRole(user, RoleEnum.ROLE_MODERATOR);
                default -> assignRole(user, RoleEnum.ROLE_USER);
            })
            .collect(Collectors.collectingAndThen(
                Collectors.toUnmodifiableSet(), set -> set.isEmpty()
                    ? Set.of(assignRole(user, RoleEnum.ROLE_USER))
                    : set
            ));

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    private Role assignRole(User user, RoleEnum roleUser) {
        Role userRole = roleRepository
            .findByName(roleUser)
            .orElseThrow(getRuntimeExceptionSupplier("Error: Role is not found."));
        userRole.getUsers().add(user);
        return userRole;
    }

    private Supplier<RuntimeException> getRuntimeExceptionSupplier(String message) {
        return () -> new RuntimeException(message);
    }
}
