/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.editor.hotkeys;

/**
 * Representation hotKey which performs some action
 *
 * @author Alexander Andrienko
 * @author <a href="mailto:ak@nuxeo.com">Arnaud Kervern</a>
 */
public class HotKeyItem {
  private String actionDescription;
  private String hotKey;
  private boolean isGlobal;

  public HotKeyItem(String actionDescription, String hotKey) {
    this(actionDescription, hotKey, false);
  }

  public HotKeyItem(String actionDescription, String hotKey, boolean isGlobal) {
    this.actionDescription = actionDescription;
    this.hotKey = hotKey;
    this.isGlobal = isGlobal;
  }

  /**
   * Get action description
   *
   * @return action description
   */
  public String getActionDescription() {
    return actionDescription;
  }

  /**
   * Get hotKey
   *
   * @return readable hotKey line
   */
  public String getHotKey() {
    return hotKey;
  }

  /** Return a boolean to know if the hot key is handled globally or by the selected schema */
  public boolean isGlobal() {
    return isGlobal;
  }
}
