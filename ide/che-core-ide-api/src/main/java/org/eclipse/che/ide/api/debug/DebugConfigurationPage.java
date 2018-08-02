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
package org.eclipse.che.ide.api.debug;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.mvp.Presenter;

/**
 * Page allows to edit debug configuration.
 *
 * @param <T> type of the debug configuration which this page should edit
 * @author Artem Zatsarynnyi
 */
public interface DebugConfigurationPage<T extends DebugConfiguration> extends Presenter {

  /**
   * Resets the page from the given {@code configuration} which this page should edit.
   *
   * <p>This method is called every time when user selects an appropriate debug configuration in
   * 'Debug Configurations' dialog and before actual displaying this page.
   */
  void resetFrom(@NotNull T configuration);

  /**
   * This method is called every time when user selects an appropriate debug configuration in 'Debug
   * Configurations' dialog.
   *
   * <p>{@inheritDoc}
   */
  @Override
  void go(final AcceptsOneWidget container);

  /**
   * Returns whether this page is changed or not.
   *
   * @return {@code true} if page is changed, and {@code false} - otherwise
   */
  boolean isDirty();

  /**
   * Sets {@link DirtyStateListener} that should be called every time when any modifications on the
   * page has been performed.
   */
  void setDirtyStateListener(DirtyStateListener listener);

  /** Listener that should be called when any modifications on page. */
  interface DirtyStateListener {
    void onDirtyStateChanged();
  }
}
