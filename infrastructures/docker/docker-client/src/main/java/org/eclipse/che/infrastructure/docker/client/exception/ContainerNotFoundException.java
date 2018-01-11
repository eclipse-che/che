/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.infrastructure.docker.client.exception;

/**
 * Occurs when docker container is not found.
 *
 * @author Mykola Morhun
 */
public class ContainerNotFoundException extends DockerException {

  public ContainerNotFoundException(String message) {
    super(message, 404);
  }
}
