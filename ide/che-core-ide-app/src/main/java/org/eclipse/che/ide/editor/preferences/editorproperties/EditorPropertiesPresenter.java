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
package org.eclipse.che.ide.editor.preferences.editorproperties;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.che.ide.api.editor.events.EditorSettingsChangedEvent;
import org.eclipse.che.ide.bootstrap.BasicIDEInitializedEvent;
import org.eclipse.che.ide.editor.preferences.EditorPreferenceSection;
import org.eclipse.che.ide.editor.preferences.editorproperties.sections.EditorPreferenceSectionFactory;
import org.eclipse.che.ide.editor.preferences.editorproperties.sections.EditorPropertiesSection;

/**
 * Presenter to manage the editor sections in the 'Preferences' menu.
 *
 * @author Roman Nikitenko
 */
public class EditorPropertiesPresenter implements EditorPreferenceSection {
  /** The preference page presenter. */
  private ParentPresenter parentPresenter;

  private Set<EditorPreferenceSection> sectionsSet = new HashSet<>();

  private final EventBus eventBus;
  private final EditorPropertiesView view;
  private final Set<EditorPropertiesSection> sections;
  private final EditorPreferenceSectionFactory editorPreferenceSectionFactory;

  @Inject
  public EditorPropertiesPresenter(
      final EditorPropertiesView view,
      final EventBus eventBus,
      final Set<EditorPropertiesSection> sections,
      final EditorPreferenceSectionFactory editorPreferenceSectionFactory) {
    this.view = view;
    this.sections = sections;
    this.editorPreferenceSectionFactory = editorPreferenceSectionFactory;
    this.eventBus = eventBus;

    eventBus.addHandler(BasicIDEInitializedEvent.TYPE, e -> init());
  }

  private void init() {
    for (EditorPropertiesSection section : sections) {
      EditorPreferenceSection editorPreferenceSection =
          editorPreferenceSectionFactory.create(section.getSectionTitle(), section.getProperties());
      editorPreferenceSection.go(view.getEditorSectionsContainer());
      editorPreferenceSection.setParent(parentPresenter);
      sectionsSet.add(editorPreferenceSection);
    }
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
}
