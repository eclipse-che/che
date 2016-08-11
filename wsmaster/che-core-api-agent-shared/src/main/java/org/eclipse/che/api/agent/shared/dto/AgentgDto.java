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
package org.eclipse.che.api.agent.shared.dto;

import org.eclipse.che.api.agent.shared.model.Agent;
import org.eclipse.che.dto.shared.DTO;

import java.util.List;
import java.util.Map;

/**
 * @author Anatoliy Bazko
 */
@DTO
public interface AgentgDto extends Agent {
    String getName();

    void setName(String name);

    String getVersion();

    void setVersion(String version);

    List<String> getDependencies();

    void setDependencies(List<String> dependencies);

    String getScript();

    void setScript(String script);

    Map<String, String> getProperties();

    void setProperties(Map<String, String> properties);
}
