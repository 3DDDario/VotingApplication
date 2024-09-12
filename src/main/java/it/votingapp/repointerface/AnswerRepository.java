package it.votingapp.repointerface;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import it.votingapp.entity.Answer;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
	
	List<Answer> findByQuestionId(Long questionId);

}
