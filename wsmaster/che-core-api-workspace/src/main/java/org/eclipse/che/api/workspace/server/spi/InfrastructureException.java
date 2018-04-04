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
