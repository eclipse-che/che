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

import static java.lang.String.format;
import static java.util.Arrays.asList;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodTemplate;
import io.fabric8.kubernetes.api.model.PodTemplateBuilder;
import io.fabric8.kubernetes.api.model.ReplicationController;
import io.fabric8.kubernetes.api.model.ReplicationControllerBuilder;
import io.fabric8.kubernetes.api.model.apps.DaemonSet;
import io.fabric8.kubernetes.api.model.apps.DaemonSetBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.ReplicaSet;
import io.fabric8.kubernetes.api.model.apps.ReplicaSetBuilder;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSetBuilder;
import io.fabric8.kubernetes.api.model.batch.CronJob;
import io.fabric8.kubernetes.api.model.batch.CronJobBuilder;
import io.fabric8.kubernetes.api.model.batch.Job;
import io.fabric8.kubernetes.api.model.batch.JobBuilder;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.DeploymentConfigBuilder;
import io.fabric8.openshift.api.model.Template;
import io.fabric8.openshift.api.model.TemplateBuilder;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ContainerSearchTest {

  private List<HasMetadata> testList;

  @BeforeMethod
  public void setup() {
    // These are all the object types that can be contained in a KubernetesList which can contain a
    // container:
    // Pod, PodTemplate, DaemonSet, Deployment, Job, ReplicaSet, ReplicationController, StatefulSet,
    // CronJob, DeploymentConfig, Template

    Container container1 = new ContainerBuilder().withName("container1").build();
    Container container2 = new ContainerBuilder().withName("container2").build();
    Container container3 = new ContainerBuilder().withName("container3").build();
    Container container4 = new ContainerBuilder().withName("container4").build();
    Container container5 = new ContainerBuilder().withName("container5").build();
    Container container6 = new ContainerBuilder().withName("container6").build();
    Container container7 = new ContainerBuilder().withName("container7").build();
    Container container8 = new ContainerBuilder().withName("container8").build();
    Container container9 = new ContainerBuilder().withName("container9").build();
    Container container10 = new ContainerBuilder().withName("container10").build();
    Container container11 = new ContainerBuilder().withName("container11").build();
    Container container12 = new ContainerBuilder().withName("container12").build();

    Pod podWithName =
        new PodBuilder()
            .withNewMetadata()
            .withName("podWithName")
            .addToLabels("app", "che")
            .endMetadata()
            .withNewSpec()
            .withContainers(container1)
            .endSpec()
            .build();

    Pod podWithGenerateName =
        new PodBuilder()
            .withNewMetadata()
            .withGenerateName("podWithGenerateName")
            .endMetadata()
            .withNewSpec()
            .withContainers(container2)
            .endSpec()
            .build();

    PodTemplate podTemplate =
        new PodTemplateBuilder()
            .withNewMetadata()
            .withName("podTemplate")
            .addToLabels("app", "che")
            .endMetadata()
            .withNewTemplate()
            .withNewSpec()
            .withContainers(container3)
            .endSpec()
            .endTemplate()
            .build();

    DaemonSet daemonSet =
        new DaemonSetBuilder()
            .withNewMetadata()
            .withName("daemonSet")
            .endMetadata()
            .withNewSpec()
            .withNewTemplate()
            .withNewSpec()
            .withContainers(container4)
            .endSpec()
            .endTemplate()
            .endSpec()
            .build();

    Deployment deployment =
        new DeploymentBuilder()
            .withNewMetadata()
            .withName("deployment")
            .addToLabels("app", "che")
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

    Job job =
        new JobBuilder()
            .withNewMetadata()
            .withName("job")
            .endMetadata()
            .withNewSpec()
            .withNewTemplate()
            .withNewSpec()
            .withContainers(container6)
            .endSpec()
            .endTemplate()
            .endSpec()
            .build();

    ReplicaSet replicaSet =
        new ReplicaSetBuilder()
            .withNewMetadata()
            .withName("replicaSet")
            .addToLabels("app", "che")
            .endMetadata()
            .withNewSpec()
            .withNewTemplate()
            .withNewSpec()
            .withContainers(container7)
            .endSpec()
            .endTemplate()
            .endSpec()
            .build();

    ReplicationController replicationController =
        new ReplicationControllerBuilder()
            .withNewMetadata()
            .withName("replicationController")
            .endMetadata()
            .withNewSpec()
            .withNewTemplate()
            .withNewSpec()
            .withContainers(container8)
            .endSpec()
            .endTemplate()
            .endSpec()
            .build();

    StatefulSet statefulSet =
        new StatefulSetBuilder()
            .withNewMetadata()
            .withName("statefulSet")
            .addToLabels("app", "che")
            .endMetadata()
            .withNewSpec()
            .withNewTemplate()
            .withNewSpec()
            .withContainers(container9)
            .endSpec()
            .endTemplate()
            .endSpec()
            .build();

    CronJob cronJob =
        new CronJobBuilder()
            .withNewMetadata()
            .withName("cronJob")
            .endMetadata()
            .withNewSpec()
            .withNewJobTemplate()
            .withNewSpec()
            .withNewTemplate()
            .withNewSpec()
            .withContainers(container10)
            .endSpec()
            .endTemplate()
            .endSpec()
            .endJobTemplate()
            .endSpec()
            .build();

    DeploymentConfig deploymentConfig =
        new DeploymentConfigBuilder()
            .withNewMetadata()
            .withName("deploymentConfig")
            .addToLabels("app", "che")
            .endMetadata()
            .withNewSpec()
            .withNewTemplate()
            .withNewSpec()
            .withContainers(container11)
            .endSpec()
            .endTemplate()
            .endSpec()
            .build();
    Template template =
        new TemplateBuilder()
            .addToObjects(
                new DeploymentBuilder()
                    .withNewMetadata()
                    .withName("deploymentWithName")
                    .endMetadata()
                    .withNewSpec()
                    .withNewTemplate()
                    .withNewMetadata()
                    .withName("podWithName")
                    .endMetadata()
                    .withNewSpec()
                    .withContainers(container12)
                    .endSpec()
                    .endTemplate()
                    .endSpec()
                    .build())
            .build();

    // Pod, PodTemplate, DaemonSet, Deployment, Job, ReplicaSet, ReplicationController, StatefulSet,
    // CronJob, DeploymentConfig, Template
    testList =
        asList(
            podWithName,
            podWithGenerateName,
            podTemplate,
            daemonSet,
            deployment,
            job,
            replicaSet,
            replicationController,
            statefulSet,
            cronJob,
            deploymentConfig,
            template);
  }

  @Test
  public void shouldFindAllContainersIfNotRestricted() {
    ContainerSearch search = new ContainerSearch(null, null, null);

    List<Container> results = search.search(testList);

    Assert.assertEquals(results.size(), 12);
    assertContainsContainer(results, "container1");
    assertContainsContainer(results, "container2");
    assertContainsContainer(results, "container3");
    assertContainsContainer(results, "container4");
    assertContainsContainer(results, "container5");
    assertContainsContainer(results, "container6");
    assertContainsContainer(results, "container7");
    assertContainsContainer(results, "container8");
    assertContainsContainer(results, "container9");
    assertContainsContainer(results, "container10");
    assertContainsContainer(results, "container11");
    assertContainsContainer(results, "container12");
  }

  @Test
  public void shouldRestrictByName() {
    ContainerSearch search = new ContainerSearch("podWithName", null, null);

    List<Container> results = search.search(testList);

    Assert.assertEquals(results.size(), 1);
    assertContainsContainer(results, "container1");
  }

  @Test
  public void shouldRestrictByGenerateName() {
    ContainerSearch search = new ContainerSearch("podWithGenerateName", null, null);

    List<Container> results = search.search(testList);

    Assert.assertEquals(results.size(), 1);
    assertContainsContainer(results, "container2");
  }

  @Test
  public void shouldRestrictByContainerName() {
    ContainerSearch search = new ContainerSearch(null, null, "container7");

    List<Container> results = search.search(testList);

    Assert.assertEquals(results.size(), 1);
    assertContainsContainer(results, "container7");
  }

  @Test
  public void shouldRestrictByParentSelector() {
    Map<String, String> selector = ImmutableMap.of("app", "che");
    ContainerSearch search = new ContainerSearch(null, selector, null);

    List<Container> results = search.search(testList);

    Assert.assertEquals(results.size(), 6);
    assertContainsContainer(results, "container1");
    assertContainsContainer(results, "container3");
    assertContainsContainer(results, "container5");
    assertContainsContainer(results, "container7");
    assertContainsContainer(results, "container9");
    assertContainsContainer(results, "container11");
  }

  @Test
  public void shouldConsiderEmptySelectorAsNotPresent() {
    ContainerSearch search = new ContainerSearch(null, Collections.emptyMap(), null);

    List<Container> results = search.search(testList);

    Assert.assertEquals(results.size(), 12);
    assertContainsContainer(results, "container1");
    assertContainsContainer(results, "container2");
    assertContainsContainer(results, "container3");
    assertContainsContainer(results, "container4");
    assertContainsContainer(results, "container5");
    assertContainsContainer(results, "container6");
    assertContainsContainer(results, "container7");
    assertContainsContainer(results, "container8");
    assertContainsContainer(results, "container9");
    assertContainsContainer(results, "container10");
    assertContainsContainer(results, "container11");
    assertContainsContainer(results, "container12");
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
