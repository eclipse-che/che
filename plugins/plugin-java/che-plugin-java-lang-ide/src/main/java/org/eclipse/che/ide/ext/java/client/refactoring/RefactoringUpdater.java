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
package org.eclipse.che.ide.ext.java.client.refactoring;

import com.google.gwt.core.client.Scheduler;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.event.FileContentUpdateEvent;
import org.eclipse.che.ide.api.event.ng.DeletedFilesController;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.resources.ExternalResourceDelta;
import org.eclipse.che.ide.api.resources.ResourceDelta;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangeInfo;
import org.eclipse.che.ide.part.editor.multipart.EditorMultiPartStackPresenter;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.resources.reveal.RevealResourceEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.ide.api.resources.ResourceDelta.ADDED;
import static org.eclipse.che.ide.api.resources.ResourceDelta.MOVED_FROM;
import static org.eclipse.che.ide.api.resources.ResourceDelta.MOVED_TO;

/**
 * Utility class for the refactoring operations.
 * It is needed for refreshing the project tree, updating content of the opening editors.
 *
 * @author Valeriy Svydenko
 * @author Vlad Zhukovskyi
 */
@Singleton
public class RefactoringUpdater {

    private final AppContext                    appContext;
    private final EditorMultiPartStackPresenter editorMultiPartStack;
    private final WorkspaceAgent                workspaceAgent;
    private final DeletedFilesController        deletedFilesController;
    private final EventBus                      eventBus;
    private final EditorAgent                   editorAgent;

    @Inject
    public RefactoringUpdater(AppContext appContext,
                              EditorMultiPartStackPresenter editorMultiPartStack,
                              WorkspaceAgent workspaceAgent,
                              DeletedFilesController deletedFilesController,
                              EventBus eventBus,
                              EditorAgent editorAgent) {
        this.appContext = appContext;
        this.editorMultiPartStack = editorMultiPartStack;
        this.workspaceAgent = workspaceAgent;
        this.deletedFilesController = deletedFilesController;
        this.eventBus = eventBus;
        this.editorAgent = editorAgent;
    }

    /**
     * Iterates over each refactoring change and according to change type performs specific update operation.
     * i.e. for {@code ChangeName#UPDATE} updates only opened editors, for {@code ChangeName#MOVE or ChangeName#RENAME_COMPILATION_UNIT}
     * updates only new paths and opened editors, for {@code ChangeName#RENAME_PACKAGE} reloads package structure and restore expansion.
     *
     * @param changes
     *         applied changes
     */
    public void updateAfterRefactoring(List<ChangeInfo> changes) {
        if (changes == null || changes.isEmpty()) {
            return;
        }

        ExternalResourceDelta[] deltas = new ExternalResourceDelta[0];
        final List<String> pathChanged = new ArrayList<>();
        for (ChangeInfo change : changes) {

            final ExternalResourceDelta delta;

            final Path newPath = Path.valueOf(change.getPath());
            final Path oldPath = !isNullOrEmpty(change.getOldPath()) ? Path.valueOf(change.getOldPath()) : Path.EMPTY;

            switch (change.getName()) {
                case MOVE:
                case RENAME_COMPILATION_UNIT:
                    delta = new ExternalResourceDelta(newPath, oldPath, ADDED | MOVED_FROM | MOVED_TO);
                    registerRemovedFile(change);
                    break;
                case RENAME_PACKAGE:
                    delta = new ExternalResourceDelta(newPath, oldPath, ADDED | MOVED_FROM | MOVED_TO);
                    break;
                case UPDATE:
                    if (!isFileRemoved(change.getPath(), changes)) {
                        pathChanged.add(change.getPath());
                        registerRemovedFile(change);
                    }
                default:
                    continue;
            }

            final int index = deltas.length;
            deltas = Arrays.copyOf(deltas, index + 1);
            deltas[index] = delta;
        }

        //here we need to remove file for file that moved or renamed JDT lib sent it to
        for (int i = 0; i < deltas.length; i++) {
            if (pathChanged.contains(deltas[i].getToPath().toString())) {
                pathChanged.remove(deltas[i].getToPath().toString());
            }
            if (pathChanged.contains(deltas[i].getFromPath().toString())) {
                pathChanged.remove(deltas[i].getFromPath().toString());
            }
        }

        if (deltas.length > 0) {
            appContext.getWorkspaceRoot().synchronize(deltas).then(new Operation<ResourceDelta[]>() {
                @Override
                public void apply(final ResourceDelta[] appliedDeltas) throws OperationException {
                    for (ResourceDelta delta : appliedDeltas) {
                        eventBus.fireEvent(new RevealResourceEvent(delta.getToPath()));
                    }
                    for (EditorPartPresenter editorPartPresenter : editorAgent.getOpenedEditors()) {
                        final String path = editorPartPresenter.getEditorInput().getFile().getLocation().toString();
                        if (pathChanged.contains(path)) {
                            eventBus.fireEvent(
                                    new FileContentUpdateEvent(editorPartPresenter.getEditorInput().getFile().getLocation().toString()));
                        }
                    }
                    setActiveEditor();
                }
            });
        } else {
            Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    for (EditorPartPresenter editorPartPresenter : editorAgent.getOpenedEditors()) {
                        eventBus.fireEvent(
                                new FileContentUpdateEvent(editorPartPresenter.getEditorInput().getFile().getLocation().toString()));
                    }
                    setActiveEditor();
                }
            });
        }
    }

    private void registerRemovedFile(ChangeInfo change) {
        for (EditorPartPresenter editorPartPresenter : editorAgent.getOpenedEditors()) {
            String editorPath = editorPartPresenter.getEditorInput().getFile().getLocation().toString();
            if (editorPath.equals(change.getOldPath())) {
                deletedFilesController.add(change.getOldPath());
                return;
            }
        }
    }

    private boolean isFileRemoved(String filePath, List<ChangeInfo> changes) {
        for (ChangeInfo changeInfo : changes) {
            if (!isNullOrEmpty(changeInfo.getOldPath()) && changeInfo.getOldPath().equals(filePath)) {
                return true;
            }
        }

        return false;
    }

    private void setActiveEditor() {
        PartPresenter activePart = editorMultiPartStack.getActivePart();
        if (activePart != null) {
            workspaceAgent.setActivePart(activePart);
        }
    }
}
