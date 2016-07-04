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
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.dialogs.InputCallback;
import org.eclipse.che.ide.api.dialogs.InputDialog;
import org.eclipse.che.ide.api.dialogs.InputValidator;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.util.NameUtils;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.api.resources.Resource.FILE;
import static org.eclipse.che.ide.api.resources.Resource.FOLDER;
import static org.eclipse.che.ide.api.resources.Resource.PROJECT;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Rename selected resource in the application context.
 *
 * @author Artem Zatsarynnyi
 * @author Vlad Zhukovskyi
 * @see AppContext#getResource()
 */
@Singleton
public class RenameItemAction extends AbstractPerspectiveAction {
    private final CoreLocalizationConstant localization;
    private final DialogFactory            dialogFactory;
    private final AppContext               appContext;

    @Inject
    public RenameItemAction(Resources resources,
                            CoreLocalizationConstant localization,
                            DialogFactory dialogFactory,
                            AppContext appContext) {
        super(singletonList(PROJECT_PERSPECTIVE_ID),
              localization.renameItemActionText(),
              localization.renameItemActionDescription(),
              null,
              resources.rename());
        this.localization = localization;
        this.dialogFactory = dialogFactory;
        this.appContext = appContext;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {


        final Resource resource = appContext.getResource();

        checkState(resource != null, "Null resource occurred");

        final String resourceName = resource.getName();
        final int selectionLength = resourceName.indexOf('.') >= 0 ? resourceName.lastIndexOf('.') : resourceName.length();
        final InputValidator validator;
        final String dialogTitle;

        if (resource.getResourceType() == FILE) {
            validator = new FileNameValidator(resourceName);
            dialogTitle = localization.renameFileDialogTitle(resourceName);
        } else if (resource.getResourceType() == FOLDER) {
            validator = new FolderNameValidator(resourceName);
            dialogTitle = localization.renameFolderDialogTitle(resourceName);
        } else if (resource.getResourceType() == PROJECT) {
            validator = new ProjectNameValidator(resourceName);
            dialogTitle = localization.renameProjectDialogTitle(resourceName);
        } else {
            throw new IllegalStateException("Not a resource");
        }

        final InputCallback inputCallback = new InputCallback() {
            @Override
            public void accepted(final String value) {
                //we shouldn't perform renaming file with the same name
                if (!value.trim().equals(resourceName)) {
                    final Path destination = resource.getLocation().parent().append(value);
                    resource.move(destination);
                }
            }
        };

        InputDialog inputDialog = dialogFactory.createInputDialog(dialogTitle,
                                                                  localization.renameDialogNewNameLabel(),
                                                                  resource.getName(), 0, selectionLength, inputCallback, null);
        inputDialog.withValidator(validator);
        inputDialog.show();
    }

    /** {@inheritDoc} */
    @Override
    public void updateInPerspective(@NotNull ActionEvent e) {
        final Resource[] resources = appContext.getResources();

        e.getPresentation().setVisible(true);
        e.getPresentation().setEnabled(resources != null && resources.length == 1);
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
