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
package org.eclipse.che.ide.api.preferences;

import org.eclipse.che.ide.api.mvp.Presenter;

/**
 * Interface of preference page. Describes main methods for all preference pages.
 *
 * @author Andrey Plotnikov
 * @author Vlad Zhukovskyi
 */
public interface PreferencePagePresenter extends Presenter {
  /** Needs for delegate updateControls function into PagePresenter. */
  interface DirtyStateListener {
    /** Updates preference view components without content panel. */
    void onDirtyChanged();
  }

  /**
   * Sets new delegate
   *
   * @param delegate
   */
  void setUpdateDelegate(DirtyStateListener delegate);

  /**
   * Return preference category. This category will used for grouping elements.
   *
   * @return
   */
  String getCategory();

  /**
   * Return preference page's title. This title will be shown into list of preferences.
   *
   * @return
   */
  String getTitle();

  /**
   * Returns whether this page is changed or not. This information is typically used by the
   * preferences presenter to decide when the information is changed.
   *
   * @return <code>true</code> if this page is changed, and <code>false</code> otherwise
   */
  boolean isDirty();

  /** Stores changes to Preferences Manager. */
  void storeChanges();

  /** Reverts changes on values stored in Preferences Manager. */
  void revertChanges();
}
