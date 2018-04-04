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
