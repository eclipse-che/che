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
package org.eclipse.che.workspace.infrastructure.kubernetes;

import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_ORIGINAL_NAME_LABEL;

import com.google.common.collect.Maps;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;

/**
 * Helps to work with Kubernetes objects names.
 *
 * @author Sergii Leshchenko
 */
public class Names {

  private static final char WORKSPACE_ID_PREFIX_SEPARATOR = '.';

  private static final int GENERATED_PART_SIZE = 8;

  private static final String CONTAINER_META_PREFIX = "che.container.";
  private static final String CONTAINER_META_NAME_SUFFIX = ".name";
  private static final String CONTAINER_META_MACHINE_SUFFIX = ".machine";

  /**
   * Returns machine name for the specified container in the specified pod.
   *
   * <p>This is a convenience method for {@link #machineName(ObjectMeta, Container)}
   */
  public static String machineName(Pod pod, Container container) {
    return machineName(pod.getMetadata(), container);
  }

  /**
   * Returns machine name for the specified container in the specified pod.
   *
   * <p>This is a convenience method for {@link #machineName(ObjectMeta, Container)}
   */
  public static String machineName(PodData podData, Container container) {
    return machineName(podData.getMetadata(), container);
  }

  /**
   * Records the machine name used for given container in the annotations of the object metadata.
   *
   * @param objectMeta the object metadata
   * @param containerName the name of the container
   * @param machineName the name of the machine of the container
   */
  public static void putMachineName(
      ObjectMeta objectMeta, String containerName, String machineName) {

    Map<String, String> annotations = objectMeta.getAnnotations();
    if (annotations == null) {
      objectMeta.setAnnotations(annotations = new HashMap<>());
    }

    putMachineName(annotations, containerName, machineName);
  }

  /**
   * Transforms the provided mapping of container names to machine names into a map of annotations
   * to be put in some Kubernetes object's metadata.
   *
   * @param containerToMachineNames the mapping of container names to machine names
   * @return a map of annotations
   */
  public static Map<String, String> createMachineNameAnnotations(
      Map<String, String> containerToMachineNames) {

    Map<String, String> ret = new HashMap<>();

    containerToMachineNames.forEach((c, m) -> putMachineName(ret, c, m));

    return ret;
  }

  /**
   * Similar to {@link #createMachineNameAnnotations(Map)} but only creates annotations for a single
   * mapping.
   *
   * @param containerName the name of the container
   * @param machineName the name of the machine
   * @return a map of annotations for the container-machine mapping
   * @see #createMachineNameAnnotations(Map)
   */
  public static Map<String, String> createMachineNameAnnotations(
      String containerName, String machineName) {
    Map<String, String> ret = Maps.newHashMapWithExpectedSize(2);
    putMachineName(ret, containerName, machineName);
    return ret;
  }

  private static void putMachineName(
      Map<String, String> annotations, String containerName, String machineName) {

    int containerIdx = findContainerIndex(annotations, containerName);
    if (containerIdx >= 0) {
      String machineNameKey = CONTAINER_META_PREFIX + containerIdx + CONTAINER_META_MACHINE_SUFFIX;
      annotations.put(machineNameKey, machineName);
    } else {
      int currentMaxIdx = maxContainerMetaIndex(annotations);
      int newIdx = currentMaxIdx + 1;
      String containerKey = CONTAINER_META_PREFIX + newIdx + CONTAINER_META_NAME_SUFFIX;
      String machineNameKey = CONTAINER_META_PREFIX + newIdx + CONTAINER_META_MACHINE_SUFFIX;

      annotations.put(containerKey, containerName);
      annotations.put(machineNameKey, machineName);
    }
  }

  /**
   * Returns machine name for the specified container in the specified pod.
   *
   * <p>Machine name is evaluated by the following algorithm:<br>
   *
   * <pre>
   * If an annotation with machine name for the corresponding container exists
   *   then return this value
   * If a label with original pod name exists
   *   then return originalPodName + '/' + containerName
   * otherwise return podName + '/' + containerName as machine name
   * </pre>
   */
  public static String machineName(ObjectMeta podMeta, Container container) {
    final Map<String, String> annotations = podMeta.getAnnotations();
    final String machineName;
    final String containerName = container.getName();
    if ((machineName = findMachineName(annotations, containerName)) != null) {
      return machineName;
    }

    final String originalPodName;
    final Map<String, String> labels = podMeta.getLabels();
    if (labels != null && (originalPodName = labels.get(CHE_ORIGINAL_NAME_LABEL)) != null) {
      return originalPodName + '/' + containerName;
    }
    return podMeta.getName() + '/' + containerName;
  }

  /** Return pod name that will be unique for a whole namespace. */
  public static String uniqueResourceName(String originalResourceName, String workspaceId) {
    return workspaceId + WORKSPACE_ID_PREFIX_SEPARATOR + originalResourceName;
  }

  /** Returns randomly generated name with given prefix. */
  public static String generateName(String prefix) {
    return NameGenerator.generate(prefix, GENERATED_PART_SIZE);
  }

  private static String findMachineName(Map<String, String> annotations, String containerName) {
    if (annotations == null) {
      return null;
    }

    for (Map.Entry<String, String> e : annotations.entrySet()) {
      String key = e.getKey();

      if (!key.startsWith(CONTAINER_META_PREFIX)) {
        continue;
      }

      if (!key.endsWith(CONTAINER_META_NAME_SUFFIX)) {
        continue;
      }

      if (!containerName.equals(e.getValue())) {
        continue;
      }

      String index =
          key.substring(
              CONTAINER_META_PREFIX.length(), key.length() - CONTAINER_META_NAME_SUFFIX.length());

      String machineNameKey = CONTAINER_META_PREFIX + index + CONTAINER_META_MACHINE_SUFFIX;

      return annotations.get(machineNameKey);
    }

    return null;
  }

  private static int maxContainerMetaIndex(Map<String, String> annotations) {
    int max = 0;

    if (annotations == null) {
      return max;
    }

    for (String key : annotations.keySet()) {
      if (!(key.startsWith(CONTAINER_META_PREFIX) && key.endsWith(CONTAINER_META_NAME_SUFFIX))) {
        continue;
      }

      String idxStr =
          key.substring(
              CONTAINER_META_PREFIX.length(), key.length() - CONTAINER_META_NAME_SUFFIX.length());

      int idx = Integer.parseInt(idxStr);

      if (max < idx) {
        max = idx;
      }
    }

    return max;
  }

  private static int findContainerIndex(Map<String, String> annotations, String containerName) {
    if (annotations == null) {
      return -1;
    }

    for (Map.Entry<String, String> e : annotations.entrySet()) {
      String key = e.getKey();
      if (key.startsWith(CONTAINER_META_PREFIX)
          && key.endsWith(CONTAINER_META_NAME_SUFFIX)
          && containerName.equals(e.getValue())) {
        String idxStr =
            key.substring(
                CONTAINER_META_PREFIX.length(), key.length() - CONTAINER_META_NAME_SUFFIX.length());

        return Integer.parseInt(idxStr);
      }
    }

    return -1;
  }
}
