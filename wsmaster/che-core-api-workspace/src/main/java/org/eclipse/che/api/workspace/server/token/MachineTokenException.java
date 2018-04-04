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
