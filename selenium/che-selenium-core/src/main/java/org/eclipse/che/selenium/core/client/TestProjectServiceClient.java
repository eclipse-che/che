/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.client;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.eclipse.che.dto.server.DtoFactory.getInstance;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.ZipUtils;
import org.eclipse.che.selenium.core.provider.TestWorkspaceAgentApiEndpoint;

/**
 * @author Musienko Maxim
 * @author Mykola Morhun
 */
@Singleton
public class TestProjectServiceClient {
  private static final int WS_AGENT_PORT = 4401;

  private final TestMachineServiceClient machineServiceClient;
  private final HttpJsonRequestFactory requestFactory;
  private final TestWorkspaceAgentApiEndpoint workspaceAgentApiEndpoint;

  @Inject
  public TestProjectServiceClient(
      TestMachineServiceClient machineServiceClient,
      HttpJsonRequestFactory requestFactory,
      TestWorkspaceAgentApiEndpoint workspaceAgentApiEndpoint) {
    this.machineServiceClient = machineServiceClient;
    this.requestFactory = requestFactory;
    this.workspaceAgentApiEndpoint = workspaceAgentApiEndpoint;
  }

  /** Set type for existing project on vfs */
  public void setProjectType(String workspaceId, String template, String projectName)
      throws Exception {
    InputStream in = getClass().getResourceAsStream("/templates/project/" + template);
    String json = IoUtil.readAndCloseQuietly(in);

    ProjectConfigDto project = getInstance().createDtoFromJson(json, ProjectConfigDto.class);
    project.setName(projectName);

    String url = workspaceAgentApiEndpoint.get(workspaceId);
    requestFactory
        .fromUrl(url + "project/" + projectName)
        .usePutMethod()
        .setAuthorizationHeader(machineServiceClient.getMachineApiToken(workspaceId))
        .setBody(project)
        .request();
  }

  /** Delete resource. */
  public void deleteResource(String workspaceId, String path) throws Exception {
    String wsAgentUrl = workspaceAgentApiEndpoint.get(workspaceId);
    requestFactory
        .fromUrl(wsAgentUrl + "project/" + path)
        .setAuthorizationHeader(machineServiceClient.getMachineApiToken(workspaceId))
        .useDeleteMethod()
        .request();
  }

  public void createFolder(String workspaceId, String folder) throws Exception {
    String url = workspaceAgentApiEndpoint.get(workspaceId) + "project/folder/" + folder;
    requestFactory
        .fromUrl(url)
        .setAuthorizationHeader(machineServiceClient.getMachineApiToken(workspaceId))
        .usePostMethod()
        .request();
  }

  /** Import zip project from file system into user workspace. */
  public void importZipProject(
      String workspaceId, Path zipFile, String projectName, String template) throws Exception {
    String url = workspaceAgentApiEndpoint.get(workspaceId) + "project/import/" + projectName;
    //    createFolder(workspaceId, projectName);

    HttpURLConnection httpConnection = null;
    try {
      httpConnection = (HttpURLConnection) new URL(url).openConnection();
      httpConnection.setRequestMethod("POST");
      httpConnection.setRequestProperty("Content-Type", "application/zip");
      httpConnection.addRequestProperty(
          "Authorization", machineServiceClient.getMachineApiToken(workspaceId));
      httpConnection.setDoOutput(true);

      try (OutputStream outputStream = httpConnection.getOutputStream()) {
        Files.copy(zipFile, outputStream);
        if (httpConnection.getResponseCode() != 201) {
          throw new RuntimeException(
              "Cannot deploy requested project using ProjectServiceClient REST API. Server response "
                  + httpConnection.getResponseCode()
                  + " "
                  + IoUtil.readStream(httpConnection.getErrorStream())
                  + "REST url: "
                  + url);
        }
      }
    } finally {
      ofNullable(httpConnection).ifPresent(HttpURLConnection::disconnect);
    }

    setProjectType(workspaceId, template, projectName);
  }

  /** Import project from file system into a user workspace */
  public void importProject(
      String workspaceId, Path sourceFolder, String projectName, String template) throws Exception {

    if (!Files.exists(sourceFolder)) {
      throw new IOException(format("%s not found", sourceFolder));
    }

    if (!Files.isDirectory(sourceFolder)) {
      throw new IOException(format("%s not a directory", sourceFolder));
    }

    Path zip = Files.createTempFile("project", projectName);
    ZipUtils.zipDir(sourceFolder.toString(), sourceFolder.toFile(), zip.toFile(), null);

    importZipProject(workspaceId, zip, projectName, template);
  }

