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
package org.eclipse.che.ide.api.debug;

import com.google.gwt.user.client.ui.AcceptsOneWidget;

import org.eclipse.che.ide.api.mvp.Presenter;

import javax.validation.constraints.NotNull;

/**
 * Page allows to edit debug configuration.
 *
 * @param <T>
 *         type of the debug configuration which this page should edit
 * @author Artem Zatsarynnyi
 */
public interface DebugConfigurationPage<T extends DebugConfiguration> extends Presenter {

    /**
     * Resets the page from the given {@code configuration}
     * which this page should edit.
     * <p/>
     * This method is called every time when user selects
     * an appropriate debug configuration in 'Debug Configurations'
     * dialog and before actual displaying this page.
     */
    void resetFrom(@NotNull T configuration);

    /**
     * This method is called every time when user selects an appropriate
     * debug configuration in 'Debug Configurations' dialog.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    void go(final AcceptsOneWidget container);

    /**
     * Returns whether this page is changed or not.
     *
     * @return {@code true} if page is changed, and {@code false} - otherwise
     */
    boolean isDirty();

    /**
     * Sets {@link DirtyStateListener} that should be called
     * every time when any modifications on the page has been performed.
     */
    void setDirtyStateListener(DirtyStateListener listener);

    /** Listener that should be called when any modifications on page. */
    interface DirtyStateListener {
        void onDirtyStateChanged();
    }
}
