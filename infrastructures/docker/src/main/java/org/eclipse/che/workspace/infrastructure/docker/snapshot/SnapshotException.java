package org.eclipse.che.workspace.infrastructure.docker.snapshot;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;

/**
 * @author Alexander Garagatyi
 */
public class SnapshotException extends ServerException {
    public SnapshotException(String message) {
        super(message);
    }

    public SnapshotException(ServiceError serviceError) {
        super(serviceError);
    }

    public SnapshotException(Throwable cause) {
        super(cause);
    }

    public SnapshotException(String message, Throwable cause) {
        super(message, cause);
    }
}
