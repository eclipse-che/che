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
package org.eclipse.che.ide.editor.preferences.editorproperties.propertiessection;

import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.event.EditorSettingsChangedEvent;
import org.eclipse.che.ide.editor.preferences.EditorPreferenceSection;
import org.eclipse.che.ide.editor.preferences.editorproperties.EditorPropertiesManager;

import java.util.List;
import java.util.Map;

/**
 * Presenter for the editor properties section in the 'Preferences' menu.
 *
 * @author Roman Nikitenko
 */
public class EditorPropertiesSectionPresenter implements EditorPreferenceSection, EditorPropertiesSectionView.ActionDelegate {
    /** The preference page presenter. */
    private       EditorPreferenceSection.ParentPresenter parentPresenter;
    private final EventBus                                eventBus;
    private final EditorPropertiesSectionView             view;
    private final EditorPropertiesManager                 editorPropertiesManager;
    private final List<String>                            properties;

    @AssistedInject
    public EditorPropertiesSectionPresenter(@Assisted String title,
                                            @Assisted List<String> properties,
                                            final EditorPropertiesSectionView view,
                                            final EventBus eventBus,
                                            final EditorPropertiesManager editorPropertiesManager) {
        this.view = view;
        this.view.setSectionTitle(title);
        this.view.setDelegate(this);
        this.properties = properties;
        this.eventBus = eventBus;
        this.editorPropertiesManager = editorPropertiesManager;
    }

    @Override
    public void storeChanges() {
        Map<String, JSONValue> editorProperties = editorPropertiesManager.getEditorProperties();
        for (String property : editorProperties.keySet()) {
            JSONValue actualValue = view.getPropertyValueById(property);
            actualValue = actualValue != null ? actualValue : editorProperties.get(property);
            editorProperties.put(property, actualValue);
        }
        editorPropertiesManager.storeEditorProperties(editorProperties);
        eventBus.fireEvent(new EditorSettingsChangedEvent());
    }

    @Override
    public void refresh() {
        addProperties();
    }

    @Override
    public boolean isDirty() {
        Map<String, JSONValue> editorProperties = editorPropertiesManager.getEditorProperties();
        for (String property : editorProperties.keySet()) {
            JSONValue actualValue = view.getPropertyValueById(property);
            if (actualValue != null && !actualValue.equals(editorProperties.get(property))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void go(final AcceptsOneWidget container) {
        addProperties();
        container.setWidget(view);
    }

    @Override
    public void setParent(final ParentPresenter parent) {
        this.parentPresenter = parent;
    }

    private void addProperties() {
        Map<String, JSONValue> editorProperties = editorPropertiesManager.getEditorProperties();
        for (String property : properties) {
            JSONValue value = editorProperties.get(property);
            view.addProperty(property, value);
        }
    }

    @Override
    public void onPropertyChanged() {
        parentPresenter.signalDirtyState();
    }
}
