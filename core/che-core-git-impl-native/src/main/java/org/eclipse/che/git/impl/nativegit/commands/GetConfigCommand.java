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
package org.eclipse.che.git.impl.nativegit.commands;

import org.eclipse.che.api.git.GitException;

import java.io.File;

/**
 * Get git configuration.
 *
 * @author Eugene Voevodin
 */
public class GetConfigCommand extends GitCommand<Void> {

    private String  name;
    private boolean getList;
    private boolean getAll;

    public GetConfigCommand(File place) {
        super(place);
    }

    /** @see GitCommand#execute() */
    @Override
    public Void execute() throws GitException {
        reset();
        commandLine.add("config");
        if (getList) {
            commandLine.add("--list");
        } else {
            if (name == null) {
                throw new GitException("Nothing to get, name wasn't set.");
            }
            if (getAll) {
                commandLine.add("--get-all", name);
            } else {
                commandLine.add("--get", name);
            }
        }
        start();
        return null;
    }

    /**
     * @param name
     *         what to get from config
     * @return GetConfigCommand with established name
     */
    public GetConfigCommand setValue(String name) {
        this.name = name;
        return this;
    }

    /**
     * @param getList
     *         if <code>true</code> all config will be selected
     * @return GetConfigCommand with established getList parameter
     */
    public GetConfigCommand setGetList(boolean getList) {
        this.getList = getList;
        return this;
    }

    /**
     * @param getAll
     *         if <code>true</code> all values matched to #name
     * @return GetConfigCommand with established getAll parameter
     */
    public GetConfigCommand setGetAll(boolean getAll) {
        this.getAll = getAll;
        return this;
    }
}
