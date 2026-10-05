package com.ansh.examsystem.service;

import com.ansh.examsystem.model.*;
import com.ansh.examsystem.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private final ExamRepository examRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final QuestionRepository questionRepository;

    public StudentService(ExamRepository examRepository, ExamAttemptRepository examAttemptRepository,
                           QuestionRepository questionRepository) {
        this.examRepository = examRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.questionRepository = questionRepository;
    }

    public List<Exam> listExamsForClass(Long classId) {
        return examRepository.findBySubject_SchoolClass_Id(classId);
    }

    public Map<Long, ExamAttempt> myAttempts(Long studentId) {
        return examAttemptRepository.findByStudentId(studentId).stream()
                .collect(Collectors.toMap(a -> a.getExam().getId(), a -> a));
    }

    public Exam getExamForStudent(Long examId, User student) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new IllegalStateException("Exam not found"));
        if (student.getSchoolClass() == null
                || !exam.getSubject().getSchoolClass().getId().equals(student.getSchoolClass().getId())) {
            throw new IllegalStateException("You don't have access to this exam.");
        }
        return exam;
    }

    public ExamAttempt startOrResumeAttempt(Long examId, User student) {
        Exam exam = getExamForStudent(examId, student);
        if (!"LIVE".equals(exam.getStatus())) {
            throw new IllegalStateException("This exam isn't live right now.");
        }

        Optional<ExamAttempt> existing = examAttemptRepository.findByStudentIdAndExamId(student.getId(), examId);
        if (existing.isPresent()) {
            if (existing.get().getSubmittedAt() != null) {
                throw new IllegalStateException("You've already submitted this exam.");
            }
            return existing.get();
        }

        List<Long> pool = exam.getChapters().stream()
                .flatMap(c -> questionRepository.findByChapterId(c.getId()).stream())
                .map(Question::getId)
                .collect(Collectors.toList());
        Collections.shuffle(pool);
        List<Long> selected = pool.stream().limit(exam.getQuestionCount()).collect(Collectors.toList());

        ExamAttempt attempt = new ExamAttempt();
        attempt.setStudent(student);
        attempt.setExam(exam);
        attempt.setQuestionIds(selected.stream().map(String::valueOf).collect(Collectors.joining(",")));
        return examAttemptRepository.save(attempt);
    }

    public List<Question> getAttemptQuestions(ExamAttempt attempt) {
        List<Long> ids = Arrays.stream(attempt.getQuestionIds().split(","))
                .map(Long::parseLong).collect(Collectors.toList());
        Map<Long, Question> byId = questionRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Question::getId, q -> q));
        return ids.stream().map(byId::get).collect(Collectors.toList());
    }

    public int submitAttempt(Long examId, User student, Map<Long, Integer> answers) {
        ExamAttempt attempt = examAttemptRepository.findByStudentIdAndExamId(student.getId(), examId)
                .orElseThrow(() -> new IllegalStateException("No attempt found — start the exam first."));
        if (attempt.getSubmittedAt() != null) {
            throw new IllegalStateException("Already submitted.");
        }

        List<Question> questions = getAttemptQuestions(attempt);
        int score = 0;
        StringBuilder answerStr = new StringBuilder();
        for (Question q : questions) {
            Integer chosen = answers.get(q.getId());
            if (chosen != null && chosen == q.getCorrectOption()) score++;
            answerStr.append(q.getId()).append(":").append(chosen == null ? 0 : chosen).append(",");
        }

        attempt.setAnswers(answerStr.toString());
        attempt.setScore(score);
        attempt.setSubmittedAt(LocalDateTime.now());
        examAttemptRepository.save(attempt);
        return score;
    }
}
