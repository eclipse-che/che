/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.jseditor.client.editortype;

import org.eclipse.che.ide.api.filetypes.FileType;

import javax.inject.Singleton;
import java.util.Map.Entry;

/**
 * An interface for content type to editor type mappings.
 *
 * @author "Mickaël Leduque"
 */
@Singleton
public interface EditorTypeMapping extends Iterable<Entry<FileType, EditorType>> {

    public static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";

    /**
     * Sets a mapping for a content type.
     *
     * @param contentType
     *         the content type
     * @param editorType
     *         the associated editor type
     */
    void setEditorType(final FileType contentType, EditorType editorType);

    /**
     * Searches an editor type for a content type.
     *
     * @param contentType
     *         the content type
     * @return an editor type (never null)
     */
    EditorType getEditorType(final FileType contentType);


    /**
     * Loads the mappings from the user preferences.
     */
    void loadFromPreferences();

    /**
     * Stores the mappings in the user preferences.
     */
    void storeInPreferences();

}
