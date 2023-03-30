package io.github.xpakx.alingo.sound.graphql;

import io.github.xpakx.alingo.sound.graphql.dto.ServiceResponse;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class SubgraphController {
    @QueryMapping
    public ServiceResponse service() {
        return new ServiceResponse(
                "sound",
                "1",
                """
                        type Files {
                            files: [String]!
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
                            getSounds(page: Int): Files!
                            service: Service!
                        }
                        """);
    }
}
