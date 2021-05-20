/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.workspace.server.model.impl.VolumeImpl;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;

/**
 * Helps prepare objects for PVC strategy tests.
 *
 * @author Sergii Leshchenko
 */
public class TestObjects {

  public static InternalMachineConfigBuilder newMachineConfig() {
    return new InternalMachineConfigBuilder();
  }

  public static TestPodBuilder newPod(String podName) {
    return new TestPodBuilder(podName);
  }

  public static TestContainerBuilder newContainer(String containerName) {
    return new TestContainerBuilder(containerName);
  }

  public static class TestPodBuilder {

    private String name;
    private List<Container> initContainers = new ArrayList<>();
    private List<Container> containers = new ArrayList<>();
    private List<Volume> volumes = new ArrayList<>();

    public TestPodBuilder(String podName) {
      this.name = podName;
    }

    public TestPodBuilder withInitContainers(Container... containers) {
      this.initContainers.addAll(Arrays.asList(containers));
      return this;
    }

    public TestPodBuilder withContainers(Container... containers) {
      this.containers.addAll(Arrays.asList(containers));
      return this;
    }

    public TestPodBuilder withPVCVolume(String volumeName, String pvcName) {
      this.volumes.add(
          new VolumeBuilder()
              .withName(volumeName)
              .withNewPersistentVolumeClaim()
              .withClaimName(pvcName)
              .endPersistentVolumeClaim()
              .build());
      return this;
    }

    public Pod build() {
      return new PodBuilder()
          .withNewMetadata()
          .withName(name)
          .endMetadata()
          .withNewSpec()
          .withInitContainers(initContainers)
          .withContainers(containers)
          .withVolumes(volumes)
          .endSpec()
          .build();
    }
  }

  public static class TestContainerBuilder {

    private String name;
    private List<VolumeMount> volumeMounts = new ArrayList<>();

    public TestContainerBuilder(String containerName) {
      this.name = containerName;
    }

    public TestContainerBuilder withVolumeMount(
        String volumeName, String mountPath, String subpath) {
      this.volumeMounts.add(
          new VolumeMountBuilder()
              .withName(volumeName)
              .withMountPath(mountPath)
              .withSubPath(subpath)
              .build());
      return this;
    }

    public Container build() {
      return new ContainerBuilder().withName(name).withVolumeMounts(volumeMounts).build();
    }
  }

  public static class InternalMachineConfigBuilder {

    private Map<String, VolumeImpl> volumes = new HashMap<>();

    public InternalMachineConfigBuilder withVolume(String volumeName, String path) {
      volumes.put(volumeName, new VolumeImpl().withPath(path));
      return this;
    }

    public InternalMachineConfig build() {
      return new InternalMachineConfig(new HashMap<>(), new HashMap<>(), new HashMap<>(), volumes);
    }
  }
}
