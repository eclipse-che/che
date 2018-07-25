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

import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.AUTO_COMPLETE_COMMENTS;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.AUTO_PAIR_ANGLE_BRACKETS;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.AUTO_PAIR_BRACES;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.AUTO_PAIR_PARENTHESES;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.AUTO_PAIR_QUOTATIONS;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.AUTO_PAIR_SQUARE_BRACKETS;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.SMART_INDENTATION;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.ide.api.editor.EditorLocalizationConstants;

/**
 * The class provides info about 'Typing' editor's section.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class TypingPropertiesSection implements EditorPropertiesSection {
  private final List<String> properties;
  private final EditorLocalizationConstants locale;

  @Inject
  public TypingPropertiesSection(EditorLocalizationConstants locale) {
    this.locale = locale;
    properties =
        Arrays.asList(
            AUTO_PAIR_PARENTHESES.toString(),
            AUTO_PAIR_BRACES.toString(),
            AUTO_PAIR_SQUARE_BRACKETS.toString(),
            AUTO_PAIR_ANGLE_BRACKETS.toString(),
            AUTO_PAIR_QUOTATIONS.toString(),
            AUTO_COMPLETE_COMMENTS.toString(),
            SMART_INDENTATION.toString());
  }

  @Override
  public List<String> getProperties() {
    return properties;
  }

  @Override
  public String getSectionTitle() {
    return locale.typingPropertiesSection();
  }
}
