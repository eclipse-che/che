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
import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.dto.shared.DTO;

import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.MANDATORY;
import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.OPTIONAL;

/**
 * @author Alexander Garagatyi
 */
@DTO
public interface MachineSourceDto extends MachineSource {
    @Override
    @FactoryParameter(obligation = MANDATORY)
    String getType();

    void setType(String type);

    MachineSourceDto withType(String type);

    @Override
    @FactoryParameter(obligation = OPTIONAL)
    String getLocation();

    void setLocation(String location);

    MachineSourceDto withLocation(String location);

    /**
     * @return content of the machine source. No need to use an external link.
     */
    @Override
    @FactoryParameter(obligation = OPTIONAL)
    String getContent();

    /**
     * Defines the new content to use for this machine source.
     * Alternate way is to provide a location
     * @param content the content instead of an external link like with location
     */
    void setContent(String content);

    /**
     * Defines the new content to use for this machine source.
     * Alternate way is to provide a location
     * @param content the content instead of an external link like with location
     * @return the current intance of the object
     */
    MachineSourceDto withContent(String content);

}
