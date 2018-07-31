/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.part.editor.recent;

import org.eclipse.che.ide.api.resources.File;

/**
 * Extension for the recent list which process recent file lists.
 *
 * @author Vlad Zhukovskiy
 */
public interface RecentFileList extends RecentList<File> {
  /**
   * Return recent file list user dialog.
   *
   * @return user dialog with recent file list
   */
  OpenRecentFilesPresenter getRecentViewDialog();
}
