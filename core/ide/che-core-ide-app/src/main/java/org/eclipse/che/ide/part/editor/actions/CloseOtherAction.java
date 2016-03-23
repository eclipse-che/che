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

import com.google.common.base.Predicate;
import com.google.gwt.core.client.Scheduler;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.project.tree.VirtualFile;

import static com.google.common.collect.Iterables.filter;

/**
 * Performs closing all opened editors except selected one.
 *
 * @author Vlad Zhukovskiy
 */
@Singleton
public class CloseOtherAction extends EditorAbstractAction {

    @Inject
    public CloseOtherAction(EditorAgent editorAgent,
                            EventBus eventBus,
                            CoreLocalizationConstant locale) {
        super(locale.editorTabCloseAllExceptSelected(), locale.editorTabCloseAllExceptSelectedDescription(), null, editorAgent, eventBus);
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        final VirtualFile virtualFile = getEditorFile(e);

        Iterable<EditorPartPresenter> filtered = filter(editorAgent.getOpenedEditors(), new Predicate<EditorPartPresenter>() {
            @Override
            public boolean apply(EditorPartPresenter input) {
                return !input.getEditorInput().getFile().equals(virtualFile);
            }
        });

        for (final EditorPartPresenter toClose : filtered) {
            Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    eventBus.fireEvent(new FileEvent(toClose.getEditorInput().getFile(), FileEvent.FileOperation.CLOSE));
                }
            });
        }
    }
}
