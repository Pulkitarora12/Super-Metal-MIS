package com.notes.notes.controller;

import com.notes.notes.entity.User;
import com.notes.notes.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class BaseController {

    @Autowired
    private UserService userService;

    @ModelAttribute("loggedInUser")
    public User addLoggedInUserToModel(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            return userService.findByUsername(userDetails.getUsername());
        }
        return null;
    }
}
