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
package org.eclipse.che.ide.editor.preferences.keymaps;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.inject.Inject;
import java.util.List;
import org.eclipse.che.ide.api.editor.keymap.Keymap;
import org.eclipse.che.ide.editor.preferences.EditorPrefLocalizationConstant;
import org.eclipse.che.ide.ui.listbox.CustomListBox;

/** Implementation of the {@link KeymapsPreferenceView}. */
public class KeymapsPreferenceViewImpl extends Composite implements KeymapsPreferenceView {

  /** UI binder interface for the {@link KeymapsPreferenceViewImpl} component. */
  interface KeymapsPreferenceViewImplUiBinder
      extends UiBinder<FlowPanel, KeymapsPreferenceViewImpl> {}

  /** The UI binder instance. */
  private static final KeymapsPreferenceViewImplUiBinder UIBINDER =
      GWT.create(KeymapsPreferenceViewImplUiBinder.class);

  private ActionDelegate delegate;

  @UiField(provided = true)
  EditorPrefLocalizationConstant constants;

  @UiField CustomListBox keyBindingSelection;

  @Inject
  public KeymapsPreferenceViewImpl(final EditorPrefLocalizationConstant constants) {
    this.constants = constants;

    initWidget(UIBINDER.createAndBindUi(this));
  }

  @Override
  public void setDelegate(final ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public void setKeyBindings(List<Keymap> availableKeyBindings, Keymap currentKeyBinding) {
    keyBindingSelection.clear();

    for (Keymap keymap : availableKeyBindings) {
      keyBindingSelection.addItem(keymap.getDisplay(), keymap.getKey());
      if (currentKeyBinding != null && keymap.getKey().equals(currentKeyBinding.getKey())) {
        keyBindingSelection.setSelectedIndex(availableKeyBindings.indexOf(keymap));
      }
    }
  }

  @UiHandler("keyBindingSelection")
  void handleSelectionChanged(ChangeEvent event) {
    final String value = keyBindingSelection.getValue(keyBindingSelection.getSelectedIndex());
    delegate.onKeyBindingSelected(Keymap.fromKey(value));
  }
}
