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
