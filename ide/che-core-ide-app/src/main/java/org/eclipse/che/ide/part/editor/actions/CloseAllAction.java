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
package org.eclipse.che.ide.part.editor.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.event.FileEvent;

import static org.eclipse.che.ide.api.event.FileEvent.FileOperation.CLOSE;

/**
 * Performs closing all opened editors.
 *
 * @author Vlad Zhukovskiy
 */
@Singleton
public class CloseAllAction extends EditorAbstractAction {

    @Inject
    public CloseAllAction(EditorAgent editorAgent,
                          EventBus eventBus,
                          CoreLocalizationConstant locale) {
        super(locale.editorTabCloseAll(), locale.editorTabCloseAllDescription(), null, editorAgent, eventBus);
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        for (EditorPartPresenter editor : editorAgent.getOpenedEditors()) {
            eventBus.fireEvent(new FileEvent(editor.getEditorInput().getFile(), CLOSE));
        }
    }
}
