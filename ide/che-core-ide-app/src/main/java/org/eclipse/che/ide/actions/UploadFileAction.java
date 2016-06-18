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

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.selection.SelectionAgent;
import org.eclipse.che.ide.upload.file.UploadFilePresenter;

import javax.validation.constraints.NotNull;
import java.util.Arrays;

import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Upload file Action
 *
 * @author Roman Nikitenko
 * @author Dmitry Shnurenko
 */
@Singleton
public class UploadFileAction extends AbstractPerspectiveAction {

    private final UploadFilePresenter  presenter;
    private final SelectionAgent       selectionAgent;

    @Inject
    public UploadFileAction(UploadFilePresenter presenter,
                            CoreLocalizationConstant locale,
                            SelectionAgent selectionAgent,
                            Resources resources) {
        super(Arrays.asList(PROJECT_PERSPECTIVE_ID), locale.uploadFileName(), locale.uploadFileDescription(), null, resources.uploadFile());
        this.presenter = presenter;
        this.selectionAgent = selectionAgent;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        presenter.showDialog();
    }

    /** {@inheritDoc} */
    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        event.getPresentation().setVisible(true);
        boolean enabled = false;
        Selection<?> selection = selectionAgent.getSelection();
        if (selection != null) {
            enabled = selection.getHeadElement() != null;
        }
        event.getPresentation().setEnabled(enabled);
    }
}
