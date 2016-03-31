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
package org.eclipse.che.plugin.cpp.ide.editor;

import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.jseditor.client.defaulteditor.DefaultEditorProvider;

import javax.inject.Inject;


/**
 *  EditorProvider that provides a text editor configured for C++ source files.
 *
 *  @author Vitalii Parfonov
 */
public class CppEditorProvider extends CEditorProvider {

    @Inject
    public CppEditorProvider(final DefaultEditorProvider editorProvider,
                             final NotificationManager notificationManager) {
        super(editorProvider, notificationManager);
    }

    @Override
    public String getId() {
        return "CppEditor";
    }

    @Override
    public String getDescription() {
        return "C++ Editor";
    }
}
