package dev.andrew.uiproxy.model;

public record TriviaQuestion(
        String question,
        String option1,
        String option2,
        String option3,
        String option4,
        int answerIndex) {
}
