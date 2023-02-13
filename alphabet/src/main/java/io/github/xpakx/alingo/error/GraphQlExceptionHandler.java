package io.github.xpakx.alingo.error;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import io.github.xpakx.alingo.game.error.NotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;

@Component
public class GraphQlExceptionHandler  extends DataFetcherExceptionResolverAdapter {
    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        if(ex instanceof ConstraintViolationException) {
            return GraphqlErrorBuilder.newError()
                    .errorType(ErrorType.BAD_REQUEST)
                    .message(ex.getMessage())
                    .path(env.getExecutionStepInfo().getPath())
                    .location(env.getField().getSourceLocation())
                    .build();
        }
        if(ex instanceof RuntimeException) {
            return GraphqlErrorBuilder.newError()
                    .errorType(getStatus(ex))
                    .message(ex.getMessage())
                    .path(env.getExecutionStepInfo().getPath())
                    .location(env.getField().getSourceLocation())
                    .build();
        }
        return null;
    }


    private ErrorType getStatus(Throwable ex) {
        ResponseStatus status = AnnotatedElementUtils.findMergedAnnotation(ex.getClass(), ResponseStatus.class);
        if(status == null) {
            return ErrorType.INTERNAL_ERROR;
        }
        if(status.code() == HttpStatus.NOT_FOUND) {
            return ErrorType.NOT_FOUND;
        }
        if(status.code() == HttpStatus.BAD_REQUEST) {
            return ErrorType.BAD_REQUEST;
        }
        return ErrorType.INTERNAL_ERROR;
    }
}
