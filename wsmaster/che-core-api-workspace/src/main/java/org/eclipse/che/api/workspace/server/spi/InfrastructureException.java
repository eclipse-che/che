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
package org.eclipse.che.api.workspace.server.spi;

/**
 * An exception thrown by {@link RuntimeInfrastructure} and related components. Indicates that an
 * infrastructure operation can't be performed or an error occurred during operation execution.
 *
 * @author Yevhenii Voevodin
 */
public class InfrastructureException extends Exception {

  public InfrastructureException(String message) {
    super(message);
  }

  public InfrastructureException(Exception e) {
    super(e);
  }

  public InfrastructureException(String message, Throwable cause) {
    super(message, cause);
  }
}
