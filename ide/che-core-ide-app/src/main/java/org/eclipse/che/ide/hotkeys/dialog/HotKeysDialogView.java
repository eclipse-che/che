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
package org.eclipse.che.ide.hotkeys.dialog;

import com.google.inject.ImplementedBy;
import java.util.List;
import java.util.Map;
import org.eclipse.che.ide.api.editor.hotkeys.HotKeyItem;
import org.eclipse.che.ide.api.keybinding.Scheme;
import org.eclipse.che.ide.api.mvp.View;

/**
 * This representation of widget that provides an ability to show hotKeys list for IDE and editor.
 *
 * @author Alexander Andrienko
 * @author Artem Zatsarynnyi
 * @author @author <a href="mailto:ak@nuxeo.com">Arnaud Kervern</a>
 */
@ImplementedBy(HotKeysDialogViewImpl.class)
public interface HotKeysDialogView extends View<HotKeysDialogView.ActionDelegate> {

  /** Reset filter input and Show dialog. */
  void show();

  /** Clear and Render keybinding combination. */
  void renderKeybindings();

  /** Hide dialog. */
  void hide();

  /**
   * Set keybindings map for displaying.
   *
   * @param data map which binds categories keybindings and their keybindings
   */
  void setData(Map<String, List<HotKeyItem>> data);

  void setSchemes(String select, List<Scheme> schemes);

  /** Value of selected scheme in the ListBox field */
  String getSelectedScheme();

  interface ActionDelegate {

    /** Show list hotKeys. */
    void showHotKeys();

    /** Perform some action in response to user's clicking 'Save' button. */
    void onSaveClicked();

    /** Perform some action in response to user's clicking 'Close' button. */
    void onCloseClicked();

    /** Will be called when 'Print' button clicked. */
    void onPrintClicked();

    /**
     * Filter keybindings by filtered text in the description and keybindings.
     *
     * @param filteredText text for filter keybindings
     */
    void onFilterValueChanged(String filteredText);

    /** Perform some action in response to scheme selection change */
    void onSchemeSelectionChanged();
  }
}
