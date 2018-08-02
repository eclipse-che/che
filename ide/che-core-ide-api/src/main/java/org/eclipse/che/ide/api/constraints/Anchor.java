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
package org.eclipse.che.ide.api.constraints;

/**
 * Defines possible positions of an action relative to another action.
 *
 * @author <a href="mailto:evidolob@codenvy.com">Evgen Vidolob</a>
 * @version $Id:
 */
public enum Anchor {
  /**
   * Anchor type that specifies the action to be the first in the list at the moment of addition.
   */
  FIRST,
  /** Anchor type that specifies the action to be the last in the list at the moment of addition. */
  LAST,
  /** Anchor type that specifies the action to be placed before the relative action. */
  BEFORE,
  /** Anchor type that specifies the action to be placed after the relative action. */
  AFTER
}
