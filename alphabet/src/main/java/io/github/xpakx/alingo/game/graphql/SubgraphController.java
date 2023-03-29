package io.github.xpakx.alingo.game.graphql;

import io.github.xpakx.alingo.game.graphql.dto.ServiceResponse;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class SubgraphController {
    @QueryMapping
    public ServiceResponse service() {
        return new ServiceResponse(
                "alphabet",
                "1",
                """
                        type ExerciseDto {
                            id: ID!
                            options: [String!]!
                        }
                                                
                        type ExercisesResponse {
                            page: Int!
                            size: Int!
                            totalSize: Int!
                            exercises: [ExerciseDto]!
                        }
                                                
                        type AnswerResponse {
                            correct: Boolean!
                            correctAnswer: String!
                        }
                                                
                        type Language {
                            id: ID!
                            name: String!
                        }
                                                
                        type Course {
                            id: ID!
                            name: String!
                            description: String
                            difficulty: String
                        }
                                                
                        type CourseData {
                            id: ID!
                            name: String!
                            description: String
                            difficulty: String
                            premium: Boolean!
                            language: Language
                        }
                                                
                        type CourseForList {
                            id: ID!
                            name: String!
                            description: String
                            difficulty: String
                            premium: Boolean!
                        }
                                                
                        type CourseList {
                            page: Int!
                            size: Int!
                            totalSize: Int!
                            courses: [CourseForList]!
                        }
                                                
                        type CourseMin {
                            id: ID!
                            name: String!
                        }
                                                
                        type Exercise {
                            id: ID!
                            letter: String
                            wrongAnswer: String!
                            correctAnswer: String!
                            order: Int!
                        }
                                                
                        type ExerciseData {
                            id: ID!
                            letter: String
                            wrongAnswer: String!
                            correctAnswer: String!
                            order: Int!
                        }
                                                
                        enum Difficulty {
                            EASY
                            MEDIUM
                            HARD
                            VERY_HARD
                        }
                                                
                        type Service {
                            name: String!
                            version: String!
                            schema: String!
                        }
                                                
                        type Query {
                            courseExercises(course: Int, page: Int, amount: Int): ExercisesResponse!
                            getLanguage(id: ID): Language!
                            getLanguages(page: Int, amount: Int): [Language]!
                            getCoursesForLanguage(languageId: ID, page: Int, amount: Int): CourseList!
                            getCourse(id: ID): CourseData!
                            getExercise(id: ID): ExerciseData!
                            service: Service!
                        }
                                                
                        type Mutation {
                            answer(exercise: Int, guess: String): AnswerResponse!
                            addLanguage(name: String): Language!
                            editLanguage(languageId: ID, name: String): Language!
                            addCourse(name: String, description: String, difficulty: Difficulty, languageId: Int, premium: Boolean): Course!
                            editCourse(courseId: ID, name: String, description: String, difficulty: Difficulty, languageId: Int, premium: Boolean): Course!
                            addExercise(letter: String, wrongAnswer: String, correctAnswer: String, courseId: Int, sound: String): Exercise!
                            editExercise(exerciseId: ID, letter: String, wrongAnswer: String, correctAnswer: String, courseId: Int, sound: String): Exercise!
                            reorderExercise(exerciseId: ID, order: Int): Exercise!
                        }
                        """);
    }
}
