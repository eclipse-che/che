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
package org.eclipse.che.plugin.maven.client.comunnication.progressor.background;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.ImplementedBy;

/**
 * View of {@link BackgroundLoaderPresenter}.
 *
 * @author Valeriy Svydenko
 */
@ImplementedBy(BackgroundLoaderViewImpl.class)
public interface BackgroundLoaderView extends IsWidget {
    interface ActionDelegate {
        /**
         * Performs any actions appropriate in response to the user having clicked the status label.
         * Show more detailed information about the process of resolving.
         */
        void showResolverInfo();
    }

    /** Sets the delegate to receive events from this view. */
    void setDelegate(ActionDelegate delegate);

    /** Hides the loader. */
    void hide();

    /** Shows the loader. */
    void show();

    /**
     * Set label into loader which describes current state of loader.
     *
     * @param text
     *         message of the status
     */
    void setOperationLabel(String text);

    /**
     * Change the value of resolved modules of the project.
     *
     * @param percentage
     *         value of resolved modules
     */
    void updateProgressBar(int percentage);

}
