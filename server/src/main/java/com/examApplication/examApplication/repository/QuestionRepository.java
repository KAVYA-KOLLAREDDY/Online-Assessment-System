	package com.examApplication.examApplication.repository;
	
	import java.util.List;
	
	import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.examApplication.examApplication.entity.Exam;
import com.examApplication.examApplication.entity.Question;
import com.examApplication.examApplication.entity.SubTopic;
import com.examApplication.examApplication.entity.Subject;
	
public interface QuestionRepository extends JpaRepository<Question, Integer>{
	List<Question> findByExam(Exam exam);
		List<Question> findByExamExamId(int examId);
	    Integer countByExam_ExamId(Integer examId); // what does this do?
	    @Query(value = "SELECT COUNT(*) FROM question_options WHERE question_id = :questionId AND is_correct = true", nativeQuery = true)
	    Integer countCorrectOptionsByQuestionId(@Param("questionId") Integer questionId);

}
