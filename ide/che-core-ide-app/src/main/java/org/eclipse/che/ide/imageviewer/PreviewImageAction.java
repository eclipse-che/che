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
package org.eclipse.che.ide.imageviewer;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.core.AgentURLModifier;
import org.eclipse.che.ide.util.browser.BrowserUtils;

/**
 * Action for previewing images in dedicated window.
 *
 * @author Vitaliy Guliy
 */
@Singleton
public class PreviewImageAction extends AbstractPerspectiveAction {

  private final AgentURLModifier agentURLModifier;
  private final AppContext appContext;

  private final List<String> extensions = new ArrayList<>();

  @Inject
  public PreviewImageAction(
      AgentURLModifier agentURLModifier,
      AppContext appContext,
      CoreLocalizationConstant constant,
      @Named("PNGFileType") FileType pngFile,
      @Named("BMPFileType") FileType bmpFile,
      @Named("GIFFileType") FileType gifFile,
      @Named("ICOFileType") FileType iconFile,
      @Named("SVGFileType") FileType svgFile,
      @Named("JPEFileType") FileType jpeFile,
      @Named("JPEGFileType") FileType jpegFile,
      @Named("JPGFileType") FileType jpgFile) {
    super(
        singletonList(PROJECT_PERSPECTIVE_ID),
        constant.actionPreviewImageTitle(),
        constant.actionPreviewImageDescription());
    this.agentURLModifier = agentURLModifier;
    this.appContext = appContext;

    extensions.add(pngFile.getExtension());
    extensions.add(bmpFile.getExtension());
    extensions.add(gifFile.getExtension());
    extensions.add(iconFile.getExtension());
    extensions.add(svgFile.getExtension());
    extensions.add(jpeFile.getExtension());
    extensions.add(jpegFile.getExtension());
    extensions.add(jpgFile.getExtension());
  }

  @Override
  public void updateInPerspective(ActionEvent e) {
    final Resource[] resources = appContext.getResources();
    if (resources != null && resources.length == 1) {
      final Resource selectedResource = resources[0];
      if (Resource.FILE == selectedResource.getResourceType()) {
        final String fileExtension = ((File) selectedResource).getExtension();
        e.getPresentation().setEnabledAndVisible(extensions.contains(fileExtension));
        return;
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
