/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.tutorial.parts.part;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;
import com.google.inject.ImplementedBy;

/**
 * The view of {@link MyPartPresenter}.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
@ImplementedBy(MyPartViewImpl.class)
public interface MyPartView extends View<MyPartView.ActionDelegate> {
    /** Required for delegating functions in view. */
    public interface ActionDelegate extends BaseActionDelegate {
        /** Performs some actions in response to a user's clicking on Button */
        void onButtonClicked();
    }

    /**
     * Set title of my part.
     *
     * @param title
     *         title that need to be set
     */
    void setTitle(String title);
}