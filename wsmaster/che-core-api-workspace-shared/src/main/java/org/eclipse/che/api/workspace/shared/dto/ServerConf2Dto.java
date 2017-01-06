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
package org.eclipse.che.api.workspace.shared.dto;

import org.eclipse.che.api.core.factory.FactoryParameter;
import org.eclipse.che.api.core.model.workspace.ServerConf2;
import org.eclipse.che.dto.shared.DTO;

import java.util.Map;

import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.MANDATORY;
import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.OPTIONAL;

/**
 * @author Alexander Garagatyi
 */
@DTO
public interface ServerConf2Dto extends ServerConf2 {
    @Override
    @FactoryParameter(obligation = MANDATORY)
    String getPort();

    void setPort(String port);

    ServerConf2Dto withPort(String port);

    @Override
    @FactoryParameter(obligation = MANDATORY)
    String getProtocol();

    void setProtocol(String protocol);

    ServerConf2Dto withProtocol(String protocol);

    @Override
    @FactoryParameter(obligation = OPTIONAL)
    Map<String, String> getProperties();

    void setProperties(Map<String, String> properties);

    ServerConf2Dto withProperties(Map<String, String> properties);
}
