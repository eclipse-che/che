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
package org.eclipse.che.selenium.core.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.machine.Server;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.selenium.core.provider.TestApiEndpointUrlProvider;
import org.eclipse.che.selenium.core.user.TestUserNamespaceResolver;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.workspace.MemoryMeasure;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED;
import static org.eclipse.che.dto.server.DtoFactory.getInstance;

/**
 * @author Musienko Maxim
 */
@Singleton
public class TestWorkspaceServiceClient {
    private final String                    baseHttpUrl;
    private final HttpJsonRequestFactory    requestFactory;
    private final TestUserNamespaceResolver testUserNamespaceResolver;

    @Inject
    public TestWorkspaceServiceClient(TestApiEndpointUrlProvider apiEndpointProvider,
                                      HttpJsonRequestFactory requestFactory,
                                      TestUserNamespaceResolver testUserNamespaceResolver) {
        this.baseHttpUrl = apiEndpointProvider.get() + "workspace";
        this.requestFactory = requestFactory;
        this.testUserNamespaceResolver = testUserNamespaceResolver;
    }


    /**
     * Returns the list of workspaces names that belongs to the user.
     */
    public List<String> getAll(String authToken) throws Exception {
        List<WorkspaceDto> workspaces = requestFactory.fromUrl(baseHttpUrl)
                                                      .setAuthorizationHeader(authToken)
                                                      .request()
                                                      .asList(WorkspaceDto.class);
        return workspaces.stream()
                         .map(ws -> ws.getConfig().getName())
                         .collect(Collectors.toList());
    }

    /**
     * Stops workspace.
     */
    public void stop(String workspaceName,
                     String userName,
                     String authToken,
                     boolean createSnapshot) throws Exception {
        if (!exists(workspaceName, userName, authToken)) {
            return;
        }

        Workspace workspace = getByName(workspaceName, userName, authToken);
        String apiUrl = getIdBasedUrl(workspace.getId()) + "/runtime/?create-snapshot=" + valueOf(createSnapshot);

        requestFactory.fromUrl(apiUrl)
                      .setAuthorizationHeader(authToken)
                      .useDeleteMethod()
                      .request();
    }

    /**
     * Returns workspace by its name.
     */
    public Workspace getByName(String workspace, String username, String authToken) throws Exception {
        return requestFactory.fromUrl(getNameBasedUrl(workspace, username))
                             .setAuthorizationHeader(authToken)
                             .request()
                             .asDto(WorkspaceDto.class);
    }

    /**
     * Indicates if workspace exists.
     */
    public boolean exists(String workspace, String username, String authToken) throws Exception {
        try {
            requestFactory.fromUrl(getNameBasedUrl(workspace, username))
                          .setAuthorizationHeader(authToken)
                          .request();
        } catch (NotFoundException e) {
            return false;
        }

        return true;
    }

    /**
     * Deletes workspace.
     */
    public void delete(String workspaceName, String userName, String authToken) throws Exception {
        if (!exists(workspaceName, userName, authToken)) {
            return;
        }

        Workspace workspace = getByName(workspaceName, userName, authToken);
        if (workspace.getStatus() != STOPPED) {
            stop(workspaceName, userName, authToken, false);
            waitStatus(workspaceName, userName, authToken, STOPPED);
        }

        requestFactory.fromUrl(getIdBasedUrl(workspace.getId()))
                      .setAuthorizationHeader(authToken)
                      .useDeleteMethod()
                      .request();
    }

    /**
     * Waits needed status.
     */
    public void waitStatus(String workspaceName,
                           String userName,
                           String authToken,
                           WorkspaceStatus expectedStatus) throws Exception {

        WorkspaceStatus status = null;
        for (int i = 0; i < 120; i++) {
            status = getByName(workspaceName, userName, authToken).getStatus();
            if (status == expectedStatus) {
                return;
            } else {
                WaitUtils.sleepQuietly(5);
            }
        }

        throw new IllegalStateException(format("Workspace %s, status=%s, expected status=%s", workspaceName, status, expectedStatus));
    }

