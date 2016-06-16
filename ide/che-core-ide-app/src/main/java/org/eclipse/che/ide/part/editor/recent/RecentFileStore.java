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
package org.eclipse.che.ide.part.editor.recent;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.IdeActions;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.event.FileEventHandler;
import org.eclipse.che.ide.api.project.node.HasStorablePath.StorablePath;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.project.node.FileReferenceNode;
import org.eclipse.che.ide.util.Pair;

import javax.validation.constraints.NotNull;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static com.google.common.collect.Lists.newLinkedList;
import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.api.constraints.Anchor.BEFORE;
import static org.eclipse.che.ide.api.constraints.Constraints.FIRST;
import static org.eclipse.che.ide.api.constraints.Constraints.LAST;
import static org.eclipse.che.ide.api.event.FileEvent.FileOperation.OPEN;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Default implementation of Recent File List.
 *
 * @author Vlad Zhukovskiy
 */
@Singleton
public class RecentFileStore implements RecentFileList, FileEventHandler {

    public static final int    MAX_FILES_IN_STACK         = 25;
    public static final int    MAX_PATH_LENGTH_TO_DISPLAY = 50;
    public static final String RECENT_GROUP_ID            = "Recent";

    private final ProjectExplorerPresenter projectExplorer;
    private final OpenRecentFilesPresenter openRecentFilesPresenter;
    private final ActionManager            actionManager;
    private final RecentFileActionFactory  recentFileActionFactory;
    private final CoreLocalizationConstant locale;
    private final DefaultActionGroup       recentGroup;

    private LinkedList<FileReferenceNode>                         recentStorage = newLinkedList();
    private LinkedList<Pair<FileReferenceNode, RecentFileAction>> fileToAction  = newLinkedList();

    @Inject
    public RecentFileStore(EventBus eventBus,
                           ProjectExplorerPresenter projectExplorer,
                           OpenRecentFilesPresenter openRecentFilesPresenter,
                           ActionManager actionManager,
                           RecentFileActionFactory recentFileActionFactory,
                           CoreLocalizationConstant locale) {
        this.projectExplorer = projectExplorer;
        this.openRecentFilesPresenter = openRecentFilesPresenter;
        this.actionManager = actionManager;
        this.recentFileActionFactory = recentFileActionFactory;
        this.locale = locale;

        ClearRecentListAction action = new ClearRecentListAction();

        recentGroup = new DefaultActionGroup(RECENT_GROUP_ID, true, actionManager);
        actionManager.registerAction(IdeActions.GROUP_RECENT_FILES, recentGroup);

        actionManager.registerAction("clearRecentList", action);
        recentGroup.addSeparator();
        recentGroup.add(action, LAST);

        DefaultActionGroup editGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_EDIT);
        editGroup.add(recentGroup, new Constraints(BEFORE, "openRecentFiles"));

        eventBus.addHandler(FileEvent.TYPE, this);
    }

    /** {@inheritDoc} */
    @Override
    public void onFileOperation(FileEvent event) {
        if (event.getOperationType() == OPEN) {
            VirtualFile file = event.getFile();
            if (file instanceof FileReferenceNode) {
                add((FileReferenceNode)file);
            } else {
                //we got this file not from the project explorer
                projectExplorer.getNodeByPath(new StorablePath(file.getPath())).then(new Operation<Node>() {
                    @Override
                    public void apply(Node node) throws OperationException {
                        if (node instanceof FileReferenceNode) {
                            add((FileReferenceNode)node);
                        }
                    }
                });
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return recentStorage.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public boolean add(final FileReferenceNode item) {
        if (item == null) {
            return false;
        }

        //initial precondition
        if (recentStorage.size() == MAX_FILES_IN_STACK) {
            remove(recentStorage.getLast());
        }

        remove(item);

        recentStorage.addFirst(item);
        openRecentFilesPresenter.setRecentFiles(getAll());

        //register recent item action
        RecentFileAction action = recentFileActionFactory.newRecentFileAction(item);
        fileToAction.add(Pair.of(item, action));
        actionManager.registerAction(action.getId(), action);
        recentGroup.add(action, FIRST);

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean remove(FileReferenceNode item) {
        recentStorage.remove(item);
        openRecentFilesPresenter.setRecentFiles(getAll());

        //with one cycle de-register action and remove it from recent group
        Iterator<Pair<FileReferenceNode, RecentFileAction>> iterator = fileToAction.iterator();
        while (iterator.hasNext()) {
            Pair<FileReferenceNode, RecentFileAction> pair = iterator.next();
            if (pair.getFirst().equals(item)) {
                recentGroup.remove(pair.getSecond());
                actionManager.unregisterAction(pair.getSecond().getId());
                iterator.remove();
                return true;
            }
        }

        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(FileReferenceNode item) {
        return recentStorage.contains(item);
    }

    /** {@inheritDoc} */
    @Override
    public List<FileReferenceNode> getAll() {
        return recentStorage;
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        openRecentFilesPresenter.clearRecentFiles();

        recentStorage.clear();

        //de-register all previously registered actions
        for (Pair<FileReferenceNode, RecentFileAction> pair : fileToAction) {
            actionManager.unregisterAction(pair.getSecond().getId());
            recentGroup.remove(pair.getSecond());
        }

        fileToAction.clear();
    }

    /** {@inheritDoc} */
    @Override
    public OpenRecentFilesPresenter getRecentViewDialog() {
        return openRecentFilesPresenter;
    }

    /**
     * Split path if it more then 50 characters. Otherwise, if path is less then 50 characters then it returns as is.
     *
     * @param path
     *         path to check
     * @return path to display
     */
    static String getShortPath(String path) {
        if (path.length() < MAX_PATH_LENGTH_TO_DISPLAY) {
            return path;
        }

        int bIndex = path.length() - MAX_PATH_LENGTH_TO_DISPLAY;
        String raw = path.substring(bIndex);

        if (raw.indexOf('/') == -1) {
            return raw;
        }

        raw = raw.substring(raw.indexOf('/'));
        raw = "..." + raw;

        return raw;
    }

    private class ClearRecentListAction extends AbstractPerspectiveAction {

        public ClearRecentListAction() {
            super(singletonList(PROJECT_PERSPECTIVE_ID), locale.openRecentFileClearTitle(), locale.openRecentFileClearDescription(), null,
                  null);
        }

        @Override
        public void updateInPerspective(@NotNull ActionEvent event) {
            event.getPresentation().setEnabledAndVisible(!isEmpty());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            clear();
        }
    }
}
