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
package org.eclipse.che.ide.ui.menubutton;

import java.util.List;
import java.util.Optional;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.util.Pair;

/** Provides {@link MenuItem}s for {@link MenuButton}. */
public interface ItemsProvider {

  /**
   * Returns an {@code Optional} with a default {@link MenuItem} which should be passed to {@link
   * ActionHandler#onAction(MenuItem)} when user makes a short click on a {@link MenuButton}.
   *
   * <p>Returns an empty {@code Optional} if no default {@link MenuItem}.
   */
  Optional<MenuItem> getDefaultItem();

  /** Returns top level items. */
  List<MenuItem> getItems();

  /** Checks whether the given {@code item} is a group. Group item cannot be selected. */
  boolean isGroup(MenuItem item);

  /** Returns the pair of the given {@code parent} children and their labels. */
  @Nullable
  Pair<List<MenuItem>, String> getChildren(MenuItem parent);

  /** Sets the {@link DataChangedHandler}. */
  void setDataChangedHandler(DataChangedHandler handler);

  interface DataChangedHandler {
    /** Should be called when provided by {@link ItemsProvider} data has been changed. */
    void onDataChanged();
  }
}
