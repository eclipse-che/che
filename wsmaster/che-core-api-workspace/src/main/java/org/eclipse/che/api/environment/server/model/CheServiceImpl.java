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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Description of docker service as a representation of machine in Che.
 *
 * @author Alexander Garagatyi
 */
// TODO rename because it doesn't implement anything
public class CheServiceImpl {
    private String                     id;
    private String                     containerName;
    private List<String>               command;
    private List<String>               entrypoint;
    private String                     image;
    private List<String>               dependsOn;
    private Map<String, String>        environment;
    private List<String>               expose;
    private List<String>               ports;
    private Map<String, String>        labels;
    private List<String>               links;
    private List<String>               volumes;
    private List<String>               volumesFrom;
    private Long                       memLimit;
    private CheServiceBuildContextImpl build;
    private List<String>               networks;

    public CheServiceImpl() {}

    public CheServiceImpl(CheServiceImpl service) {
        id = service.getId();
        image = service.getImage();
        if (service.getBuild() != null) {
            build = new CheServiceBuildContextImpl(service.getBuild());
        }
        if (service.getEntrypoint() != null) {
            entrypoint = new ArrayList<>(service.getEntrypoint());
        }
        if (service.getCommand() != null) {
            command = new ArrayList<>(service.getCommand());
        }
        if (service.getEnvironment() != null) {
            environment = new HashMap<>(service.getEnvironment());
        }
        if (service.getDependsOn() != null) {
            dependsOn = new ArrayList<>(service.getDependsOn());
        }
        containerName = service.getContainerName();
        if (service.getLinks() != null) {
            links = new ArrayList<>(service.getLinks());
        }
        if (service.getLabels() != null) {
            labels = new HashMap<>(service.getLabels());
        }
        if (service.getExpose() != null) {
            expose = new ArrayList<>(service.getExpose());
        }
        if (service.getPorts() != null) {
            ports = new ArrayList<>(service.getPorts());
        }
        if (service.getVolumesFrom() != null) {
            volumesFrom = new ArrayList<>(service.getVolumesFrom());
        }
        if (service.getVolumes() != null) {
            volumes = new ArrayList<>(service.getVolumes());
        }
        memLimit = service.getMemLimit();
        if (service.getNetworks() != null) {
            networks = new ArrayList<>(service.getNetworks());
        }
    }

    /**
     * Unique identifier of machine.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public CheServiceImpl withId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Image for container creation.
     */
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public CheServiceImpl withImage(String image) {
        this.image = image;
        return this;
    }

    /**
     * Build context for container image creation.
     */
    public CheServiceBuildContextImpl getBuild() {
        return build;
    }

    public void setBuild(CheServiceBuildContextImpl build) {
        this.build = build;
    }

    public CheServiceImpl withBuild(CheServiceBuildContextImpl build) {
        this.build = build;
        return this;
    }

    /**
     * Override the default entrypoint.
     */
    public List<String> getEntrypoint() {
        if (entrypoint == null) {
            entrypoint = new ArrayList<>();
        }
        return entrypoint;
    }

    public void setEntrypoint(List<String> entrypoint) {
        this.entrypoint = entrypoint;
    }

    public CheServiceImpl withEntrypoint(List<String> entrypoint) {
        this.entrypoint = entrypoint;
        return this;
    }

    /**
     * Override the default command.
     */
    public List<String> getCommand() {
        if (command == null) {
            command = new ArrayList<>();
        }
        return command;
    }

    public void setCommand(List<String> command) {
        this.command = command;
    }

    public CheServiceImpl withCommand(List<String> command) {
        this.command = command;
        return this;
    }

