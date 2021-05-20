/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.devfile;

import static org.eclipse.che.api.workspace.server.devfile.Constants.KUBERNETES_COMPONENT_TYPE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.fabric8.kubernetes.api.model.PodBuilder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileFormatException;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EntrypointImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesRecipeParser;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class KubernetesComponentIntegrityValidatorTest {

  @Mock private KubernetesRecipeParser kubernetesRecipeParser;

  private KubernetesComponentValidator validator;

  @BeforeMethod
  public void setup() {
    Set<String> k8sComponentTypes = new HashSet<>();
    k8sComponentTypes.add(KUBERNETES_COMPONENT_TYPE);
    validator = new KubernetesComponentValidator(kubernetesRecipeParser, k8sComponentTypes);
  }

  @Test
  public void shouldApplySelector() throws Exception {
    // given
    when(kubernetesRecipeParser.parse(any(String.class)))
        .thenReturn(
            Arrays.asList(
                new PodBuilder().withNewMetadata().addToLabels("app", "test").endMetadata().build(),
                new PodBuilder()
                    .withNewMetadata()
                    .addToLabels("app", "other")
                    .endMetadata()
                    .build()));

    Map<String, String> selector = new HashMap<>();
    selector.put("app", "test");

    ComponentImpl component = new ComponentImpl();
    component.setType(KUBERNETES_COMPONENT_TYPE);
    component.setReference("ref");
    component.setSelector(selector);
    component.setReferenceContent("content");

    // when
    validator.validateComponent(component, __ -> "");

    // then no exception is thrown
  }

  @Test
  public void shouldApplyEntrypoint() throws Exception {
    // given
    when(kubernetesRecipeParser.parse(any(String.class)))
        .thenReturn(
            Arrays.asList(
                new PodBuilder()
                    .withNewSpec()
                    .addNewContainer()
                    .withName("container_a")
                    .endContainer()
                    .endSpec()
                    .build(),
                new PodBuilder()
                    .withNewSpec()
                    .addNewContainer()
                    .withName("container_b")
                    .endContainer()
                    .endSpec()
                    .build()));

    ComponentImpl component = new ComponentImpl();
    component.setType(KUBERNETES_COMPONENT_TYPE);
    component.setReferenceContent("content");
    component.setReference("ref");

    EntrypointImpl entrypoint = new EntrypointImpl();
    entrypoint.setContainerName("container_a");
    component.setEntrypoints(Collections.singletonList(entrypoint));

    // when
    validator.validateComponent(component, __ -> "");

    // then no exception is thrown
  }

  @Test
  public void shouldValidateContainerMatchingEntrypointInPodMatchingSelector() throws Exception {
    // given
    when(kubernetesRecipeParser.parse(any(String.class)))
        .thenReturn(
            Arrays.asList(
                new PodBuilder()
                    .withNewMetadata()
                    .addToLabels("app", "test")
                    .endMetadata()
                    .withNewSpec()
                    .addNewContainer()
                    .withName("container_a")
                    .endContainer()
                    .endSpec()
                    .build(),
                new PodBuilder()
                    .withNewMetadata()
                    .addToLabels("app", "other")
                    .endMetadata()
                    .withNewSpec()
                    .addNewContainer()
                    .withName("container_a")
                    .endContainer()
                    .endSpec()
                    .build()));

    Map<String, String> selector = new HashMap<>();
    selector.put("app", "test");

    ComponentImpl component = new ComponentImpl();
    component.setType(KUBERNETES_COMPONENT_TYPE);
    component.setReference("ref");
    component.setSelector(selector);
    component.setReferenceContent("content");

    EntrypointImpl entrypoint = new EntrypointImpl();
    entrypoint.setContainerName("container_a");
    component.setEntrypoints(Collections.singletonList(entrypoint));

    // when
    validator.validateComponent(component, __ -> "");

    // then no exception is thrown
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp =
          "Failed to validate content reference of component 'ref' of type 'kubernetes': The selector of the component 'ref' of type 'kubernetes' filters out all objects from the list.")
  public void shouldThrowExceptionOnSelectorFilteringOutEverything() throws Exception {
    // given
    when(kubernetesRecipeParser.parse(any(String.class)))
        .thenReturn(
            Collections.singletonList(
                new PodBuilder()
                    .withNewMetadata()
                    .addToLabels("app", "test")
                    .endMetadata()
                    .build()));

    Map<String, String> selector = new HashMap<>();
    selector.put("app", "a different value");

    ComponentImpl component = new ComponentImpl();
    component.setType(KUBERNETES_COMPONENT_TYPE);
    component.setReference("ref");
    component.setSelector(selector);
    component.setReferenceContent("content");

    // when
    validator.validateComponent(component, __ -> "");

    // then exception is thrown
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp =
          "Failed to validate content reference of component 'ref' of type 'kubernetes': Component 'ref' of type 'kubernetes' contains an entry point that doesn't match any container.")
  public void shouldThrowExceptionOnEntrypointNotMatchingAnyContainer() throws Exception {
    // given
    when(kubernetesRecipeParser.parse(any(String.class)))
        .thenReturn(
            Collections.singletonList(
                new PodBuilder()
                    .withNewSpec()
                    .addNewContainer()
                    .withName("container")
                    .endContainer()
                    .endSpec()
                    .build()));

    ComponentImpl component = new ComponentImpl();
    component.setType(KUBERNETES_COMPONENT_TYPE);
    component.setReferenceContent("content");
    component.setReference("ref");

    EntrypointImpl entrypoint = new EntrypointImpl();
    entrypoint.setContainerName("not that container");
    component.setEntrypoints(Collections.singletonList(entrypoint));

    // when
    validator.validateComponent(component, __ -> "");

    // then exception is thrown
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp =
          "Failed to validate content reference of component 'ref' of type 'kubernetes': Component 'ref' of type 'kubernetes' contains an entry point that doesn't match any container.")
  public void shouldThrowExceptionOnEntrypointNotMatchingAnyContainerOfPodsMatchingSelector()
      throws Exception {
    // given
    when(kubernetesRecipeParser.parse(any(String.class)))
        .thenReturn(
            Arrays.asList(
                new PodBuilder()
                    .withNewMetadata()
                    .addToLabels("app", "test")
                    .endMetadata()
                    .withNewSpec()
                    .addNewContainer()
                    .withName("container_a")
                    .endContainer()
                    .endSpec()
                    .build(),
                new PodBuilder()
                    .withNewMetadata()
                    .addToLabels("app", "other")
                    .endMetadata()
                    .withNewSpec()
                    .addNewContainer()
                    .withName("container_b")
                    .endContainer()
                    .endSpec()
                    .build()));

    Map<String, String> selector = new HashMap<>();
    selector.put("app", "test");

    ComponentImpl component = new ComponentImpl();
    component.setType(KUBERNETES_COMPONENT_TYPE);
    component.setReferenceContent("content");
    component.setReference("ref");
    component.setSelector(selector);

    EntrypointImpl entrypoint = new EntrypointImpl();
    entrypoint.setContainerName("container_b");
    component.setEntrypoints(Collections.singletonList(entrypoint));

    // when
    validator.validateComponent(component, __ -> "");

    // then exception is thrown
  }
}
