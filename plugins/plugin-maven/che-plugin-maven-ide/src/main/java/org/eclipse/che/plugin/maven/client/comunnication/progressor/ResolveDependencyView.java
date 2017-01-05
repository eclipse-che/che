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
package org.eclipse.che.plugin.maven.client.comunnication.progressor;

import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.mvp.View;

/**
 * View of {@link ResolveDependencyPresenter}.
 *
 * @author Valeriy Svydenko
 */
@ImplementedBy(ResolveDependencyViewImpl.class)
public interface ResolveDependencyView extends View<ResolveDependencyView.ActionDelegate> {

    /** Shows the widget. */
    void show();

    /** Hides the widget. */
    void hide();

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

    interface ActionDelegate {

    }
}