    /**
     * Environment variables that should be added into container.
     */
    public Map<String, String> getEnvironment() {
        if (environment == null) {
            environment = new HashMap<>();
        }
        return environment;
    }

    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment;
    }

    public CheServiceImpl withEnvironment(Map<String, String> environment) {
        this.environment = environment;
        return this;
    }

    /**
     * Express dependency between services.
     *
     * <p/> Environment engine implementation should start services in dependency order.
     */
    public List<String> getDependsOn() {
        if (dependsOn == null) {
            dependsOn = new ArrayList<>();
        }

        return dependsOn;
    }

    public void setDependsOn(List<String> dependsOn) {
        this.dependsOn = dependsOn;
    }

    public CheServiceImpl withDependsOn(List<String> dependsOn) {
        this.dependsOn = dependsOn;
        return this;
    }

    /**
     * Specify a custom container name, rather than a generated default name.
     */
    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public CheServiceImpl withContainerName(String containerName) {
        this.containerName = containerName;
        return this;
    }

    /**
     * Link to containers in another service.
     *
     * <p/> Either specify both the service name and a link alias (SERVICE:ALIAS), or just the service name.
     * <br/> Examples:
     * <ul>
     *     <li>db</li>
     *     <li>db:database</li>
     * </ul>
     */
    public List<String> getLinks() {
        if (links == null) {
            links = new ArrayList<>();
        }
        return links;
    }

    public void setLinks(List<String> links) {
        this.links = links;
    }

    public CheServiceImpl withLinks(List<String> links) {
        this.links = links;
        return this;
    }

    /**
     * Add metadata to containers using Docker labels.
     */
    public Map<String, String> getLabels() {
        if (labels == null) {
            labels = new HashMap<>();
        }
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public CheServiceImpl withLabels(Map<String, String> labels) {
        this.labels = labels;
        return this;
    }

    /**
     * Expose ports without publishing them to the host machine - they’ll only be accessible to linked services.
     *
     * <p/> Only the internal port can be specified.
     * <br/> Examples:
     * <ul>
     *     <li>3000</li>
     *     <li>8000</li>
     * </ul>
     */
    public List<String> getExpose() {
        if (expose == null) {
            expose = new ArrayList<>();
        }
        return expose;
    }

    public void setExpose(List<String> expose) {
        this.expose = expose;
    }

    public CheServiceImpl withExpose(List<String> expose) {
        this.expose = expose;
        return this;
    }

    /**
     * Expose ports. Either specify both ports (HOST:CONTAINER), or just the container port (a random host port will be chosen).
     *
     * <p/> Examples:
     * <ul>
     *     <li>80</li>
     *     <li>3000</li>
     *     <li>8080:80</li>
     *     <li>80:8000</li>
     *     <li>9090-9091:8080-8081</li>
     *     <li>127.0.0.1:8001:8001</li>
     *     <li>127.0.0.1:5000-5010:5000-5010</li>
     * </ul>
     */
    public List<String> getPorts() {
        if (ports == null) {
            ports = new ArrayList<>();
        }
        return ports;
    }

    public void setPorts(List<String> ports) {
        this.ports = ports;
    }

    public CheServiceImpl withPorts(List<String> ports) {
        this.ports = ports;
        return this;
    }

    /**
     * Mount paths or named volumes.
     *
     * <p/> Examples:
     * <ul>
     *     <li>/var/lib/mysql</li>
     *     <li>/opt/data:/var/lib/mysql</li>
     *     <li>data-volume:/var/lib/mysql</li>
     * </ul>
     */
    public List<String> getVolumes() {
        if (volumes == null) {
            volumes = new ArrayList<>();
        }
        return volumes;
    }

    public void setVolumes(List<String> volumes) {
        this.volumes = volumes;
    }

    public CheServiceImpl withVolumes(List<String> volumes) {
        this.volumes = volumes;
        return this;
    }

    /**
     * Mount all of the volumes from another service.
     *
     * <p/> Optionally access level can be specified: read-only access (ro) or read-write (rw).
     * If no access level is specified, then read-write will be used.
     * <p/> Examples:
     * <ul>
     *     <li>service_name</li>
     *     <li>service_name:ro</li>
     *     <li>service_name:rw</li>
     * </ul>
     */
    public List<String> getVolumesFrom() {
        if (volumesFrom == null) {
            volumesFrom = new ArrayList<>();
        }
        return volumesFrom;
    }

    public void setVolumesFrom(List<String> volumesFrom) {
        this.volumesFrom = volumesFrom;
    }

    public CheServiceImpl withVolumesFrom(List<String> volumesFrom) {
        this.volumesFrom = volumesFrom;
        return this;
    }

    /**
     * Memory limit for the container of service, specified in bytes.
     */
    public Long getMemLimit() {
        return memLimit;
    }

    public void setMemLimit(Long memLimit) {
        this.memLimit = memLimit;
    }

    public CheServiceImpl withMemLimit(Long memLimit) {
        this.memLimit = memLimit;
        return this;
    }

    /**
     * List of networks that should be connected to service.
     */
    public List<String> getNetworks() {
        if (networks == null) {
            networks = new ArrayList<>();
        }
        return networks;
    }

    public void setNetworks(List<String> networks) {
        this.networks = networks;
    }

    public CheServiceImpl withNetworks(List<String> networks) {
        this.networks = networks;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CheServiceImpl)) return false;
        CheServiceImpl service = (CheServiceImpl)o;
        return Objects.equals(getId(), service.getId()) &&
               Objects.equals(getContainerName(), service.getContainerName()) &&
               Objects.equals(getCommand(), service.getCommand()) &&
               Objects.equals(getEntrypoint(), service.getEntrypoint()) &&
               Objects.equals(getImage(), service.getImage()) &&
               Objects.equals(getDependsOn(), service.getDependsOn()) &&
               Objects.equals(getEnvironment(), service.getEnvironment()) &&
               Objects.equals(getExpose(), service.getExpose()) &&
               Objects.equals(getPorts(), service.getPorts()) &&
               Objects.equals(getLabels(), service.getLabels()) &&
               Objects.equals(getLinks(), service.getLinks()) &&
               Objects.equals(getVolumes(), service.getVolumes()) &&
               Objects.equals(getVolumesFrom(), service.getVolumesFrom()) &&
               Objects.equals(getMemLimit(), service.getMemLimit()) &&
               Objects.equals(getBuild(), service.getBuild()) &&
               Objects.equals(getNetworks(), service.getNetworks());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(),
                            getContainerName(),
                            getCommand(),
                            getEntrypoint(),
                            getImage(),
                            getDependsOn(),
                            getEnvironment(),
                            getExpose(),
                            getPorts(),
                            getLabels(),
                            getLinks(),
                            getVolumes(),
                            getVolumesFrom(),
                            getMemLimit(),
                            getBuild(),
                            getNetworks());
    }

    @Override
    public String toString() {
        return "CheServiceImpl{" +
               "id='" + id + '\'' +
               ", containerName='" + containerName + '\'' +
               ", command=" + command +
               ", entrypoint=" + entrypoint +
               ", image='" + image + '\'' +
               ", dependsOn=" + dependsOn +
               ", environment=" + environment +
               ", expose=" + expose +
               ", ports=" + ports +
               ", labels=" + labels +
               ", links=" + links +
               ", volumes=" + volumes +
               ", volumesFrom=" + volumesFrom +
               ", memLimit=" + memLimit +
               ", build=" + build +
               ", networks=" + networks +
               '}';
    }
}
