/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.editor.preferences.editorproperties.sections;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.SHOW_WHITESPACES;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.ide.api.editor.EditorLocalizationConstants;

/**
 * The class provides info about 'White spaces' editor's section.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class WhiteSpacesPropertiesSection implements EditorPropertiesSection {
  private final List<String> properties;
  private final EditorLocalizationConstants locale;

  @Inject
  public WhiteSpacesPropertiesSection(EditorLocalizationConstants locale) {
    this.locale = locale;
    properties = singletonList(SHOW_WHITESPACES.toString());
  }

  @Override
  public List<String> getProperties() {
    return properties;
  }

  @Override
  public String getSectionTitle() {
    return locale.whiteSpacesPropertiesSection();
  }
}
