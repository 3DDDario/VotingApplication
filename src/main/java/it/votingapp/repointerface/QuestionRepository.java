package it.votingapp.repointerface;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import it.votingapp.entity.Question;

public interface QuestionRepository extends JpaRepository<Question, Long>{
	
	 @Query("SELECT q FROM Question q LEFT JOIN FETCH q.answers")
	    List<Question> findAll();

}
