package org.eclipse.che.workspace.infrastructure.docker;

import org.eclipse.che.api.workspace.server.spi.ValidationException;

import static java.lang.String.format;

/**
 * @author Alexander Garagatyi
 */
public class ArgumentsValidator {
    /**
     * Checks that object reference is not null, throws {@link ValidationException} otherwise.
     *
     * <p>Exception uses error message built from error message template and error message parameters.
     */
    public static void checkNotNull(Object object, String errorMessageTemplate, Object... errorMessageParams)
            throws ValidationException {
        if (object == null) {
            throw new ValidationException(format(errorMessageTemplate, errorMessageParams));
        }
    }

    public static void checkArgument(boolean expression, String error) throws ValidationException {
        if (expression) {
            throw new ValidationException(error);
        }
    }

    public static void checkArgument(boolean expression, String errorMessageTemplate, Object... errorMessageParams)
            throws ValidationException {
        if (expression) {
            throw new ValidationException(format(errorMessageTemplate, errorMessageParams));
        }
    }

    private ArgumentsValidator() {}
}
