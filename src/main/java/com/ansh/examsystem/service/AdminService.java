package com.ansh.examsystem.service;

import com.ansh.examsystem.model.*;
import com.ansh.examsystem.repository.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AdminService {

    private final ClassRepository classRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    public java.util.List<User> listPendingUsers() {
        return userRepository.findAll().stream()
                .filter(u -> !u.isApproved())
                .toList();
    }

    public void approveUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        user.setApproved(true);
        userRepository.save(user);
    }

    public void assignClass(Long userId, Long classId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        SchoolClass sc = classRepository.findById(classId)
                .orElseThrow(() -> new IllegalStateException("Class not found"));
        user.setSchoolClass(sc);
        userRepository.save(user);
    }

    public AdminService(ClassRepository classRepository, SubjectRepository subjectRepository,
            UserRepository userRepository) {
        this.classRepository = classRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
    }

    // Classes
    public List<SchoolClass> listClasses() {
        return classRepository.findAll();
    }

    public void createClass(String name) {
        SchoolClass sc = new SchoolClass();
        sc.setName(name);
        classRepository.save(sc);
    }

    public void deleteClass(Long id) {
        classRepository.deleteById(id);
    }

    // Subjects
    public List<Subject> listSubjects() {
        return subjectRepository.findAll();
    }

    public void createSubject(String name, Long classId, Long teacherId) {
        SchoolClass sc = classRepository.findById(classId)
                .orElseThrow(() -> new IllegalStateException("Class not found"));
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalStateException("Teacher not found"));

        Subject subject = new Subject();
        subject.setName(name);
        subject.setSchoolClass(sc);
        subject.setTeacher(teacher);
        subjectRepository.save(subject);
    }

    public void deleteSubject(Long id) {
        subjectRepository.deleteById(id);
    }

    // Users
    public List<User> listUsers() {
        return userRepository.findAll();
    }

    public List<User> listTeachers() {
        return userRepository.findByRole(Role.TEACHER);
    }

    public void deleteUser(Long id, Long currentAdminId) {
        if (id.equals(currentAdminId)) {
            throw new IllegalStateException("You can't delete your own account while logged in.");
        }
        userRepository.deleteById(id);
    }
}