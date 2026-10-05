package com.ansh.examsystem.repository;

import com.ansh.examsystem.model.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassRepository extends JpaRepository<SchoolClass, Long> {
}