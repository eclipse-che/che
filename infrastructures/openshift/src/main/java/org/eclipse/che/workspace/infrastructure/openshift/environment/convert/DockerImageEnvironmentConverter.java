/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift.environment.convert;

import static java.lang.String.format;
import static org.eclipse.che.workspace.infrastructure.openshift.Constants.MACHINE_NAME_ANNOTATION_FMT;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.inject.Singleton;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.workspace.infrastructure.docker.environment.dockerimage.DockerImageEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;

/**
 * Converts {@link DockerImageEnvironment} to {@link OpenShiftEnvironment}.
 *
 * @author Sergii Leshchenko
 * @author Anton Korneta
 */
@Singleton
public class DockerImageEnvironmentConverter {

  static final String POD_NAME = "dockerimage";
  static final String CONTAINER_NAME = "container";

  public OpenShiftEnvironment convert(DockerImageEnvironment environment)
      throws InfrastructureException {
    final Iterator<String> iterator = environment.getMachines().keySet().iterator();
    if (!iterator.hasNext()) {
      throw new InternalInfrastructureException(
          "DockerImage environment must contain at least one machine configuration");
    }
    final String machineName = iterator.next();
    final String dockerImage = environment.getRecipe().getContent();
    final Map<String, String> annotations = new HashMap<>();
    annotations.put(format(MACHINE_NAME_ANNOTATION_FMT, CONTAINER_NAME), machineName);

    final Pod pod =
        new PodBuilder()
            .withNewMetadata()
            .withName(POD_NAME)
            .withAnnotations(annotations)
            .endMetadata()
            .withNewSpec()
            .withContainers(
                new ContainerBuilder().withImage(dockerImage).withName(CONTAINER_NAME).build())
            .endSpec()
            .build();
    return OpenShiftEnvironment.builder()
        .setMachines(environment.getMachines())
        .setInternalRecipe(environment.getRecipe())
        .setWarnings(environment.getWarnings())
        .setPods(ImmutableMap.of(POD_NAME, pod))
        .build();
  }
}
