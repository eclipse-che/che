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
package org.eclipse.che.ide.api.macro;

import java.util.Set;
import org.eclipse.che.api.promises.client.Promise;

/**
 * Macro which can be used for the simple text substitutions. Mainly used in command lines before
 * sending command to the machine for execution.
 *
 * <p>Implementations of this interface have to be registered using a multibinder in order to be
 * picked-up on application's start-up. Also macro can be registered in 'runtime' with {@link
 * MacroRegistry#register(Set)}.
 *
 * @author Artem Zatsarynnyi
 * @see BaseMacro
 * @see MacroProcessor#expandMacros(String)
 * @see MacroRegistry
 */
public interface Macro {

  /** Returns macro name. The recommended syntax is ${macro.name}. */
  String getName();

  /** Returns macro description. */
  String getDescription();

  /**
   * Expand macro into the real value.
   *
   * @return a promise that resolves to the real value associated with macro
   */
  Promise<String> expand();
}
