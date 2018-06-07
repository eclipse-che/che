package org.eclipse.che.api.project.server.impl;

import static java.util.Collections.unmodifiableSet;

import com.google.inject.Inject;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.workspace.server.WorkspaceService;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class RemoteProjects {
  private static final Logger LOG = LoggerFactory.getLogger(RemoteProjects.class);
  private final String apiEndpoint;
  private final HttpJsonRequestFactory httpJsonRequestFactory;
  private final String workspaceId;

  @Inject
  public RemoteProjects(
      @Named("che.api") String apiEndpoint, HttpJsonRequestFactory requestFactory) {
    this.apiEndpoint = apiEndpoint;
    this.workspaceId = System.getenv("CHE_WORKSPACE_ID");
    System.out.println("workspace id= " + workspaceId);
    System.out.println("api endpoint= " + apiEndpoint);
    this.httpJsonRequestFactory = requestFactory;
    // check connection
    try {
      workspaceDto();
    } catch (ServerException e) {
      LOG.error(e.getLocalizedMessage());
      System.exit(1);
    }
  }

  public Set<ProjectConfig> getAll() throws ServerException {
    WorkspaceConfig config = workspaceDto().getConfig();
    Set<ProjectConfig> projectConfigs = new HashSet<>(config.getProjects());

    return unmodifiableSet(projectConfigs);
  }

  public Set<ProjectConfig> getAll(String wsPath) throws ServerException {
    WorkspaceConfig config = workspaceDto().getConfig();
    Set<ProjectConfig> projectConfigs = new HashSet<>(config.getProjects());

    projectConfigs.removeIf(it -> it.getPath().equals(wsPath));
    projectConfigs.removeIf(it -> !it.getPath().startsWith(wsPath));

    return unmodifiableSet(projectConfigs);
  }

  /** @return WorkspaceDto */
  private WorkspaceDto workspaceDto() throws ServerException {

    final UriBuilder builder =
        UriBuilder.fromUri(apiEndpoint)
            .path(WorkspaceService.class)
            .path(WorkspaceService.class, "getByKey");
    final String href = builder.build(workspaceId).toString();
    try {
      return httpJsonRequestFactory
          .fromUrl(href)
          .useGetMethod()
          .request()
          .asDto(WorkspaceDto.class);
    } catch (IOException | ApiException e) {
      throw new ServerException(e);
    }
  }
}
