package it.votingapp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.votingapp.entity.Answer;
import it.votingapp.repointerface.AnswerRepository;

@Service
public class AnswerService {
	
    @Autowired
    private AnswerRepository answerRepository;
    
    public List<Answer> getAllAnswers() {
        return answerRepository.findAll();
    }

    public List<Answer> getAnswersByQuestionId(Long questionId) {
        return answerRepository.findByQuestionId(questionId);
    }

    @Transactional
    public void saveAnswer(Answer answer) {
        answerRepository.save(answer);
    }
    
    @Transactional
    public void deleteAnswer(long answerId) {
    	answerRepository.deleteById(answerId);
    	answerRepository.flush();
    }
}
