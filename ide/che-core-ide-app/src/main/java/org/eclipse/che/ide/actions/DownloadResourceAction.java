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
package org.eclipse.che.ide.actions;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.core.AgentURLModifier;
import org.eclipse.che.ide.download.DownloadContainer;

/**
 * Download resource which is in context now to the local machine.
 *
 * @author Roman Nikitenko
 * @author Vlad Zhukovskyi
 * @see AppContext#getResource()
 */
@Singleton
public class DownloadResourceAction extends AbstractPerspectiveAction {

  private final AppContext appContext;
  private final DownloadContainer downloadContainer;
  private final AgentURLModifier urlModifier;

  @Inject
  public DownloadResourceAction(
      AppContext appContext,
      CoreLocalizationConstant locale,
      DownloadContainer downloadContainer,
      AgentURLModifier urlModifier) {
    super(
        singletonList(PROJECT_PERSPECTIVE_ID),
        locale.downloadItemName(),
        locale.downloadItemDescription());
    this.appContext = appContext;
    this.downloadContainer = downloadContainer;
    this.urlModifier = urlModifier;
  }

  /** {@inheritDoc} */
  @Override
  public void actionPerformed(ActionEvent e) {
    final Resource resource = appContext.getResource();

    checkState(resource != null, "Null resource occurred");

    downloadContainer.setUrl(urlModifier.modify(resource.getURL()));
  }

  /** {@inheritDoc} */
  @Override
  public void updateInPerspective(@NotNull ActionEvent e) {
    final Resource[] resources = appContext.getResources();

    e.getPresentation().setVisible(true);
    e.getPresentation().setEnabled(resources != null && resources.length == 1);
  }
}
