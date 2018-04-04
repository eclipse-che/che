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
package org.eclipse.che.ide.editor.preferences.keymaps;

import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.api.editor.keymap.Keymap;
import org.eclipse.che.ide.api.editor.keymap.KeymapChangeEvent;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.editor.preferences.EditorPreferenceSection;

/** Presenter for the keymap preference selection section. */
public class KeyMapsPreferencePresenter
    implements EditorPreferenceSection, KeymapsPreferenceView.ActionDelegate {

  /** The editor preference main property name. */
  public static final String KEYMAP_PREF_KEY = "keymap";

  private final KeymapsPreferenceView view;
  private final EventBus eventBus;
  private final PreferencesManager preferencesManager;

  /** Has any of the keymap preferences been changed ? */
  private boolean dirty = false;

  /** The preference page presenter. */
  private ParentPresenter parentPresenter;

  private Keymap selectedKeymap;

  @Inject
  public KeyMapsPreferencePresenter(
      final KeymapsPreferenceView view,
      final EventBus eventBus,
      final PreferencesManager preferencesManager) {
    this.view = view;
    this.eventBus = eventBus;
    this.preferencesManager = preferencesManager;

    this.view.setDelegate(this);
  }

  @Override
  public void storeChanges() {
    JSONString jsonString = new JSONString(selectedKeymap.getKey());
    preferencesManager.setValue(KEYMAP_PREF_KEY, jsonString.stringValue());

    eventBus.fireEvent(new KeymapChangeEvent(selectedKeymap.getKey()));

    dirty = false;
  }

  @Override
  public void refresh() {
    readPreference();
  }

  protected void readPreference() {
    final String keymapKey = preferencesManager.getValue(KEYMAP_PREF_KEY);
    Keymap keymap = null;
    if (keymapKey != null) {
      keymap = Keymap.fromKey(keymapKey);
    } else {
      for (Keymap km : Keymap.getInstances()) {
        if (km.getKey().contains("default")) {
          keymap = km;
          break;
        }
      }
    }
    view.setKeyBindings(Keymap.getInstances(), keymap);
  }

  @Override
  public boolean isDirty() {
    return dirty;
  }

  @Override
  public void go(final AcceptsOneWidget container) {
    readPreference();
    container.setWidget(view);
  }

  @Override
  public void setParent(final ParentPresenter parent) {
    this.parentPresenter = parent;
  }

  @Override
  public void onKeyBindingSelected(Keymap keymap) {
    selectedKeymap = keymap;

    dirty = true;
    parentPresenter.signalDirtyState();
  }
}
