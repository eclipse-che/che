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
package org.eclipse.che.plugin.cpp.ide;

import com.google.inject.name.Named;

import org.eclipse.che.ide.api.editor.EditorRegistry;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.plugin.cpp.ide.editor.CEditorProvider;
import org.eclipse.che.plugin.cpp.ide.editor.CppEditorProvider;

import javax.inject.Inject;

@Extension(title = "C/C++ JS Editor")
public class CppJsEditorExtension {

    @Inject
    public CppJsEditorExtension(final EditorRegistry editorRegistry,
                                final @Named("CFileType") FileType cFile,
                                final @Named("HFileType") FileType hFile,
                                final @Named("CppFileType") FileType classFile,
                                final CEditorProvider cEditorProvider,
                                final CppEditorProvider cppEditorProvider) {
        // register editor provider
        editorRegistry.registerDefaultEditor(cFile, cEditorProvider);
        editorRegistry.registerDefaultEditor(hFile, cEditorProvider);
        editorRegistry.registerDefaultEditor(classFile, cppEditorProvider);
        editorRegistry.registerDefaultEditor(classFile, cppEditorProvider);
    }
}
