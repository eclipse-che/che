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
package org.eclipse.che.ide.editor.orion.client.jso;

/** Overlay over orion ruler click events */
public class OrionRulerClickEventOverlay extends OrionEventOverlay {

  /** JSO mandated protected constructor. */
  protected OrionRulerClickEventOverlay() {}

  /**
   * Returns the line number where event was occurred.
   *
   * @return the line number
   */
  public final native int getLineIndex() /*-{
        return this.lineIndex;
    }-*/;
}
