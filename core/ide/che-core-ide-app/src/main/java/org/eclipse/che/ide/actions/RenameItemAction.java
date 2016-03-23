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
package org.eclipse.che.ide.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.project.node.resource.SupportRename;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.selection.SelectionAgent;
import org.eclipse.che.ide.project.node.FileReferenceNode;
import org.eclipse.che.ide.project.node.FolderReferenceNode;
import org.eclipse.che.ide.project.node.ModuleNode;
import org.eclipse.che.ide.project.node.ProjectNode;
import org.eclipse.che.ide.project.node.ResourceBasedNode;
import org.eclipse.che.ide.ui.dialogs.CancelCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.InputCallback;
import org.eclipse.che.ide.ui.dialogs.input.InputDialog;
import org.eclipse.che.ide.ui.dialogs.input.InputValidator;
import org.eclipse.che.ide.util.NameUtils;

import javax.validation.constraints.NotNull;
import java.util.Arrays;

import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Action for renaming an item which is selected in 'Project Explorer'.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class RenameItemAction extends AbstractPerspectiveAction {
    private final CoreLocalizationConstant localization;
    private final DialogFactory            dialogFactory;
    private final AppContext               appContext;
    private final SelectionAgent           selectionAgent;

    @Inject
    public RenameItemAction(Resources resources,
                                  SelectionAgent selectionAgent,
                            CoreLocalizationConstant localization,
                            DialogFactory dialogFactory,
                            AppContext appContext) {
        super(Arrays.asList(PROJECT_PERSPECTIVE_ID),
              localization.renameItemActionText(),
              localization.renameItemActionDescription(),
              null,
              resources.rename());
        this.selectionAgent = selectionAgent;
        this.localization = localization;
        this.dialogFactory = dialogFactory;
        this.appContext = appContext;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {


        final Selection<?> selection = selectionAgent.getSelection();
        if (selection == null || selection.isEmpty()) {
            return;
        }

        final ResourceBasedNode<?> selectedNode = (ResourceBasedNode<?>)selection.getHeadElement();

        if (selectedNode == null) {
            return;
        }

        renameNode(selectedNode);
    }

    /** {@inheritDoc} */
    @Override
    public void updateInPerspective(@NotNull ActionEvent e) {
        if ((appContext.getCurrentProject() == null && !appContext.getCurrentUser().isUserPermanent())) {
            e.getPresentation().setVisible(true);
            e.getPresentation().setEnabled(false);
            return;
        }

        final Selection<?> selection = selectionAgent.getSelection();

        if (selection == null || selection.isEmpty()) {
            e.getPresentation().setEnabled(false);
            return;
        }

        if (selection.isMultiSelection()) {
            //this is temporary commented
            e.getPresentation().setEnabled(false);
            return;
        }

        final Object possibleNode = selection.getHeadElement();

        boolean isModuleNode = possibleNode instanceof ModuleNode;
        boolean isSupportRename = possibleNode instanceof SupportRename;

        boolean enable = !isModuleNode && isSupportRename && ((SupportRename)possibleNode).getRenameProcessor() != null;

        e.getPresentation().setEnabled(enable);
    }

    /**
     * Asks the user for new name and renames the node.
     *
     * @param node
     *         node to rename
     */
    private void renameNode(final ResourceBasedNode<?> node) {
        final InputCallback inputCallback = new InputCallback() {
            @Override
            public void accepted(final String value) {
                //we shouldn't perform renaming file with the same name
                if (!value.trim().equals(node.getName())) {
                    node.rename(value);
                }
            }
        };

        askForNewName(node, inputCallback, null);
    }

    /**
     * Asks the user for new node name.
     *
     * @param node
     * @param inputCallback
     * @param cancelCallback
     */
    public void askForNewName(final ResourceBasedNode<?> node, final InputCallback inputCallback, final CancelCallback cancelCallback) {
        final int selectionLength = node.getName().indexOf('.') >= 0
                                    ? node.getName().lastIndexOf('.')
                                    : node.getName().length();

        InputDialog inputDialog = dialogFactory.createInputDialog(getDialogTitle(node),
                                                                  localization.renameDialogNewNameLabel(),
                                                                  node.getName(), 0, selectionLength, inputCallback, null);
        if (node instanceof FileReferenceNode) {
            inputDialog.withValidator(new FileNameValidator(node.getName()));
        } else if (node instanceof FolderReferenceNode) {
            inputDialog.withValidator(new FolderNameValidator(node.getName()));
        } else if (node instanceof ProjectNode) {
            inputDialog.withValidator(new ProjectNameValidator(node.getName()));
        }
        inputDialog.show();
    }

    private String getDialogTitle(ResourceBasedNode<?> node) {
        if (node instanceof FileReferenceNode) {
            return localization.renameFileDialogTitle(node.getName());
        } else if (node instanceof FolderReferenceNode) {
            return localization.renameFolderDialogTitle(node.getName());
        } else if (node instanceof ProjectNode) {
            return localization.renameProjectDialogTitle(node.getName());
        }
        return localization.renameNodeDialogTitle();
    }

    private abstract class AbstractNameValidator implements InputValidator {
        private final String selfName;

        public AbstractNameValidator(final String selfName) {
            this.selfName = selfName;
        }

        @Override
        public Violation validate(String value) {
            if (value.trim().equals(selfName)) {
                return new Violation() {
                    @Override
                    public String getMessage() {
                        return "";
                    }

                    @Override
                    public String getCorrectedValue() {
                        return null;
                    }
                };
            }

            return isValidName(value);
        }

        public abstract Violation isValidName(String value);
    }

    private class FileNameValidator extends AbstractNameValidator {

        public FileNameValidator(String selfName) {
            super(selfName);
        }

        @Override
        public Violation isValidName(String value) {
            if (!NameUtils.checkFileName(value)) {
                return new Violation() {
                    @Override
                    public String getMessage() {
                        return localization.invalidName();
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

    private class FolderNameValidator extends AbstractNameValidator {

        public FolderNameValidator(String selfName) {
            super(selfName);
        }

        @Override
        public Violation isValidName(String value) {
            if (!NameUtils.checkFolderName(value)) {
                return new Violation() {
                    @Override
                    public String getMessage() {
                        return localization.invalidName();
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

    private class ProjectNameValidator extends AbstractNameValidator {

        public ProjectNameValidator(String selfName) {
            super(selfName);
        }

        @Override
        public Violation isValidName(String value) {
            final String correctValue = value.contains(" ") ? value.replaceAll(" ", "-") : null;
            final String errormessage = !NameUtils.checkFileName(value) ? localization.invalidName() : null;
            if (correctValue != null || errormessage != null) {
                return new Violation() {
                    @Override
                    public String getMessage() {
                        return errormessage;
                    }

                    @Nullable
                    @Override
                    public String getCorrectedValue() {
                        return correctValue;
                    }
                };
            }
            return null;
        }
    }
}
