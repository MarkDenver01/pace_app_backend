package io.pace.backend.domain;

import io.pace.backend.data.entity.Student;
import io.pace.backend.domain.request.StudentRequest;

import java.util.List;

public interface StudentDomainService {

    List<Student> getPendingStudents();

    Student approveStudent(StudentRequest studentRequest);


}
