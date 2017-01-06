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
package org.eclipse.che.ide.projecttype.wizard.presenter;

import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.mvp.Presenter;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode;

/**
 * @author Evgen Vidolob
 * @author Oleksii Orel
 */
@ImplementedBy(ProjectWizardViewImpl.class)
public interface ProjectWizardView extends View<ProjectWizardView.ActionDelegate> {

    void showPage(Presenter presenter);

    void showDialog(ProjectWizardMode wizardMode);

    void setLoaderVisibility(boolean visible);

    /** The method just close the dialog. For example after save button clicked. */
    void close();

    void setNextButtonEnabled(boolean enabled);

    void setFinishButtonEnabled(boolean enabled);

    void setPreviousButtonEnabled(boolean enabled);

    interface ActionDelegate {
        /** Performs any actions appropriate in response to the user having pressed the Next button */
        void onNextClicked();

        /** Performs any actions appropriate in response to the user having pressed the Back button */
        void onBackClicked();

        /** Performs any actions appropriate in response to the user having pressed the Create button */
        void onSaveClicked();

        /** Performs any actions appropriate in response to the user having pressed the Cancel button */
        void onCancelClicked();
    }
}
