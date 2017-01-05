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
package org.eclipse.che.ide.editor.preferences.editorproperties.sections;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.editor.EditorLocalizationConstants;

import java.util.Arrays;
import java.util.List;

import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.SHOW_CONTENT_ASSIST_AUTOMATICALLY;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.SHOW_OCCURRENCES;

/**
 * The class provides info about 'Language tolls' editor's section.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class LanguageToolsPropertiesSection implements EditorPropertiesSection {
    private final List<String>                properties;
    private final EditorLocalizationConstants locale;

    @Inject
    public LanguageToolsPropertiesSection(EditorLocalizationConstants locale) {
        this.locale = locale;
        properties = Arrays.asList(SHOW_OCCURRENCES.toString(),
                                   SHOW_CONTENT_ASSIST_AUTOMATICALLY.toString());
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
