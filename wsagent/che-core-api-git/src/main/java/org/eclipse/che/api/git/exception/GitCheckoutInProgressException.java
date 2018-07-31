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

import java.util.Map;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;

public class GitCheckoutInProgressException extends GitException {
  public GitCheckoutInProgressException(String message) {
    super(message);
  }

  public GitCheckoutInProgressException(
      String message, int errorCode, Map<String, String> attributes) {
    super(message, errorCode, attributes);
  }

  public GitCheckoutInProgressException(ServiceError serviceError) {
    super(serviceError);
  }

  public GitCheckoutInProgressException(String message, int errorCode) {
    super(message, errorCode);
  }

  public GitCheckoutInProgressException(Throwable cause) {
    super(cause);
  }

  public GitCheckoutInProgressException(String message, Throwable cause) {
    super(message, cause);
  }
}
