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
package org.eclipse.che.infrastructure.docker.client;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;

/**
 * Intended to be used in case of problem parsing and interpreting the Docker configuration file.
 *
 * @author Stéphane Daviet
 */
public class DockerFileException extends ApiException {
  public DockerFileException(ServiceError serviceError) {
    super(serviceError);
  }

  public DockerFileException(String message) {
    super(message);
  }

  public DockerFileException(String message, Throwable cause) {
    super(message, cause);
  }

  public DockerFileException(Throwable cause) {
    super(cause);
  }
}
