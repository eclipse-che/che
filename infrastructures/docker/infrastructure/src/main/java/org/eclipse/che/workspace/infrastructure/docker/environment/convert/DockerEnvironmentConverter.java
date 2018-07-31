/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker.environment.convert;

import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;

/**
 * Converts {@link InternalEnvironment} into {@link DockerEnvironment}.
 *
 * @author Sergii Leshchenko
 */
public interface DockerEnvironmentConverter {

  /**
   * Returns {@link DockerEnvironment} that is converted from the specified {@link
   * InternalEnvironment}.
   *
   * @param environment environment to converting
   */
  DockerEnvironment convert(InternalEnvironment environment) throws ValidationException;
}
