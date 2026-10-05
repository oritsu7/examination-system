package com.ansh.examsystem.controller;

import com.ansh.examsystem.model.Role;
import com.ansh.examsystem.repository.ClassRepository;
import com.ansh.examsystem.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final AuthService authService;
    private final ClassRepository classRepository;

    public AuthController(AuthService authService, ClassRepository classRepository) {
        this.authService = authService;
        this.classRepository = classRepository;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/signup")
    public String signupPage(Model model) {
        model.addAttribute("classes", classRepository.findAll());
        return "auth/signup";
    }

    @PostMapping("/signup")
    public String signup(@RequestParam String name,
                          @RequestParam String email,
                          @RequestParam String password,
                          @RequestParam Role role,
                          @RequestParam(required = false) Long classId,
                          Model model) {
        try {
            authService.registerUser(name, email, password, role, classId);
            return "redirect:/login?registered";
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("classes", classRepository.findAll());
            return "auth/signup";
        }
    }
}
