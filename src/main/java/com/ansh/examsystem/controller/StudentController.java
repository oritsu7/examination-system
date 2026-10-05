package com.ansh.examsystem.controller;

import com.ansh.examsystem.model.ExamAttempt;
import com.ansh.examsystem.model.Question;
import com.ansh.examsystem.model.User;
import com.ansh.examsystem.repository.UserRepository;
import com.ansh.examsystem.service.StudentService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/student")
public class StudentController {

    private final StudentService studentService;
    private final UserRepository userRepository;

    public StudentController(StudentService studentService, UserRepository userRepository) {
        this.studentService = studentService;
        this.userRepository = userRepository;
    }

    private User currentUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found"));
    }

    @GetMapping
    public String dashboard(Model model, Authentication auth) {
        User student = currentUser(auth);
        if (student.getSchoolClass() == null) {
            model.addAttribute("noClass", true);
            model.addAttribute("exams", Collections.emptyList());
            model.addAttribute("attempts", Collections.emptyMap());
            return "student/dashboard";
        }
        model.addAttribute("noClass", false);
        model.addAttribute("exams", studentService.listExamsForClass(student.getSchoolClass().getId()));
        model.addAttribute("attempts", studentService.myAttempts(student.getId()));
        return "student/dashboard";
    }

    @GetMapping("/exams/{examId}/attempt")
    public String attempt(@PathVariable Long examId, Model model, Authentication auth) {
        User student = currentUser(auth);
        try {
            ExamAttempt attempt = studentService.startOrResumeAttempt(examId, student);
            List<Question> questions = studentService.getAttemptQuestions(attempt);
            model.addAttribute("exam", studentService.getExamForStudent(examId, student));
            model.addAttribute("questions", questions);
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return "student/exam-error";
        }
        return "student/take-exam";
    }

    @PostMapping("/exams/{examId}/attempt")
    public String submit(@PathVariable Long examId, @RequestParam Map<String, String> allParams, Authentication auth) {
        User student = currentUser(auth);
        Map<Long, Integer> answers = new HashMap<>();
        for (Map.Entry<String, String> e : allParams.entrySet()) {
            if (e.getKey().startsWith("answer_")) {
                Long qid = Long.parseLong(e.getKey().substring("answer_".length()));
                answers.put(qid, Integer.parseInt(e.getValue()));
            }
        }
        studentService.submitAttempt(examId, student, answers);
        return "redirect:/student/exams/" + examId + "/result";
    }

    @GetMapping("/exams/{examId}/result")
    public String result(@PathVariable Long examId, Model model, Authentication auth) {
        User student = currentUser(auth);
        ExamAttempt attempt = studentService.myAttempts(student.getId()).get(examId);
        model.addAttribute("attempt", attempt);
        return "student/result";
    }
}
