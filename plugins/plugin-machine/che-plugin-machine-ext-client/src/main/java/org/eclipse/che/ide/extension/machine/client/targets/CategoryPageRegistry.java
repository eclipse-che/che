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
package org.eclipse.che.ide.extension.machine.client.targets;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.util.loging.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Registry for pages.
 *
 * @author Oleksii Orel
 */
@Singleton
public class CategoryPageRegistry {
    //CategoryPage Map with page category as a key
    private final Map<String, CategoryPage> categoryPages;

    public CategoryPageRegistry() {
        this.categoryPages = new HashMap<>();
    }

    @Inject(optional = true)
    private void register(Set<CategoryPage> categoryPages) {
        for (CategoryPage page : categoryPages) {
            final String category = page.getCategory();
            if (this.categoryPages.containsKey(category)) {
                Log.warn(this.getClass(), "Category page with category '" + category + "' is already registered.");
            } else {
                this.categoryPages.put(category, page);
            }
        }
    }

    /**
     * Returns CategoryPage for the specified page category or {@code null} if none.
     *
     * @param category
     *         the category of using page
     * @return categoryPage or {@code null}
     */
    @Nullable
    public CategoryPage getCategoryPage(String category) {
        return categoryPages.get(category);
    }
}
