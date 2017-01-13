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
package org.eclipse.che.ide.editor.preferences.editorproperties;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.api.event.EditorSettingsChangedEvent;
import org.eclipse.che.ide.editor.preferences.EditorPreferenceSection;
import org.eclipse.che.ide.editor.preferences.editorproperties.sections.EditorPreferenceSectionFactory;
import org.eclipse.che.ide.editor.preferences.editorproperties.sections.EditorPropertiesSection;

import java.util.HashSet;
import java.util.Set;

/**
 * Presenter to manage the editor sections in the 'Preferences' menu.
 *
 * @author Roman Nikitenko
 */
public class EditorPropertiesPresenter implements EditorPreferenceSection, WsAgentStateHandler {
    /** The preference page presenter. */
    private ParentPresenter parentPresenter;
    private Set<EditorPreferenceSection> sectionsSet = new HashSet<>();

    private final EventBus                       eventBus;
    private final EditorPropertiesView           view;
    private final Set<EditorPropertiesSection>   sections;
    private final EditorPreferenceSectionFactory editorPreferenceSectionFactory;

    @Inject
    public EditorPropertiesPresenter(final EditorPropertiesView view,
                                     final EventBus eventBus,
                                     final Set<EditorPropertiesSection> sections,
                                     final EditorPreferenceSectionFactory editorPreferenceSectionFactory) {
        this.view = view;
        this.sections = sections;
        this.editorPreferenceSectionFactory = editorPreferenceSectionFactory;
        this.eventBus = eventBus;

        eventBus.addHandler(WsAgentStateEvent.TYPE, this);
    }

    @Override
    public void storeChanges() {
        for (EditorPreferenceSection section : sectionsSet) {
            if (section.isDirty()) {
                section.storeChanges();
            }
        }
        eventBus.fireEvent(new EditorSettingsChangedEvent());
    }

    @Override
    public void refresh() {
        for (EditorPreferenceSection section : sectionsSet) {
            if (section.isDirty()) {
                section.refresh();
            }
        }
    }

    @Override
    public boolean isDirty() {
        for (EditorPreferenceSection section : sectionsSet) {
            if (section.isDirty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void go(final AcceptsOneWidget container) {
        container.setWidget(view);
    }

    @Override
    public void setParent(final EditorPreferenceSection.ParentPresenter parent) {
        this.parentPresenter = parent;
    }

    @Override
    public void onWsAgentStarted(WsAgentStateEvent event) {
        for (EditorPropertiesSection section : sections) {
            EditorPreferenceSection editorPreferenceSection =
                    editorPreferenceSectionFactory.create(section.getSectionTitle(), section.getProperties());
            editorPreferenceSection.go(view.getEditorSectionsContainer());
            editorPreferenceSection.setParent(parentPresenter);
            sectionsSet.add(editorPreferenceSection);
        }
    }

    @Override
    public void onWsAgentStopped(WsAgentStateEvent event) {
    }
}
