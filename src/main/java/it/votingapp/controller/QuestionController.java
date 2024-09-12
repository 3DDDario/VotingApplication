package it.votingapp.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import it.votingapp.entity.Answer;
import it.votingapp.entity.Question;
import it.votingapp.service.AnswerService;
import it.votingapp.service.QuestionService;

@Controller
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AnswerService answerService;

    @GetMapping("/survey-creation")
    public String surveyCreation(Model model) {
    	List<Question> questions = questionService.getAllQuestions();
        questions.forEach(question -> question.setAnswers(new ArrayList<>(question.getAnswers())));
        questions.forEach(question -> question.getAnswers().sort(Comparator.comparing(Answer::getId)));
        model.addAttribute("questions", questions);
        model.addAttribute("questionsSize", questions.size());
        return "survey-creation";
    }
    
    @DeleteMapping("/question/delete/{id}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/answer/delete/{id}")
    public ResponseEntity<?> deleteAnswer(@PathVariable Long id) {
        System.out.println("Received request to delete answer with ID: " + id);
        try {
            answerService.deleteAnswer(id);
            System.out.println("Answer deleted successfully");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.out.println("Error deleting answer: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/saveAll")
    public String saveAll(@RequestParam Map<String, String> allParams) {
        // Create a map to store questions by their index
        Map<Integer, Question> questionMap = new LinkedHashMap<>();
        
        // Loop through all parameters to identify questions and answers
        for (String paramName : allParams.keySet()) {
            if (paramName.startsWith("questions[")) {
                String[] parts = paramName.split("\\.");
                int questionIndex = Integer.parseInt(parts[0].replace("questions[", "").replace("]", ""));
                
                // Retrieve or create the Question object
                Question question = questionMap.getOrDefault(questionIndex, new Question());
                
                if (parts[1].equals("text")) {
                    question.setText(allParams.get(paramName));
                } else if (parts[1].equals("id")) {
                    try {
                        long questionId = extractNumericId(allParams.get(paramName));
                        question.setId(questionId); // Ensure you have a method to set the ID
                    } catch (NumberFormatException e) {
                        // Handle the case where parsing fails
                        System.err.println("Invalid question ID format: " + allParams.get(paramName));
                    }
                } else if (parts[1].startsWith("answers")) {
                    int answerIndex = Integer.parseInt(parts[1].split("\\[")[1].replace("]", ""));
                    
                    List<Answer> answers = question.getAnswers();
                    if (answers == null) {
                        answers = new ArrayList<>();
                        question.setAnswers(answers);
                    }
                    
                    // Use a separate counter for each question to keep track of the answer index
                    while (answers.size() <= answerIndex) {
                        answers.add(new Answer());
                    }
                    
                    Answer answer = answers.get(answerIndex);
                    
                    if (parts[2].equals("text")) {
                        answer.setText(allParams.get(paramName));
                    } else if (parts[2].equals("id")) {
                        try {
                            long answerId = extractNumericId(allParams.get(paramName));
                            answer.setId(answerId); // Ensure you have a method to set the ID
                        } catch (NumberFormatException e) {
                            // Handle the case where parsing fails
                            System.err.println("Invalid answer ID format: " + allParams.get(paramName));
                        }
                    }
                    
                    // Link the answer to the correct question
                    answer.setQuestion(question);
                }
                
                // Put the question back into the map
                questionMap.put(questionIndex, question);
            }
        }
        
        // Save each question along with its answers
        for (Question question : questionMap.values()) {
            for (Answer answer : question.getAnswers()) {
                System.out.println("Saving answer: " + answer.getText() + " for question: " + question.getText());
            }
            questionService.saveQuestion(question);
        }
        
        return "redirect:/survey-creation";
    }

    // Helper method to extract numeric ID from the formatted string
    private long extractNumericId(String idString) throws NumberFormatException {
        // Extract the numeric part from the ID string
        String numericId = idString.replaceAll("\\D", ""); // Remove non-numeric characters
        return Long.parseLong(numericId);
    }


    // Displays a list of questions for users to choose answers
    @GetMapping("/survey")
    public String showQuestions(Model model) {
        List<Question> questions = questionService.getAllQuestions();
        questions.forEach(question -> question.setAnswers(new ArrayList<>(question.getAnswers())));
        questions.forEach(question -> question.getAnswers().sort(Comparator.comparing(Answer::getId)));
        model.addAttribute("questions", questions);
        return "survey";
    }

    // Submits the selected answers from the "survey" page and saves them in the database
    @PostMapping("/submitAnswers")
    public String submitAnswers(@RequestParam Map<String, String> allParams, Model model) {
        List<String> selectedAnswers = new ArrayList<>();

        try {
            // Fetch all questions from the database
            List<Question> questions = questionService.getAllQuestions();

            for (String paramName : allParams.keySet()) {
                if (paramName.startsWith("question_")) {
                    int questionIndex = Integer.parseInt(paramName.replace("question_", ""));
                    int answerIndex = Integer.parseInt(allParams.get(paramName));

                    if (questionIndex >= 0 && questionIndex < questions.size()) {
                        Question question = questions.get(questionIndex);

                        // Fetch answers for the question using the custom method
                        List<Answer> answers = answerService.getAnswersByQuestionId(question.getId());
                        if (answerIndex >= 0 && answerIndex < answers.size()) {
                            Answer selectedAnswer = answers.get(answerIndex);

                            // Increment the counter for the selected answer
                            selectedAnswer.incrementCounter();

                            selectedAnswers.add((question.getText()) + ": " + selectedAnswer.getText());

                            // Save the selected answer with the updated counter
                            answerService.saveAnswer(selectedAnswer);
                        }
                    }
                }
            }

            // Add both selectedAnswers and questions to the model
            model.addAttribute("selectedAnswers", selectedAnswers);
            model.addAttribute("questions", questions); // Ensure questions are added to the model

        } catch (NumberFormatException e) {
            model.addAttribute("message", "Invalid input. Please try again.");
            e.printStackTrace();
        }

        return "result"; // Return the name of the Thymeleaf template
    }
}