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

import org.eclipse.che.api.core.factory.FactoryParameter;
import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.dto.shared.DTO;

import java.util.Map;

import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.MANDATORY;
import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.OPTIONAL;

/**
 * @author Alexander Garagatyi
 */
@DTO
public interface CommandDto extends Command {

    @Override
    @FactoryParameter(obligation = MANDATORY)
    String getName();

    void setName(String name);

    CommandDto withName(String name);

    @Override
    @FactoryParameter(obligation = MANDATORY)
    String getCommandLine();

    void setCommandLine(String commandLine);

    CommandDto withCommandLine(String commandLine);

    @Override
    @FactoryParameter(obligation = MANDATORY)
    String getType();

    void setType(String type);

    CommandDto withType(String type);

    @Override
    @FactoryParameter(obligation = OPTIONAL)
    Map<String, String> getAttributes();

    void setAttributes(Map<String, String> attributes);

    CommandDto withAttributes(Map<String, String> attributes);
}
