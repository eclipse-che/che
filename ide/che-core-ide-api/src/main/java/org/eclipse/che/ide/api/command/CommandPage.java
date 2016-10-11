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
package org.eclipse.che.ide.api.command;

import com.google.gwt.user.client.ui.AcceptsOneWidget;

import org.eclipse.che.ide.api.mvp.Presenter;

/**
 * Page allows to edit command.
 *
 * @author Artem Zatsarynnyi
 * @author Valeriy Svydenko
 */
public interface CommandPage extends Presenter {

    /**
     * Resets the page with the given {@code command}.
     * <p>Typically, implementors should hold the given {@code command} and edit it directly.
     * <p>This method is called every time when user selects an appropriate
     * command in 'Commands' dialog but before the actual displaying of the page.
     */
    void resetFrom(CommandImpl command);

    /**
     * Called every time when user selects an appropriate command in 'Commands' dialog.
     * <p>Typically, should be used for initializing page.
     * <p>{@inheritDoc}
     */
    @Override
    void go(AcceptsOneWidget container);

    /** Called when currently edited command has been saved. */
    void onSave();

    /**
     * Whether the page has been changed or not?
     *
     * @return {@code true} if page is changed, and {@code false} - otherwise
     */
    boolean isDirty();

    /** Sets {@link DirtyStateListener}. */
    void setDirtyStateListener(DirtyStateListener listener);

    /** Sets {@link FieldStateActionDelegate} that should be operated all panels. */
    void setFieldStateActionDelegate(FieldStateActionDelegate delegate);

    interface FieldStateActionDelegate {
        /** Sets a state of the visibility for the Preview URL panel. */
        void updatePreviewURLState(boolean isVisible);
    }

    /** Listener that should be called every time when any modifications on the page has been performed. */
    interface DirtyStateListener {
        void onDirtyStateChanged();
    }
}