    /**
     * Creates a new workspace.
     */
    public Workspace createWorkspace(String workspaceName,
                                     int memory,
                                     MemoryMeasure memoryUnit,
                                     String pathToPattern,
                                     String authToken) throws Exception {
        String json = FileUtils.readFileToString(new File(pathToPattern), Charset.forName("UTF-8"));
        WorkspaceConfigDto workspace = getInstance().createDtoFromJson(json, WorkspaceConfigDto.class);

        EnvironmentDto environment = workspace.getEnvironments().get("replaced_name");
        environment.getMachines().get("dev-machine").getAttributes()
                   .put("memoryLimitBytes", Long.toString(convertToByte(memory, memoryUnit)));
        workspace.getEnvironments().remove("replaced_name");
        workspace.getEnvironments().put(workspaceName, environment);
        workspace.setName(workspaceName);
        workspace.setDefaultEnv(workspaceName);

        return requestFactory.fromUrl(baseHttpUrl)
                             .usePostMethod()
                             .setAuthorizationHeader(authToken)
                             .setBody(workspace)
                             .request()
                             .asDto(WorkspaceDto.class);
    }

    /**
     * Starts workspace.
     */
    public void start(String workspaceId, String workspaceName, String authToken) throws Exception {
        requestFactory.fromUrl(getIdBasedUrl(workspaceId) + "/runtime")
                      .addQueryParam("environment", workspaceName)
                      .setAuthorizationHeader(authToken)
                      .usePostMethod()
                      .request();
    }

    /**
     * Gets workspace by its id.
     */
    public Workspace getById(String workspaceId, String authToken) throws Exception {
        return requestFactory.fromUrl(getIdBasedUrl(workspaceId))
                             .setAuthorizationHeader(authToken)
                             .request()
                             .asDto(WorkspaceDto.class);
    }

    /**
     * Return server URL related with defined port
     */
    public String getServerAddressByPort(String workspaceId, String authToken, int port) throws Exception {
        Workspace workspace = getById(workspaceId, authToken);
        return workspace.getRuntime()
                        .getMachines()
                        .get(0)
                        .getRuntime()
                        .getServers()
                        .get(valueOf(port) + "/tcp")
                        .getAddress();
    }

    /**
     * Return ServerDto object by exposed port
     *
     * @param workspaceId
     *         workspace id of current user
     * @param authToken
     *         authorization token for current user
     * @param exposedPort
     *         exposed port of server
     * @return ServerDto object
     */
    public Server getServerByExposedPort(String workspaceId, String authToken, String exposedPort) throws Exception {
        Workspace workspace = requestFactory.fromUrl(getIdBasedUrl(workspaceId))
                                            .setAuthorizationHeader(authToken)
                                            .request()
                                            .asDto(WorkspaceDto.class);

        return workspace.getRuntime()
                        .getDevMachine()
                        .getRuntime()
                        .getServers()
                        .get(exposedPort);
    }

    private String getNameBasedUrl(String workspaceName, String username) {
        return baseHttpUrl
               + "/"
               + testUserNamespaceResolver.resolve(username)
               + "/"
               + workspaceName;
    }

    private String getIdBasedUrl(String workspaceId) {
        return baseHttpUrl + "/" + workspaceId;
    }

    private long convertToByte(int numberOfMemValue, MemoryMeasure desiredMeasureMemory) {
        long calculatedValue = 0;
        //represents values of bytes in 1 megabyte (2x20)
        final long MEGABYTES_CONST = 1048576;

        // represents values of bytes in 1 gygabyte (2x30)
        final long GYGABYTES_CONST = 1073741824;

        switch (desiredMeasureMemory) {
            case MB:
                calculatedValue = numberOfMemValue * MEGABYTES_CONST;
                break;
            case GB:
                calculatedValue = numberOfMemValue * GYGABYTES_CONST;
                break;
        }
        return calculatedValue;
    }
}
