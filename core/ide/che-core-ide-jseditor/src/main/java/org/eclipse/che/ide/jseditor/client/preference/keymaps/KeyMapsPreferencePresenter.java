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

import java.util.List;
import java.util.Map.Entry;

import org.eclipse.che.ide.jseditor.client.editortype.EditorType;
import org.eclipse.che.ide.jseditor.client.editortype.EditorTypeRegistry;
import org.eclipse.che.ide.jseditor.client.keymap.Keymap;
import org.eclipse.che.ide.jseditor.client.keymap.KeymapChangeEvent;
import org.eclipse.che.ide.jseditor.client.keymap.KeymapValuesHolder;
import org.eclipse.che.ide.jseditor.client.preference.EditorPreferenceSection;
import org.eclipse.che.ide.jseditor.client.prefmodel.KeymapPrefReader;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

/** Presenter for the keymap preference selection section. */
public class KeyMapsPreferencePresenter implements EditorPreferenceSection, KeymapsPreferenceView.ActionDelegate {

    private final KeymapsPreferenceView view;

    private final KeymapPrefReader keymapPrefReader;
    private final EventBus         eventBus;

    private KeymapValuesHolder keymapValuesHolder;
    private KeymapValuesHolder prefKeymaps;

    /** Has any of the keymap preferences been changed ? */
    private boolean dirty = false;

    /** The preference page presenter. */
    private ParentPresenter parentPresenter;

    private EditorTypeRegistry editorTypeRegistry;

    @Inject
    public KeyMapsPreferencePresenter(final KeymapsPreferenceView view,
                                      final KeymapPrefReader keymapPrefReader,
                                      final EventBus eventBus,
                                      final EditorTypeRegistry editorTypeRegistry) {
        this.view = view;
        this.eventBus = eventBus;
        this.keymapPrefReader = keymapPrefReader;
        this.editorTypeRegistry = editorTypeRegistry;

        this.view.setDelegate(this);

        this.keymapValuesHolder = new KeymapValuesHolder();
        this.view.setKeymapValuesHolder(keymapValuesHolder);
        this.prefKeymaps = new KeymapValuesHolder();
    }

    @Override
    public void storeChanges() {
        keymapPrefReader.storePrefs(this.keymapValuesHolder);
        for (final Entry<EditorType, Keymap> entry : this.keymapValuesHolder) {
            this.eventBus.fireEvent(new KeymapChangeEvent(entry.getKey().getEditorTypeKey(), entry.getValue().getKey()));
        }
        dirty = false;
    }

    @Override
    public void refresh() {
        readPreferenceFromPreferenceManager();
        view.refresh();
    }

    protected void readPreferenceFromPreferenceManager() {
        keymapPrefReader.readPref(prefKeymaps);
        // init the default keymap
        for (EditorType editorType : editorTypeRegistry.getEditorTypes()) {
            List<Keymap> editorKeymaps = Keymap.getInstances(editorType);
            if (editorKeymaps.size() > 0) {
                keymapValuesHolder.setKeymap(editorType, editorKeymaps.get(0));
            }
        }
        for (final Entry<EditorType, Keymap> entry : prefKeymaps) {
            keymapValuesHolder.setKeymap(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void go(final AcceptsOneWidget container) {
        container.setWidget(null);
        readPreferenceFromPreferenceManager();
        container.setWidget(view);
    }

    @Override
    public void setParent(final ParentPresenter parent) {
        this.parentPresenter = parent;
    }

    @Override
    public void editorKeymapChanged(final EditorType editorType, final Keymap keymap) {
        if (editorType == null || keymap == null) {
            return;
        }

        dirty = false;
        for (final Entry<EditorType, Keymap> entry : this.keymapValuesHolder) {
            final Keymap prefKeymap = prefKeymaps.getKeymap(entry.getKey());
            if (entry.getValue() == null) {
                dirty = (prefKeymap != null);
            } else {
                dirty = !(entry.getValue().equals(prefKeymap));
            }

            if (dirty) {
                break;
            }
        }

        parentPresenter.signalDirtyState();
    }

}
