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
package org.eclipse.che.api.core.model.workspace;

/**
 * Describes a warning, a pair of a message and a code which may indicate some context specific
 * non-critical violation.
 *
 * @author Yevhenii Voevodin
 */
public interface Warning {

  /** Returns the code of the warning. */
  int getCode();

  /** Returns the message explaining the warning. */
  String getMessage();
}
