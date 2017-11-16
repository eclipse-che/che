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
package org.eclipse.che.workspace.infrastructure.docker.environment;

import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.config.Recipe;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;

/**
 * Parser for creating {@link DockerEnvironment} with parameters defined in the {@link Environment}.
 *
 * @author Alexander Andrienko
 */
public interface DockerConfigSourceSpecificEnvironmentParser {
  /**
   * Parses compose file from {@link Environment} into {@link DockerEnvironment}.
   *
   * <p>{@link Recipe#getContent()} in {@code Environment} must not be null even. It is supposed
   * that class that uses this methods sets it if needed.
   *
   * @param environment environment to parsing
   * @throws ValidationException in case invalid argument in the {@link Environment}
   * @throws InfrastructureException when parsing fails due to some internal server error or
   *     inability to parse environment due to other reasons
   */
  DockerEnvironment parse(InternalEnvironment environment)
      throws ValidationException, InfrastructureException;
}
