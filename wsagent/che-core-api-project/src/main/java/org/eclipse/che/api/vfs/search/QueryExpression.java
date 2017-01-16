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
package org.eclipse.che.api.vfs.search;

/** Container for parameters of query that executed by Searcher.*/
public class QueryExpression {
    private String name;
    private String path;
    private String text;
    private int    skipCount;
    private int    maxItems;

    /** Optional file path parameter. Only file with the specified path or children are included in result. */
    public String getPath() {
        return path;
    }

    public QueryExpression setPath(String path) {
        this.path = path;
        return this;
    }

    /** Optional file name parameter. Only files that matched to specified name template are included in result. */
    public String getName() {
        return name;
    }

    public QueryExpression setName(String name) {
        this.name = name;
        return this;
    }

    /** Text for searching. */
    public String getText() {
        return text;
    }

    public QueryExpression setText(String text) {
        this.text = text;
        return this;
    }

    /** Number of items in search result that should be skipped. This parameter used for paging through large set of search result. */
    public int getSkipCount() {
        return skipCount;
    }

    public QueryExpression setSkipCount(int skipCount) {
        this.skipCount = skipCount;
        return this;
    }

    /** Max number of results that might be returned after executing this query. */
    public int getMaxItems() {
        return maxItems;
    }

    public QueryExpression setMaxItems(int maxItems) {
        this.maxItems = maxItems;
        return this;
    }

    @Override
    public String toString() {
        return "QueryExpression{" +
               "text='" + text + '\'' +
               ", name='" + name + '\'' +
               ", path='" + path + '\'' +
               ", skipCount=" + skipCount +
               ", maxItems=" + maxItems +
               '}';
    }
}
