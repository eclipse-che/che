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
package org.eclipse.che.workspace.infrastructure.openshift.environment;

import static java.util.Collections.emptyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link OpenShiftEnvironmentValidator}.
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class OpenShiftEnvironmentValidatorTest {
  @Mock private OpenShiftEnvironment openShiftEnvironment;

  private OpenShiftEnvironmentValidator environmentValidator;

  @BeforeMethod
  public void setUp() throws Exception {
    environmentValidator = new OpenShiftEnvironmentValidator();
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp = "Environment should contain at least 1 pod"
  )
  public void shouldThrowExceptionWhenEnvDoesNotHaveAnyPods() throws Exception {
    // given
    when(openShiftEnvironment.getPods()).thenReturn(emptyMap());

    // when
    environmentValidator.validate(openShiftEnvironment);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp =
        "Environment contains machines that are missing in recipe: pod1/db"
  )
  public void shouldThrowExceptionWhenMachineIsDeclaredButThereIsNotContainerInOpenShiftRecipe()
      throws Exception {
    // given
    Pod pod = createPod("pod1", "main");
    when(openShiftEnvironment.getPods()).thenReturn(ImmutableMap.of("pod1", pod));
    when(openShiftEnvironment.getMachines())
        .thenReturn(ImmutableMap.of("pod1/db", mock(InternalMachineConfig.class)));

    // when
    environmentValidator.validate(openShiftEnvironment);
  }

  private Pod createPod(String name, String... containers) {
    return new PodBuilder()
        .withNewMetadata()
        .withName(name)
        .endMetadata()
        .withNewSpec()
        .withContainers(
            Arrays.stream(containers).map(this::createContainer).collect(Collectors.toList()))
        .endSpec()
        .build();
  }

  private Container createContainer(String name) {
    return new ContainerBuilder().withName(name).build();
  }
}
