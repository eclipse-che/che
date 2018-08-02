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
package org.eclipse.che.api.git.exception;

import java.util.Map;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;

public class GitCommitInProgressException extends GitException {

  public GitCommitInProgressException(String message) {
    super(message);
  }

  public GitCommitInProgressException(String message, int errorCode) {
    super(message, errorCode);
  }

  public GitCommitInProgressException(
      String message, int errorCode, Map<String, String> attributes) {
    super(message, errorCode, attributes);
  }

  public GitCommitInProgressException(ServiceError serviceError) {
    super(serviceError);
  }

  public GitCommitInProgressException(String message, Throwable cause) {
    super(message, cause);
  }

  public GitCommitInProgressException(Throwable cause) {
    super(cause);
  }
}
