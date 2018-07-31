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
