package io.kodra.students.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kodra.students.model.Student;
import io.kodra.students.repository.StudentRepository;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WebMvcTest(StudentController.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudentRepository repository;

    @Test
    void addStudent_returnsSavedStudent() throws Exception {
        Student payload = new Student(null, "Alice", "alice@example.com", 12345);
        Student saved = new Student(1L, "Alice", "alice@example.com", 12345);
        when(repository.save(any(Student.class))).thenReturn(saved);

        mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.mobileNumber").value(12345));
    }

    @Test
    void getStudents_returnsAllStudents() throws Exception {
        List<Student> students = Arrays.asList(
                new Student(1L, "Alice", "alice@example.com", 12345),
                new Student(2L, "Bob", "bob@example.com", 67890)
        );
        when(repository.findAll()).thenReturn(students);

        mockMvc.perform(get("/students"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[1].name").value("Bob"));
    }

    @Test
    void getStudentById_returnsStudentWhenPresent() throws Exception {
        Student student = new Student(10L, "Charlie", "charlie@example.com", 55555);
        when(repository.findById(10L)).thenReturn(Optional.of(student));

        mockMvc.perform(get("/students/10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Charlie"));
    }

    @Test
    void getStudentById_returnsServerErrorWhenMissing() throws Exception {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mockMvc.perform(get("/students/99")))
                .isInstanceOf(ServletException.class)
                .hasRootCauseInstanceOf(RuntimeException.class)
                .rootCause()
                .hasMessage("Student not found");
    }
}
