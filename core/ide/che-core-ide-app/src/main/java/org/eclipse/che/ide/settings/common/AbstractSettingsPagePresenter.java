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

import javax.validation.constraints.NotNull;

/**
 * The class which is common for all all page presenters which will be displayed in project settings window. To add properties view
 * to settings dialog need extend this presenter and add your created presenter to gin module using gin multi binder.
 *
 * @author Dmitry Shnurenko
 */
public abstract class AbstractSettingsPagePresenter implements SettingsPagePresenter {

    public static final String DEFAULT_CATEGORY = "Java Compiler";

    protected DirtyStateListener delegate;

    private final String        title;
    private final String        category;
    private final ImageResource icon;

    public AbstractSettingsPagePresenter(String title, String category, ImageResource icon) {
        this.title = title;
        this.category = category;
        this.icon = icon;
    }

    public AbstractSettingsPagePresenter(String title) {
        this(title, DEFAULT_CATEGORY, null);
    }

    /** {@inheritDoc} */
    @Override
    public void setUpdateDelegate(@NotNull DirtyStateListener delegate) {
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public String getTitle() {
        return title;
    }

    /** {@inheritDoc} */
    @Override
    public ImageResource getIcon() {
        return icon;
    }

    /** {@inheritDoc} */
    @Override
    public String getCategory() {
        return category;
    }
}
