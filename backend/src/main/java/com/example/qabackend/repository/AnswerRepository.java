package com.example.qabackend.repository;

import com.example.qabackend.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    List<Answer> findByQuestionIdAndDeletedAtIsNullOrderByCreatedAtAsc(Long questionId);

    long countByQuestionIdAndDeletedAtIsNull(Long questionId);

    Optional<Answer> findByIdAndDeletedAtIsNull(Long id);
}
