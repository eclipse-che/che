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
package org.eclipse.che.api.debug.shared.model;

/** @author Anatoliy Bazko */
public interface Variable {
  /** The variable name. */
  String getName();

  /** The variable value. */
  SimpleValue getValue();

  /** The variable type. E.g.: String, int etc. */
  String getType();

  /** The path to the variable. */
  VariablePath getVariablePath();

  /** Indicates if variable is primitive. */
  boolean isPrimitive();
}
