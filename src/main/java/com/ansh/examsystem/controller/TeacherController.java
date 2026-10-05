package com.ansh.examsystem.controller;

import com.ansh.examsystem.model.User;
import com.ansh.examsystem.repository.UserRepository;
import com.ansh.examsystem.service.TeacherService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/teacher")
public class TeacherController {

    private final TeacherService teacherService;
    private final UserRepository userRepository;

    public TeacherController(TeacherService teacherService, UserRepository userRepository) {
        this.teacherService = teacherService;
        this.userRepository = userRepository;
    }

    private User currentUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found"));
    }

    @GetMapping
    public String dashboard(Model model, Authentication auth) {
        User teacher = currentUser(auth);
        model.addAttribute("subjects", teacherService.listMySubjects(teacher.getId()));
        return "teacher/dashboard";
    }

    // ---- Chapters ----

    @GetMapping("/subjects/{subjectId}/chapters")
    public String chapters(@PathVariable Long subjectId, Model model, Authentication auth) {
        User teacher = currentUser(auth);
        model.addAttribute("subjectId", subjectId);
        model.addAttribute("chapters", teacherService.listChapters(subjectId, teacher.getId()));
        return "teacher/chapters";
    }

    @PostMapping("/subjects/{subjectId}/chapters")
    public String createChapter(@PathVariable Long subjectId, @RequestParam String name, Authentication auth) {
        User teacher = currentUser(auth);
        teacherService.createChapter(subjectId, name, teacher.getId());
        return "redirect:/teacher/subjects/" + subjectId + "/chapters";
    }

    @PostMapping("/chapters/{chapterId}/delete")
    public String deleteChapter(@PathVariable Long chapterId, @RequestParam Long subjectId, Authentication auth) {
        User teacher = currentUser(auth);
        teacherService.deleteChapter(chapterId, teacher.getId());
        return "redirect:/teacher/subjects/" + subjectId + "/chapters";
    }

    // ---- Questions ----

    @GetMapping("/chapters/{chapterId}/questions")
    public String questions(@PathVariable Long chapterId, Model model, Authentication auth) {
        User teacher = currentUser(auth);
        model.addAttribute("chapterId", chapterId);
        model.addAttribute("questions", teacherService.listQuestions(chapterId, teacher.getId()));
        return "teacher/questions";
    }

    @PostMapping("/chapters/{chapterId}/questions")
    public String createQuestion(@PathVariable Long chapterId,
            @RequestParam String text,
            @RequestParam String option1,
            @RequestParam String option2,
            @RequestParam String option3,
            @RequestParam String option4,
            @RequestParam int correctOption,
            Authentication auth) {
        User teacher = currentUser(auth);
        teacherService.createQuestion(chapterId, text, option1, option2, option3, option4, correctOption,
                teacher.getId());
        return "redirect:/teacher/chapters/" + chapterId + "/questions";
    }

    @PostMapping("/questions/{questionId}/delete")
    public String deleteQuestion(@PathVariable Long questionId, @RequestParam Long chapterId, Authentication auth) {
        User teacher = currentUser(auth);
        teacherService.deleteQuestion(questionId, teacher.getId());
        return "redirect:/teacher/chapters/" + chapterId + "/questions";
    }

    // ---- Exams ----

    @GetMapping("/subjects/{subjectId}/exams")
    public String exams(@PathVariable Long subjectId, Model model, Authentication auth) {
        User teacher = currentUser(auth);
        model.addAttribute("subjectId", subjectId);
        model.addAttribute("exams", teacherService.listExams(subjectId, teacher.getId()));
        model.addAttribute("chapters", teacherService.listChapters(subjectId, teacher.getId()));
        return "teacher/exams";
    }

    @PostMapping("/subjects/{subjectId}/exams")
    public String createExam(@PathVariable Long subjectId,
            @RequestParam(required = false) List<Long> chapterIds,
            @RequestParam int questionCount,
            @RequestParam int timerMinutes,
            Authentication auth, Model model) {
        User teacher = currentUser(auth);
        List<Long> ids = chapterIds != null ? chapterIds : List.of();
        try {
            teacherService.createExam(subjectId, ids, questionCount, timerMinutes, teacher.getId());
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("subjectId", subjectId);
            model.addAttribute("exams", teacherService.listExams(subjectId, teacher.getId()));
            model.addAttribute("chapters", teacherService.listChapters(subjectId, teacher.getId()));
            return "teacher/exams";
        }
        return "redirect:/teacher/subjects/" + subjectId + "/exams?created";
    }

    @PostMapping("/exams/{examId}/start")
    public String startExam(@PathVariable Long examId, @RequestParam Long subjectId, Authentication auth) {
        User teacher = currentUser(auth);
        teacherService.startExam(examId, teacher.getId());
        return "redirect:/teacher/subjects/" + subjectId + "/exams";
    }

    @GetMapping("/exams/{examId}/results")
    public String results(@PathVariable Long examId, Model model, Authentication auth) {
        User teacher = currentUser(auth);
        model.addAttribute("exam", teacherService.getExamForResults(examId, teacher.getId()));
        model.addAttribute("attempts", teacherService.getExamResults(examId, teacher.getId()));
        model.addAttribute("absentees", teacherService.getAbsentees(examId, teacher.getId()));
        return "teacher/exam-results";
    }
}
