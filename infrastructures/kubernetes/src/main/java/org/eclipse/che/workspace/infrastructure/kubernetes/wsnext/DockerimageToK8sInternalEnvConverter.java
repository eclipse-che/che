package org.eclipse.che.workspace.infrastructure.kubernetes.wsnext;

import javax.inject.Inject;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.wsnext.InternalEnvironmentConverter;
import org.eclipse.che.workspace.infrastructure.docker.environment.dockerimage.DockerImageEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.convert.DockerImageEnvironmentConverter;

/**
 * Converts Dockerimage workspace environment to kubernetes environment.
 * Other internal environments returns without changing.
 *
 * <p>Allows support of sidecar tooling flow on Dockerimage environments while it is directly
 * implemented in the kubernetes environment only.
 *
 * @author Oleksandr Garagatyi
 */
public class DockerimageToK8sInternalEnvConverter implements InternalEnvironmentConverter {
  private final DockerImageEnvironmentConverter dockerImageEnvConverter;

  @Inject
  public DockerimageToK8sInternalEnvConverter(
      DockerImageEnvironmentConverter dockerImageEnvConverter) {
    this.dockerImageEnvConverter = dockerImageEnvConverter;
  }

  @Override
  public InternalEnvironment convert(InternalEnvironment internalEnvironment)
      throws InfrastructureException {

    if (internalEnvironment instanceof DockerImageEnvironment) {
      return dockerImageEnvConverter.convert((DockerImageEnvironment) internalEnvironment);
    }
    return internalEnvironment;
  }
}
