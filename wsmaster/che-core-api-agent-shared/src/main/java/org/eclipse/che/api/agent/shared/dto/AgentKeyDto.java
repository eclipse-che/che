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

import org.eclipse.che.api.agent.shared.model.AgentKey;
import org.eclipse.che.dto.shared.DTO;

/**
 * @author Anatolii Bazko
 */
@DTO
public interface AgentKeyDto extends AgentKey {

    String getName();

    void setName(String name);

    AgentKeyDto withName(String name);

    String getVersion();

    void setVersion(String version);

    AgentKeyDto withVersion(String version);
}
