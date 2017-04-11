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
package org.eclipse.che.ide.imageviewer;

import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import static java.util.Collections.singletonList;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.machine.WsAgentURLModifier;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Resource;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Action for previewing images in dedicated window.
 *
 * @author Vitaliy Guliy
 */
@Singleton
public class PreviewImageAction extends AbstractPerspectiveAction {

    private final WsAgentURLModifier wsAgentURLModifier;
    private final AppContext         appContext;

    @Inject
    @Named("PNGFileType")
    private FileType pngFile;

    @Inject
    @Named("BMPFileType")
    private FileType bmpFile;

    @Inject
    @Named("GIFFileType")
    private FileType gifFile;

    @Inject
    @Named("ICOFileType")
    private FileType iconFile;

    @Inject
    @Named("SVGFileType")
    private FileType svgFile;

    @Inject
    @Named("JPEFileType")
    private FileType jpeFile;

    @Inject
    @Named("JPEGFileType")
    private FileType jpegFile;

    @Inject
    @Named("JPGFileType")
    private FileType jpgFile;

    @Inject
    public PreviewImageAction(WsAgentURLModifier wsAgentURLModifier,
                             AppContext appContext,
                              CoreLocalizationConstant constant) {
        super(singletonList(PROJECT_PERSPECTIVE_ID),
                constant.actionPreviewImageTitle(),
                constant.actionPreviewImageDescription(),
                null,
                null);
        this.wsAgentURLModifier = wsAgentURLModifier;
        this.appContext = appContext;
    }

    @Override
    public void updateInPerspective(ActionEvent e) {
        final Resource[] resources = appContext.getResources();
        if (resources != null && resources.length == 1) {
            final Resource selectedResource = resources[0];
            if (Resource.FILE == selectedResource.getResourceType()) {
                final String fileExtension = ((File)selectedResource).getExtension();

                if (pngFile.getExtension().equals(fileExtension) ||
                        bmpFile.getExtension().equals(fileExtension) ||
                        gifFile.getExtension().equals(fileExtension) ||
                        iconFile.getExtension().equals(fileExtension) ||
                        svgFile.getExtension().equals(fileExtension) ||
                        jpeFile.getExtension().equals(fileExtension) ||
                        jpegFile.getExtension().equals(fileExtension) ||
                        jpgFile.getExtension().equals(fileExtension)) {
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
            final String contentUrl = ((File)selectedResource).getContentUrl();
            Window.open(wsAgentURLModifier.modify(contentUrl), "_blank", null);
        }
    }

}
