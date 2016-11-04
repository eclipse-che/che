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

import org.eclipse.che.api.git.shared.AddRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Arguments holder for {@link org.eclipse.che.api.git.GitConnection#add(AddParams)}.
 *
 * @author Igor Vinokur
 */
public class AddParams {

    private List<String> filePattern;
    private boolean      isUpdate;

    private AddParams() {
    }

    /**
     * Create new {@link AddParams} instance.
     *
     * @param filePattern
     *         file pattern of files to add
     */
    public static AddParams create(List<String> filePattern) {
        return new AddParams().withFilePattern(filePattern);
    }

    /** Create new {@link AddParams} instance */
    public static AddParams create() {
        return new AddParams();
    }

    /** @see AddRequest#getFilePattern() */
    public List<String> getFilePattern() {
        return filePattern == null ? new ArrayList<>() : filePattern;
    }

    /** @see AddRequest#withFilePattern(List) */
    public AddParams withFilePattern(List<String> filePattern) {
        this.filePattern = filePattern;
        return this;
    }

    /** @see AddRequest#isUpdate() **/
    public boolean isUpdate() {
        return isUpdate;
    }

    /** @see AddRequest#withUpdate(boolean) **/
    public AddParams withUpdate(boolean isUpdate) {
        this.isUpdate = isUpdate;
        return this;
    }
}
