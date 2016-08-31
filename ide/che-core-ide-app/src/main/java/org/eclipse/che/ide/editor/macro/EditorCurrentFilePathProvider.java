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
package org.eclipse.che.ide.editor.macro;

import com.google.common.annotations.Beta;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.resource.Path;

/**
 * Provider which is responsible for retrieving the absolute file path from the opened editor.
 *
 * Macro provided: <code>${editor.current.file.path}</code>
 *
 * @see AbstractEditorMacroProvider
 * @see EditorAgent
 * @since 4.7.0
 */
@Beta
@Singleton
public class EditorCurrentFilePathProvider extends AbstractEditorMacroProvider {

    public static final String KEY = "${editor.current.file.path}";

    private PromiseProvider promises;
    private AppContext      appContext;

    @Inject
    public EditorCurrentFilePathProvider(EditorAgent editorAgent,
                                         PromiseProvider promises,
                                         AppContext appContext) {
        super(editorAgent);
        this.promises = promises;
        this.appContext = appContext;
    }

    /** {@inheritDoc} */
    @Override
    public String getKey() {
        return KEY;
    }

    /** {@inheritDoc} */
    @Override
    public Promise<String> getValue() {
        final EditorPartPresenter editor = getActiveEditor();

        if (editor == null) {
            return promises.resolve("");
        }

        final Path absolutePath = appContext.getProjectsRoot().append(editor.getEditorInput().getFile().getLocation());

        return promises.resolve(absolutePath.toString());
    }
}
