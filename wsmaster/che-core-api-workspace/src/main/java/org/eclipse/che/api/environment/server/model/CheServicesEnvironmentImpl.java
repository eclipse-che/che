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
import java.util.stream.Collectors;

/**
 * Description of docker container environment as representation of environment of machines in Che.
 *
 * @author Alexander Garagatyi
 */
public class CheServicesEnvironmentImpl {
    private String                      version;
    private Map<String, CheServiceImpl> services;

    public CheServicesEnvironmentImpl() {}

    public CheServicesEnvironmentImpl(CheServicesEnvironmentImpl environment) {
        version = environment.getVersion();
        if (environment.getServices() != null) {
            services = environment.getServices()
                                  .entrySet()
                                  .stream()
                                  .collect(Collectors.toMap(Map.Entry::getKey,
                                                            entry -> new CheServiceImpl(entry.getValue())));
        }
    }

    /**
     * Version of environment syntax.
     */
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public CheServicesEnvironmentImpl withVersion(String version) {
        this.version = version;
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
        return Objects.equals(getVersion(), that.getVersion()) &&
               Objects.equals(getServices(), that.getServices());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVersion(), getServices());
    }

    @Override
    public String toString() {
        return "CheServicesEnvironmentImpl{" +
               "version='" + version + '\'' +
               ", services=" + services +
               '}';
    }
}
