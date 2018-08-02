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
package org.eclipse.che.plugin.web.client.html;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.core.AgentURLModifier;
import org.eclipse.che.ide.util.browser.BrowserUtils;
import org.eclipse.che.plugin.web.client.WebLocalizationConstant;

/**
 * Action for previewing an HTML page.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class PreviewHTMLAction extends AbstractPerspectiveAction {

  private final AgentURLModifier agentURLModifier;
  private final AppContext appContext;

  @Inject
  public PreviewHTMLAction(
      AgentURLModifier agentURLModifier,
      AppContext appContext,
      WebLocalizationConstant localizationConstants) {
    super(
        singletonList(PROJECT_PERSPECTIVE_ID),
        localizationConstants.previewHTMLActionTitle(),
        localizationConstants.previewHTMLActionDescription());
    this.agentURLModifier = agentURLModifier;
    this.appContext = appContext;
  }

  @Override
  public void updateInPerspective(ActionEvent e) {
    final Resource[] resources = appContext.getResources();
    if (resources != null && resources.length == 1) {
      final Resource selectedResource = resources[0];
      if (Resource.FILE == selectedResource.getResourceType()) {
        final String fileExtension = ((File) selectedResource).getExtension();
        if ("html".equals(fileExtension)) {
          e.getPresentation().setEnabledAndVisible(true);
          return;
        }
      }
    }

    e.getPresentation().setEnabledAndVisible(false);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    final Resource selectedResource = appContext.getResource();
    if (Resource.FILE == selectedResource.getResourceType()) {
      final String contentUrl = ((File) selectedResource).getContentUrl();
      BrowserUtils.openInNewTab(agentURLModifier.modify(contentUrl));
    }
  }
}
