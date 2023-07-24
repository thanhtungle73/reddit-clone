package com.thanhtungle.redditclone.service.impl;

import com.thanhtungle.redditclone.exception.NotFoundException;
import com.thanhtungle.redditclone.exception.ServiceException;
import com.thanhtungle.redditclone.model.dto.AuthenticationResponseDto;
import com.thanhtungle.redditclone.model.dto.LoginRequestDto;
import com.thanhtungle.redditclone.model.dto.RegisterRequestDto;
import com.thanhtungle.redditclone.model.entity.NotificationEmail;
import com.thanhtungle.redditclone.model.entity.User;
import com.thanhtungle.redditclone.model.entity.VerificationToken;
import com.thanhtungle.redditclone.repository.UserRepository;
import com.thanhtungle.redditclone.repository.VerificationTokenRepository;
import com.thanhtungle.redditclone.service.AuthService;
import com.thanhtungle.redditclone.service.MailService;
import com.thanhtungle.redditclone.service.TokenService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final MailService mailService;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    @Transactional
    public void signup(RegisterRequestDto registerRequest) {
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setCreated(Instant.now());
        user.setEnabled(false);
        userRepository.save(user);

        String token = generateVerificationToken(user);

        NotificationEmail notificationEmail = new NotificationEmail(
                "Please activate your account",
                user.getEmail(),
                "Welcome to Reddit clone, we're glad to have you. "
                        + "\nPlease click on the below url to activate your account: "
                        + "\nhttp://localhost:8080/api/v1/auth/accountVerification/" + token);
        mailService.sendMail(notificationEmail);
    }

    private String generateVerificationToken(User user) {
        String token = UUID.randomUUID().toString();

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationTokenRepository.save(verificationToken);

        return token;
    }

    public void verifyAccount(String token) {
        Optional<VerificationToken> verificationToken = verificationTokenRepository.findByToken(token);
        verificationToken.orElseThrow(() -> new ServiceException("Invalid token."));
        fetchUserAndEnable(verificationToken.get());
    }

    @Transactional
    private void fetchUserAndEnable(VerificationToken verificationToken) {
        String username = verificationToken.getUser().getUsername();
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new ServiceException("User cannot be found with username - " + username)
        );
        user.setEnabled(true);
        userRepository.save(user);
    }

    public AuthenticationResponseDto login(LoginRequestDto loginRequestBody) {
           Authentication auth = authenticationManager.authenticate(
                   new UsernamePasswordAuthenticationToken(loginRequestBody.getUsername(), loginRequestBody.getPassword())
           );
           User user = userRepository.findByUsername(loginRequestBody.getUsername()).orElseThrow(() ->
                   new NotFoundException("User with username" + loginRequestBody.getUsername() + " could not be found.")
           );
           user.setPassword(null);
           String token = tokenService.generateToken(auth);

           return new AuthenticationResponseDto(user, token);
    }
}