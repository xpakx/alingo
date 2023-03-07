package io.github.xpakx.alingo.error;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.Objects;

@Component
public class GraphQlExceptionHandler extends DataFetcherExceptionResolverAdapter {
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

    @Override
    protected List<GraphQLError> resolveToMultipleErrors(Throwable ex, DataFetchingEnvironment env) {
        if(ex instanceof ConstraintViolationException) {
            return ((ConstraintViolationException) ex).getConstraintViolations()
                    .stream()
                    .map(ConstraintViolation::getMessage)
                    .filter(Objects::nonNull)
                    .map(s -> toGraphQLError(s, env))
                    .toList();
        }
        return super.resolveToMultipleErrors(ex, env);
    }

    private GraphQLError toGraphQLError(String s, DataFetchingEnvironment env) {
        return GraphqlErrorBuilder.newError()
                .errorType(ErrorType.BAD_REQUEST)
                .message(s)
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .build();
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
