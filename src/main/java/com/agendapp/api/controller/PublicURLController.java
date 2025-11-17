package com.agendapp.api.controller;

import com.agendapp.api.service.user.UserService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PublicURLController {

    private final UserService userService;

    public PublicURLController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/reservas/{brandname}")
    public String renderUserOfferingsPage(@PathVariable String brandname) {
        String userId = null;
        try {
            userId = userService.findUserIdByBrandName(brandname);
        } catch (UsernameNotFoundException e) {
            return "forward:/404.html";
        }
        return "redirect:/public/user-offerings.html?userId=" + userId;
    }
}