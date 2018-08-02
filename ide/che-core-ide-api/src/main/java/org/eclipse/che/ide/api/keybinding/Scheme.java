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
package org.eclipse.che.ide.api.keybinding;

import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.util.input.CharCodeWithModifiers;

/**
 * Scheme is set of the key bindings.
 *
 * @author Evgen Vidolob
 * @author Artem Zatsarynnyi
 * @author <a href="mailto:ak@nuxeo.com">Arnaud Kervern</a>
 */
public interface Scheme {

  /**
   * Get id of the scheme.
   *
   * @return the scheme id
   */
  String getSchemeId();

  /**
   * Get scheme description.
   *
   * @return the scheme description
   */
  String getDescription();

  /**
   * Add key binding for action.
   *
   * @param key the hot key which bind
   * @param actionId the action id which keys bind
   */
  void addKey(@NotNull CharCodeWithModifiers key, @NotNull String actionId);

  /**
   * Add multiple key bindings at once
   *
   * @param keys Map of action's id with corresponding hot key
   */
  void addKeys(@NotNull Map<String, CharCodeWithModifiers> keys);

  /**
   * Remove key binding for action.
   *
   * @param key the hot key to remove
   * @param actionId the action's id for which key need to remove
   */
  void removeKey(@NotNull CharCodeWithModifiers key, @NotNull String actionId);

  /**
   * @return all actions that have the specified key. If there are no such actions then the method
   *     returns an empty array
   */
  @NotNull
  List<String> getActionIds(int digest);

  /**
   * @return keyboard shortcut for the action with the specified <code>actionId</code> or an null if
   *     the action doesn't have any keyboard shortcut
   */
  @Nullable
  CharCodeWithModifiers getKeyBinding(@NotNull String actionId);

  /** @return a boolean to check if the action is handled by this scheme */
  boolean contains(@NotNull String actionId);
}
