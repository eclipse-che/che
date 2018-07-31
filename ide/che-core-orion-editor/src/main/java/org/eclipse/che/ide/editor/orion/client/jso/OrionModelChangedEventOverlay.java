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
package org.eclipse.che.ide.editor.orion.client.jso;

/** Overlay over orion 'model changed' events. */
public class OrionModelChangedEventOverlay extends OrionEventOverlay {

  /** JSO mandated protected constructor. */
  protected OrionModelChangedEventOverlay() {}

  /**
   * Returns the number of added characters.
   *
   * @return the number of added characters
   */
  public final native int getAddedCharCount() /*-{
        return this.addedCharCount;
    }-*/;

  /**
   * Returns the number of added lines.
   *
   * @return the number of added lines
   */
  public final native int getAddedLineCount() /*-{
        return this.addedLineCount;
    }-*/;

  /**
   * Returns the number of removed characters.
   *
   * @return the number of removed characters
   */
  public final native int getRemovedCharCount() /*-{
        return this.removedCharCount;
    }-*/;

  /**
   * Returns the number of removed lines.
   *
   * @return the number of removed lines
   */
  public final native int getRemovedLineCount() /*-{
        return this.removedLineCount;
    }-*/;

  public final native String getText() /*-{
        return this.text;
    }-*/;
  /**
   * Returns the start of the change in the text.
   *
   * @return the start
   */
  public final native int getStart() /*-{
        return this.start;
    }-*/;
}
