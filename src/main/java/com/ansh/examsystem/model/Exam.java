package com.ansh.examsystem.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "exams")
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @ManyToMany
    @JoinTable(name = "exam_chapters",
        joinColumns = @JoinColumn(name = "exam_id"),
        inverseJoinColumns = @JoinColumn(name = "chapter_id"))
    private List<Chapter> chapters;

    private int questionCount;
    private int timerMinutes;

    private boolean started = false;
    private LocalDateTime startedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }

    public List<Chapter> getChapters() { return chapters; }
    public void setChapters(List<Chapter> chapters) { this.chapters = chapters; }

    public int getQuestionCount() { return questionCount; }
    public void setQuestionCount(int questionCount) { this.questionCount = questionCount; }

    public int getTimerMinutes() { return timerMinutes; }
    public void setTimerMinutes(int timerMinutes) { this.timerMinutes = timerMinutes; }

    public boolean isStarted() { return started; }
    public void setStarted(boolean started) { this.started = started; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    // Derived, not persisted — plain methods with no backing field are ignored by Hibernate
    public LocalDateTime getDeadline() {
        return started ? startedAt.plusMinutes(timerMinutes) : null;
    }

    public String getStatus() {
        if (!started) return "NOT_STARTED";
        return LocalDateTime.now().isAfter(getDeadline()) ? "ENDED" : "LIVE";
    }
}
