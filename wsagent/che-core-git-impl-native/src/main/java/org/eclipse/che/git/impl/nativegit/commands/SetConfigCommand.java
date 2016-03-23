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
import org.eclipse.che.commons.lang.Pair;


import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Set git configuration.
 *
 * @author Eugene Voevodin
 */
public class SetConfigCommand extends GitCommand<Void> {

    private List<Pair<String, String>> set;
    private List<Pair<String, String>> add;
    private List<String>               unset;

    public SetConfigCommand(File place) {
        super(place);
    }

    /** @see GitCommand#execute() */
    @Override
    public Void execute() throws GitException {
        reset();
        commandLine.add("config");
        if (set != null) {
            for (Pair<String, String> pair : set) {
                commandLine.add(pair.first, pair.second);
            }
        }
        if (add != null) {
            for (Pair<String, String> pair : add) {
                commandLine.add("--add", pair.first, pair.second);
            }
        }
        if (unset != null) {
            for (String name : unset) {
                commandLine.add("--unset", name);
            }
        }
        start();
        return null;
    }

    /**
     * @param name
     *         git config parameter such as user.name
     * @param value
     *         value that will used with parameter
     * @return SetConfigCommand with with established name and value
     */
    public SetConfigCommand setValue(String name, String value) {
        if (set == null) {
            set = new LinkedList<>();
        }
        set.add(Pair.of(name, value == null || value.isEmpty() ? "\"\"" : value));
        return this;
    }

    /**
     * Add multiple line in config.
     *
     * @param name
     *         git config parameter such as user.name
     * @param value
     *         value that will used with parameter
     * @return SetConfigCommand with with established name and value
     */
    public SetConfigCommand addValue(String name, String value) {
        if (add == null) {
            add = new LinkedList<>();
        }
        add.add(Pair.of(name, value == null || value.isEmpty() ? "\"\"" : value));
        return this;
    }

    /**
     * Remove configuration variable.
     *
     * @param name
     *         name of configuration variable
     * @return SetConfigCommand with with established name and value
     */
    public SetConfigCommand unsetValue(String name) {
        if (unset == null) {
            unset = new LinkedList<>();
        }
        unset.add(name);
        return this;
    }
}
