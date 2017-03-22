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
package org.eclipse.che.ide.command.toolbar.previews;

import java.util.Objects;

/** Holds preview URL and it's name displaying in a 'Previews' list. */
class PreviewUrl {

    private final String url;
    private final String displayName;

    PreviewUrl(String url, String displayName) {
        this.url = url;
        this.displayName = displayName;
    }

    public String getUrl() {
        return url;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PreviewUrl that = (PreviewUrl)o;
        return Objects.equals(url, that.url) &&
               Objects.equals(displayName, that.displayName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, displayName);
    }
}
