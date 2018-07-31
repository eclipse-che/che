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
package org.eclipse.che.ide.api.parts;

import org.eclipse.che.ide.api.constraints.Direction;

/** @author Evgen Vidolob */
public class EditorMultiPartStackState {

  private EditorPartStack editorPartStack;

  private Direction direction;
  private double size;
  private EditorMultiPartStackState splitFirst;
  private EditorMultiPartStackState splitSecond;

  public EditorMultiPartStackState(EditorPartStack editorPartStack) {
    this.editorPartStack = editorPartStack;
  }

  public EditorMultiPartStackState(
      Direction direction,
      double size,
      EditorMultiPartStackState splitFirst,
      EditorMultiPartStackState splitSecond) {
    this.direction = direction;
    this.size = size;
    this.splitFirst = splitFirst;
    this.splitSecond = splitSecond;
  }

  public EditorPartStack getEditorPartStack() {
    return editorPartStack;
  }

  public Direction getDirection() {
    return direction;
  }

  public double getSize() {
    return size;
  }

  public EditorMultiPartStackState getSplitFirst() {
    return splitFirst;
  }

  public EditorMultiPartStackState getSplitSecond() {
    return splitSecond;
  }
}
