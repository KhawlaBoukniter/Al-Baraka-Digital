package org.albarakadigital.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/")
    public String root(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        String role = authentication.getAuthorities().toString();
        if (role.contains("CLIENT")) return "redirect:/client/dashboard";
        if (role.contains("AGENT_BANCAIRE")) return "redirect:/agent/dashboard";
        if (role.contains("ADMIN")) return "redirect:/admin/dashboard";
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/client/dashboard")
    public String clientDashboard(Model model, Authentication auth) {
        model.addAttribute("email", auth.getName());
        return "client/dashboard";
    }

    @GetMapping("/agent/dashboard")
    public String agentDashboard(Model model, Authentication auth) {
        model.addAttribute("email", auth.getName());
        return "agent/dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model, Authentication auth) {
        model.addAttribute("email", auth.getName());
        return "admin/dashboard";
    }
}