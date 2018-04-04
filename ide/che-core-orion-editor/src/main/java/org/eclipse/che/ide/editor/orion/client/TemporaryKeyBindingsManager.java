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
package org.eclipse.che.ide.editor.orion.client;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.ide.api.editor.keymap.KeyBinding;
import org.eclipse.che.ide.api.editor.texteditor.HasKeyBindings;

/** Hold {@link KeyBinding} until the editor is ready to accept them. */
public class TemporaryKeyBindingsManager implements HasKeyBindings {

  private final List<KeyBinding> bindings = new ArrayList<>();

  @Override
  public void addKeyBinding(final KeyBinding keyBinding) {
    this.bindings.add(keyBinding);
  }

  @Override
  public void addKeyBinding(KeyBinding keyBinding, String actionDescription) {
    this.bindings.add(keyBinding);
  }

  public List<KeyBinding> getbindings() {
    return this.bindings;
  }
}
