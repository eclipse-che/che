/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
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
 * Action clears list of recently opened files.
 *
 * @author Oleksii Orel
 */
@Singleton
public class ClearRecentListAction extends AbstractPerspectiveAction {

  private RecentFileList recentFileList;

  @Inject
  public ClearRecentListAction(RecentFileList recentFileList, CoreLocalizationConstant locale) {
    super(
        singletonList(PROJECT_PERSPECTIVE_ID),
        locale.openRecentFileClearTitle(),
        locale.openRecentFileClearDescription());

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
    recentFileList.clear();
  }
}
