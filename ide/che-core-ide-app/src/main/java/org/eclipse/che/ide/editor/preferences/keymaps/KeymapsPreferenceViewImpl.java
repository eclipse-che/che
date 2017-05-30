/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.editor.preferences.keymaps;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.inject.Inject;

import org.eclipse.che.ide.api.editor.keymap.Keymap;
import org.eclipse.che.ide.editor.preferences.EditorPrefLocalizationConstant;
import org.eclipse.che.ide.ui.listbox.CustomListBox;

import java.util.List;

/**
 * Implementation of the {@link KeymapsPreferenceView}.
 */
public class KeymapsPreferenceViewImpl extends Composite implements KeymapsPreferenceView {

    /** UI binder interface for the {@link KeymapsPreferenceViewImpl} component. */
    interface KeymapsPreferenceViewImplUiBinder extends UiBinder<FlowPanel, KeymapsPreferenceViewImpl> {
    }

    /** The UI binder instance. */
    private static final KeymapsPreferenceViewImplUiBinder UIBINDER = GWT.create(KeymapsPreferenceViewImplUiBinder.class);

    private ActionDelegate     delegate;

    @UiField(provided = true)
    EditorPrefLocalizationConstant constants;

    @UiField
    CustomListBox keyBindingSelection;

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
