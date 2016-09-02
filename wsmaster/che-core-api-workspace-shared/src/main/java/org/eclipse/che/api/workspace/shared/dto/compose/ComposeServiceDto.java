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
package org.eclipse.che.api.workspace.shared.dto.compose;

import org.eclipse.che.api.core.model.workspace.compose.ComposeService;
import org.eclipse.che.dto.shared.DTO;

import java.util.List;
import java.util.Map;

/**
 * @author Alexander Garagatyi
 */
@DTO
public interface ComposeServiceDto extends ComposeService {
    void setImage(String image);

    ComposeServiceDto withImage(String image);

    @Override
    BuildContextDto getBuild();

    void setBuild(BuildContextDto build);

    ComposeServiceDto withBuild(BuildContextDto build);

    void setEntrypoint(List<String> entrypoint);

    ComposeServiceDto withEntrypoint(List<String> entrypoint);

    void setCommand(List<String> command);

    ComposeServiceDto withCommand(List<String> command);

    void setEnvironment(Map<String, String> environment);

    ComposeServiceDto withEnvironment(Map<String, String> environment);

    void setDependsOn(List<String> dependsOn);

    ComposeServiceDto withDependsOn(List<String> dependsOn);

    void setContainerName(String containerName);

    ComposeServiceDto withContainerName(String containerName);

    void setLinks(List<String> links);

    ComposeServiceDto withLinks(List<String> links);

    void setLabels(Map<String, String> labels);

    ComposeServiceDto withLabels(Map<String, String> labels);

    void setExpose(List<String> expose);

    ComposeServiceDto withExpose(List<String> expose);

    void setPorts(List<String> ports);

    ComposeServiceDto withPorts(List<String> ports);

    void setVolumes(List<String> volumes);

    ComposeServiceDto withVolumes(List<String> volumes);

    void setVolumesFrom(List<String> volumesFrom);

    ComposeServiceDto withVolumesFrom(List<String> volumesFrom);

    void setMemLimit(Long memLimit);

    ComposeServiceDto withMemLimit(Long memLimit);
}
