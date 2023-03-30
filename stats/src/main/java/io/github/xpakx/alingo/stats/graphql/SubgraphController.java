package io.github.xpakx.alingo.stats.graphql;

import io.github.xpakx.alingo.stats.graphql.dto.ServiceResponse;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class SubgraphController {
    @QueryMapping
    public ServiceResponse service() {
        return new ServiceResponse(
                "stats",
                "1",
                """
                        type Page {
                            content: [Guess]!
                            totalPages: Int
                            totalElements: Int
                            last: Boolean
                            size: Int
                            number: Int
                            numberOfElements: Int
                            first: Boolean
                            empty: Boolean
                        }
                                                
                        type Guess {
                            id: ID!
                            username: String!
                            correct: Boolean!
                            letter: String
                            exerciseId: Int
                            courseId: Int
                            courseName: String
                            language: String
                        }
                        
                        type Service {
                            name: String!
                            version: String!
                            schema: String!
                        }
                                                
                        type Mutation {
                            dummy: String
                        }
                                                
                        type Query {
                            getGuesses(username: String, page: Int, amount: Int): Page!
                            service: Service!
                        }
                        """);
    }
}
