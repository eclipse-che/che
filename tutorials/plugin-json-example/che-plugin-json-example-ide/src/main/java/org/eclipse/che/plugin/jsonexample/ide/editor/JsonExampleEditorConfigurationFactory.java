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
package org.eclipse.che.plugin.jsonexample.ide.editor;

import org.eclipse.che.ide.api.editor.texteditor.TextEditor;

/**
 * Helper factory for creating the JSON Example specific editor configuraiton.
 */
// TODO: remove, if unused
public interface JsonExampleEditorConfigurationFactory {

    /**
     * Create a {@link JsonExampleEditorConfiguration}.
     *
     * @param editor
     *    the editor
     * @return the JSON Example editor configuration
     */
    JsonExampleEditorConfiguration create(TextEditor editor);
}
