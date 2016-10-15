/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.machine.shared.dto;

import org.eclipse.che.api.core.rest.shared.dto.Hyperlinks;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.model.machine.Snapshot;
import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * @author Alexander Garagatyi
 */
@DTO
public interface SnapshotDto extends Snapshot, Hyperlinks {
    void setId(String id);

    SnapshotDto withId(String id);

    void setType(String type);

    SnapshotDto withType(String type);

    void setDescription(String description);

    SnapshotDto withDescription(String description);

    void setCreationDate(long creationDate);

    SnapshotDto withCreationDate(long creationDate);

    void setWorkspaceId(String workspaceId);

    SnapshotDto withWorkspaceId(String workspaceId);

    void setDev(boolean isDev);

    SnapshotDto withDev(boolean isDev);

    SnapshotDto withMachineName(String name);

    void setMachineName(String name);

    SnapshotDto withEnvName(String envName);

    void setEnvName(String envName);

    @Override
    SnapshotDto withLinks(List<Link> links);
}
