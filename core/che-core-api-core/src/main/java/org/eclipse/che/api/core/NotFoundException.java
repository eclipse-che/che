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

/**
 * A {@code NotFoundException} is thrown if requested resource was not found.
 *
 * <p>Typically in REST API such errors are converted in HTTP response with status 404.
 *
 * @author andrew00x
 */
@SuppressWarnings("serial")
public class NotFoundException extends ApiException {
  public NotFoundException(String message) {
    super(message);
  }

  public NotFoundException(ServiceError serviceError) {
    super(serviceError);
  }
}
