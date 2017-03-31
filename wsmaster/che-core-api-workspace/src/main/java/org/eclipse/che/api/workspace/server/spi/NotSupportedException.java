package org.eclipse.che.api.workspace.server.spi;

/**
 * @author gazarenkov
 */
public class NotSupportedException extends Exception {
    public NotSupportedException() {
        super("Operation not supported");
    }
}
