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
package org.eclipse.che.api.core;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

import java.util.Collections;
import java.util.Map;
import org.eclipse.che.api.core.rest.shared.dto.ExtendedError;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;

/**
 * A {@code UnauthorizedException} is thrown when caller isn't authorized to access some resource.
 *
 * <p>Typically in REST API such errors are converted in HTTP response with status 401.
 *
 * @author andrew00x
 */
@SuppressWarnings("serial")
public class UnauthorizedException extends ApiException {

  public UnauthorizedException(String message) {
    super(message);
  }

  public UnauthorizedException(ServiceError serviceError) {
    super(serviceError);
  }

  public UnauthorizedException(String message, int errorCode, Map<String, String> attributes) {
    super(
        newDto(ExtendedError.class)
            .withMessage(message)
            .withErrorCode(errorCode)
            .withAttributes(attributes));
  }

  public UnauthorizedException(String message, int errorCode) {
    this(message, errorCode, Collections.emptyMap());
  }
}
