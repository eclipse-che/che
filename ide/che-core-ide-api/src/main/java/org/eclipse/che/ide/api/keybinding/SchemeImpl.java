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
package org.eclipse.che.ide.api.keybinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.util.input.CharCodeWithModifiers;

/**
 * @author Evgen Vidolob
 * @author Artem Zatsarynnyi
 * @author <a href="mailto:ak@nuxeo.com">Arnaud Kervern</a>
 */
public class SchemeImpl implements Scheme {

  private String id;

  private String description;

  private Map<Integer, List<String>> handlers;

  private Map<String, CharCodeWithModifiers> actionId2CharCode;

  public SchemeImpl(String id, String description) {
    this.id = id;
    this.description = description;
    handlers = new HashMap<>();
    actionId2CharCode = new HashMap<>();
  }

  /** {@inheritDoc} */
  @Override
  public String getSchemeId() {
    return id;
  }

  /** {@inheritDoc} */
  @Override
  public String getDescription() {
    return description;
  }

  /** {@inheritDoc} */
  @Override
  public void addKey(@NotNull CharCodeWithModifiers key, @NotNull String actionId) {
    final int digest = key.getKeyDigest();
    if (!handlers.containsKey(digest)) {
      handlers.put(digest, new ArrayList<String>());
    }
    handlers.get(digest).add(actionId);
    actionId2CharCode.put(actionId, key);
  }

  @Override
  public void addKeys(Map<String, CharCodeWithModifiers> keys) {
    for (Map.Entry<String, CharCodeWithModifiers> entry : keys.entrySet()) {
      addKey(entry.getValue(), entry.getKey());
    }
  }

  /** {@inheritDoc} */
  @Override
  public void removeKey(@NotNull CharCodeWithModifiers key, @NotNull String actionId) {
    final int digest = key.getKeyDigest();

    List<String> array = handlers.get(digest);
    if (array != null) {
      array.remove(actionId);
      if (array.isEmpty()) {
        handlers.remove(digest);
      }
    }

    actionId2CharCode.remove(actionId);
  }

  /** {@inheritDoc} */
  @NotNull
  @Override
  public List<String> getActionIds(int digest) {
    if (handlers.containsKey(digest)) {
      return handlers.get(digest);
    }
    return new ArrayList<>();
  }

  /** {@inheritDoc} */
  @Nullable
  @Override
  public CharCodeWithModifiers getKeyBinding(@NotNull String actionId) {
    return actionId2CharCode.get(actionId);
  }

  @Override
  public boolean contains(String actionId) {
    return actionId2CharCode.containsKey(actionId);
  }
}
