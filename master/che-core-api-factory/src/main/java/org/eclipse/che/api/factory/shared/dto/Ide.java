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

import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.OPTIONAL;

import org.eclipse.che.api.core.factory.FactoryParameter;
import org.eclipse.che.dto.shared.DTO;

/**
 * Describe IDE interface Look and Feel
 *
 * @author Sergii Kabashniuk
 */
@DTO
public interface Ide {

    /**
     * @return configuration of IDE on application loaded event.
     */
    @FactoryParameter(obligation = OPTIONAL)
    OnAppLoaded getOnAppLoaded();

    void setOnAppLoaded(OnAppLoaded onAppLoaded);

    Ide withOnAppLoaded(OnAppLoaded onAppLoaded);

    /**
     * @return configuration of IDE on application closed event.
     */
    @FactoryParameter(obligation = OPTIONAL)
    OnAppClosed getOnAppClosed();

    void setOnAppClosed(OnAppClosed onAppClosed);

    Ide withOnAppClosed(OnAppClosed onAppClosed);

    /**
     * @return configuration of IDE on projects loaded event.
     */
    @FactoryParameter(obligation = OPTIONAL)
    OnProjectsLoaded getOnProjectsLoaded();

    void setOnProjectsLoaded(OnProjectsLoaded onProjectsLoaded);

    Ide withOnProjectsLoaded(OnProjectsLoaded onProjectsLoaded);

}