  /** Creates file in the project. */
  public void createFileInProject(
      String workspaceId, String parentFolder, String fileName, String content) throws Exception {
    String apiRESTUrl =
        workspaceAgentApiEndpoint.get(workspaceId)
            + "project/file/"
            + parentFolder
            + "?name="
            + fileName;

    HttpURLConnection httpConnection = null;
    try {
      httpConnection = (HttpURLConnection) new URL(apiRESTUrl).openConnection();
      httpConnection.setRequestMethod("POST");
      httpConnection.setRequestProperty("Content-Type", "text/plain");
      httpConnection.addRequestProperty(
          "Authorization", machineServiceClient.getMachineApiToken(workspaceId));
      httpConnection.setDoOutput(true);
      try (OutputStream output = httpConnection.getOutputStream()) {
        output.write(content.getBytes("UTF-8"));
        if (httpConnection.getResponseCode() != 201) {
          throw new RuntimeException(
              "Cannot create requested content in the current project: "
                  + apiRESTUrl
                  + " something went wrong "
                  + httpConnection.getResponseCode()
                  + IoUtil.readStream(httpConnection.getErrorStream()));
        }
      }
    } finally {
      ofNullable(httpConnection).ifPresent(HttpURLConnection::disconnect);
    }
  }

  public ProjectConfigDto getFirstProject(String workspaceId) throws Exception {
    String apiUrl = workspaceAgentApiEndpoint.get(workspaceId) + "project";
    return requestFactory
        .fromUrl(apiUrl)
        .setAuthorizationHeader(machineServiceClient.getMachineApiToken(workspaceId))
        .request()
        .asList(ProjectConfigDto.class)
        .get(0);
  }

  /** Updates file content. */
  public void updateFile(String workspaceId, String pathToFile, String content) throws Exception {
    String url = workspaceAgentApiEndpoint.get(workspaceId) + "project/file/" + pathToFile;

    HttpURLConnection httpConnection = null;
    try {
      httpConnection = (HttpURLConnection) new URL(url).openConnection();
      httpConnection.setRequestMethod("PUT");
      httpConnection.setRequestProperty("Content-Type", "text/plain");
      httpConnection.addRequestProperty(
          "Authorization", machineServiceClient.getMachineApiToken(workspaceId));
      httpConnection.setDoOutput(true);

      try (OutputStream output = httpConnection.getOutputStream()) {
        output.write(content.getBytes("UTF-8"));
        if (httpConnection.getResponseCode() != 200) {
          throw new RuntimeException(
              "Cannot update content in the current file: "
                  + url
                  + " something went wrong "
                  + httpConnection.getResponseCode()
                  + IoUtil.readStream(httpConnection.getErrorStream()));
        }
      }
    } finally {
      ofNullable(httpConnection).ifPresent(HttpURLConnection::disconnect);
    }
  }

  public boolean checkProjectType(String wokspaceId, String projectName, String projectType)
      throws Exception {
    return getProjectParameters(wokspaceId, projectName)
        .get("type")
        .getAsString()
        .equals(projectType);
  }

  public boolean checkProjectLanguage(String wokspaceId, String projectName, String projectLanguage)
      throws Exception {
    return getProjectParameters(wokspaceId, projectName)
        .getAsJsonObject("attributes")
        .get("language")
        .getAsString()
        .equals(projectLanguage);
  }

  public List<String> getExternalLibraries(String workspaceId, String projectName)
      throws Exception {
    List<String> result = new ArrayList<>();
    HttpJsonResponse response =
        requestFactory
            .fromUrl(workspaceAgentApiEndpoint.get(workspaceId) + "java/navigation/libraries")
            .useGetMethod()
            .addQueryParam("projectpath", "/" + projectName)
            .request();

    parseResponseAsArray(response)
        .forEach(
            jsonElement -> result.add(jsonElement.getAsJsonObject().get("name").getAsString()));

    return result;
  }

  private JsonObject parseResponseAsJsonObject(HttpJsonResponse response) {
    return new JsonParser().parse(response.asString()).getAsJsonObject();
  }

  private JsonArray parseResponseAsArray(HttpJsonResponse response) {
    return new JsonParser().parse(response.asString()).getAsJsonArray();
  }

  private JsonObject getProjectParameters(String workspaceId, String projectName) throws Exception {
    HttpJsonResponse response =
        requestFactory
            .fromUrl(workspaceAgentApiEndpoint.get(workspaceId) + "project/" + projectName)
            .useGetMethod()
            .request();

    return parseResponseAsJsonObject(response);
  }
}
