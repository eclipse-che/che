package org.eclipse.che.api.core.model.workspace.runtime;

/**
 * @author gazarenkov
 */
public interface RuntimeIdentity {
    String getWorkspaceId();

    String getEnvName();

    String getOwner();
}
