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

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.Pod;
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

  /** Returns machine name that has the following format `{POD_NAME}/{CONTAINER_NAME}`. */
  public static String machineName(String podName, String containerName) {
    return podName + '/' + containerName;
  }

  /** Returns machine name that has the following format `{POD_NAME}/{CONTAINER_NAME}`. */
  public static String machineName(Pod pod, Container container) {
    return machineName(pod.getMetadata().getName(), container.getName());
  }

  /**
   * Returns pod name that is extracted from machine name.
   *
   * @see #machineName(String, String)
   */
  public static String podName(String machineName) {
    return machineName.split("/")[0];
  }

  /** Return pod name that will be unique for a whole namespace. */
  public static String uniquePodName(String originalPodName, String workspaceId) {
    return workspaceId + WORKSPACE_ID_PREFIX_SEPARATOR + originalPodName;
  }

  /** Return original pod name that is extracted from unique value. */
  public static String originalPodName(String podName, String workspaceId) {
    return podName.replace(workspaceId + WORKSPACE_ID_PREFIX_SEPARATOR, "");
  }

  /** Returns route name that will be unique whole a namespace. */
  public static String uniqueRouteName() {
    return NameGenerator.generate(ROUTE_PREFIX, ROUTE_PREFIX_SIZE);
  }
}
