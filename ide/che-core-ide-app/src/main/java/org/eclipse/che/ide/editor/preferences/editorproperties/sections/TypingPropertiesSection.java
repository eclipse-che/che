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

import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.AUTO_COMPLETE_COMMENTS;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.AUTO_PAIR_ANGLE_BRACKETS;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.AUTO_PAIR_BRACES;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.AUTO_PAIR_PARENTHESES;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.AUTO_PAIR_QUOTATIONS;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.AUTO_PAIR_SQUARE_BRACKETS;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.SMART_INDENTATION;

/**
 * The class provides info about 'Typing' editor's section.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class TypingPropertiesSection implements EditorPropertiesSection {
    private final List<String>                properties;
    private final EditorLocalizationConstants locale;

    @Inject
    public TypingPropertiesSection(EditorLocalizationConstants locale) {
        this.locale = locale;
        properties = Arrays.asList(AUTO_PAIR_PARENTHESES.toString(),
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
