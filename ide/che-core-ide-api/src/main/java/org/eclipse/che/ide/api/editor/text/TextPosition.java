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
package org.eclipse.che.ide.api.editor.text;

/** A position in the text editor. */
public class TextPosition {
  /** The line in the text. */
  private final int line;

  /** The character position on the line. */
  private final int character;

  public TextPosition(int line, int character) {
    this.line = line;
    this.character = character;
  }

  /**
   * Return the line coordinate ofthe text position
   *
   * @return the line coordinate
   */
  public int getLine() {
    return line;
  }

  /**
   * Return the character coordinate ofthe text position
   *
   * @return the character coordinate
   */
  public int getCharacter() {
    return character;
  }

  @Override
  public String toString() {
    return "(" + line + ", " + character + ")";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + character;
    result = prime * result + line;
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof TextPosition)) {
      return false;
    }
    final TextPosition other = (TextPosition) obj;
    if (character != other.character) {
      return false;
    }
    if (line != other.line) {
      return false;
    }
    return true;
  }
}
