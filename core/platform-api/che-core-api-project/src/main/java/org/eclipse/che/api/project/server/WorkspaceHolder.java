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

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.model.workspace.EnvironmentState;
import org.eclipse.che.api.core.model.workspace.ProjectConfig;
import org.eclipse.che.api.core.model.workspace.UsersWorkspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.rest.DefaultHttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.shared.dto.Link;
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

import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * For caching and proxy-ing Workspace Configuration
 *
 * @author gazarenkov
 */
@Singleton
public class WorkspaceHolder {

    private String apiEndpoint;

    protected final UsersWorkspaceImpl workspace;

    private HttpJsonRequestFactory httpJsonRequestFactory;

    @Inject
    public WorkspaceHolder(@Named("api.endpoint") String apiEndpoint)
            throws ServerException {

        this.apiEndpoint = apiEndpoint;
        this.httpJsonRequestFactory = new DefaultHttpJsonRequestFactory();

        // TODO - invent mechanism to recognize workspace ID
        // for Docker container name of this property is defined in
        // org.eclipse.che.plugin.docker.machine.DockerInstanceMetadata.CHE_WORKSPACE_ID
        // it resides on Workspace Master side so not accessible from agent code
        String workspaceId = System.getenv("CHE_WORKSPACE_ID");

        if (workspaceId == null)
            throw new ServerException("Workspace ID is not defined for Workspace Agent");

        this.workspace = new UsersWorkspaceImpl(workspaceDto(workspaceId));

    }

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
     * @param projects
     * @throws ServerException
     */
    public void updateProjects(Collection<RegisteredProject> projects) throws ServerException {


        workspace.setProjects(new ArrayList<>(projects));

        final String href = UriBuilder.fromUri(apiEndpoint)
                                      .path(WorkspaceService.class).path(WorkspaceService.class, "update")
                                      .build(workspace.getId()).toString();
        final Link link = newDto(Link.class).withMethod("PUT").withHref(href);

        try {
            httpJsonRequestFactory.fromLink(link)
                                  .setBody(workspace)
                                  .request();
        } catch (IOException | ApiException e) {
            throw new ServerException(e.getMessage());
        }

        // sync local projects
        for (RegisteredProject project : projects) {
            if(!project.isSynced())
                project.setSync();
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
        final Link link = newDto(Link.class).withMethod("GET").withHref(href);

        try {
            return httpJsonRequestFactory.fromLink(link).request().asDto(UsersWorkspaceDto.class);
        } catch (IOException | ApiException e) {
            throw new ServerException(e);
        }
    }


    protected static class UsersWorkspaceImpl implements UsersWorkspace {

        private final UsersWorkspaceDto dto;

        private List<? extends ProjectConfig> projects;

        UsersWorkspaceImpl(UsersWorkspaceDto dto) {
            this.dto = dto;
            this.projects = dto.getProjects();
        }


        @Override
        public String getId() {
            return dto.getId();
        }

        @Override
        public String getOwner() {
            return dto.getOwner();
        }

        @Override
        public boolean isTemporary() {
            return dto.isTemporary();
        }

        @Override
        public List<? extends EnvironmentState> getEnvironments() {
            return dto.getEnvironments();
        }

        @Override
        public WorkspaceStatus getStatus() {
            return dto.getStatus();
        }

        @Override
        public String getName() {
            return dto.getName();
        }

        @Override
        public String getDescription() {
            return dto.getDescription();
        }

        @Override
        public String getDefaultEnv() {
            return dto.getDefaultEnv();
        }

        @Override
        public List<? extends Command> getCommands() {
            return dto.getCommands();
        }

        @Override
        public List<? extends ProjectConfig> getProjects() {
            return this.projects;
        }

        @Override
        public Map<String, String> getAttributes() {
            return dto.getAttributes();
        }

        public void setProjects(final List<RegisteredProject> projects) {

            List<ProjectConfig> p = new ArrayList<>();
            for(RegisteredProject project : projects) {
                ProjectConfig config = new NewProjectConfig(project.getPath(), project.getType(), project.getMixins(),
                        project.getName(), project.getDescription(), project.getPersistableAttributes(),
                        project.getSource());
                p.add(config);
            }
            this.projects = p;

        }
    }




}
