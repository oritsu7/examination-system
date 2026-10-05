package com.ansh.examsystem.service;

import com.ansh.examsystem.model.Role;
import com.ansh.examsystem.model.SchoolClass;
import com.ansh.examsystem.model.User;
import com.ansh.examsystem.repository.ClassRepository;
import com.ansh.examsystem.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final ClassRepository classRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, ClassRepository classRepository,
                        PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.classRepository = classRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerUser(String name, String email, String rawPassword, Role role, Long classId) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("An account with this email already exists.");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setApproved(false);

        if (role == Role.STUDENT) {
            if (classId == null) {
                throw new IllegalStateException("Please select your class.");
            }
            SchoolClass schoolClass = classRepository.findById(classId)
                    .orElseThrow(() -> new IllegalStateException("Selected class not found."));
            user.setSchoolClass(schoolClass);
        }

        userRepository.save(user);
    }
}
