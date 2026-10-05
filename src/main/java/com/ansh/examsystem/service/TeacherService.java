package com.ansh.examsystem.service;

import com.ansh.examsystem.model.*;
import com.ansh.examsystem.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeacherService {

    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final QuestionRepository questionRepository;
    private final ExamRepository examRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final UserRepository userRepository;

    public TeacherService(SubjectRepository subjectRepository, ChapterRepository chapterRepository,
                           QuestionRepository questionRepository, ExamRepository examRepository,
                           ExamAttemptRepository examAttemptRepository, UserRepository userRepository) {
        this.subjectRepository = subjectRepository;
        this.chapterRepository = chapterRepository;
        this.questionRepository = questionRepository;
        this.examRepository = examRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.userRepository = userRepository;
    }

    public List<Subject> listMySubjects(Long teacherId) {
        return subjectRepository.findByTeacherId(teacherId);
    }

    private Subject requireOwnedSubject(Long subjectId, Long teacherId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalStateException("Subject not found"));
        if (!subject.getTeacher().getId().equals(teacherId)) {
            throw new IllegalStateException("You don't have access to this subject.");
        }
        return subject;
    }

    private Chapter requireOwnedChapter(Long chapterId, Long teacherId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new IllegalStateException("Chapter not found"));
        requireOwnedSubject(chapter.getSubject().getId(), teacherId);
        return chapter;
    }

    private Exam requireOwnedExam(Long examId, Long teacherId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new IllegalStateException("Exam not found"));
        requireOwnedSubject(exam.getSubject().getId(), teacherId);
        return exam;
    }

    // ---- Chapters ----

    public List<Chapter> listChapters(Long subjectId, Long teacherId) {
        requireOwnedSubject(subjectId, teacherId);
        return chapterRepository.findBySubjectId(subjectId);
    }

    public void createChapter(Long subjectId, String name, Long teacherId) {
        Subject subject = requireOwnedSubject(subjectId, teacherId);
        Chapter chapter = new Chapter();
        chapter.setName(name);
        chapter.setSubject(subject);
        chapterRepository.save(chapter);
    }

    public void deleteChapter(Long chapterId, Long teacherId) {
        requireOwnedChapter(chapterId, teacherId);
        chapterRepository.deleteById(chapterId);
    }

    // ---- Questions ----

    public List<Question> listQuestions(Long chapterId, Long teacherId) {
        requireOwnedChapter(chapterId, teacherId);
        return questionRepository.findByChapterId(chapterId);
    }

    public void createQuestion(Long chapterId, String text, String o1, String o2, String o3, String o4,
                                int correctOption, Long teacherId) {
        Chapter chapter = requireOwnedChapter(chapterId, teacherId);
        Question q = new Question();
        q.setText(text);
        q.setOption1(o1);
        q.setOption2(o2);
        q.setOption3(o3);
        q.setOption4(o4);
        q.setCorrectOption(correctOption);
        q.setChapter(chapter);
        questionRepository.save(q);
    }

    public void deleteQuestion(Long questionId, Long teacherId) {
        Question q = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalStateException("Question not found"));
        requireOwnedChapter(q.getChapter().getId(), teacherId);
        questionRepository.deleteById(questionId);
    }

    // ---- Exams ----

    public List<Exam> listExams(Long subjectId, Long teacherId) {
        requireOwnedSubject(subjectId, teacherId);
        return examRepository.findBySubjectId(subjectId);
    }

    public void createExam(Long subjectId, List<Long> chapterIds, int questionCount, int timerMinutes, Long teacherId) {
        Subject subject = requireOwnedSubject(subjectId, teacherId);
        List<Chapter> chapters = chapterRepository.findAllById(chapterIds);

        if (chapters.isEmpty()) {
            throw new IllegalStateException("Select at least one chapter.");
        }

        for (Chapter c : chapters) {
            if (!c.getSubject().getId().equals(subjectId)) {
                throw new IllegalStateException("One of the selected chapters doesn't belong to this subject.");
            }
        }

        long availableQuestions = chapters.stream()
                .mapToLong(c -> questionRepository.findByChapterId(c.getId()).size())
                .sum();
        if (questionCount > availableQuestions) {
            throw new IllegalStateException(
                    "Only " + availableQuestions + " questions exist across the selected chapters — reduce the question count.");
        }

        Exam exam = new Exam();
        exam.setSubject(subject);
        exam.setChapters(chapters);
        exam.setQuestionCount(questionCount);
        exam.setTimerMinutes(timerMinutes);
        examRepository.save(exam);
    }

    public void startExam(Long examId, Long teacherId) {
        Exam exam = requireOwnedExam(examId, teacherId);
        if (exam.isStarted()) {
            throw new IllegalStateException("This exam has already been started.");
        }
        exam.setStarted(true);
        exam.setStartedAt(LocalDateTime.now());
        examRepository.save(exam);
    }

    // ---- Results ----

    public Exam getExamForResults(Long examId, Long teacherId) {
        return requireOwnedExam(examId, teacherId);
    }

    public List<ExamAttempt> getExamResults(Long examId, Long teacherId) {
        requireOwnedExam(examId, teacherId);
        return examAttemptRepository.findByExamId(examId).stream()
                .filter(a -> a.getSubmittedAt() != null)
                .sorted((a, b) -> b.getScore() - a.getScore())
                .collect(Collectors.toList());
    }

    public List<User> getAbsentees(Long examId, Long teacherId) {
        Exam exam = requireOwnedExam(examId, teacherId);
        List<User> allStudentsInClass = userRepository.findByRoleAndSchoolClassId(
                Role.STUDENT, exam.getSubject().getSchoolClass().getId());
        Set<Long> submittedIds = examAttemptRepository.findByExamId(examId).stream()
                .filter(a -> a.getSubmittedAt() != null)
                .map(a -> a.getStudent().getId())
                .collect(Collectors.toSet());
        return allStudentsInClass.stream()
                .filter(s -> !submittedIds.contains(s.getId()))
                .collect(Collectors.toList());
    }
}
