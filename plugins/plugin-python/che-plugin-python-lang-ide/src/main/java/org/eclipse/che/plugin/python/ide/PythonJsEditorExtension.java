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
package org.eclipse.che.plugin.python.ide;

import com.google.inject.name.Named;

import org.eclipse.che.ide.api.editor.EditorRegistry;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.plugin.python.ide.editor.PythonEditorProvider;

import javax.inject.Inject;

/** @author Valeriy Svydenko */
@Extension(title = "Python JS Editor")
public class PythonJsEditorExtension {

    @Inject
    public PythonJsEditorExtension(final EditorRegistry editorRegistry,
                                   final @Named("PythonFileType") FileType pythonFile,
                                   final PythonEditorProvider pythonEditorProvider) {
        editorRegistry.registerDefaultEditor(pythonFile, pythonEditorProvider);
    }

}
