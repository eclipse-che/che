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
package org.eclipse.che.plugin.maven.generator.archetype.dto;

import org.eclipse.che.dto.shared.DTO;

/**
 * DTO for project generating task.
 *
 * @author Artem Zatsarynnyi
 */
@DTO
public interface GenerationTaskDescriptor {
    Status getStatus();

    void setStatus(Status status);

    GenerationTaskDescriptor withStatus(Status status);

    String getStatusUrl();

    void setStatusUrl(String StatusUrl);

    GenerationTaskDescriptor withStatusUrl(String StatusUrl);

    String getDownloadUrl();

    void setDownloadUrl(String downloadUrl);

    GenerationTaskDescriptor withDownloadUrl(String downloadUrl);

    String getReport();

    void setReport(String report);

    GenerationTaskDescriptor withReport(String report);

    enum Status {
        IN_PROGRESS,
        SUCCESSFUL,
        FAILED
    }
}
