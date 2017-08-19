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
package org.eclipse.che.api.workspace.server.spi;

/**
 * Thrown by {@link RuntimeContext} and related components. Indicates that an operation cannot be
 * performed due to context state violation(e.g. workspace cannot be stopped while it is starting).
 *
 * @author Yevhenii Voevodin
 */
public class StateException extends InfrastructureException {
  public StateException(String message) {
    super(message);
  }
}
