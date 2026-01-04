package io.kodra.students.repository;

import io.kodra.students.model.Student;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class StudentRepositoryTest {

    @Autowired
    private StudentRepository repository;

    @Test
    void saveAndFindById_persistsStudent() {
        Student student = new Student(null, "Dana", "dana@example.com", 11111);

        Student saved = repository.save(student);

        assertThat(saved.getId()).isNotNull();

        assertThat(repository.findById(saved.getId()))
                .isPresent()
                .hasValueSatisfying(found -> {
                    assertThat(found.getName()).isEqualTo("Dana");
                    assertThat(found.getEmail()).isEqualTo("dana@example.com");
                    assertThat(found.getMobileNumber()).isEqualTo(11111);
                });
    }
}
