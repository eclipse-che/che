/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.client;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Resources.getResource;
import static com.google.common.io.Resources.toByteArray;
import static java.lang.String.format;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.write;
import static java.util.Optional.ofNullable;
import static org.eclipse.che.dto.server.DtoFactory.getInstance;
import static org.eclipse.che.selenium.core.project.ProjectTemplates.PLAIN_JAVA;

import com.google.common.io.Resources;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.ZipUtils;
import org.eclipse.che.selenium.core.provider.TestWorkspaceAgentApiEndpointUrlProvider;

/**
 * @author Musienko Maxim
 * @author Mykola Morhun
 */
@Singleton
public class TestProjectServiceClient {

  private static final String BEARER_TOKEN_PREFIX = "Bearer ";
  private final TestMachineServiceClient machineServiceClient;
  private final HttpJsonRequestFactory requestFactory;
  private final TestWorkspaceAgentApiEndpointUrlProvider workspaceAgentApiEndpointUrlProvider;

  @Inject
  public TestProjectServiceClient(
      TestMachineServiceClient machineServiceClient,
      HttpJsonRequestFactory requestFactory,
      TestWorkspaceAgentApiEndpointUrlProvider workspaceAgentApiEndpointUrlProvider) {
    this.machineServiceClient = machineServiceClient;
    this.requestFactory = requestFactory;
    this.workspaceAgentApiEndpointUrlProvider = workspaceAgentApiEndpointUrlProvider;
  }

  /** Set type for existing project on vfs */
  public void setProjectType(String workspaceId, String template, String projectName)
      throws Exception {
    InputStream in = getClass().getResourceAsStream("/templates/project/" + template);
    String json = IoUtil.readAndCloseQuietly(in);

    ProjectConfigDto project = getInstance().createDtoFromJson(json, ProjectConfigDto.class);
    project.setName(projectName);

    requestFactory
        .fromUrl(workspaceAgentApiEndpointUrlProvider.get(workspaceId) + "project/" + projectName)
        .usePutMethod()
        .setAuthorizationHeader(
            BEARER_TOKEN_PREFIX + machineServiceClient.getMachineApiToken(workspaceId))
        .setBody(project)
        .request();
  }

  /** Delete resource. */
  public void deleteResource(String workspaceId, String path) throws Exception {
    requestFactory
        .fromUrl(workspaceAgentApiEndpointUrlProvider.get(workspaceId) + "project/" + path)
        .setAuthorizationHeader(
            BEARER_TOKEN_PREFIX + machineServiceClient.getMachineApiToken(workspaceId))
        .useDeleteMethod()
        .request();
  }

  public void createFolder(String workspaceId, String folder) throws Exception {
    String url = workspaceAgentApiEndpointUrlProvider.get(workspaceId) + "project/folder/" + folder;
    requestFactory
        .fromUrl(url)
        .setAuthorizationHeader(
            BEARER_TOKEN_PREFIX + machineServiceClient.getMachineApiToken(workspaceId))
        .usePostMethod()
        .request();
  }

