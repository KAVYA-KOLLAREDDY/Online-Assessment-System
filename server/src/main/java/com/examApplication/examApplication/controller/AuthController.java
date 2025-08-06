package com.examApplication.examApplication.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.examApplication.examApplication.dto.AuthenticationRequestDTO;
import com.examApplication.examApplication.dto.AuthenticationResponseDTO;
import com.examApplication.examApplication.dto.RegistrationRequest;
import com.examApplication.examApplication.dto.RegistrationRequestDTO;
import com.examApplication.examApplication.entity.auth.Role;
import com.examApplication.examApplication.entity.auth.User;
import com.examApplication.examApplication.repository.RoleRepository;
import com.examApplication.examApplication.repository.UserRepository;
import com.examApplication.examApplication.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * The {@code AuthController} class handles all authentication-related
 * endpoints,
 * such as {@code /login}, {@code /register}, {@code /refresh-access-token}, and
 * {@code /logout}.
 * <p>
 * It delegates core logic to the {@link AuthService}.
 * </p>
 * 
 * @author Pavan
 * @version 1.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

	/**
	 * The {@code AuthService} that contains the business logic for authentication
	 * and registration.
	 */
	private final AuthService authService;

	/**
	 * Authenticates a user using the provided credentials and returns an access
	 * token.
	 *
	 * @param request  the {@link AuthenticationRequestDTO} containing
	 *                 username/email and password
	 * @param servReq  the {@link HttpServletRequest} to extract the
	 *                 {@code User-Agent} header
	 * @param response the {@link HttpServletResponse} used to set cookies or
	 *                 headers
	 * @return a {@link ResponseEntity} containing an
	 *         {@link AuthenticationResponseDTO} with the token;
	 *         HTTP status {@code 200 OK}
	 */
	@PostMapping("/login")
	public ResponseEntity<AuthenticationResponseDTO> login(
			@RequestBody AuthenticationRequestDTO request,
			HttpServletRequest servReq,
			HttpServletResponse response) {

		return ResponseEntity.ok(new AuthenticationResponseDTO(HttpStatus.OK,
				authService.login(servReq.getHeader("User-Agent"), request, response)));
	}

	/**
	 * Registers a new user using the provided registration information.
	 *
	 * @param request the {@link RegistrationRequestDTO} containing username, email,
	 *                and password
	 * @return a {@link ResponseEntity} with HTTP status {@code 204 No Content} if
	 *         registration succeeds
	 */
	@PostMapping("/register")
	public ResponseEntity<Void> register(@RequestParam String role,@RequestBody RegistrationRequestDTO request) {
		authService.register(request.getName(), request.getEmail(), request.getPassword(),role);
		
		return ResponseEntity.noContent().build();
	}

	/**
	 * Generates a new access token using the refresh token provided in the request.
	 *
	 * @param request  the {@link HttpServletRequest} used to extract refresh token
	 *                 (e.g. from cookie)
	 * @param response the {@link HttpServletResponse} used to write the new access
	 *                 token
	 * @return a {@link ResponseEntity} with the new access token in an
	 *         {@link AuthenticationResponseDTO};
	 *         HTTP status {@code 201 Created}
	 */
	@PostMapping("/refresh-access-token")
	public ResponseEntity<AuthenticationResponseDTO> refreshAccessToken(
			HttpServletRequest request,
			HttpServletResponse response) {

		String accessToken = authService.generateNewTokenViaRefresh(request, response);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(new AuthenticationResponseDTO(HttpStatus.OK, accessToken));
	}

	/**
	 * Logs the user out by invalidating tokens or session-related data.
	 *
	 * @param request  the {@link HttpServletRequest} containing authentication
	 *                 details
	 * @param response the {@link HttpServletResponse} used to clear cookies or
	 *                 headers
	 * @return a {@link ResponseEntity} with HTTP status {@code 204 No Content}
	 *         after successful logout
	 */
	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
		authService.logout(request, response);
		return ResponseEntity.noContent().build();
	}
}
