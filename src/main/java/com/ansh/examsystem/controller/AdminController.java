package com.ansh.examsystem.controller;

import com.ansh.examsystem.model.User;
import com.ansh.examsystem.repository.UserRepository;
import com.ansh.examsystem.service.AdminService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final UserRepository userRepository;

    public AdminController(AdminService adminService, UserRepository userRepository) {
        this.adminService = adminService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("classCount", adminService.listClasses().size());
        model.addAttribute("subjectCount", adminService.listSubjects().size());
        model.addAttribute("userCount", adminService.listUsers().size());
        return "admin/dashboard";
    }

    // ---- Classes ----
    @GetMapping("/classes")
    public String classes(Model model) {
        model.addAttribute("classes", adminService.listClasses());
        return "admin/classes";
    }

    @PostMapping("/classes")
    public String createClass(@RequestParam String name) {
        adminService.createClass(name);
        return "redirect:/admin/classes";
    }

    @PostMapping("/classes/{id}/delete")
    public String deleteClass(@PathVariable Long id, Model model) {
        try {
            adminService.deleteClass(id);
        } catch (Exception e) {
            model.addAttribute("error", "Can't delete a class that still has subjects.");
            model.addAttribute("classes", adminService.listClasses());
            return "admin/classes";
        }
        return "redirect:/admin/classes";
    }

    // ---- Subjects ----
    @GetMapping("/subjects")
    public String subjects(Model model) {
        model.addAttribute("subjects", adminService.listSubjects());
        model.addAttribute("classes", adminService.listClasses());
        model.addAttribute("teachers", adminService.listTeachers());
        return "admin/subjects";
    }

    @PostMapping("/subjects")
    public String createSubject(@RequestParam String name, @RequestParam Long classId,
                                 @RequestParam Long teacherId, Model model) {
        try {
            adminService.createSubject(name, classId, teacherId);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("subjects", adminService.listSubjects());
            model.addAttribute("classes", adminService.listClasses());
            model.addAttribute("teachers", adminService.listTeachers());
            return "admin/subjects";
        }
        return "redirect:/admin/subjects";
    }

    @PostMapping("/subjects/{id}/delete")
    public String deleteSubject(@PathVariable Long id) {
        adminService.deleteSubject(id);
        return "redirect:/admin/subjects";
    }

    // ---- Users ----
    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", adminService.listUsers());
        model.addAttribute("classes", adminService.listClasses());
        return "admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, Authentication authentication, Model model) {
        User currentAdmin = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in admin not found"));
        try {
            adminService.deleteUser(id, currentAdmin.getId());
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("users", adminService.listUsers());
            model.addAttribute("classes", adminService.listClasses());
            return "admin/users";
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/approve")
    public String approveUser(@PathVariable Long id) {
        adminService.approveUser(id);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/assign-class")
    public String assignClass(@PathVariable Long id, @RequestParam Long classId) {
        adminService.assignClass(id, classId);
        return "redirect:/admin/users";
    }
}
