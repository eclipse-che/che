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

import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.EXPAND_TAB;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.TAB_SIZE;

/**
 * The class provides info about 'Tabs' editor's section.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class TabsPropertiesSection implements EditorPropertiesSection {
    private final List<String>                properties;
    private final EditorLocalizationConstants locale;

    @Inject
    public TabsPropertiesSection(EditorLocalizationConstants locale) {
        this.locale = locale;
        properties = Arrays.asList(TAB_SIZE.toString(),
                                   EXPAND_TAB.toString());
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
