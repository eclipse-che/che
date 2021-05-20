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
package org.eclipse.che.workspace.infrastructure.openshift;

/**
 * Constants for OpenShift implementation of spi.
 *
 * @author Sergii Leshchenko
 */
public final class Constants {
  private Constants() {}

  /**
   * Attribute name of {@link
   * org.eclipse.che.workspace.infrastructure.kubernetes.api.shared.KubernetesNamespaceMeta} that
   * stores project display name.
   */
  public static final String PROJECT_DISPLAY_NAME_ATTRIBUTE = "displayName";

  /**
   * Attribute name of {@link
   * org.eclipse.che.workspace.infrastructure.kubernetes.api.shared.KubernetesNamespaceMeta} that
   * stores project description.
   */
  public static final String PROJECT_DESCRIPTION_ATTRIBUTE = "description";

  /**
   * Annotation name of {@link io.fabric8.openshift.api.model.Project} that stores project display
   * name.
   */
  public static final String PROJECT_DISPLAY_NAME_ANNOTATION = "openshift.io/display-name";

  /**
   * Annotation name of {@link io.fabric8.openshift.api.model.Project} that stores project
   * description.
   */
  public static final String PROJECT_DESCRIPTION_ANNOTATION = "openshift.io/description";
}
