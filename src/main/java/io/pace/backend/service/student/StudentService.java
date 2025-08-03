package io.pace.backend.service.student;

import io.pace.backend.data.entity.Role;
import io.pace.backend.data.entity.Student;
import io.pace.backend.data.entity.User;
import io.pace.backend.data.state.RoleState;
import io.pace.backend.domain.StudentDomainService;
import io.pace.backend.domain.request.StudentRequest;
import io.pace.backend.repository.RoleRepository;
import io.pace.backend.repository.StudentRepository;
import io.pace.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService implements StudentDomainService {
    @Autowired
    public StudentRepository studentRepository;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public RoleRepository roleRepository;

    private  PasswordEncoder passwordEncoder;

    @Override
    public List<Student> getPendingStudents() {
        return studentRepository.findByUserAccountStatus(0); // pending
    }

    @Override
    public Student approveStudent(StudentRequest studentRequest) {
        Student student = studentRepository.findById(studentRequest.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));


        // Create user account
        Role userRole = roleRepository.findRoleByRoleState(RoleState.USER)
                .orElseThrow(() -> new RuntimeException("USER role not found"));

        User user = new User();
        user.setUserName(student.getUserName());
        user.setEmail(student.getEmail());
        user.setPassword(passwordEncoder.encode("default123")); // Or random generator
        user.setRole(userRole);

        userRepository.save(user);

        student.setUser(user);
        student.setUserAccountStatus(1); // Approved
        return studentRepository.save(student);
    }
}
