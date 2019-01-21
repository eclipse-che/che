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
package org.eclipse.che.workspace.infrastructure.openshift;

/**
 * Constants for Kubernetes infrastructure specific warnings.
 *
 * @author Sergii Leshchenko
 */
public final class Warnings {

  public static final int ROUTE_IGNORED_WARNING_CODE = 5100;
  public static final String ROUTES_IGNORED_WARNING_MESSAGE =
      "Routes specified in OpenShift recipe are ignored. "
          + "To expose ports please define servers in machine configuration.";

  public static final int SECRET_IGNORED_WARNING_CODE =
      org.eclipse.che.workspace.infrastructure.kubernetes.Warnings.SECRET_IGNORED_WARNING_CODE;
  public static final String SECRET_IGNORED_WARNING_MESSAGE =
      "Secrets specified in OpenShift recipe are ignored.";

  private Warnings() {}
}
