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
package org.eclipse.che.api.vfs.server.search;

/** @author andrew00x */
public class QueryExpression {
    private String name;
    private String path;
    private String mediaType;
    private String text;
    private int    skipCount;
    private int    maxItems;

    public String getPath() {
        return path;
    }

    public QueryExpression setPath(String path) {
        this.path = path;
        return this;
    }

    public String getName() {
        return name;
    }

    public QueryExpression setName(String name) {
        this.name = name;
        return this;
    }

    public String getMediaType() {
        return mediaType;
    }

    public QueryExpression setMediaType(String mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    public String getText() {
        return text;
    }

    public QueryExpression setText(String text) {
        this.text = text;
        return this;
    }

    public int getSkipCount() {
        return skipCount;
    }

    public QueryExpression setSkipCount(int skipCount) {
        this.skipCount = skipCount;
        return this;
    }

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
               "name='" + name + '\'' +
               ", path='" + path + '\'' +
               ", mediaType='" + mediaType + '\'' +
               ", text='" + text + '\'' +
               ", skipCount=" + skipCount +
               ", maxItems=" + maxItems +
               '}';
    }
}
