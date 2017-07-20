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

import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.ZipUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.eclipse.che.dto.server.DtoFactory.getInstance;

/**
 * @author Musienko Maxim
 * @author Morhun Mykola
 */
@Singleton
public class TestProjectServiceClient {
    private static final int WS_AGENT_PORT = 4401;

    private final TestMachineServiceClient   machineServiceClient;
    private final TestWorkspaceServiceClient workspaceServiceClient;
    private final HttpJsonRequestFactory     requestFactory;

    @Inject
    public TestProjectServiceClient(TestMachineServiceClient machineServiceClient,
                                    TestWorkspaceServiceClient workspaceServiceClient,
                                    HttpJsonRequestFactory requestFactory) {
        this.machineServiceClient = machineServiceClient;
        this.workspaceServiceClient = workspaceServiceClient;
        this.requestFactory = requestFactory;
    }

    /**
     * Set type for existing project on vfs
     */
    public void setProjectType(String workspaceId,
                               String authToken,
                               String template,
                               String projectName) throws Exception {
        InputStream in = getClass().getResourceAsStream("/templates/project/" + template);
        String json = IoUtil.readAndCloseQuietly(in);

        String url = getWsAgentUrl(workspaceId, authToken);
        ProjectConfigDto project = getInstance().createDtoFromJson(json, ProjectConfigDto.class);

        requestFactory.fromUrl(url + "/" + projectName)
                      .usePutMethod()
                      .setAuthorizationHeader(machineServiceClient.getMachineApiToken(workspaceId, authToken))
                      .setBody(project)
                      .request();
    }

    /**
     * Delete resource.
     */
    public void deleteResource(String workspaceId, String authToken, String path) throws Exception {
        String wsAgentUrl = getWsAgentUrl(workspaceId, authToken);
        requestFactory.fromUrl(wsAgentUrl + "/" + path)
                      .setAuthorizationHeader(machineServiceClient.getMachineApiToken(workspaceId, authToken))
                      .useDeleteMethod()
                      .request();
    }

    public void createFolder(String workspaceId, String authToken, String folder) throws Exception {
        String url = getWsAgentUrl(workspaceId, authToken) + "/folder/" + folder;
        requestFactory.fromUrl(url)
                      .setAuthorizationHeader(machineServiceClient.getMachineApiToken(workspaceId, authToken))
                      .usePostMethod()
                      .request();
    }

    /**
     * Import zip project from file system into user workspace.
     */
    public void importZipProject(String workspaceId,
                                 String authToken,
                                 Path zipFile,
                                 String projectName,
                                 String template) throws Exception {
        String url = getWsAgentUrl(workspaceId, authToken) + "/import/" + projectName;
        createFolder(workspaceId, authToken, projectName);

        HttpURLConnection httpConnection = null;
        try {
            httpConnection = (HttpURLConnection)new URL(url).openConnection();
            httpConnection.setRequestMethod("POST");
            httpConnection.setRequestProperty("Content-Type", "application/zip");
            httpConnection.addRequestProperty("Authorization", machineServiceClient.getMachineApiToken(workspaceId, authToken));
            httpConnection.setDoOutput(true);

            try (OutputStream outputStream = httpConnection.getOutputStream()) {
                Files.copy(zipFile, outputStream);
                if (httpConnection.getResponseCode() != 201) {
                    throw new RuntimeException(
                            "Cannot deploy requested project using ProjectServiceClient REST API. Server response " +
                            httpConnection.getResponseCode() + " " +
                            IoUtil.readStream(httpConnection.getErrorStream()) + "REST url: " + url);
                }
            }
        } finally {
            ofNullable(httpConnection).ifPresent(HttpURLConnection::disconnect);
        }

        setProjectType(workspaceId, authToken, template, projectName);
    }


