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
package org.eclipse.che.api.environment.server.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toMap;

/**
 * Description of docker container environment as representation of environment of machines in Che.
 *
 * @author Alexander Garagatyi
 */
// TODO rename it's doesn't implement anything
public class CheServicesEnvironmentImpl {
    private String                      workspaceId;
    private Map<String, CheServiceImpl> services;
    // TODO add networks, use it
    // TODO use external networks for preconfigured networks for example

    public CheServicesEnvironmentImpl() {}

    public CheServicesEnvironmentImpl(Map<String, CheServiceImpl> services) {
        this.services = services;
    }

    public CheServicesEnvironmentImpl(CheServicesEnvironmentImpl environment) {
        workspaceId = environment.getWorkspaceId();
        if (environment.getServices() != null) {
            services = environment.getServices()
                                  .entrySet()
                                  .stream()
                                  .collect(toMap(Map.Entry::getKey,
                                                 entry -> new CheServiceImpl(entry.getValue())));
        }
    }

    /**
     * ID of workspace to which this environment belongs.
     * May be used for internal purposes of environment runtime handling.
     */
    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public CheServicesEnvironmentImpl withWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    /**
     * Mapping of compose services names to services configuration.
     */
    public Map<String, CheServiceImpl> getServices() {
        if (services == null) {
            services = new HashMap<>();
        }
        return services;
    }

    public void setServices(Map<String, CheServiceImpl> services) {
        this.services = services;
    }

    public CheServicesEnvironmentImpl withServices(Map<String, CheServiceImpl> services) {
        this.services = services;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CheServicesEnvironmentImpl)) return false;
        CheServicesEnvironmentImpl that = (CheServicesEnvironmentImpl)o;
        return Objects.equals(getWorkspaceId(), that.getWorkspaceId()) &&
               Objects.equals(getServices(), that.getServices());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getWorkspaceId(), getServices());
    }

    @Override
    public String toString() {
        return "CheServicesEnvironmentImpl{" +
               "workspaceId='" + workspaceId + '\'' +
               ", services=" + services +
               '}';
    }
}
