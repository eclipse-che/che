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
package org.eclipse.che.ide.ext.java.client.refactoring;

import com.google.common.base.Predicate;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.event.FileContentUpdateEvent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.project.node.HasStorablePath.StorablePath;
import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.api.project.tree.VirtualFile;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.project.node.PackageNode;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangeInfo;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.project.node.FileReferenceNode;
import org.eclipse.che.ide.resource.Path;

import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Predicates.not;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Iterables.filter;
import static org.eclipse.che.api.promises.client.js.Promises.resolve;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * Utility class for the refactoring operations.
 * It is needed for refreshing the project tree, updating content of the opening editors.
 *
 * @author Valeriy Svydenko
 * @author Vlad Zhukovskyi
 */
@Singleton
public class RefactoringUpdater {
    private final EditorAgent              editorAgent;
    private final EventBus                 eventBus;
    private final ProjectExplorerPresenter projectExplorer;
    private final JavaLocalizationConstant locale;
    private final NotificationManager      notificationManager;

    private Predicate<ChangeInfo> UPDATE_ONLY = new Predicate<ChangeInfo>() {
        @Override
        public boolean apply(ChangeInfo input) {
            return ChangeInfo.ChangeName.UPDATE.equals(input.getName());
        }
    };

    @Inject
    public RefactoringUpdater(EditorAgent editorAgent,
                              EventBus eventBus,
                              NotificationManager notificationManager,
                              ProjectExplorerPresenter projectExplorer,
                              JavaLocalizationConstant locale) {
        this.editorAgent = editorAgent;
        this.notificationManager = notificationManager;
        this.eventBus = eventBus;
        this.projectExplorer = projectExplorer;
        this.locale = locale;
    }

    /**
     * Iterates over each refactoring change and according to change type performs specific update operation.
     * i.e. for {@code ChangeName#UPDATE} updates only opened editors, for {@code ChangeName#MOVE or ChangeName#RENAME_COMPILATION_UNIT}
     * updates only new paths and opened editors, for {@code ChangeName#RENAME_PACKAGE} reloads package structure and restore expansion.
     *
     * @param changes
     *         applied changes
     */
    public void updateAfterRefactoring(RefactorInfo refactoringInfo, List<ChangeInfo> changes) {
        if (changes == null || changes.isEmpty()) {
            return;
        }

        final Iterable<ChangeInfo> changesExceptUpdates = filter(changes, not(UPDATE_ONLY));
        final Iterable<ChangeInfo> updateChangesOnly = filter(changes, UPDATE_ONLY);

        Promise<Void> promise = resolve(null);
        promise = proceedGeneralChanges(promise, changesExceptUpdates.iterator(), refactoringInfo);
        proceedUpdateChanges(promise, updateChangesOnly.iterator());
    }

    /** Iterate over changes except update changes. Refresh tree according to change type. */
    private Promise<Void> proceedGeneralChanges(Promise<Void> promise, Iterator<ChangeInfo> iterator, final RefactorInfo refactorInfo) {
        if (!iterator.hasNext()) {
            return promise;
        }

        final ChangeInfo changeInfo = iterator.next();

        if (changeInfo == null || changeInfo.getName() == null) {
            return proceedGeneralChanges(promise, iterator, refactorInfo);
        }

        final Promise<Void> derivedPromise;

        switch (changeInfo.getName()) {
            case MOVE:
            case RENAME_COMPILATION_UNIT:
                if (refactorInfo != null && refactorInfo.getSelectedItems() != null) {
                    removeNodeFor(changeInfo, refactorInfo.getSelectedItems());
                }

                derivedPromise = promise.thenPromise(proceedRefactoringMove(changeInfo));
                break;
            case RENAME_PACKAGE:
                derivedPromise = promise.thenPromise(proceedRefactoringRenamePackage(changeInfo, refactorInfo));
                break;
            default:
                return proceedGeneralChanges(promise, iterator, refactorInfo);
        }

        return proceedGeneralChanges(derivedPromise, iterator, refactorInfo);
    }

    /** Iterate over changes that has UPDATE mode. In this case we try to update opened editors only. */
    private Promise<Void> proceedUpdateChanges(Promise<Void> promise, Iterator<ChangeInfo> iterator) {
        if (!iterator.hasNext()) {
            return promise;
        }

        final ChangeInfo changeInfo = iterator.next();

        //iterate over opened files in editor and find those file that matches ours
        final FileReferenceNode editorFile = getOpenedFileOrNull(!isNullOrEmpty(changeInfo.getOldPath()) ? changeInfo.getOldPath()
                                                                                                         : changeInfo.getPath());

        //if no one file were found, than it means that we shouldn't update anything
        if (editorFile == null) {
            return proceedUpdateChanges(promise, iterator);
        }

        final Promise<Void> derivedPromise = promise.thenPromise(new Function<Void, Promise<Void>>() {
            @Override
            public Promise<Void> apply(Void arg) throws FunctionException {
                return projectExplorer.getNodeByPath(new StorablePath(changeInfo.getPath()), true, false)
                                      .thenPromise(updateEditorContent(editorFile))
                                      .catchError(onNodeNotFound());

            }
        });

        return proceedUpdateChanges(derivedPromise, iterator);
    }

