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
package org.eclipse.che.ide.editor.orion.client.jso;

/**
 * This is the event sent when the text in the model has changed.
 *
 * @author Evgen Vidolob
 */
public class ModelChangedEventOverlay extends OrionEventOverlay {
  protected ModelChangedEventOverlay() {}

  /** @return the number of characters added to the model. */
  public final native int addedCharCount() /*-{
        return this.addedCharCount;
    }-*/;

  /** @return The number of lines added to the model. */
  public final native int addedLineCount() /*-{
        return this.addedLineCount;
    }-*/;

  /** @return The number of characters removed from the model. */
  public final native int removedCharCount() /*-{
        return this.removedCharCount;
    }-*/;

  /** @return The number of lines removed from the model. */
  public final native int removedLineCount() /*-{
        return this.removedLineCount;
    }-*/;

  /** @return The character offset in the model where the change has occurred. */
  public final native int start() /*-{
        return this.start;
    }-*/;

  public final native String getText() /*-{
        return this.text;
  }-*/;
}
