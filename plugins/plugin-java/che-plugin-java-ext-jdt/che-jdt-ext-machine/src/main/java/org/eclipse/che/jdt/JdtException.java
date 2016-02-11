package org.eclipse.che.jdt;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;

/**
 * Base exception for all JDT exceptions
 *
 * @author Evgen Vidolob
 */
public class JdtException extends ServerException {
    public JdtException(String message) {
        super(message);
    }

    public JdtException(ServiceError serviceError) {
        super(serviceError);
    }

    public JdtException(Throwable cause) {
        super(cause);
    }

    public JdtException(String message, Throwable cause) {
        super(message, cause);
    }
}
