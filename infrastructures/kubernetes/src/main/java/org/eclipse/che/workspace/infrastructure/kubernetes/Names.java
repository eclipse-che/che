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

import static java.lang.String.format;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_ORIGINAL_NAME_LABEL;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.MACHINE_NAME_ANNOTATION_FMT;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.Pod;
import java.util.Map;
import org.eclipse.che.commons.lang.NameGenerator;

/**
 * Helps to work with Kubernetes objects names.
 *
 * @author Sergii Leshchenko
 */
public class Names {

  static final char WORKSPACE_ID_PREFIX_SEPARATOR = '.';

  static final int GENERATED_PART_SIZE = 8;

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
  public static String machineName(Pod pod, Container container) {
    final Map<String, String> annotations = pod.getMetadata().getAnnotations();
    final String machineName;
    final String containerName = container.getName();
    if (annotations != null
        && (machineName = annotations.get(format(MACHINE_NAME_ANNOTATION_FMT, containerName)))
            != null) {
      return machineName;
    }

    final String originalPodName;
    final Map<String, String> labels = pod.getMetadata().getLabels();
    if (labels != null && (originalPodName = labels.get(CHE_ORIGINAL_NAME_LABEL)) != null) {
      return originalPodName + '/' + containerName;
    }
    return pod.getMetadata().getName() + '/' + containerName;
  }

  /** Return pod name that will be unique for a whole namespace. */
  public static String uniquePodName(String originalPodName, String workspaceId) {
    return workspaceId + WORKSPACE_ID_PREFIX_SEPARATOR + originalPodName;
  }

  /** Returns randomly generated name with given prefix. */
  public static String generateName(String prefix) {
    return NameGenerator.generate(prefix, GENERATED_PART_SIZE);
  }
}
