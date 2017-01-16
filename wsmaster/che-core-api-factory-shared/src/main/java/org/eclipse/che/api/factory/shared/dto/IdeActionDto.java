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
package org.eclipse.che.api.factory.shared.dto;

import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.OPTIONAL;

import org.eclipse.che.api.core.factory.FactoryParameter;
import org.eclipse.che.api.core.model.factory.Action;
import org.eclipse.che.dto.shared.DTO;

import java.util.Map;

/**
 * Describe ide action.
 *
 * @author Sergii Kabashniuk
 */
@DTO
public interface IdeActionDto extends Action {

    /**
     * Action Id
     *
     * @return id of action.
     */
    @Override
    @FactoryParameter(obligation = OPTIONAL)
    String getId();

    void setId(String id);

    IdeActionDto withId(String id);

    /***
     *
     * @return Action properties
     */
    @Override
    @FactoryParameter(obligation = OPTIONAL)
    Map<String, String> getProperties();

    void setProperties(Map<String, String> properties);

    IdeActionDto withProperties(Map<String, String> properties);
}
