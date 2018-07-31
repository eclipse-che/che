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
package org.eclipse.che.ide.macro.chooser;

import static java.util.Comparator.comparing;
import static org.eclipse.che.ide.util.StringUtils.containsIgnoreCase;

import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.macro.Macro;
import org.eclipse.che.ide.api.macro.MacroRegistry;

/**
 * Provides a simple mechanism for the user to choose a {@link Macro}.
 *
 * @author Artem Zatsarynnyi
 * @see #show(MacroChosenCallback)
 */
public class MacroChooser implements MacroChooserView.ActionDelegate {

  private final MacroChooserView view;
  private final MacroRegistry macroRegistry;

  /**
   * Provides macros list for the view. All changes made in provider should be reflected in the view
   * automatically.
   */
  private final ListDataProvider<Macro> macrosProvider;

  private MacroChosenCallback callback;

  @Inject
  public MacroChooser(MacroChooserView view, MacroRegistry macroRegistry) {
    this.view = view;
    this.macroRegistry = macroRegistry;

    macrosProvider = new ListDataProvider<>();

    view.setDelegate(this);
    view.bindMacrosList(macrosProvider);
  }

  /**
   * Pops up a macro chooser dialog. {@link MacroChosenCallback} can be specified to be invoked when
   * the user chose a {@link Macro}.
   *
   * @param callback callback that will be called to report about chosen {@link Macro}
   */
  public void show(@Nullable MacroChosenCallback callback) {
    this.callback = callback;

    updateMacrosProvider(macroRegistry.getMacros());

    view.showDialog();
  }

  @Override
  public void onMacroChosen(Macro macro) {
    view.close();

    if (callback != null) {
      callback.onMacroChosen(macro);
    }
  }

  @Override
  public void onFilterChanged(String filterValue) {
    final List<Macro> macrosList = new ArrayList<>();

    if (filterValue.isEmpty()) {
      macrosList.addAll(macroRegistry.getMacros());
    } else {
      // filter works by macro's name and description
      for (Macro macro : macroRegistry.getMacros()) {
        if (containsIgnoreCase(macro.getName(), filterValue)
            || containsIgnoreCase(macro.getDescription(), filterValue)) {
          macrosList.add(macro);
        }
      }
    }

    updateMacrosProvider(macrosList);
  }

  /** Updates internal {@link #macrosProvider} with the given {@code macrosList}. */
  private void updateMacrosProvider(List<Macro> macrosList) {
    macrosProvider.getList().clear();
    macrosProvider.getList().addAll(macrosList);
    macrosProvider.getList().sort(comparing(Macro::getName));
  }

  /** Callback to notify when some macro has been chosen. */
  public interface MacroChosenCallback {

    /** Called when macro has been chosen. */
    void onMacroChosen(Macro macro);
  }
}
