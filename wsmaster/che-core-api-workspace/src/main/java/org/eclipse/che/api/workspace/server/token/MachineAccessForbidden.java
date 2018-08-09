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
package org.eclipse.che.api.workspace.server.token;

/**
 * An exception should be thrown by {@link MachineTokenProvider} when an user doesn't have the
 * needed permissions.
 *
 * @author Sergii Leshchenko
 */
public class MachineAccessForbidden extends MachineTokenException {

  public MachineAccessForbidden(String message) {
    super(message);
  }

  public MachineAccessForbidden(Exception e) {
    super(e);
  }

  public MachineAccessForbidden(String message, Throwable cause) {
    super(message, cause);
  }
}
