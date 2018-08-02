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
package org.eclipse.che.ide.preferences;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.preferences.PreferencePagePresenter;

/**
 * Interface of Preferences view.
 *
 * @author <a href="mailto:aplotnikov@exoplatform.com">Andrey Plotnikov</a>
 */
public interface PreferencesView extends View<PreferencesView.ActionDelegate> {
  /** Needs for delegate some function into preferences view. */
  interface ActionDelegate {
    /**
     * Performs actions when user click Save button. Actually when button is pressed, preferences
     * must be stored on the server.
     */
    void onSaveClicked();

    /** Loads preferences from the server discarding any changes. */
    void onRefreshClicked();

    /** Performs any actions appropriate in response to the user having pressed the Close button */
    void onCloseClicked();

    /**
     * Performs any actions appropriate in response to select some preference.
     *
     * @param preference selected preference
     */
    void onPreferenceSelected(PreferencePagePresenter preference);

    /** Performs any actions on the preferences window closing. */
    void onCloseWindow();
  }

  /**
   * Select the pointed preference.
   *
   * @param preference preference to select.
   */
  void selectPreference(PreferencePagePresenter preference);

  /** Close view. */
  void close();

  /** Show preferences. */
  void showDialog();

  /**
   * Returns content panel.
   *
   * @return
   */
  AcceptsOneWidget getContentPanel();

  /**
   * Enables or disables Save button.
   *
   * @param enabled <code>true</code> to enable the button, <code>false</code> to disable it
   */
  void enableSaveButton(boolean enabled);

  /**
   * Sets available preferences.
   *
   * @param preferences
   */
  void setPreferences(Map<String, Set<PreferencePagePresenter>> preferences);
}
