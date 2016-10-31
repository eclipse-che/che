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
package org.eclipse.che.api.git.params;

import org.eclipse.che.api.git.shared.RmRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Arguments holder for {@link org.eclipse.che.api.git.GitConnection#rm(RmParams)}.
 *
 * @author Igor Vinokur
 */
public class RmParams {

    private List<String> items;
    private boolean      cached;
    private boolean      isRecursively;

    private RmParams() {
    }

    /**
     * Create new {@link RmParams} instance.
     *
     * @param items
     *         files to remove
     */
    public static RmParams create(List<String> items) {
        return new RmParams().withItems(items);
    }

    /** @see RmRequest#getItems() */
    public List<String> getItems() {
        return items == null ? new ArrayList<>() : items;
    }

    /** @see RmRequest#withItems(List) */
    public RmParams withItems(List<String> items) {
        this.items = items;
        return this;
    }

    /** @see RmRequest#isCached() */
    public boolean isCached() {
        return cached;
    }

    /** @see RmRequest#withCached(boolean) */
    public RmParams withCached(boolean cached) {
        this.cached = cached;
        return this;
    }

    /** @see RmRequest#isRecursively() */
    public boolean isRecursively() {
        return isRecursively;
    }

    /** @see RmRequest#withRecursively(boolean) */
    public RmParams withRecursively(boolean recursively) {
        isRecursively = recursively;
        return this;
    }
}
