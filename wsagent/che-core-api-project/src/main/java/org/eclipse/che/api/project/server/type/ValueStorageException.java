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
package org.eclipse.che.api.project.server.type;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;

/**
 * Thrown when source of persisted value is invalid. For instance file not found or can not be read
 * when expected.
 *
 * @author gazarenkov
 */
public class ValueStorageException extends ConflictException {

  public ValueStorageException(String message) {
    super(message);
  }

  public ValueStorageException(ServiceError serviceError) {
    super(serviceError);
  }

  public ValueStorageException(String message, Throwable cause) {
    super(message, cause);
  }
}
