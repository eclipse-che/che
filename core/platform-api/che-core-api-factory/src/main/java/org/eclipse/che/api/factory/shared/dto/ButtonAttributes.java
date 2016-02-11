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
package org.eclipse.che.api.factory.shared.dto;

import org.eclipse.che.api.core.factory.FactoryParameter;
import org.eclipse.che.dto.shared.DTO;

import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.OPTIONAL;

/**
 * @author Alexander Garagatyi
 */
@DTO
public interface ButtonAttributes {
    @FactoryParameter(obligation = OPTIONAL)
    String getColor();

    void setColor(String color);

    ButtonAttributes withColor(String color);

    @FactoryParameter(obligation = OPTIONAL)
    Boolean getCounter();

    void setCounter(Boolean counter);

    ButtonAttributes withCounter(Boolean counter);

    @FactoryParameter(obligation = OPTIONAL)
    String getLogo();

    void setLogo(String logo);

    ButtonAttributes withLogo(String logo);

    @FactoryParameter(obligation = OPTIONAL)
    String getStyle();

    void setStyle(String style);

    ButtonAttributes withStyle(String style);
}
