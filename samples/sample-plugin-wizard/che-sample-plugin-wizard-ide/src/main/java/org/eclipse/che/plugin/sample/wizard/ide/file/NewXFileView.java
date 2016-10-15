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
package org.eclipse.che.plugin.sample.wizard.ide.file;

import org.eclipse.che.ide.api.mvp.View;

/**
 * The view of {@link NewXFilePresenter}.
 *
 * * @author Vitalii Parfonov
 */
public interface NewXFileView extends View<NewXFileView.ActionDelegate> {

    String getName();

    String getHeader();

    /** Show dialog. */
    void showDialog();

    /** Close dialog. */
    void close();

    interface ActionDelegate {
        /** Performs any actions appropriate in response to the user having pressed the Ok button. */
        void onOkClicked();

        /** Performs any actions appropriate in response to the user having pressed the Cancel button. */
        void onCancelClicked();
    }
}