  /** Import zip project from file system into user workspace. */
  public void importZipProject(
      String workspaceId, Path zipFile, String projectName, String template) throws Exception {
    String url =
        workspaceAgentApiEndpointUrlProvider.get(workspaceId) + "project/import/" + projectName;
    //    createFolder(workspaceId, projectName);

    HttpURLConnection httpConnection = null;
    try {
      httpConnection = (HttpURLConnection) new URL(url).openConnection();
      httpConnection.setRequestMethod("POST");
      httpConnection.setRequestProperty("Content-Type", "application/zip");
      httpConnection.addRequestProperty(
          "Authorization",
          BEARER_TOKEN_PREFIX + machineServiceClient.getMachineApiToken(workspaceId));
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
    try (ZipOutputStream out = ZipUtils.stream(zip)) {
      ZipUtils.add(out, sourceFolder, sourceFolder);
      if (PLAIN_JAVA.equals(template)) {
        Path tmpDir = Files.createTempDirectory("TestProject");
        Path dotClasspath = createFile(tmpDir.resolve(".classpath"));
        Path dotProject = Files.createFile(tmpDir.resolve(".project"));
        write(
            dotProject,
            format(
                    Resources.toString(getResource("projects/jdt-ls-project-files/project"), UTF_8),
                    projectName)
                .getBytes());
        write(dotClasspath, toByteArray(getResource("projects/jdt-ls-project-files/classpath")));
        ZipUtils.add(out, dotClasspath);
        ZipUtils.add(out, dotProject);
      }
    }

    importZipProject(workspaceId, zip, projectName, template);
  }

  /** Import project from file system into a user workspace */
  public void importProject(
      String workspaceId,
      String projectName,
      String location,
      String type,
      Map<String, String> parameters)
      throws Exception {
    SourceStorageDto source = getInstance().createDto(SourceStorageDto.class);
    source.setLocation(location);
    source.setType(type);
    source.setParameters(parameters);

    importProject(workspaceId, projectName, source);
  }

  /** Import project from file system into a user workspace */
  public void importProject(String workspaceId, String projectName, SourceStorageDto source)
      throws Exception {

    requestFactory
        .fromUrl(
            workspaceAgentApiEndpointUrlProvider.get(workspaceId) + "project/import/" + projectName)
        .usePostMethod()
        .setAuthorizationHeader(machineServiceClient.getMachineApiToken(workspaceId))
        .setBody(source)
        .request();
  }

  /** Creates file in the project. */
  public void createFileInProject(
      String workspaceId, String parentFolder, String fileName, String content) throws Exception {
    String apiRESTUrl =
        workspaceAgentApiEndpointUrlProvider.get(workspaceId)
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
          "Authorization",
          BEARER_TOKEN_PREFIX + machineServiceClient.getMachineApiToken(workspaceId));
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
    String apiUrl = workspaceAgentApiEndpointUrlProvider.get(workspaceId) + "project";
    return requestFactory
        .fromUrl(apiUrl)
        .setAuthorizationHeader(
            BEARER_TOKEN_PREFIX + machineServiceClient.getMachineApiToken(workspaceId))
        .request()
        .asList(ProjectConfigDto.class)
        .get(0);
  }

  /** Updates file content. */
  public void updateFile(String workspaceId, String pathToFile, String content) throws Exception {
    String url =
        workspaceAgentApiEndpointUrlProvider.get(workspaceId) + "project/file/" + pathToFile;

    HttpURLConnection httpConnection = null;
    try {
      httpConnection = (HttpURLConnection) new URL(url).openConnection();
      httpConnection.setRequestMethod("PUT");
      httpConnection.setRequestProperty("Content-Type", "text/plain");
      httpConnection.addRequestProperty(
          "Authorization",
          BEARER_TOKEN_PREFIX + machineServiceClient.getMachineApiToken(workspaceId));
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
    return getProject(wokspaceId, projectName).getType().equals(projectType);
  }

  public boolean checkProjectLanguage(String workspaceId, String projectName, String language)
      throws Exception {

    return getProject(workspaceId, projectName).getAttributes().get("language").contains(language);
  }

  public boolean checkProjectLanguage(
      String workspaceId, String projectName, List<String> languages) throws Exception {

    return getProject(workspaceId, projectName)
        .getAttributes()
        .get("language")
        .containsAll(languages);
  }

  public List<String> getExternalLibraries(String workspaceId, String projectName)
      throws Exception {
    return requestFactory
        .fromUrl(
            workspaceAgentApiEndpointUrlProvider.get(workspaceId) + "java/navigation/libraries")
        .useGetMethod()
        .addQueryParam("projectpath", "/" + projectName)
        .request()
        .asList(ProjectConfigDto.class)
        .stream()
        .map(e -> e.getName())
        .collect(Collectors.toList());
  }

  private ProjectConfig getProject(String workspaceId, String projectName) throws Exception {
    return requestFactory
        .fromUrl(workspaceAgentApiEndpointUrlProvider.get(workspaceId) + "project/" + projectName)
        .useGetMethod()
        .setAuthorizationHeader(
            BEARER_TOKEN_PREFIX + machineServiceClient.getMachineApiToken(workspaceId))
        .request()
        .asDto(ProjectConfigDto.class);
  }
}
