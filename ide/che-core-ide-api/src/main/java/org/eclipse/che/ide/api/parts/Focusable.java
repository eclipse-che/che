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
package org.eclipse.che.ide.api.parts;

/**
 * A view that implements this interface can receive keyboard focus.
 *
 * @author Vitaliy Guliy
 */
public interface Focusable {

  /** Explicitly focus/unfocus this view. Only one view can be focused at a time. */
  void setFocus(boolean focused);

  /**
   * Check is this view focused.
   *
   * @return <b>true</b> if view has focus, otherwise return <b>false</b>
   */
  boolean isFocused();
}
