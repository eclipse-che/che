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
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;

import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.MANDATORY;
import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.OPTIONAL;

/**
 * Factory of version 4.0
 *
 * @author Max Shaposhnik
 *
 */
public interface FactoryV4_0 {

    /**
     * @return Version for Codenvy Factory API.
     */
    @FactoryParameter(obligation = MANDATORY)
    String getV();

    void setV(String v);

    FactoryV4_0 withV(String v);


    /**
     * Describes parameters of the workspace that should be used for factory
     */
    @FactoryParameter(obligation = MANDATORY)
    WorkspaceConfigDto getWorkspace();

    void setWorkspace(WorkspaceConfigDto workspace);

    FactoryV4_0 withWorkspace(WorkspaceConfigDto workspace);


    /**
     * Describe restrictions of the factory
     */
    @FactoryParameter(obligation = OPTIONAL, trackedOnly = true)
    Policies getPolicies();

    void setPolicies(Policies policies);

    FactoryV4_0 withPolicies(Policies policies);


    /**
     * Identifying information of author
     */
    @FactoryParameter(obligation = OPTIONAL)
    Author getCreator();

    void setCreator(Author creator);

    FactoryV4_0 withCreator(Author creator);


    /**
     * Describes factory button
     */
    @FactoryParameter(obligation = OPTIONAL)
    Button getButton();

    void setButton(Button button);

    FactoryV4_0 withButton(Button button);


    /**
     * Describes ide look and feel.
     */
    @FactoryParameter(obligation = OPTIONAL)
    Ide getIde();

    void setIde(Ide ide);

    FactoryV4_0 withIde(Ide ide);


    /**
     * @return - id of stored factory object
     */
    @FactoryParameter(obligation = OPTIONAL, setByServer = true)
    String getId();

    void setId(String id);

    FactoryV4_0 withId(String id);
    
    /**
     * @return - name of stored factory object
     */
    @FactoryParameter(obligation = OPTIONAL)
    String getName();

    void setName(String name);

    FactoryV4_0 withName(String name);

}
