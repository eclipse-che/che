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
package org.eclipse.che.workspace.infrastructure.docker.environment.compose;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import com.google.common.base.Joiner;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.workspace.infrastructure.docker.environment.compose.model.ComposeService;

/** @author Alexander Garagatyi */
class ComposeEnvironmentValidator {
  private static final String SERVICE_NAME_REGEXP = "[a-zA-Z0-9._-]+";
  private static final Pattern SERVICE_NAME_PATTERN = Pattern.compile(SERVICE_NAME_REGEXP);

  // DockerContainer syntax patterns
  /**
   * Examples:
   *
   * <ul>
   *   <li>8080/tcp
   *   <li>8080/udp
   *   <li>8080
   *   <li>8/tcp
   *   <li>8
   * </ul>
   */
  private static final Pattern EXPOSE_PATTERN = Pattern.compile("^[1-9]+[0-9]*(/(tcp|udp))?$");
  /**
   * Examples:
   *
   * <ul>
   *   <li>service1
   *   <li>service1:alias1
   * </ul>
   */
  private static final Pattern LINK_PATTERN =
      Pattern.compile(
          "^(?<containerName>" + SERVICE_NAME_REGEXP + ")(:" + SERVICE_NAME_REGEXP + ")?$");

  private static final Pattern VOLUME_FROM_PATTERN =
      Pattern.compile("^(?<containerName>" + SERVICE_NAME_REGEXP + ")(:(ro|rw))?$");

  void validate(ComposeEnvironment env) throws ValidationException {
    checkArgument(
        env.getServices() != null && !env.getServices().isEmpty(),
        "Environment should contain at least 1 service");

    List<String> missingServices =
        env.getMachines()
            .keySet()
            .stream()
            .filter(machineName -> !env.getServices().containsKey(machineName))
            .collect(toList());
    checkArgument(
        missingServices.isEmpty(),
        "Environment contains machines that are missing in environment recipe: %s",
        Joiner.on(", ").join(missingServices));

    // needed to validate different kinds of dependencies in containers to other containers
    Set<String> containersNames = env.getServices().keySet();

    for (Map.Entry<String, ComposeService> serviceEntry : env.getServices().entrySet()) {
      validateService(serviceEntry.getKey(), serviceEntry.getValue(), containersNames);
    }
  }

  private void validateService(
      String serviceName, ComposeService service, Set<String> servicesNames)
      throws ValidationException {

    checkArgument(
        SERVICE_NAME_PATTERN.matcher(serviceName).matches(),
        "Name of service '%s' in environment is invalid",
        serviceName);

    checkArgument(
        !isNullOrEmpty(service.getImage())
            || (service.getBuild() != null
                && (!isNullOrEmpty(service.getBuild().getContext())
                    || !isNullOrEmpty(service.getBuild().getDockerfile()))),
        "Field 'image' or 'build.context' is required in service '%s' in environment",
        serviceName);

    checkArgument(
        service.getBuild() == null
            || (isNullOrEmpty(service.getBuild().getContext())
                != isNullOrEmpty(service.getBuild().getDockerfile())),
        "Service '%s' in environment contains mutually exclusive dockerfile content and build context.",
        serviceName);

    for (String expose : service.getExpose()) {
      checkArgument(
          EXPOSE_PATTERN.matcher(expose).matches(),
          "Exposed port '%s' in service '%s' in environment is invalid",
          expose,
          serviceName);
    }

    for (String link : service.getLinks()) {
      Matcher matcher = LINK_PATTERN.matcher(link);

      checkArgument(
          matcher.matches(),
          "Link '%s' in service '%s' in environment is invalid",
          link,
          serviceName);

      String containerFromLink = matcher.group("containerName");
      checkArgument(
          !serviceName.equals(containerFromLink),
          "Container '%s' has illegal link to itself",
          serviceName);
      checkArgument(
          servicesNames.contains(containerFromLink),
          "Service '%s' in environment contains link to non existing service '%s'",
          serviceName,
          containerFromLink);
    }

    for (String depends : service.getDependsOn()) {
      checkArgument(
          SERVICE_NAME_PATTERN.matcher(depends).matches(),
          "Dependency '%s' in service '%s' in environment is invalid",
          depends,
          serviceName);

      checkArgument(
          !serviceName.equals(depends),
          "Container '%s' has illegal dependency to itself",
          serviceName);
      checkArgument(
          servicesNames.contains(depends),
          "Service '%s' in environment contains dependency to non existing service '%s'",
          serviceName,
          depends);
    }

    for (String volumesFrom : service.getVolumesFrom()) {
      Matcher matcher = VOLUME_FROM_PATTERN.matcher(volumesFrom);

      checkArgument(
          matcher.matches(),
          "Service name '%s' in field 'volumes_from' of service '%s' in environment is invalid",
          volumesFrom,
          serviceName);

      String containerFromVolumesFrom = matcher.group("containerName");
      checkArgument(
          !serviceName.equals(containerFromVolumesFrom),
          "Container '%s' can not mount volume from itself",
          serviceName);
      checkArgument(
          servicesNames.contains(containerFromVolumesFrom),
          "Service '%s' in environment contains non existing service '%s' in 'volumes_from' field",
          serviceName,
          containerFromVolumesFrom);
    }

    checkArgument(
        service.getPorts() == null || service.getPorts().isEmpty(),
        "Ports binding is forbidden but found in service '%s' of environment",
        serviceName);

    checkArgument(
        service.getVolumes() == null || service.getVolumes().isEmpty(),
        "Volumes binding is forbidden but found in service '%s' of environment",
        serviceName);

    checkArgument(
        service.getNetworks() == null || service.getNetworks().isEmpty(),
        "Networks configuration is forbidden but found in service '%s' of environment",
        serviceName);
  }

  private static void checkArgument(boolean expression, String error) throws ValidationException {
    if (!expression) {
      throw new ValidationException(error);
    }
  }

  private static void checkArgument(
      boolean expression, String errorMessageTemplate, Object... errorMessageParams)
      throws ValidationException {
    if (!expression) {
      throw new ValidationException(format(errorMessageTemplate, errorMessageParams));
    }
  }
}
