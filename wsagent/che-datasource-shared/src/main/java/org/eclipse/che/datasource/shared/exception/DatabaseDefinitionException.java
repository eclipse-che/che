package org.eclipse.che.datasource.shared.exception;

/**
 * Created by test on 7/15/17.
 */
public class DatabaseDefinitionException extends Exception{
    /** Serialization UID. */
    private static final long serialVersionUID = 1L;

    public DatabaseDefinitionException() {
    }


    public DatabaseDefinitionException(final String message) {
        super(message);
    }

    public DatabaseDefinitionException(final Throwable cause) {
        super(cause);
    }

    public DatabaseDefinitionException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public DatabaseDefinitionException(final String message,
                                       final Throwable cause,
                                       final boolean enableSuppression,
                                       final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
