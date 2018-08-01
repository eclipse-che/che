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
package org.eclipse.che.ide.editor.orion.client;

import com.google.gwt.core.shared.GWT;
import org.eclipse.che.ide.api.editor.keymap.Keymap;

/**
 * Keymaps supported by Orion.
 *
 * @author "Mickaël Leduque"
 */
public class KeyMode {

  public static Keymap DEFAULT;
  public static Keymap EMACS;
  public static Keymap VI;

  public static final void init() {
    KeymodeDisplayConstants constants = GWT.create(KeymodeDisplayConstants.class);
    DEFAULT = Keymap.newKeymap("orion_default", constants.defaultKeymap());
    EMACS = Keymap.newKeymap("Orion_emacs", constants.emacs());
    VI = Keymap.newKeymap("Orion_vim", constants.vi());
  }
}
