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

import org.eclipse.che.ide.jseditor.client.editortype.EditorType;
import org.eclipse.che.ide.jseditor.client.keymap.Keymap;
import org.eclipse.che.ide.jseditor.client.keymap.KeymapValuesHolder;
import org.eclipse.che.ide.util.loging.Log;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.user.cellview.client.Column;

public class KeymapSelectionColumn extends Column<EditorType, Keymap> {
    private final KeymapValuesHolder valuesHolder;

    public KeymapSelectionColumn(final KeymapValuesHolder valuesHolder,
                                 final FieldUpdater<EditorType, Keymap> fieldUpdater,
                                 final String selectWidthStyle) {
        super(new KeymapSelectionCell("gwt-ListBox", selectWidthStyle));
        this.valuesHolder = valuesHolder;

        setFieldUpdater(new FieldUpdater<EditorType, Keymap>() {

            @Override
            public void update(final int index, final EditorType editorType, final Keymap keymap) {
                Log.debug(KeymapSelectionColumn.class, "Value update for editor " + editorType + " keymap=" + keymap);
                KeymapSelectionColumn.this.valuesHolder.setKeymap(editorType, keymap);
                fieldUpdater.update(index, editorType, keymap);
            }
        });
    }

    @Override
    public Keymap getValue(final EditorType editorType) {
        if (this.valuesHolder == null) {
            return null;
        } else {
            return this.valuesHolder.getKeymap(editorType);
        }
    }

    public void setSelection(final EditorType key, final Keymap value) {
        final KeymapSelectionCell cell = (KeymapSelectionCell)getCell();
        cell.setViewData(key, value.getKey());
    }

}
