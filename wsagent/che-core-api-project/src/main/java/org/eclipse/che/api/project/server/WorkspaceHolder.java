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

import static org.eclipse.che.api.project.server.DtoConverter.asDto;

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
                           @Named("env.CHE_WORKSPACE_ID") String workspaceId,
                           HttpJsonRequestFactory httpJsonRequestFactory) throws ServerException {
        this.apiEndpoint = apiEndpoint;
        this.httpJsonRequestFactory = httpJsonRequestFactory;

        // TODO - invent mechanism to recognize workspace ID
        // for Docker container name of this property is defined in
        // org.eclipse.che.plugin.docker.machine.DockerInstanceMetadata.CHE_WORKSPACE_ID
        // it resides on Workspace Master side so not accessible from agent code
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
     * Add project on WS-master side.
     *
     * @param project
     *         project to add
     * @throws ServerException
     */
    void addProject(RegisteredProject project) throws ServerException {
        if (project.isDetected()) {
            return;
        }

        workspace.addProject(project);

        final String href = UriBuilder.fromUri(apiEndpoint)
                                      .path(WorkspaceService.class)
                                      .path(WorkspaceService.class, "addProject")
                                      .build(workspace.getId()).toString();
        try {
            httpJsonRequestFactory.fromUrl(href).usePostMethod().setBody(asDto(project)).request();
        } catch (IOException | ApiException e) {
            throw new ServerException(e.getMessage());
        }

        if (!project.isSynced()) {
            project.setSync();
        }
    }

    /**
     * Updates project on WS-master side.
     *
     * @param project
     *         project to update
     * @throws ServerException
     */
    void updateProject(RegisteredProject project) throws ServerException {
        if (project.isDetected()) {
            return;
        }

        // TODO workspace.addProject(project); but replace
        workspace.updateProject(project);

        final String href = UriBuilder.fromUri(apiEndpoint)
                                      .path(WorkspaceService.class)
                                      .path(WorkspaceService.class, "updateProject")
                                      .build(workspace.getId()).toString();
        try {
            httpJsonRequestFactory.fromUrl(href).usePutMethod().setBody(asDto(project)).request();
        } catch (IOException | ApiException e) {
            throw new ServerException(e.getMessage());
        }

        if (!project.isSynced()) {
            project.setSync();
        }
    }

    /**
     * Removes projects on WS-master side.
     *
     * @param projects
     *         projects to remove
     * @throws ServerException
     */
    void removeProjects(Collection<RegisteredProject> projects) throws ServerException {
        for (RegisteredProject project : projects) {
            removeProject(project);
        }
    }

    private void removeProject(RegisteredProject project) throws ServerException {
        if (project.isDetected()) {
            return;
        }

        workspace.removeProject(project);

        final String href = UriBuilder.fromUri(apiEndpoint)
                                      .path(WorkspaceService.class)
                                      .path(WorkspaceService.class, "deleteProject")
                                      .build(workspace.getId(), project.getName()).toString();
        try {
            httpJsonRequestFactory.fromUrl(href).useDeleteMethod().request();
        } catch (IOException | ApiException e) {
            throw new ServerException(e.getMessage());
        }
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
        try {
            return httpJsonRequestFactory.fromUrl(href).useGetMethod().request().asDto(UsersWorkspaceDto.class);
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

        public List<? extends ProjectConfig> getProjects() {
            return getConfig().getProjects();
        }

        public void removeProject(ProjectConfig project) {
            getConfig().getProjects().removeIf(p -> p.getPath().equals(project.getPath()));
        }

        public void setProjects(List<? extends ProjectConfig> projects) {
            getConfig().setProjects(projects);
        }

        public void addProject(RegisteredProject project) {
            final ProjectConfig config = new NewProjectConfig(project.getPath(),
                                                              project.getType(),
                                                              project.getMixins(),
                                                              project.getName(),
                                                              project.getDescription(),
                                                              project.getPersistableAttributes(),
                                                              project.getSource());
            List<ProjectConfig> list = new ArrayList<>(getConfig().getProjects());
            list.add(config);
            getConfig().setProjects(list);
        }

        public void updateProject(RegisteredProject project) {
            removeProject(project);
            addProject(project);
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

        public void setProjects(List<? extends ProjectConfig> projects) {
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
