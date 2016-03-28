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
package org.eclipse.che.ide.newresource;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.project.node.ResourceBasedNode;
import org.eclipse.che.ide.ui.dialogs.InputCallback;
import org.eclipse.che.ide.ui.dialogs.input.InputDialog;

/**
 * Action to create new folder.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class NewFolderAction extends AbstractNewResourceAction {
    private final CoreLocalizationConstant localizationConstant;
    private final AppContext               appContext;
    
    @Inject
    public NewFolderAction(CoreLocalizationConstant localizationConstant, Resources resources, AppContext appContext) {
        super(localizationConstant.actionNewFolderTitle(),
              localizationConstant.actionNewFolderDescription(),
              resources.defaultFolder());
        this.localizationConstant = localizationConstant;
        this.appContext = appContext;
    }

    @Override
    public void actionPerformed(ActionEvent e) {


        InputDialog inputDialog = dialogFactory.createInputDialog(
                localizationConstant.newResourceTitle(localizationConstant.actionNewFolderTitle()),
                localizationConstant.newResourceLabel(localizationConstant.actionNewFolderTitle().toLowerCase()),
                new InputCallback() {
                    @Override
                    public void accepted(String value) {
                        onAccepted(value);
                    }
                }, null).withValidator(folderNameValidator);
        inputDialog.show();
    }

    private void onAccepted(String value) {
        final ResourceBasedNode<?> parent = getResourceBasedNode();

        if (parent == null) {
            throw new IllegalStateException("No selected parent.");
        }

        final String folderPath = ((HasStorablePath)parent).getStorablePath() + '/' + value;

        projectServiceClient.createFolder(appContext.getWorkspace().getId(), folderPath, createCallback(parent));
    }
}
