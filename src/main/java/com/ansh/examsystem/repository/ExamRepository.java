package com.ansh.examsystem.repository;

import com.ansh.examsystem.model.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findBySubjectId(Long subjectId);
    List<Exam> findBySubject_SchoolClass_Id(Long classId);
}