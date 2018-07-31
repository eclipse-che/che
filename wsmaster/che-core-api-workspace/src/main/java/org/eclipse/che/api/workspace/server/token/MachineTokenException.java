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
package org.eclipse.che.api.workspace.server.token;

import org.eclipse.che.api.workspace.server.spi.InfrastructureException;

/**
 * An exception should be thrown by {@link MachineTokenProvider} when an error occurred during token
 * fetching.
 *
 * @author Sergii Leshchenko
 */
public class MachineTokenException extends InfrastructureException {
  public MachineTokenException(String message) {
    super(message);
  }

  public MachineTokenException(Exception e) {
    super(e);
  }

  public MachineTokenException(String message, Throwable cause) {
    super(message, cause);
  }
}
