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

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;

/**
 * Action shows dialog with recently opened files.
 *
 * @author Vlad Zhukovskiy
 */
@Singleton
public class OpenRecentFilesAction extends AbstractPerspectiveAction {

  private final RecentFileList recentFileList;

  @Inject
  public OpenRecentFilesAction(RecentFileList recentFileList, CoreLocalizationConstant locale) {
    super(
        singletonList(PROJECT_PERSPECTIVE_ID),
        locale.openRecentFileTitle(),
        locale.openRecentFileDescription());
    this.recentFileList = recentFileList;
  }

  /** {@inheritDoc} */
  @Override
  public void updateInPerspective(@NotNull ActionEvent event) {
    event.getPresentation().setEnabledAndVisible(!recentFileList.isEmpty());
  }

  /** {@inheritDoc} */
  @Override
  public void actionPerformed(ActionEvent e) {
    recentFileList.getRecentViewDialog().show();
  }
}
