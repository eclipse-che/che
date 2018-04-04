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
package org.eclipse.che.api.core.rest.shared.dto;

import org.eclipse.che.dto.shared.DTO;

/**
 * Describes error which may be serialized to JSON format with {@link
 * org.eclipse.che.api.core.rest.ApiExceptionMapper}
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @see org.eclipse.che.api.core.ApiException
 * @see org.eclipse.che.api.core.rest.ApiExceptionMapper
 */
@DTO
public interface ServiceError {
  /**
   * Get error message.
   *
   * @return error message
   */
  String getMessage();

  ServiceError withMessage(String message);

  /**
   * Set error message.
   *
   * @param message error message
   */
  void setMessage(String message);
}
