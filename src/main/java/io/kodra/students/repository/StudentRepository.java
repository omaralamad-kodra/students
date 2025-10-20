package io.kodra.students.repository;

import io.kodra.students.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    @Override
    Optional<Student> findById(Long id);
}
