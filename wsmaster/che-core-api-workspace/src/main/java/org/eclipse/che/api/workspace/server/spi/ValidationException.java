package org.eclipse.che.api.workspace.server.spi;

/**
 * @author gazarenkov
 */
public class ValidationException extends Exception {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
