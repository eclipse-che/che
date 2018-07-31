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
package org.eclipse.che.ide.editor.preferences.editorproperties.sections;

import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.SHOW_CONTENT_ASSIST_AUTOMATICALLY;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.SHOW_OCCURRENCES;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.ide.api.editor.EditorLocalizationConstants;

/**
 * The class provides info about 'Language tolls' editor's section.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class LanguageToolsPropertiesSection implements EditorPropertiesSection {
  private final List<String> properties;
  private final EditorLocalizationConstants locale;

  @Inject
  public LanguageToolsPropertiesSection(EditorLocalizationConstants locale) {
    this.locale = locale;
    properties =
        Arrays.asList(SHOW_OCCURRENCES.toString(), SHOW_CONTENT_ASSIST_AUTOMATICALLY.toString());
  }

  @Override
  public List<String> getProperties() {
    return properties;
  }

  @Override
  public String getSectionTitle() {
    return locale.languageToolsPropertiesSection();
  }
}
