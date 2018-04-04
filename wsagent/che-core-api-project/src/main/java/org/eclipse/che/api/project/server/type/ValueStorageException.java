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
}
