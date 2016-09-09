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
package org.eclipse.che.api.environment.server.compose.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.eclipse.che.api.core.model.workspace.compose.ComposeService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Alexander Garagatyi
 */
public class ComposeServiceImpl implements ComposeService {
    @JsonProperty("container_name")
    private String              containerName;
    private List<String>        command;
    private List<String>        entrypoint;
    private String              image;
    @JsonProperty("depends_on")
    private List<String>        dependsOn;
    private Map<String, String> environment;
    private List<String>        expose;
    private List<String>        ports;
    private Map<String, String> labels;
    private List<String>        links;
    private List<String>        volumes;
    @JsonProperty("volumes_from")
    private List<String>        volumesFrom;
    @JsonProperty("mem_limit")
    private Long                memLimit;
    private BuildContextImpl    build;

    public ComposeServiceImpl() {}

    public ComposeServiceImpl(ComposeService service) {
        image = service.getImage();
        if (service.getBuild() != null) {
            build = new BuildContextImpl(service.getBuild());
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
    }

    @Override
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public ComposeServiceImpl withImage(String image) {
        this.image = image;
        return this;
    }

    @Override
    public BuildContextImpl getBuild() {
        return build;
    }

    public void setBuild(BuildContextImpl build) {
        this.build = build;
    }

    public ComposeServiceImpl withBuild(BuildContextImpl build) {
        this.build = build;
        return this;
    }

    @Override
    public List<String> getEntrypoint() {
        if (entrypoint == null) {
            entrypoint = new ArrayList<>();
        }
        return entrypoint;
    }

    public void setEntrypoint(List<String> entrypoint) {
        this.entrypoint = entrypoint;
    }

    public ComposeServiceImpl withEntrypoint(List<String> entrypoint) {
        this.entrypoint = entrypoint;
        return this;
    }

    @Override
    public List<String> getCommand() {
        if (command == null) {
            command = new ArrayList<>();
        }
        return command;
    }

    public void setCommand(List<String> command) {
        this.command = command;
    }

    public ComposeServiceImpl withCommand(List<String> command) {
        this.command = command;
        return this;
    }

    @Override
    public Map<String, String> getEnvironment() {
        if (environment == null) {
            environment = new HashMap<>();
        }
        return environment;
    }

    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment;
    }

    public ComposeServiceImpl withEnvironment(Map<String, String> environment) {
        this.environment = environment;
        return this;
    }

    @Override
    public List<String> getDependsOn() {
        if (dependsOn == null) {
            dependsOn = new ArrayList<>();
        }

        return dependsOn;
    }

    public void setDependsOn(List<String> dependsOn) {
        this.dependsOn = dependsOn;
    }

    public ComposeServiceImpl withDependsOn(List<String> dependsOn) {
        this.dependsOn = dependsOn;
        return this;
    }

    @Override
    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public ComposeServiceImpl withContainerName(String containerName) {
        this.containerName = containerName;
        return this;
    }

    @Override
    public List<String> getLinks() {
        if (links == null) {
            links = new ArrayList<>();
        }
        return links;
    }

    public void setLinks(List<String> links) {
        this.links = links;
    }

    public ComposeServiceImpl withLinks(List<String> links) {
        this.links = links;
        return this;
    }

    @Override
    public Map<String, String> getLabels() {
        if (labels == null) {
            labels = new HashMap<>();
        }
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public ComposeServiceImpl withLabels(Map<String, String> labels) {
        this.labels = labels;
        return this;
    }

    @Override
    public List<String> getExpose() {
        if (expose == null) {
            expose = new ArrayList<>();
        }
        return expose;
    }

    public void setExpose(List<String> expose) {
        this.expose = expose;
    }

    public ComposeServiceImpl withExpose(List<String> expose) {
        this.expose = expose;
        return this;
    }

    @Override
    public List<String> getPorts() {
        if (ports == null) {
            ports = new ArrayList<>();
        }
        return ports;
    }

    public void setPorts(List<String> ports) {
        this.ports = ports;
    }

    public ComposeServiceImpl withPorts(List<String> ports) {
        this.ports = ports;
        return this;
    }

    @Override
    public List<String> getVolumes() {
        if (volumes == null) {
            volumes = new ArrayList<>();
        }
        return volumes;
    }

    public void setVolumes(List<String> volumes) {
        this.volumes = volumes;
    }

    public ComposeServiceImpl withVolumes(List<String> volumes) {
        this.volumes = volumes;
        return this;
    }

    @Override
    public List<String> getVolumesFrom() {
        if (volumesFrom == null) {
            volumesFrom = new ArrayList<>();
        }
        return volumesFrom;
    }

    public void setVolumesFrom(List<String> volumesFrom) {
        this.volumesFrom = volumesFrom;
    }

    public ComposeServiceImpl withVolumesFrom(List<String> volumesFrom) {
        this.volumesFrom = volumesFrom;
        return this;
    }

    @Override
    public Long getMemLimit() {
        return memLimit;
    }

    public void setMemLimit(Long memLimit) {
        this.memLimit = memLimit;
    }

    public ComposeServiceImpl withMemLimit(Long memLimit) {
        this.memLimit = memLimit;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ComposeServiceImpl)) return false;
        ComposeServiceImpl service = (ComposeServiceImpl)o;
        return Objects.equals(containerName, service.containerName) &&
               Objects.equals(build, service.build) &&
               Objects.equals(command, service.command) &&
               Objects.equals(entrypoint, service.entrypoint) &&
               Objects.equals(image, service.image) &&
               Objects.equals(dependsOn, service.dependsOn) &&
               Objects.equals(environment, service.environment) &&
               Objects.equals(expose, service.expose) &&
               Objects.equals(ports, service.ports) &&
               Objects.equals(labels, service.labels) &&
               Objects.equals(links, service.links) &&
               Objects.equals(volumes, service.volumes) &&
               Objects.equals(volumesFrom, service.volumesFrom) &&
               Objects.equals(memLimit, service.memLimit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(containerName,
                            build,
                            command,
                            entrypoint,
                            image,
                            dependsOn,
                            environment,
                            expose,
                            ports,
                            labels,
                            links,
                            volumes,
                            volumesFrom,
                            memLimit);
    }

    @Override
    public String toString() {
        return "ComposeServiceImpl{" +
               "containerName='" + containerName + '\'' +
               ", build='" + build + '\'' +
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
               '}';
    }
}
