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
package org.eclipse.che.ide.ext.java.client.editor;

import org.eclipse.che.ide.api.editor.defaulteditor.AbstractTextEditorProvider;
import org.eclipse.che.ide.api.editor.editorconfig.EditorUpdateAction;
import org.eclipse.che.ide.api.editor.editorconfig.TextEditorConfiguration;
import org.eclipse.che.ide.api.editor.reconciler.Reconciler;
import org.eclipse.che.ide.api.editor.reconciler.ReconcilingStrategy;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.editor.orion.client.OrionEditorPresenter;

import javax.inject.Inject;
import java.util.logging.Logger;

import static org.eclipse.che.ide.api.editor.partition.DocumentPartitioner.DEFAULT_CONTENT_TYPE;

/** EditorProvider that provides a text editor configured for java source files. */
public class JsJavaEditorProvider extends AbstractTextEditorProvider {

    private static final Logger LOG = Logger.getLogger(JsJavaEditorProvider.class.getName());

    private final FileWatcher                      watcher;
    private final JsJavaEditorConfigurationFactory configurationFactory;

    @Inject
    public JsJavaEditorProvider(FileWatcher watcher, JsJavaEditorConfigurationFactory jsJavaEditorConfigurationFactory) {
        this.watcher = watcher;
        this.configurationFactory = jsJavaEditorConfigurationFactory;
    }

    @Override
    public String getId() {
        return "JavaEditor";
    }

    @Override
    public String getDescription() {
        return "Java Editor";
    }

    @Override
    public TextEditor getEditor() {
        LOG.fine("JsJavaEditor instance creation.");

        final TextEditor textEditor = super.getEditor();

        if (textEditor instanceof OrionEditorPresenter) {
            final OrionEditorPresenter editor = (OrionEditorPresenter)textEditor;
            final TextEditorConfiguration configuration = configurationFactory.create(editor);
            editor.initialize(configuration);
            editor.addEditorUpdateAction(new EditorUpdateAction() {
                @Override
                public void doRefresh() {
                    final Reconciler reconciler = configuration.getReconciler();
                    if (reconciler != null) {
                        final ReconcilingStrategy strategy = reconciler.getReconcilingStrategy(DEFAULT_CONTENT_TYPE);
                        if (strategy instanceof JavaReconcilerStrategy) {
                            ((JavaReconcilerStrategy)strategy).parse();
                        }
                    }
                }
            });
        }

        watcher.editorOpened(textEditor);

        return textEditor;
    }
}
