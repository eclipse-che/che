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

import org.eclipse.che.ide.editor.preferences.EditorPreferenceSection;

import java.util.List;

/**
 * The factory which creates instances of {@link EditorPropertiesSection}.
 *
 * @author Roman Nikitenko
 */
public interface EditorPreferenceSectionFactory {

    /**
     * Creates one of implementations of {@link EditorPropertiesSection}.
     *
     * @param title
     *         title of editor's properties section
     * @param propertiesIds
     *         IDs of properties which will be added to the section
     * @return an instance of {@link EditorPropertiesSection}
     */
    EditorPreferenceSection create(String title, List<String> propertiesIds);
}
