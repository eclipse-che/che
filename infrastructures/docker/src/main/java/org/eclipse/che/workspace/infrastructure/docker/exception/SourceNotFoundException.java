package org.eclipse.che.workspace.infrastructure.docker.exception;

import org.eclipse.che.api.workspace.server.spi.InfrastructureException;

/**
 * @author Alexander Garagatyi
 */
public class SourceNotFoundException extends InfrastructureException {
    public SourceNotFoundException(String message) {
        super(message);
    }

    public SourceNotFoundException(Exception e) {
        super(e);
    }

    public SourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
