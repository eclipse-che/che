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

import java.util.List;

/**
 * The interface provides methods to get info about editor's properties section.
 *
 * @author Roman Nikitenko
 */
public interface EditorPropertiesSection {

    /** Returns IDs of properties which the section contains */
    public List<String> getProperties();

    /** Returns the title of editor's properties section */
    public String getSectionTitle();
}
