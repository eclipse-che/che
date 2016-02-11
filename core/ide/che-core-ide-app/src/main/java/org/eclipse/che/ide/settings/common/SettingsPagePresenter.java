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
package org.eclipse.che.ide.settings.common;

import com.google.gwt.resources.client.ImageResource;

import org.eclipse.che.ide.api.mvp.Presenter;

import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Provides methods which allow control and setup displaying of properties in settings dialog.
 *
 * @author Dmitry Shnurenko
 */
public interface SettingsPagePresenter extends Presenter {

    /**
     * Sets special delegate which handles actions when user change properties' values.
     *
     * @param delegate
     *         delegate which need set
     */
    void setUpdateDelegate(@NotNull DirtyStateListener delegate);

    /** Returns string representation of category.Method can return null. */
    @Nullable
    String getCategory();

    /** Returns title of current category. */
    @NotNull
    String getTitle();

    /** Returns icon which set to category. Method can return null. */
    @Nullable
    ImageResource getIcon();

    /** Returns <code>true</code> if we have unsaved changed, otherwise <code>false</code> will return. */
    boolean isDirty();

    /** Store changed properties values. */
    void storeChanges();

    /** Revert changed properties values. */
    void revertChanges();

    interface DirtyStateListener {
        /** Performs some actions when user set property value. */
        void onDirtyChanged();
    }
}
