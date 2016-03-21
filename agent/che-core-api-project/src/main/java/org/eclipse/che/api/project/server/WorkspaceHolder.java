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
package org.eclipse.che.api.project.server;

import com.google.common.annotations.VisibleForTesting;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.model.workspace.UsersWorkspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.workspace.server.DtoConverter;
import org.eclipse.che.api.workspace.server.WorkspaceService;
import org.eclipse.che.api.workspace.shared.dto.UsersWorkspaceDto;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * For caching and proxy-ing Workspace Configuration.
 *
 * @author gazarenkov
 */
@Singleton
public class WorkspaceHolder {

    private String apiEndpoint;

    protected final UsersWorkspaceImpl workspace;

    private HttpJsonRequestFactory httpJsonRequestFactory;

    @Inject
    public WorkspaceHolder(@Named("api.endpoint") String apiEndpoint,
                           HttpJsonRequestFactory httpJsonRequestFactory) throws ServerException {
        this.apiEndpoint = apiEndpoint;
        this.httpJsonRequestFactory = httpJsonRequestFactory;

        // TODO - invent mechanism to recognize workspace ID
        // for Docker container name of this property is defined in
        // org.eclipse.che.plugin.docker.machine.DockerInstanceMetadata.CHE_WORKSPACE_ID
        // it resides on Workspace Master side so not accessible from agent code
        final String workspaceId = System.getenv("CHE_WORKSPACE_ID");

        if (workspaceId == null) {
            throw new ServerException("Workspace ID is not defined for Workspace Agent");
        }

        this.workspace = new UsersWorkspaceImpl(workspaceDto(workspaceId));
    }

    @VisibleForTesting
    protected WorkspaceHolder(UsersWorkspaceDto workspace) throws ServerException {
        this.workspace = new UsersWorkspaceImpl(workspace);
    }

    /**
     * @return workspace object
     */
    public UsersWorkspace getWorkspace() {
        return this.workspace;
    }

    /**
     * updates projects on ws master side
     *
     * @param projects
     * @throws ServerException
     */
    public void updateProjects(Collection<RegisteredProject> projects) throws ServerException {
        List<RegisteredProject> persistedProjects = projects.stream()
                                                            .filter(project -> !project.isDetected())
                                                            .collect(Collectors.toList());

        workspace.setProjects(persistedProjects);

        final String href = UriBuilder.fromUri(apiEndpoint)
                                      .path(WorkspaceService.class).path(WorkspaceService.class, "update")
                                      .build(workspace.getId()).toString();
        final Link link = newDto(Link.class).withMethod("PUT").withHref(href);

        try {
            httpJsonRequestFactory.fromLink(link)
                                  .setBody(DtoConverter.asDto(workspace.getConfig()))
                                  .request();
        } catch (IOException | ApiException e) {
            throw new ServerException(e.getMessage());
        }

        // sync local projects
        projects.stream()
                .filter(project -> !project.isSynced())
                .forEach(RegisteredProject::setSync);
    }

    /**
     * @param wsId
     * @return
     * @throws ServerException
     */
    private UsersWorkspaceDto workspaceDto(String wsId) throws ServerException {
        final String href = UriBuilder.fromUri(apiEndpoint)
                                      .path(WorkspaceService.class).path(WorkspaceService.class, "getById")
                                      .build(wsId).toString();
        final Link link = newDto(Link.class).withMethod("GET").withHref(href);

        try {
            return httpJsonRequestFactory.fromLink(link).request().asDto(UsersWorkspaceDto.class);
        } catch (IOException | ApiException e) {
            throw new ServerException(e);
        }
    }

    protected static class UsersWorkspaceImpl implements UsersWorkspace {
        private String              id;
        private String              owner;
        private boolean             isTemporary;
        private WorkspaceStatus     status;
        private WorkspaceConfigImpl workspaceConfig;

        UsersWorkspaceImpl(UsersWorkspace usersWorkspace) {
            id = usersWorkspace.getId();
            owner = usersWorkspace.getOwner();
            isTemporary = usersWorkspace.isTemporary();
            status = usersWorkspace.getStatus();
            workspaceConfig = new WorkspaceConfigImpl(usersWorkspace.getConfig());
        }

        @Override
        public WorkspaceConfigImpl getConfig() {
            return workspaceConfig;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getOwner() {
            return owner;
        }

        @Override
        public boolean isTemporary() {
            return isTemporary;
        }

        @Override
        public WorkspaceStatus getStatus() {
            return status;
        }

        public void setProjects(final List<RegisteredProject> projects) {
            List<NewProjectConfig> p = projects.stream()
                                               .map(project -> new NewProjectConfig(project.getPath(),
                                                                                    project.getType(),
                                                                                    project.getMixins(),
                                                                                    project.getName(),
                                                                                    project.getDescription(),
                                                                                    project.getPersistableAttributes(),
                                                                                    project.getSource()))
                                               .collect(Collectors.toList());
            getConfig().setProjects(p);
        }
    }

    protected static class WorkspaceConfigImpl implements WorkspaceConfig {
        private String                        name;
        private String                        description;
        private String                        defaultEnvName;
        private List<? extends Command>       commands;
        private List<? extends ProjectConfig> projects;
        private List<? extends Environment>   environments;
        private Map<String, String>           attributes;

        WorkspaceConfigImpl(WorkspaceConfig config) {
            name = config.getName();
            description = config.getDescription();
            defaultEnvName = config.getDefaultEnv();
            commands = config.getCommands();
            projects = config.getProjects();
            environments = config.getEnvironments();
            attributes = config.getAttributes();
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public String getDefaultEnv() {
            return defaultEnvName;
        }

        @Override
        public List<? extends Command> getCommands() {
            return commands;
        }

        @Override
        public List<? extends ProjectConfig> getProjects() {
            return projects;
        }

        public void setProjects(List<NewProjectConfig> projects) {
            this.projects = projects;
        }

        @Override
        public List<? extends Environment> getEnvironments() {
            return environments;
        }

        @Override
        public Map<String, String> getAttributes() {
            return attributes;
        }
    }
}
