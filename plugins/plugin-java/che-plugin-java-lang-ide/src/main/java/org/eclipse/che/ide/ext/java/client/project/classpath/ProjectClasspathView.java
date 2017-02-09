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
package org.eclipse.che.ide.ext.java.client.project.classpath;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.pages.ClasspathPagePresenter;

import java.util.Map;
import java.util.Set;

/**
 * The view of {@link ProjectClasspathPresenter}.
 *
 * @author Valeriy Svydenko
 */
@ImplementedBy(ProjectClasspathViewImpl.class)
public interface ProjectClasspathView extends View<ProjectClasspathView.ActionDelegate> {

    /** Show view. */
    void show();

    /** Close view. */
    void hideWindow();

    /** Returns the component used for configurations display. */
    AcceptsOneWidget getConfigurationsContainer();

    /** Sets the focus on the 'Close' button. */
    void setDoneButtonInFocus();

    /** Returns {@code true} if close button is in the focus and {@code false} - otherwise. */
    boolean isDoneButtonInFocus();

    /** Sets all pages of classpath configuration */
    void setPages(Map<String, Set<ClasspathPagePresenter>> properties);

    /**
     * Selects classpath configuration page
     *
     * @param property
     *         chosen page
     */
    void selectPage(ClasspathPagePresenter property);

    /** Action handler for the view actions/controls. */
    interface ActionDelegate {

        /** Called when 'Done' button is clicked. */
        void onDoneClicked();

        /** Called when window is closing. */
        void onCloseClicked();

        /** Performs any actions appropriate in response to the user having clicked the Enter key. */
        void onEnterClicked();

        /** Clears all information about previous state. */
        void clearData();

        /** Performs any actions appropriate in response to the user having clicked on the configuration. */
        void onConfigurationSelected(ClasspathPagePresenter property);
    }
}
