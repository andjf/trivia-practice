package dev.andrew.lake.model;

import io.github.sashirestela.openai.common.ResponseFormat.JsonSchema;

public record TriviaQuestion(
        String question,
        String option1,
        String option2,
        String option3,
        String option4,
        int answerIndex) {

    public static JsonSchema jsonSchema() {
        return JsonSchema.builder()
                .name("TriviaQuestion")
                .description("A trivia question with 4 options")
                .schemaClass(TriviaQuestion.class)
                .build();
    }

}
