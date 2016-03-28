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

import org.eclipse.che.api.core.factory.FactoryParameter;
import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.dto.shared.DTO;

import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.MANDATORY;

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
    @FactoryParameter(obligation = MANDATORY)
    String getLocation();

    void setLocation(String location);

    MachineSourceDto withLocation(String location);
}
