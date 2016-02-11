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

import org.eclipse.che.ide.jseditor.client.defaulteditor.EditorBuilder;

import javax.inject.Singleton;
import java.util.List;

/**
 * Manages all registered editor types.
 *
 * @author "Mickaël Leduque"
 */
@Singleton
public interface EditorTypeRegistry {

    /**
     * Registers an editor type.
     *
     * @param editorType
     *         the editor type instance
     * @param name
     *         the (user-visible) name of the editor type
     * @param editorBuilder
     *         a provider for the editor type
     */
    void registerEditorType(EditorType editorType, String name, EditorBuilder editorBuilder);

    /**
     * Returns the editor provider for a registered editor type.
     *
     * @param editorType
     *         the editor type
     * @return the provider
     */
    EditorBuilder getRegisteredBuilder(EditorType editorType);

    /**
     * Returns the user-visible name for the registered editor type.
     *
     * @param editorType
     * @return
     */
    String getName(EditorType editorType);

    /**
     * Returns a list of all registered editor types.
     *
     * @return the editor types
     */
    List<EditorType> getEditorTypes();

}