    /**
     * Import project from file system into a user workspace
     */
    public void importProject(String workspaceId,
                              String authToken,
                              Path sourceFolder,
                              String projectName,
                              String template) throws Exception {

        if (!Files.exists(sourceFolder)) {
            throw new IOException(format("%s not found", sourceFolder));
        }

        if (!Files.isDirectory(sourceFolder)) {
            throw new IOException(format("%s not a directory", sourceFolder));
        }

        Path zip = Files.createTempFile("project", projectName);
        ZipUtils.zipDir(sourceFolder.toString(), sourceFolder.toFile(), zip.toFile(), null);

        importZipProject(workspaceId, authToken, zip, projectName, template);
    }


    /**
     * Creates file in the project.
     */
    public void createFileInProject(String workspaceId,
                                    String authToken,
                                    String parentFolder,
                                    String fileName,
                                    String content) throws Exception {
        String apiRESTUrl = getWsAgentUrl(workspaceId, authToken) + "/file/" + parentFolder + "?name=" + fileName;

        HttpURLConnection httpConnection = null;
        try {
            httpConnection = (HttpURLConnection)new URL(apiRESTUrl).openConnection();
            httpConnection.setRequestMethod("POST");
            httpConnection.setRequestProperty("Content-Type", "text/plain");
            httpConnection.addRequestProperty("Authorization", machineServiceClient.getMachineApiToken(workspaceId, authToken));
            httpConnection.setDoOutput(true);
            try (OutputStream output = httpConnection.getOutputStream()) {
                output.write(content.getBytes("UTF-8"));
                if (httpConnection.getResponseCode() != 201) {
                    throw new RuntimeException(
                            "Cannot create requested content in the current project: " + apiRESTUrl + " something went wrong " +
                            httpConnection.getResponseCode() +
                            IoUtil.readStream(httpConnection.getErrorStream()));
                }
            }
        } finally {
            ofNullable(httpConnection).ifPresent(HttpURLConnection::disconnect);
        }
    }

    public ProjectConfig getFirstProject(String workspaceId, String authToken) throws Exception {
        String apiUrl = getWsAgentUrl(workspaceId, authToken);
        return requestFactory.fromUrl(apiUrl)
                             .setAuthorizationHeader(machineServiceClient.getMachineApiToken(workspaceId, authToken))
                             .request()
                             .asList(ProjectConfigDto.class)
                             .get(0);
    }

    /**
     * Updates file content.
     */
    public void updateFile(String workspaceId,
                           String authToken,
                           String pathToFile,
                           String content) throws Exception {
        String url = getWsAgentUrl(workspaceId, authToken) + "/file/" + pathToFile;

        HttpURLConnection httpConnection = null;
        try {
            httpConnection = (HttpURLConnection)new URL(url).openConnection();
            httpConnection.setRequestMethod("PUT");
            httpConnection.setRequestProperty("Content-Type", "text/plain");
            httpConnection.addRequestProperty("Authorization", machineServiceClient.getMachineApiToken(workspaceId, authToken));
            httpConnection.setDoOutput(true);

            try (OutputStream output = httpConnection.getOutputStream()) {
                output.write(content.getBytes("UTF-8"));
                if (httpConnection.getResponseCode() != 200) {
                    throw new RuntimeException(
                            "Cannot update content in the current file: " + url + " something went wrong " +
                            httpConnection.getResponseCode() +
                            IoUtil.readStream(httpConnection.getErrorStream()));
                }
            }
        } finally {
            ofNullable(httpConnection).ifPresent(HttpURLConnection::disconnect);
        }
    }

    private String getWsAgentUrl(String workspaceId, String authToken) throws Exception {
        return workspaceServiceClient.getById(workspaceId, authToken)
                                     .getRuntime()
                                     .getMachines()
                                     .get(0)
                                     .getRuntime()
                                     .getServers()
                                     .get(String.valueOf(WS_AGENT_PORT) + "/tcp")
                                     .getUrl() + "/project";
    }
}
