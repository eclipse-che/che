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
package org.eclipse.che.ide.ext.java.client.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.JavaUtils;
import org.eclipse.che.ide.ext.java.client.project.node.PackageNode;
import org.eclipse.che.ide.ext.java.client.project.node.SourceFolderNode;
import org.eclipse.che.ide.json.JsonHelper;
import org.eclipse.che.ide.newresource.AbstractNewResourceAction;
import org.eclipse.che.ide.project.node.FolderReferenceNode;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.ui.dialogs.InputCallback;
import org.eclipse.che.ide.ui.dialogs.input.InputDialog;
import org.eclipse.che.ide.ui.dialogs.input.InputValidator;

import javax.validation.constraints.NotNull;

/**
 * Action to create new Java package.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class NewPackageAction extends AbstractNewResourceAction {

    private static final String MAVEN = "maven";

    private final AppContext appContext;
    private final InputValidator nameValidator = new NameValidator();

    @Inject
    public NewPackageAction(JavaResources javaResources,
                            JavaLocalizationConstant localizationConstant,
                            AppContext appContex) {
        super(localizationConstant.actionNewPackageTitle(),
              localizationConstant.actionNewPackageDescription(),
              javaResources.packageItem());
        this.appContext = appContex;
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent e) {
        CurrentProject project = appContext.getCurrentProject();
        if (project == null || !MAVEN.equals(project.getRootProject().getType())) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        Selection<?> selection = projectExplorer.getSelection();
        if (selection == null) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        e.getPresentation().setVisible(true);
        e.getPresentation().setEnabled(selection.isSingleSelection() &&
                (selection.getHeadElement() instanceof SourceFolderNode || selection.getHeadElement() instanceof PackageNode));
    }

    @Override
    public void actionPerformed(ActionEvent e) {


        InputDialog inputDialog = dialogFactory.createInputDialog("New " + title, "Name:", new InputCallback() {
            @Override
            public void accepted(String value) {
                onAccepted(value);
            }
        }, null).withValidator(nameValidator);
        inputDialog.show();
    }

    private void onAccepted(String value) {
        final FolderReferenceNode parent = (FolderReferenceNode)getResourceBasedNode();
        if (parent == null) {
            throw new IllegalStateException("No selected parent.");
        }

        final String path = parent.getStorablePath() + '/' + value.replace('.', '/');

        projectServiceClient.createFolder(appContext.getDevMachine(), path, createCallback());
    }

    protected AsyncRequestCallback<ItemReference> createCallback() {
        return new AsyncRequestCallback<ItemReference>(dtoUnmarshallerFactory.newUnmarshaller(ItemReference.class)) {
            @Override
            protected void onSuccess(final ItemReference itemReference) {
                projectExplorer.getNodeByPath(new HasStorablePath.StorablePath(itemReference.getPath()), true).then(selectNode());
            }

            @Override
            protected void onFailure(Throwable exception) {
                String message = JsonHelper.parseJsonMessage(exception.getMessage());
                dialogFactory.createMessageDialog("New package",
                                                  message.contains("already exists") ? "Package already exists." : message,
                                                  null).show();
            }
        };
    }

    private class NameValidator implements InputValidator {
        @Nullable
        @Override
        public Violation validate(String value) {
            try {
                JavaUtils.checkPackageName(value);
            } catch (final IllegalStateException e) {
                return new Violation() {
                    @Nullable
                    @Override
                    public String getMessage() {
                        String errorMessage = e.getMessage();
                        if (errorMessage == null || errorMessage.isEmpty()) {
                            return coreLocalizationConstant.invalidName();
                        }
                        return errorMessage;
                    }

                    @Nullable
                    @Override
                    public String getCorrectedValue() {
                        return null;
                    }
                };
            }
            return null;
        }
    }
}
