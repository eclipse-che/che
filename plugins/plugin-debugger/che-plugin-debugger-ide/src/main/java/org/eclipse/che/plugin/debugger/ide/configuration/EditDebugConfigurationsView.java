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
package org.eclipse.che.plugin.debugger.ide.configuration;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import java.util.List;
import java.util.Map;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationType;
import org.eclipse.che.ide.api.mvp.View;

/**
 * The view of {@link EditDebugConfigurationsPresenter}.
 *
 * @author Artem Zatsarynnyi
 */
public interface EditDebugConfigurationsView
    extends View<EditDebugConfigurationsView.ActionDelegate> {

  /** Show view. */
  void showDialog();

  /** Close view. */
  void close();

  /** Select next configuration. */
  void selectNextItem();

  /** Returns the component used for displaying debug configuration page. */
  AcceptsOneWidget getDebugConfigurationPageContainer();

  /** Clear debug configuration page panel. */
  void clearDebugConfigurationPageContainer();

  /**
   * Sets debug configuration types and debug configurations to display.
   *
   * @param categories available debug configuration types and list of configurations
   */
  void setData(Map<DebugConfigurationType, List<DebugConfiguration>> categories);

  /** Returns configuration name. */
  String getConfigurationName();

  /** Sets configuration name. */
  void setConfigurationName(String name);

  /** Sets enabled state of the 'Cancel' button. */
  void setCancelButtonState(boolean enabled);

  /** Sets enabled state of the 'Save' button. */
  void setSaveButtonState(boolean enabled);

  /** Sets enabled state of the 'Debug' button. */
  void setDebugButtonState(boolean enabled);

  /** Sets enabled state of the filter input field. */
  void setFilterState(boolean enabled);

  /** Returns the selected configuration type or type of the selected debug configuration. */
  @Nullable
  DebugConfigurationType getSelectedConfigurationType();

  /** Select the specified debug configuration. */
  void setSelectedConfiguration(DebugConfiguration config);

  /** Returns the selected debug configuration. */
  @Nullable
  DebugConfiguration getSelectedConfiguration();

  /** Focus the 'Close' button. */
  void focusCloseButton();

  /** Returns {@code true} if 'Cancel' button is focused and {@code false} - otherwise. */
  boolean isCancelButtonFocused();

  /** Returns {@code true} if 'Close' button is focused and {@code false} - otherwise. */
  boolean isCloseButtonFocused();

  /** Action handler for the view actions/controls. */
  interface ActionDelegate {

    /** Called when 'Ok' button is clicked. */
    void onCloseClicked();

    /** Called when 'Apply' button is clicked. */
    void onSaveClicked();

    /** Called when 'Cancel' button is clicked. */
    void onCancelClicked();

    /** Called when 'Debug' button is clicked. */
    void onDebugClicked();

    /** Called when 'Add' button is clicked. */
    void onAddClicked();

    /** Called when 'Duplicate' button is clicked. */
    void onDuplicateClicked();

    /** Called when 'Remove' button is clicked. */
    void onRemoveClicked(DebugConfiguration selectedConfiguration);

    /** Called when 'Enter' key is pressed. */
    void onEnterPressed();

    /**
     * Called when some debug configuration is selected.
     *
     * @param configuration selected configuration
     */
    void onConfigurationSelected(DebugConfiguration configuration);

    /** Called when configuration's name has been changed. */
    void onNameChanged();
  }
}
