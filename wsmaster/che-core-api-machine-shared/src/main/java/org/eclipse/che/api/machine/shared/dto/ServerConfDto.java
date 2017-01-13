/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.machine.shared.dto;

import org.eclipse.che.api.core.model.machine.ServerConf;
import org.eclipse.che.dto.shared.DTO;

/**
 * @author Alexander Garagatyi
 */
@DTO
public interface ServerConfDto extends ServerConf {
    void setRef(String ref);

    ServerConfDto withRef(String ref);

    void setPort(String port);

    ServerConfDto withPort(String port);

    void setProtocol(String protocol);

    ServerConfDto withProtocol(String protocol);

    void setPath(String path);

    ServerConfDto withPath(String path);
}
