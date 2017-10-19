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