    /** Iterates over opened editors and fetch file with specified path or returns null. */
    private FileReferenceNode getOpenedFileOrNull(String path) {
        VirtualFile vFile = null;
        EditorPartPresenter openedEditor = editorAgent.getOpenedEditor(Path.valueOf(path));
        if (openedEditor != null) {
            vFile = openedEditor.getEditorInput().getFile();
        }

        if (vFile == null || !(vFile instanceof FileReferenceNode)) {
            return null;
        }

        return (FileReferenceNode)vFile;
    }

    /** Takes input file, provide into ones new data object and notifies editors to re-read file content. */
    private Function<Node, Promise<Void>> updateEditorContent(final FileReferenceNode editorFileToUpdate) {
        return new Function<Node, Promise<Void>>() {
            @Override
            public Promise<Void> apply(Node node) throws FunctionException {
                //here we consume node and if it is file node than we set data from one file into other
                if (node instanceof FileReferenceNode) {
                    setFileDataObject((FileReferenceNode)node, editorFileToUpdate);
                }

                return resolve(null);
            }
        };
    }

    /** Set data object from one file into other and notify editor to re-read file content if such opened. */
    private void setFileDataObject(FileReferenceNode from, FileReferenceNode to) {
        if (from == null || to == null) {
            return;
        }

        String tempPath = to.getPath();
        to.setData(from.getData());
        to.setParent(from.getParent());
        editorAgent.updateEditorNode(tempPath, to);
        eventBus.fireEvent(new FileContentUpdateEvent(from.getPath()));
    }

    /** Removes from Project Tree node which matches old path from Refactoring change info object. */
    private void removeNodeFor(ChangeInfo changeInfo, List<?> proceedItems) {
        for (Object proceedItem : proceedItems) {
            if (proceedItem instanceof FileReferenceNode
                && ((FileReferenceNode)proceedItem).getStorablePath().equals(changeInfo.getOldPath())) {
                projectExplorer.removeNode((FileReferenceNode)proceedItem, false);
            }
        }
    }

    /** Find node by new path and notify editors to re-read files if need. */
    private Function<Void, Promise<Void>> proceedRefactoringMove(final ChangeInfo changeInfo) {
        return new Function<Void, Promise<Void>>() {
            @Override
            public Promise<Void> apply(Void arg) throws FunctionException {
                FileReferenceNode editorFileToUpdate = getOpenedFileOrNull(changeInfo.getOldPath());

                return projectExplorer.getNodeByPath(new StorablePath(changeInfo.getPath()), true, false)
                                      .thenPromise(updateEditorContent(editorFileToUpdate))
                                      .catchError(onNodeNotFound());
            }
        };
    }

    /** Find new package node and restore expansion if need. */
    private Function<Void, Promise<Void>> proceedRefactoringRenamePackage(final ChangeInfo changeInfo, final RefactorInfo refactorInfo) {
        return new Function<Void, Promise<Void>>() {
            @Override
            public Promise<Void> apply(Void arg) throws FunctionException {
                //according to Rename package action it can be enabled if we have only one selected Package in selection agent
                Object refItem = refactorInfo.getSelectedItems().get(0);
                final boolean wasPackageExpanded = refItem instanceof PackageNode && projectExplorer.isExpanded((Node)refItem);

                return projectExplorer.getNodeByPath(new StorablePath(changeInfo.getPath()), true, false)
                                      .thenPromise(new Function<Node, Promise<Void>>() {
                                          @Override
                                          public Promise<Void> apply(Node node) throws FunctionException {
                                              //restore expand state
                                              if (wasPackageExpanded) {
                                                  projectExplorer.setExpanded(node, true);
                                              }

                                              return resolve(null);
                                          }
                                      })
                                      .catchError(onNodeNotFound());

            }
        };
    }

    /** Simply notify user in any failed cases. */
    private Operation<PromiseError> onNodeNotFound() {
        return new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                notificationManager.notify(locale.failedToProcessRefactoringOperation(), arg.getMessage(), FAIL, true);
            }
        };
    }
}
