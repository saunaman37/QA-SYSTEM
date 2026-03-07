package com.example.qabackend.repository;

import com.example.qabackend.entity.Question;
import com.example.qabackend.entity.QuestionStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByDeletedAtIsNull(Sort sort);

    List<Question> findByDeletedAtIsNullAndStatus(QuestionStatus status, Sort sort);

    @Query("SELECT q FROM Question q WHERE q.deletedAt IS NULL AND (q.title LIKE %:keyword% OR q.content LIKE %:keyword%)")
    List<Question> findByKeyword(@Param("keyword") String keyword, Sort sort);

    @Query("SELECT q FROM Question q WHERE q.deletedAt IS NULL AND (q.title LIKE %:keyword% OR q.content LIKE %:keyword%) AND q.status = :status")
    List<Question> findByKeywordAndStatus(@Param("keyword") String keyword, @Param("status") QuestionStatus status, Sort sort);

    Optional<Question> findByIdAndDeletedAtIsNull(Long id);
}
