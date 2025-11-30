package com.notes.notes.controller;

import com.notes.notes.entity.User;
import com.notes.notes.repository.RoleRepository;
import com.notes.notes.repository.UserRepository;
import com.notes.notes.security.jwt.JwtUtils;
import com.notes.notes.security.request.SignupRequest;
import com.notes.notes.security.services.UserDetailsImpl;
import com.notes.notes.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    UserService userService;

    @GetMapping("/login")
    public String showLoginPage() {
        return "auth/login"; // login.html
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String username,
                               @RequestParam String password,
                               Model model) {

        Authentication authentication;

        try {
            authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (AuthenticationException exception) {
            model.addAttribute("error", "Invalid username or password");
            return "redirect:/auth/login"; // return back with error
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        return "redirect:/auth/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetailsImpl user) {
        if (!user.isVerified()) {
            return "redirect:/auth/verification-pending";
        }
        return "auth/Dashboard";
    }


    @GetMapping("/signup")
    public String showSignupPage(Model model) {
        model.addAttribute("signupRequest", new SignupRequest());
        return "auth/signup"; // signup.html
    }

    @PostMapping("/signup")
    public String processSignup(
            @Valid @ModelAttribute("signupRequest") SignupRequest signupRequest,
            @RequestParam(value = "employeeSignup", required = false) Boolean employeeSignup,
            @AuthenticationPrincipal UserDetailsImpl loggedInUser,
            Model model) {

        if (userRepository.existsByUserName(signupRequest.getUsername())) {
            model.addAttribute("error", "Username already taken");
            return "auth/signup";
        }

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            model.addAttribute("error", "Email already registered");
            return "auth/signup";
        }

        User user = new User(
                signupRequest.getUsername(),
                signupRequest.getEmail(),
                encoder.encode(signupRequest.getPassword())
        );

        user.setEmployeeFullName(signupRequest.getEmployeeFullName());
        user.setEmployeeDepartment(signupRequest.getEmployeeDepartment());
        user.setEmployeePhone(signupRequest.getEmployeePhone());

        // If admin registering employee → auto enable & verify
        if (Boolean.TRUE.equals(employeeSignup) && loggedInUser != null &&
                loggedInUser.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {

            user.setVerified(true);
            user.setEnabled(true);
        }

        userService.saveUser(user);

        model.addAttribute("success", "Employee registered successfully!");
        return "redirect:/auth/login";
    }



    @GetMapping("/verification-pending")
    public String verificationPending() {
        return "auth/Verification-pending";
    }

}
