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
package org.eclipse.che.api.git.exception;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

import java.util.Collections;
import java.util.Map;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.shared.dto.ExtendedError;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;

/** @author andrew00x */
public class GitException extends ServerException {
  public GitException(String message) {
    super(message);
  }

  public GitException(String message, int errorCode, Map<String, String> attributes) {
    super(
        newDto(ExtendedError.class)
            .withMessage(message)
            .withErrorCode(errorCode)
            .withAttributes(attributes));
  }

  public GitException(ServiceError serviceError) {
    super(serviceError);
  }

  public GitException(String message, int errorCode) {
    this(message, errorCode, Collections.emptyMap());
  }

  public GitException(Throwable cause) {
    super(cause);
  }

  public GitException(String message, Throwable cause) {
    super(message, cause);
  }
}
