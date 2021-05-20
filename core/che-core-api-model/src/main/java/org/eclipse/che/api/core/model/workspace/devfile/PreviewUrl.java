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
package org.eclipse.che.api.core.model.workspace.devfile;

/**
 * Preview url is optional parameter of {@link Command}. It is used to construct proper
 * service+ingress/route and to compose valid path to the application. Typical use-case for
 * applications that doesn't have UI on root path. Preview url also partially replaces endpoint,
 * that is not needed to expose the application.
 */
public interface PreviewUrl {

  /**
   * {@code port} specifies where application, that is executed by command, listens. It is used to
   * create service+ingress/route pair to make application accessible.
   *
   * @return applications's listen port
   */
  int getPort();

  /**
   * Specifies path and/or query parameters that should be opened after command execution.
   *
   * @return path and/or query params to open or {@code null} when not defined
   */
  String getPath();
}
