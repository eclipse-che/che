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
package org.eclipse.che.api.devfile.server.convert.tool.kubernetes;

import static java.lang.String.format;
import static java.util.Arrays.asList;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.openshift.api.model.Template;
import io.fabric8.openshift.api.model.TemplateBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ContainerSearchTest {

  private List<HasMetadata> testList;

  @BeforeMethod
  public void setup() {
    Container container1 = new ContainerBuilder().withName("container1").build();
    Container container2 = new ContainerBuilder().withName("container2").build();
    Container container3 = new ContainerBuilder().withName("container3").build();
    Container container4 = new ContainerBuilder().withName("container4").build();
    Container container5 = new ContainerBuilder().withName("container5").build();
    Container container6 = new ContainerBuilder().withName("container6").build();
    Container container7 = new ContainerBuilder().withName("container7").build();

    Pod topLevelPodWithName =
        new PodBuilder()
            .withNewMetadata()
            .withName("podWithName")
            .addToLabels("app", "che")
            .endMetadata()
            .withNewSpec()
            .withContainers(container1)
            .endSpec()
            .build();

    Pod topLevelPodWithGenerateName =
        new PodBuilder()
            .withNewMetadata()
            .withGenerateName("podWithGenerateName")
            .endMetadata()
            .withNewSpec()
            .withContainers(container2)
            .endSpec()
            .build();

    Deployment deploymentWithNameWithPodWithName =
        new DeploymentBuilder()
            .withNewMetadata()
            .withName("deploymentWithName")
            .addToLabels("app", "che")
            .endMetadata()
            .withNewSpec()
            .withNewTemplate()
            .withNewMetadata()
            .withName("podWithName")
            .endMetadata()
            .withNewSpec()
            .withContainers(container3)
            .endSpec()
            .endTemplate()
            .endSpec()
            .build();

    Deployment deploymentWithNameWithPodWithGenerateName =
        new DeploymentBuilder()
            .withNewMetadata()
            .withName("deploymentWithName")
            .endMetadata()
            .withNewSpec()
            .withNewTemplate()
            .withNewMetadata()
            .withGenerateName("podWithGenerateName")
            .endMetadata()
            .withNewSpec()
            .withContainers(container4)
            .endSpec()
            .endTemplate()
            .endSpec()
            .build();

    Deployment deploymentWithGenerateNameWithPodWithName =
        new DeploymentBuilder()
            .withNewMetadata()
            .withGenerateName("deploymentWithGenerateName")
            .endMetadata()
            .withNewSpec()
            .withNewTemplate()
            .withNewMetadata()
            .withName("podWithName")
            .endMetadata()
            .withNewSpec()
            .withContainers(container5)
            .endSpec()
            .endTemplate()
            .endSpec()
            .build();

    Deployment deploymentWithGenerateNameWithPodWithGenerateName =
        new DeploymentBuilder()
            .withNewMetadata()
            .withGenerateName("deploymentWithGenerateName")
            .endMetadata()
            .withNewSpec()
            .withNewTemplate()
            .withNewMetadata()
            .withGenerateName("podWithGenerateName")
            .endMetadata()
            .withNewSpec()
            .withContainers(container6)
            .endSpec()
            .endTemplate()
            .endSpec()
            .build();

    Template template =
        new TemplateBuilder()
            .addNewDeploymentObject()
            .withNewMetadata()
            .withName("deploymentWithName")
            .endMetadata()
            .withNewSpec()
            .withNewTemplate()
            .withNewMetadata()
            .withName("podWithName")
            .endMetadata()
            .withNewSpec()
            .withContainers(container7)
            .endSpec()
            .endTemplate()
            .endSpec()
            .endDeploymentObject()
            .build();

    testList =
        asList(
            topLevelPodWithName,
            topLevelPodWithGenerateName,
            deploymentWithNameWithPodWithName,
            deploymentWithNameWithPodWithGenerateName,
            deploymentWithGenerateNameWithPodWithName,
            deploymentWithGenerateNameWithPodWithGenerateName,
            template);
  }

  @Test
  public void shouldFindAllContainersIfNotRestricted() {
    ContainerSearch search = new ContainerSearch(null, null, null, null, null);

    List<Container> results = search.search(testList);

    Assert.assertEquals(results.size(), 7);
    assertContainsContainer(results, "container1");
    assertContainsContainer(results, "container2");
    assertContainsContainer(results, "container3");
    assertContainsContainer(results, "container4");
    assertContainsContainer(results, "container5");
    assertContainsContainer(results, "container6");
    assertContainsContainer(results, "container7");
  }

  @Test
  public void shouldRestrictByDeploymentName() {
    ContainerSearch search = new ContainerSearch("deploymentWithName", null, null, null, null);

    List<Container> results = search.search(testList);

    Assert.assertEquals(results.size(), 3);
    assertContainsContainer(results, "container3");
    assertContainsContainer(results, "container4");
    assertContainsContainer(results, "container7");
  }

  @Test
  public void shouldRestrictByDeploymentGenerateName() {
    ContainerSearch search =
        new ContainerSearch("deploymentWithGenerateName", null, null, null, null);

    List<Container> results = search.search(testList);

    Assert.assertEquals(results.size(), 2);
    assertContainsContainer(results, "container5");
    assertContainsContainer(results, "container6");
  }

  @Test
  public void shouldRestrictByPodName() {
    ContainerSearch search = new ContainerSearch(null, "podWithName", null, null, null);

    List<Container> results = search.search(testList);

    Assert.assertEquals(results.size(), 4);
    assertContainsContainer(results, "container1");
    assertContainsContainer(results, "container3");
    assertContainsContainer(results, "container5");
    assertContainsContainer(results, "container7");
  }

  @Test
  public void shouldRestrictByPodGenerateName() {
    ContainerSearch search = new ContainerSearch(null, "podWithGenerateName", null, null, null);

    List<Container> results = search.search(testList);

    Assert.assertEquals(results.size(), 3);
    assertContainsContainer(results, "container2");
    assertContainsContainer(results, "container4");
    assertContainsContainer(results, "container6");
  }

  @Test
  public void shouldRestrictByContainerName() {
    ContainerSearch search = new ContainerSearch(null, null, "container7", null, null);

    List<Container> results = search.search(testList);

    Assert.assertEquals(results.size(), 1);
    assertContainsContainer(results, "container7");
  }

  @Test
  public void shouldRestrictByCombinationOfDeploymentAndPodName() {
    ContainerSearch search =
        new ContainerSearch("deploymentWithName", "podWithGenerateName", null, null, null);

    List<Container> results = search.search(testList);

    Assert.assertEquals(results.size(), 1);
    assertContainsContainer(results, "container4");
  }

  @Test
  public void shouldRestrictByDeploymentLabels() {
    Map<String, String> selector = ImmutableMap.of("app", "che");
    ContainerSearch search = new ContainerSearch(null, null, null, null, selector);

    List<Container> results = search.search(testList);

    Assert.assertEquals(results.size(), 1);
    assertContainsContainer(results, "container3");
  }

  @Test
  public void shouldRestrictByPodLabels() {
    Map<String, String> selector = ImmutableMap.of("app", "che");
    ContainerSearch search = new ContainerSearch(null, null, null, selector, null);

    List<Container> results = search.search(testList);

    Assert.assertEquals(results.size(), 1);
    assertContainsContainer(results, "container1");
  }

  private static void assertContainsContainer(Collection<Container> containers, String name) {
    containers
        .stream()
        .filter(c -> name.equals(c.getName()))
        .findAny()
        .orElseThrow(
            () ->
                new AssertionError(
                    format("Expected a container called %s but didn't find any.", name)));
  }
}
