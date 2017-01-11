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
package org.eclipse.che.api.git.params;

import org.eclipse.che.api.git.shared.TagCreateRequest;

/**
 * Arguments holder for {@link org.eclipse.che.api.git.GitConnection#tagCreate(TagCreateParams)}.
 *
 * @author Igor Vinokur
 */
public class TagCreateParams {

    private String  name;
    private String  commit;
    private String  message;
    private boolean isForce;

    private TagCreateParams() {
    }

    /**
     * Create new {@link TagCreateParams} instance.
     *
     * @param name
     *         tag name
     */
    public static TagCreateParams create(String name) {
        return new TagCreateParams().withName(name);
    }

    /** @see TagCreateRequest#getName() */
    public String getName() {
        return name;
    }

    public TagCreateParams withName(String name) {
        this.name = name;
        return this;
    }

    /** @see TagCreateRequest#getCommit() */
    public String getCommit() {
        return commit;
    }

    public TagCreateParams withCommit(String commit) {
        this.commit = commit;
        return this;
    }

    /** @see TagCreateRequest#getMessage() */
    public String getMessage() {
        return message;
    }

    public TagCreateParams withMessage(String message) {
        this.message = message;
        return this;
    }

    /** @see TagCreateRequest#isForce() */
    public boolean isForce() {
        return isForce;
    }

    public TagCreateParams withForce(boolean force) {
        isForce = force;
        return this;
    }
}
