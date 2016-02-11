/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.jseditor.client.preference.keymaps;


import java.util.Map.Entry;

import javax.inject.Inject;

import org.eclipse.che.ide.jseditor.client.editortype.EditorType;
import org.eclipse.che.ide.jseditor.client.editortype.EditorTypeRegistry;
import org.eclipse.che.ide.jseditor.client.keymap.Keymap;
import org.eclipse.che.ide.jseditor.client.keymap.KeymapValuesHolder;
import org.eclipse.che.ide.jseditor.client.preference.EditorPrefLocalizationConstant;
import org.eclipse.che.ide.jseditor.client.preference.EditorPreferenceResource;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * Implementation of the {@link KeymapsPreferenceView}.
 */
public class KeymapsPreferenceViewImpl extends Composite implements KeymapsPreferenceView {

    /** UI binder interface for the {@link KeymapsPreferenceViewImpl} component. */
    interface KeymapsPreferenceViewImplUiBinder extends UiBinder<HTMLPanel, KeymapsPreferenceViewImpl> {
    }

    /** The UI binder instance. */
    private static final KeymapsPreferenceViewImplUiBinder UIBINDER = GWT.create(KeymapsPreferenceViewImplUiBinder.class);

    private final EditorTypeRegistry                 editorTypeRegistry;
    private final EditorPreferenceResource.CellStyle cellStyle;

    private KeymapSelectionColumn keymapSelectionColumn;

    private ActionDelegate     delegate;
    private KeymapValuesHolder valuesHolder;

    @UiField(provided = true)
    EditorPrefLocalizationConstant constants;

    @UiField(provided = true)
    CellTable<EditorType> keyBindingSelection;


    @Inject
    public KeymapsPreferenceViewImpl(final EditorTypeRegistry editorTypeRegistry,
                                     final EditorPreferenceResource resources,
                                     final EditorPrefLocalizationConstant constants) {
        this.keyBindingSelection = new CellTable<EditorType>(5, resources);

        this.constants = constants;

        initWidget(UIBINDER.createAndBindUi(this));

        this.editorTypeRegistry = editorTypeRegistry;
        this.cellStyle = resources.cellStyle();

        // build keybinding selection table
        final TextColumn<EditorType> editorColumn = new TextColumn<EditorType>() {
            @Override
            public String getValue(final EditorType type) {
                return editorTypeRegistry.getName(type);
            }

            @Override
            public String getCellStyleNames(final Context context, final EditorType object) {
                return resources.cellStyle().prefCell() + " " + resources.cellStyle().firstColumn();
            }
        };

        this.keyBindingSelection.addColumn(editorColumn);

        // disable row selection
        this.keyBindingSelection.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
    }

    @Override
    public void setDelegate(final ActionDelegate delegate) {
        this.delegate = delegate;
    }

    private void handleEditorKeymapChanged(final EditorType editorType, final Keymap keymap) {
        this.delegate.editorKeymapChanged(editorType, keymap);
    }

    @Override
    protected void onLoad() {
        setSelectionFromValuesHolder();
    }

    protected void setSelectionFromValuesHolder() {
        // delayed until the view is displayed
        keyBindingSelection.setRowData(editorTypeRegistry.getEditorTypes());
        for (final Entry<EditorType, Keymap> entry : this.valuesHolder) {
            keymapSelectionColumn.setSelection(entry.getKey(), entry.getValue());
        }
        keyBindingSelection.redraw();
    }

    public void setKeymapValuesHolder(final KeymapValuesHolder newValue) {
        this.valuesHolder = newValue;

        final FieldUpdater<EditorType, Keymap> fieldUpdater = new FieldUpdater<EditorType, Keymap>() {
            @Override
            public void update(final int index, final EditorType object, final Keymap value) {
                handleEditorKeymapChanged(object, value);
            }
        };

        keymapSelectionColumn = new KeymapSelectionColumn(valuesHolder, fieldUpdater, cellStyle.selectWidth());
        keyBindingSelection.addColumn(keymapSelectionColumn);
    }

    @Override
    public void refresh() {
        setSelectionFromValuesHolder();
    }

}
