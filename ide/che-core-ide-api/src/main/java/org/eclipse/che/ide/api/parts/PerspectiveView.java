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
package org.eclipse.che.ide.api.parts;

import com.google.gwt.user.client.ui.AcceptsOneWidget;

import org.eclipse.che.ide.api.mvp.View;

/**
 * Perspective View contains abstract containers for PartStack
 *
 * @author Nikolay Zamosenchuk
 */
public interface PerspectiveView<T> extends View<T> {
    /**
     * Returns central panel.
     *
     * @return
     */
    AcceptsOneWidget getEditorPanel();

    /**
     * Returns left panel.
     *
     * @return
     */
    AcceptsOneWidget getNavigationPanel();

    /**
     * Returns bottom panel.
     *
     * @return
     */
    AcceptsOneWidget getInformationPanel();

    /**
     * Returns right panel.
     *
     * @return
     */
    AcceptsOneWidget getToolPanel();

    /** Handle View events */
    interface ActionDelegate {

        void onResize(int width, int height);

    }

}
