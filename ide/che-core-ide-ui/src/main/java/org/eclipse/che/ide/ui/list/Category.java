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
package org.eclipse.che.ide.ui.list;


import com.google.gwt.dom.client.Element;

import java.util.Collection;

/**
 * @author Evgen Vidolob
 */
public class Category<T> {

    private String                   title;
    private CategoryRenderer<T>      renderer;
    private Collection<T>            data;
    private CategoryEventDelegate<T> eventDelegate;

    public Category(String title, CategoryRenderer<T> renderer, Collection<T> data, CategoryEventDelegate<T> eventDelegate) {
        this.title = title;
        this.renderer = renderer;
        this.data = data;
        this.eventDelegate = eventDelegate;
    }

    public String getTitle() {
        return title;
    }

    public CategoryRenderer<T> getRenderer() {
        return renderer;
    }

    public Collection<T> getData() {
        return data;
    }


    public CategoryEventDelegate<T> getEventDelegate() {
        return eventDelegate;
    }

    /** Receives events fired on items in the category. */
    public interface CategoryEventDelegate<M> {
        void onListItemClicked(Element listItemBase, M itemData);
    }
}
