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

import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.EXPAND_TAB;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.TAB_SIZE;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.ide.api.editor.EditorLocalizationConstants;

/**
 * The class provides info about 'Tabs' editor's section.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class TabsPropertiesSection implements EditorPropertiesSection {
  private final List<String> properties;
  private final EditorLocalizationConstants locale;

  @Inject
  public TabsPropertiesSection(EditorLocalizationConstants locale) {
    this.locale = locale;
    properties = Arrays.asList(TAB_SIZE.toString(), EXPAND_TAB.toString());
  }

  @Override
  public List<String> getProperties() {
    return properties;
  }

  @Override
  public String getSectionTitle() {
    return locale.tabsPropertiesSection();
  }
}
