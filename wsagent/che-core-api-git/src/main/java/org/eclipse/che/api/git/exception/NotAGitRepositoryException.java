/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
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

public class NotAGitRepositoryException extends GitException {

  public NotAGitRepositoryException(String message) {
    super(message);
  }

  public NotAGitRepositoryException(String message, Throwable cause) {
    super(message, cause);
  }

  public NotAGitRepositoryException(Throwable cause) {
    super(cause);
  }

  public NotAGitRepositoryException(String message, int errorCode) {
    super(message, errorCode);
  }

  public NotAGitRepositoryException(ServiceError serviceError) {
    super(serviceError);
  }

  public NotAGitRepositoryException(String message, int errorCode, Map<String, String> attributes) {
    super(message, errorCode, attributes);
  }
}
