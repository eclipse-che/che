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
package org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.pages;

import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Abstract base implementation for all java project properties page implementations.
 * It's simpler to get started using Properties.
 *
 * @author Valeriy Svydenko
 */
public abstract class AbstractClasspathPagePresenter implements ClasspathPagePresenter {

    private String      title;
    private String      category;
    private SVGResource icon;

    protected DirtyStateListener delegate;

    public AbstractClasspathPagePresenter(String title, String category, SVGResource icon) {
        this.title = title;
        this.category = category;
        this.icon = icon;
    }

    @Override
    public void setUpdateDelegate(DirtyStateListener delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public SVGResource getIcon() {
        return icon;
    }

}
