package org.eclipse.che.plugin.docker.client.exception;

/**
 * Occurs when docker container is not found.
 *
 * @author Mykola Morhun
 */
public class ContainerNotFoundException extends DockerException {

    public ContainerNotFoundException(String message) {
        super(message, 404);
    }

}
