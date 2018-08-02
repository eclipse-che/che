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
package org.eclipse.che.ide.api.macro;

import java.util.List;
import java.util.Set;

/**
 * Registry for {@link Macro}s.
 *
 * @author Artem Zatsarynnyi
 * @see Macro
 */
public interface MacroRegistry {

  /** Register set of macros. */
  void register(Set<Macro> macros);

  /** Unregister the given macro. */
  void unregister(Macro macro);

  /** Returns the names of all registered {@link Macro}s. */
  Set<String> getNames();

  /** Returns {@link Macro} by it's name. */
  Macro getMacro(String name);

  /** Returns all registered {@link Macro}s. */
  List<Macro> getMacros();
}
