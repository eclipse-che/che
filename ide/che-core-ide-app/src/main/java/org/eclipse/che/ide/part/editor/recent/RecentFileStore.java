/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.part.editor.recent;

import static com.google.common.collect.Lists.newLinkedList;
import static org.eclipse.che.ide.api.constraints.Constraints.FIRST;
import static org.eclipse.che.ide.api.editor.events.FileEvent.FileOperation.OPEN;
import static org.eclipse.che.ide.api.resources.ResourceDelta.REMOVED;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.IdeActions;
import org.eclipse.che.ide.api.editor.events.FileEvent;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.util.Pair;
import org.eclipse.che.ide.util.loging.Log;

/**
 * Default implementation of Recent File List.
 *
 * @author Vlad Zhukovskiy
 */
@Singleton
public class RecentFileStore implements RecentFileList {

  public static final int MAX_FILES_IN_STACK = 25;
  public static final int MAX_PATH_LENGTH_TO_DISPLAY = 50;
  public static final String RECENT_GROUP_ID = "Recent";

  private final OpenRecentFilesPresenter openRecentFilesPresenter;
  private final ActionManager actionManager;
  private final RecentFileActionFactory recentFileActionFactory;

  private DefaultActionGroup recentGroup;
  private LinkedList<File> recentStorage = newLinkedList();
  private LinkedList<Pair<File, RecentFileAction>> fileToAction = newLinkedList();

  @Inject
  public RecentFileStore(
      EventBus eventBus,
      OpenRecentFilesPresenter openRecentFilesPresenter,
      ActionManager actionManager,
      RecentFileActionFactory recentFileActionFactory) {
    this.openRecentFilesPresenter = openRecentFilesPresenter;
    this.actionManager = actionManager;
    this.recentFileActionFactory = recentFileActionFactory;

    eventBus.addHandler(
        FileEvent.TYPE,
        event -> {
          if (event.getOperationType() == OPEN) {
            VirtualFile file = event.getFile();
            if (file instanceof File) {
              add((File) file);
            }
          }
        });

    eventBus.addHandler(
        ResourceChangedEvent.getType(),
        event -> {
          if (event.getDelta().getKind() != REMOVED) {
            return;
          }

          final Resource resource = event.getDelta().getResource();

          if (!resource.isFile()) {
            return;
          }

          if (recentStorage.contains(resource.asFile())) {
            if (!remove(resource.asFile())) {
              Log.warn(getClass(), "File has not been removed from recent list");
            }
          }
        });
  }

  private void ensureGroupExist() {
    if (recentGroup == null) {
      recentGroup = (DefaultActionGroup) actionManager.getAction(IdeActions.GROUP_RECENT_FILES);
    }
  }

  /** {@inheritDoc} */
  @Override
  public boolean isEmpty() {
    return recentStorage.isEmpty();
  }

  /** {@inheritDoc} */
  @Override
  public boolean add(final File item) {
    ensureGroupExist();

    if (item == null || recentGroup == null) {
      return false;
    }

    // initial precondition
    if (recentStorage.size() == MAX_FILES_IN_STACK) {
      remove(recentStorage.getLast());
    }

    remove(item);

    recentStorage.addFirst(item);
    openRecentFilesPresenter.setRecentFiles(getAll());

    // register recent item action
    RecentFileAction action = recentFileActionFactory.newRecentFileAction(item);
    fileToAction.add(Pair.of(item, action));
    actionManager.registerAction(action.getId(), action);
    recentGroup.add(action, FIRST);

    return true;
  }

  /** {@inheritDoc} */
  @Override
  public boolean remove(File item) {
    if (recentGroup == null) {
      return false;
    }

    recentStorage.remove(item);
    openRecentFilesPresenter.setRecentFiles(getAll());

    // with one cycle de-register action and remove it from recent group
    Iterator<Pair<File, RecentFileAction>> iterator = fileToAction.iterator();
    while (iterator.hasNext()) {
      Pair<File, RecentFileAction> pair = iterator.next();
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
  public boolean contains(File item) {
    return recentStorage.contains(item);
  }

  /** {@inheritDoc} */
  @Override
  public List<File> getAll() {
    return recentStorage;
  }

  /** {@inheritDoc} */
  @Override
  public void clear() {
    if (recentGroup == null) {
      return;
    }

    openRecentFilesPresenter.clearRecentFiles();
    recentStorage.clear();

    // de-register all previously registered actions
    for (Pair<File, RecentFileAction> pair : fileToAction) {
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
   * Split path if it more then 50 characters. Otherwise, if path is less then 50 characters then it
   * returns as is.
   *
   * @param path path to check
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
}
