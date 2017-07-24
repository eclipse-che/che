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
package org.eclipse.che.workspace.infrastructure.docker.model;

import org.eclipse.che.commons.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Representation of configuration of docker container configuration.
 *
 * @author Alexander Garagatyi
 */
public class DockerContainerConfig {
    private String              id;
    private String              containerName;
    private List<String>        command;
    private List<String>        entrypoint;
    private String              image;
    private List<String>        dependsOn;
    private Map<String, String> environment;
    private List<String>        expose;
    private List<String>        ports;
    private Map<String, String> labels;
    private List<String>        links;
    private List<String>        volumes;
    private List<String>        volumesFrom;
    private Long                memLimit;
    private DockerBuildContext  build;
    private List<String>        networks;
    private String              pidMode;

    public DockerContainerConfig() {}

    public DockerContainerConfig(DockerContainerConfig container) {
        id = container.getId();
        image = container.getImage();
        if (container.getBuild() != null) {
            build = new DockerBuildContext(container.getBuild());
        }
        if (container.getEntrypoint() != null) {
            entrypoint = new ArrayList<>(container.getEntrypoint());
        }
        if (container.getCommand() != null) {
            command = new ArrayList<>(container.getCommand());
        }
        if (container.getEnvironment() != null) {
            environment = new HashMap<>(container.getEnvironment());
        }
        if (container.getDependsOn() != null) {
            dependsOn = new ArrayList<>(container.getDependsOn());
        }
        containerName = container.getContainerName();
        if (container.getLinks() != null) {
            links = new ArrayList<>(container.getLinks());
        }
        if (container.getLabels() != null) {
            labels = new HashMap<>(container.getLabels());
        }
        if (container.getExpose() != null) {
            expose = new ArrayList<>(container.getExpose());
        }
        if (container.getPorts() != null) {
            ports = new ArrayList<>(container.getPorts());
        }
        if (container.getVolumesFrom() != null) {
            volumesFrom = new ArrayList<>(container.getVolumesFrom());
        }
        if (container.getVolumes() != null) {
            volumes = new ArrayList<>(container.getVolumes());
        }
        memLimit = container.getMemLimit();
        if (container.getNetworks() != null) {
            networks = new ArrayList<>(container.getNetworks());
        }
        pidMode = container.getPidMode();
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

    public DockerContainerConfig withId(String id) {
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

    public DockerContainerConfig withImage(String image) {
        this.image = image;
        return this;
    }

    /**
     * Build context for container image creation.
     */
    public DockerBuildContext getBuild() {
        return build;
    }

    public void setBuild(DockerBuildContext build) {
        this.build = build;
    }

    public DockerContainerConfig withBuild(DockerBuildContext build) {
        this.build = build;
        return this;
    }

    /**
     * Override the default entrypoint.
     */
    @Nullable
    public List<String> getEntrypoint() {
        return entrypoint;
    }

    public void setEntrypoint(List<String> entrypoint) {
        this.entrypoint = entrypoint;
    }

    public DockerContainerConfig withEntrypoint(List<String> entrypoint) {
        this.entrypoint = entrypoint;
        return this;
    }

    /**
     * Override the default command.
     */
    @Nullable
    public List<String> getCommand() {
        return command;
    }

    public void setCommand(List<String> command) {
        this.command = command;
    }

    public DockerContainerConfig withCommand(List<String> command) {
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

    public DockerContainerConfig withEnvironment(Map<String, String> environment) {
        this.environment = environment;
        return this;
    }

    /**
     * Express dependency between containers.
     *
     * <p/> Environment engine implementation should start containers in dependency order.
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

    public DockerContainerConfig withDependsOn(List<String> dependsOn) {
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

    public DockerContainerConfig withContainerName(String containerName) {
        this.containerName = containerName;
        return this;
    }

    /**
     * Link to other containers.
     *
     * <p/> Either specify both the container name and a link alias (CONTAINER:ALIAS), or just the container name.
     * <br/> Examples:
     * <ul>
     * <li>db</li>
     * <li>db:database</li>
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

    public DockerContainerConfig withLinks(List<String> links) {
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

    public DockerContainerConfig withLabels(Map<String, String> labels) {
        this.labels = labels;
        return this;
    }

    /**
     * Expose ports without publishing them to the host machine - they’ll only be accessible to linked containers.
     *
     * <p/> Only the internal port can be specified.
     * <br/> Examples:
     * <ul>
     * <li>3000</li>
     * <li>8000</li>
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

    public DockerContainerConfig withExpose(List<String> expose) {
        this.expose = expose;
        return this;
    }

    /**
     * Expose ports. Either specify both ports (HOST:CONTAINER), or just the container port (a random host port will be chosen).
     *
     * <p/> Examples:
     * <ul>
     * <li>80</li>
     * <li>3000</li>
     * <li>8080:80</li>
     * <li>80:8000</li>
     * <li>9090-9091:8080-8081</li>
     * <li>127.0.0.1:8001:8001</li>
     * <li>127.0.0.1:5000-5010:5000-5010</li>
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

    public DockerContainerConfig withPorts(List<String> ports) {
        this.ports = ports;
        return this;
    }

    /**
     * Mount paths or named volumes.
     *
     * <p/> Examples:
     * <ul>
     * <li>/var/lib/mysql</li>
     * <li>/opt/data:/var/lib/mysql</li>
     * <li>data-volume:/var/lib/mysql</li>
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

    public DockerContainerConfig withVolumes(List<String> volumes) {
        this.volumes = volumes;
        return this;
    }

    /**
     * Mount all of the volumes from another container.
     *
     * <p/> Optionally access level can be specified: read-only access (ro) or read-write (rw).
     * If no access level is specified, then read-write will be used.
     * <p/> Examples:
     * <ul>
     * <li>container_name</li>
     * <li>container_name:ro</li>
     * <li>container_name:rw</li>
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

    public DockerContainerConfig withVolumesFrom(List<String> volumesFrom) {
        this.volumesFrom = volumesFrom;
        return this;
    }

    /**
     * Memory limit for the container, specified in bytes.
     */
    public Long getMemLimit() {
        return memLimit;
    }

    public void setMemLimit(Long memLimit) {
        this.memLimit = memLimit;
    }

    public DockerContainerConfig withMemLimit(Long memLimit) {
        this.memLimit = memLimit;
        return this;
    }

    /**
     * List of networks that should be connected to container.
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

    public DockerContainerConfig withNetworks(List<String> networks) {
        this.networks = networks;
        return this;
    }

    public String getPidMode() {
        return pidMode;
    }

    public void setPidMode(String pidMode) {
        this.pidMode = pidMode;
    }

    public DockerContainerConfig withPidMode(String pidMode) {
        this.pidMode = pidMode;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DockerContainerConfig)) return false;
        DockerContainerConfig that = (DockerContainerConfig)o;
        return Objects.equals(getId(), that.getId()) &&
               Objects.equals(getContainerName(), that.getContainerName()) &&
               Objects.equals(getCommand(), that.getCommand()) &&
               Objects.equals(getEntrypoint(), that.getEntrypoint()) &&
               Objects.equals(getImage(), that.getImage()) &&
               Objects.equals(getDependsOn(), that.getDependsOn()) &&
               Objects.equals(getEnvironment(), that.getEnvironment()) &&
               Objects.equals(getExpose(), that.getExpose()) &&
               Objects.equals(getPorts(), that.getPorts()) &&
               Objects.equals(getLabels(), that.getLabels()) &&
               Objects.equals(getLinks(), that.getLinks()) &&
               Objects.equals(getVolumes(), that.getVolumes()) &&
               Objects.equals(getVolumesFrom(), that.getVolumesFrom()) &&
               Objects.equals(getMemLimit(), that.getMemLimit()) &&
               Objects.equals(getBuild(), that.getBuild()) &&
               Objects.equals(getNetworks(), that.getNetworks()) &&
               Objects.equals(getPidMode(), that.getPidMode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getContainerName(), getCommand(), getEntrypoint(), getImage(), getDependsOn(),
                            getEnvironment(), getExpose(), getPorts(), getLabels(), getLinks(), getVolumes(),
                            getVolumesFrom(), getMemLimit(), getBuild(), getNetworks(), getPidMode());
    }

    @Override
    public String toString() {
        return "DockerContainerConfig{" +
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
               ", pidMode='" + pidMode + '\'' +
               '}';
    }
}
