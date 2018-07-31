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
package org.eclipse.che.ide.macro;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.ide.api.macro.Macro;
import org.eclipse.che.ide.api.macro.MacroRegistry;
import org.eclipse.che.ide.util.loging.Log;

/**
 * Implementation for {@link MacroRegistry}.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class MacroRegistryImpl implements MacroRegistry {

  private final Map<String, Macro> macros;

  public MacroRegistryImpl() {
    this.macros = new HashMap<>();
  }

  @Inject(optional = true)
  public void register(Set<Macro> macros) {
    for (Macro macro : macros) {
      final String name = macro.getName();
      if (this.macros.containsKey(name)) {
        Log.warn(MacroRegistryImpl.class, "Command macro '" + name + "' is already registered.");
      } else {
        this.macros.put(name, macro);
      }
    }
  }

  @Override
  public void unregister(Macro macro) {
    macros.remove(macro.getName());
  }

  @Override
  public Macro getMacro(String name) {
    return macros.get(name);
  }

  @Override
  public List<Macro> getMacros() {
    return new ArrayList<>(macros.values());
  }

  @Override
  public Set<String> getNames() {
    return macros.keySet();
  }
}
