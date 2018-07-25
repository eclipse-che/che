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
package org.eclipse.che.ide.api.editor.position;

import org.eclipse.che.ide.api.editor.text.TextPosition;

/** Conversion utility for text/pixel coordinates. */
public interface PositionConverter {

  /**
   * Converts a text position (line, character) to pixel coordinates.
   *
   * @param textPosition the text position
   * @return the pixel coordinates
   */
  PixelCoordinates textToPixel(TextPosition textPosition);

  /**
   * Converts an offset text position (index in the text) to pixel coordinates.
   *
   * @param textOffset the text offset
   * @return the pixel coordinates
   */
  PixelCoordinates offsetToPixel(int textOffset);

  /**
   * Converts a {@link PixelCoordinates} object to a line/char text position.
   *
   * @param coordinates the pixel coordinates
   * @return the text position
   */
  TextPosition pixelToText(PixelCoordinates coordinates);

  /**
   * Converts a {@link PixelCoordinates} object to an offset position in the text.
   *
   * @param coordinates the pixel coordinates
   * @return the offset
   */
  int pixelToOffset(PixelCoordinates coordinates);

  public static final class PixelCoordinates {

    /** The horizontal pixel coordinate. */
    private final int x;

    /** The vertical pixel coordinate. */
    private final int y;

    /**
     * Constructor for {@link PixelCoordinates}.
     *
     * @param x the horizontal pixel coordinate
     * @param y the vertical pixel coordinate
     */
    public PixelCoordinates(final int x, final int y) {
      this.x = x;
      this.y = y;
    }

    /**
     * Returns the horizontal pixel coordinate.
     *
     * @return the horizontal coordinate
     */
    public int getX() {
      return x;
    }

    /**
     * Returns the vertical pixel coordinate.
     *
     * @return the vertical coordinate
     */
    public int getY() {
      return y;
    }

    @Override
    public String toString() {
      return "PixelCoordinates [x=" + x + ", y=" + y + "]";
    }
  }
}
