package io.pace.backend.domain.model.response;

import java.util.List;

public class StudentListResponse {
    private int total;
    private List<StudentResponse> students;

    public StudentListResponse(int total, List<StudentResponse> students) {
        this.total = total;
        this.students = students;
    }

    public int getTotal() {
        return total;
    }

    public List<StudentResponse> getStudents() {
        return students;
    }
}
