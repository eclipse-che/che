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
package org.eclipse.che.workspace.infrastructure.kubernetes.devfile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.fabric8.kubernetes.api.model.PodBuilder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.workspace.devfile.server.exception.DevfileFormatException;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EntrypointImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesRecipeParser;
import org.mockito.Mock;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class KubernetesComponentIntegrityValidatorTest {

  @Mock private KubernetesRecipeParser kubernetesRecipeParser;

  private KubernetesComponentValidator validator;

  @BeforeTest
  public void setup() {
    validator = new KubernetesComponentValidator(kubernetesRecipeParser);
  }

  @Test(expectedExceptions = DevfileFormatException.class)
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
    component.setReference("ref");
    component.setSelector(selector);
    component.setReferenceContent("content");

    // when
    validator.validateComponent(component, __ -> "");

    // then exception is thrown
  }

  @Test(expectedExceptions = DevfileFormatException.class)
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
    component.setReferenceContent("content");
    component.setReference("ref");

    EntrypointImpl entrypoint = new EntrypointImpl();
    entrypoint.setContainerName("not that container");
    component.setEntrypoints(Collections.singletonList(entrypoint));

    // when
    validator.validateComponent(component, __ -> "");

    // then exception is thrown
  }
}
