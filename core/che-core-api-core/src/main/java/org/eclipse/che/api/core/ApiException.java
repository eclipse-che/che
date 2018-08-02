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
package org.eclipse.che.api.core;

import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.dto.server.DtoFactory;

/**
 * Base class for all API errors.
 *
 * @author andrew00x
 */
@SuppressWarnings("serial")
public class ApiException extends Exception {
  private final ServiceError serviceError;

  public ApiException(ServiceError serviceError) {
    super(serviceError.getMessage());
    this.serviceError = serviceError;
  }

  public ApiException(String message) {
    super(message);

    this.serviceError = createError(message);
  }

  public ApiException(String message, Throwable cause) {
    super(message, cause);
    this.serviceError = createError(message);
  }

  public ApiException(Throwable cause) {
    super(cause);
    this.serviceError = createError(cause.getMessage());
  }

  public ServiceError getServiceError() {
    return serviceError;
  }

  private ServiceError createError(String message) {
    return DtoFactory.getInstance().createDto(ServiceError.class).withMessage(message);
  }
}
