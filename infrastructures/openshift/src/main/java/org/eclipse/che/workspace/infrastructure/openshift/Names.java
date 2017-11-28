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
package org.eclipse.che.workspace.infrastructure.openshift;

import static java.lang.String.format;
import static org.eclipse.che.workspace.infrastructure.openshift.Constants.CHE_ORIGINAL_NAME_LABEL;
import static org.eclipse.che.workspace.infrastructure.openshift.Constants.MACHINE_NAME_ANNOTATION_FMT;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.Pod;
import java.util.Map;
import org.eclipse.che.commons.lang.NameGenerator;

/**
 * Helps to work with OpenShift objects names.
 *
 * @author Sergii Leshchenko
 */
public class Names {

  static final char WORKSPACE_ID_PREFIX_SEPARATOR = '.';

  static final String ROUTE_PREFIX = "route";
  static final int ROUTE_PREFIX_SIZE = 8;

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

  /** Returns route name that will be unique whole a namespace. */
  public static String uniqueRouteName() {
    return NameGenerator.generate(ROUTE_PREFIX, ROUTE_PREFIX_SIZE);
  }
}
