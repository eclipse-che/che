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
package org.eclipse.che.ide.api.preferences;

import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Abstract base implementation for all preference page implementations.
 * It's simpler to get started using Preferences.
 *
 * @author Andrey Plotnikov
 * @author Vlad Zhukovskyi
 */
public abstract class AbstractPreferencePagePresenter implements PreferencePagePresenter {

    public static String DEFAULT_CATEGORY = "IDE";

    protected DirtyStateListener delegate;

    private String title;

    private String category;

    private SVGResource icon;

    /**
     * Create preference page.
     *
     * @param title
     * @param category
     * @param icon
     */
    public AbstractPreferencePagePresenter(String title, String category, SVGResource icon) {
        this.title = title;
        this.category = category;
        this.icon = icon;
    }

    /**
     * Create preference page with a default category for grouping elements.
     *
     * @param title
     * @param icon
     */
    public AbstractPreferencePagePresenter(String title, SVGResource icon) {
        this(title, DEFAULT_CATEGORY, icon);
    }


    /**
     * Create preference page.
     *
     * @param title
     */
    public AbstractPreferencePagePresenter(String title) {
        this(title, DEFAULT_CATEGORY, null);
    }

    /** {@inheritDoc} */
    @Override
    public void setUpdateDelegate(DirtyStateListener delegate) {
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    @Override
    public String getTitle() {
        return title;
    }

    /** {@inheritDoc} */
    @Override
    public SVGResource getIcon() {
        return icon;
    }

    /** {@inheritDoc} */
    @Override
    public String getCategory() {
        return category;
    }
}
