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
package org.eclipse.che.workspace.infrastructure.kubernetes.api.shared;

import java.util.Map;

/**
 * Describes meta information about kubernetes namespace.
 *
 * @author Sergii Leshchenko
 */
public interface KubernetesNamespaceMeta {

  /**
   * Attribute that shows if k8s namespace is configured as default. Possible values: true/false.
   * Absent value should be considered as false.
   */
  String DEFAULT_ATTRIBUTE = "default";

  /**
   * Attributes that contains information about current namespace status. Example values: Active,
   * Terminating. Absent value indicates that namespace is not created yet.
   */
  String PHASE_ATTRIBUTE = "phase";

  /**
   * Returns the name of namespace.
   *
   * <p>Value may be not a name of existing namespace, but predicted name with placeholders inside,
   * like <workspaceid>.
   */
  String getName();

  /** Returns namespace attributes, which may contains additional info about it like description. */
  Map<String, String> getAttributes();
}
