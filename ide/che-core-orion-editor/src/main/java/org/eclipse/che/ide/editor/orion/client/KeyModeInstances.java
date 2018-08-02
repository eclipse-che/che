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

import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.ide.api.editor.keymap.Keymap;
import org.eclipse.che.ide.editor.orion.client.jso.OrionKeyModeOverlay;

/**
 * An instance repository for keymode instances.
 *
 * @author "Mickaël Leduque"
 */
public class KeyModeInstances {

  private Map<Keymap, OrionKeyModeOverlay> instances = new HashMap<>();

  public void add(final Keymap key, final OrionKeyModeOverlay instance) {
    this.instances.put(key, instance);
  }

  public OrionKeyModeOverlay getInstance(final Keymap keymap) {
    return instances.get(keymap);
  }
}
