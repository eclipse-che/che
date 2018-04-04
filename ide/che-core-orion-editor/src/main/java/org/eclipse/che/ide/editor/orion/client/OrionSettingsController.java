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
package org.eclipse.che.ide.editor.orion.client;

import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.AUTO_COMPLETE_COMMENTS;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.AUTO_PAIR_ANGLE_BRACKETS;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.AUTO_PAIR_BRACES;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.AUTO_PAIR_PARENTHESES;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.AUTO_PAIR_QUOTATIONS;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.AUTO_PAIR_SQUARE_BRACKETS;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.EXPAND_TAB;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.SHOW_ANNOTATION_RULER;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.SHOW_CONTENT_ASSIST_AUTOMATICALLY;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.SHOW_FOLDING_RULER;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.SHOW_LINE_NUMBER_RULER;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.SHOW_OCCURRENCES;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.SHOW_OVERVIEW_RULER;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.SHOW_WHITESPACES;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.SHOW_ZOOM_RULER;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.SMART_INDENTATION;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.SOFT_WRAP;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.TAB_SIZE;

import com.google.gwt.json.client.JSONObject;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import java.util.EnumSet;
import org.eclipse.che.ide.api.editor.events.EditorSettingsChangedEvent;
import org.eclipse.che.ide.api.editor.events.EditorSettingsChangedEvent.EditorSettingsChangedHandler;
import org.eclipse.che.ide.editor.orion.client.jso.OrionEditorViewOverlay;
import org.eclipse.che.ide.editor.preferences.EditorPreferencesManager;
import org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties;

/**
 * The class contains methods to simplify the work with orion settings.
 *
 * @author Roman Nikitenko
 */
public class OrionSettingsController implements EditorSettingsChangedHandler {

  private OrionEditorViewOverlay editorViewOverlay;
  private final EnumSet<EditorProperties> orionPropertiesSet =
      EnumSet.noneOf(EditorProperties.class);

  private final EditorPreferencesManager editorPreferencesManager;

  @Inject
  public OrionSettingsController(
      final EventBus eventBus, final EditorPreferencesManager editorPreferencesManager) {
    this.editorPreferencesManager = editorPreferencesManager;

    fillUpEditorPropertiesSet();
    eventBus.addHandler(EditorSettingsChangedEvent.TYPE, this);
  }

  public void setEditorViewOverlay(OrionEditorViewOverlay editorViewOverlay) {
    this.editorViewOverlay = editorViewOverlay;
  }

  public void updateSettings() {
    if (editorViewOverlay != null) {
      JSONObject properties =
          editorPreferencesManager.getJsonEditorPreferencesFor(orionPropertiesSet);
      editorViewOverlay.updateSettings(properties.getJavaScriptObject());
    }
  }

  @Override
  public void onEditorSettingsChanged(EditorSettingsChangedEvent event) {
    updateSettings();
  }

  private void fillUpEditorPropertiesSet() {
    orionPropertiesSet.add(TAB_SIZE);
    orionPropertiesSet.add(EXPAND_TAB);
    orionPropertiesSet.add(AUTO_PAIR_PARENTHESES);
    orionPropertiesSet.add(AUTO_PAIR_BRACES);
    orionPropertiesSet.add(AUTO_PAIR_SQUARE_BRACKETS);
    orionPropertiesSet.add(AUTO_PAIR_ANGLE_BRACKETS);
    orionPropertiesSet.add(AUTO_PAIR_QUOTATIONS);
    orionPropertiesSet.add(AUTO_COMPLETE_COMMENTS);
    orionPropertiesSet.add(SMART_INDENTATION);
    orionPropertiesSet.add(SHOW_WHITESPACES);
    orionPropertiesSet.add(SOFT_WRAP);
    orionPropertiesSet.add(SHOW_ANNOTATION_RULER);
    orionPropertiesSet.add(SHOW_LINE_NUMBER_RULER);
    orionPropertiesSet.add(SHOW_FOLDING_RULER);
    orionPropertiesSet.add(SHOW_OVERVIEW_RULER);
    orionPropertiesSet.add(SHOW_ZOOM_RULER);
    orionPropertiesSet.add(SHOW_OCCURRENCES);
    orionPropertiesSet.add(SHOW_CONTENT_ASSIST_AUTOMATICALLY);
  }
}
