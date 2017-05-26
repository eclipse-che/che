package org.eclipse.che.api.workspace.shared.dto;

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.dto.shared.DTO;

/**
 * @author gazarenkov
 */
@DTO
public interface RuntimeIdentityDto extends RuntimeIdentity {

    @Override
    String getWorkspaceId();

    RuntimeIdentityDto withWorkspaceId(String workspaceId);

    @Override
    String getEnvName();

    RuntimeIdentityDto withEnvName(String envName);

    @Override
    String getOwner();

    RuntimeIdentityDto withOwner(String owner);
}
