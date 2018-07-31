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
package org.eclipse.che.plugin.java.server;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;

/**
 * Base exception for all JDT exceptions
 *
 * @author Evgen Vidolob
 */
public class JdtException extends ServerException {
  public JdtException(String message) {
    super(message);
  }

  public JdtException(ServiceError serviceError) {
    super(serviceError);
  }

  public JdtException(Throwable cause) {
    super(cause);
  }

  public JdtException(String message, Throwable cause) {
    super(message, cause);
  }
}
