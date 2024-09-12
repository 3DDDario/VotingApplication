-- Create the database (if it doesn't exist)
CREATE DATABASE IF NOT EXISTS votingapp;

-- Use the created database
USE votingapp;

-- Create the Question table
CREATE TABLE question (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    text VARCHAR(255)
);

-- Create the Answer table with a foreign key to the Question table
CREATE TABLE answer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    text VARCHAR(255),
    counter BIGINT DEFAULT 0,
    question_id BIGINT,
    FOREIGN KEY (question_id) REFERENCES question(id) ON DELETE CASCADE
);

-- Snippet to check submitted Questions/Answers manually
USE votingapp;
SELECT * FROM answer;
SELECT * FROM question, answer WHERE question.id = answer.question_id;

-- Drop the tables if they already exist to start fresh
DROP TABLE IF EXISTS answer;
DROP TABLE IF EXISTS question;