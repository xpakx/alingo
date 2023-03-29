package io.github.xpakx.alingo.user.graphql;

import io.github.xpakx.alingo.user.graphql.dto.ServiceResponse;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class SubgraphController {
    @QueryMapping
    public ServiceResponse service() {
        return new ServiceResponse(
                "auth",
                "1",
                """
                        type AuthenticationResponse {
                            token: String!
                            username: String!
                            moderator_role: Boolean
                        }
                                                
                        type Mutation {
                            login(username: String, password: String): AuthenticationResponse!
                            register(username: String, password: String, passwordRe: String): AuthenticationResponse!
                        }
                                                
                        type Service {
                          name: String!
                          version: String!
                          schema: String!
                        }
                                                
                        type Query {
                          service: Service!
                        }
                        """);
    }
}
