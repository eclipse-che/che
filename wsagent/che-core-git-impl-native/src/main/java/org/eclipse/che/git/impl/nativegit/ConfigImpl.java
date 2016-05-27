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
package org.eclipse.che.git.impl.nativegit;


import org.eclipse.che.api.git.Config;
import org.eclipse.che.api.git.GitException;
import org.eclipse.che.git.impl.nativegit.commands.GetConfigCommand;
import org.eclipse.che.git.impl.nativegit.commands.SetConfigCommand;

import java.io.File;
import java.util.List;

/**
 * Config is useful for git repository configuration manipulation.
 * For now it is available to load and save information about git repository user.
 *
 * @author Eugene Voevodin
 */
public class ConfigImpl extends Config {
    /**
     * @param repository
     *         git repository
     */
    ConfigImpl(File repository) throws GitException {
        super(repository);
    }

    @Override
    public String get(String name) throws GitException {
        final GetConfigCommand command = new GetConfigCommand(repository).setValue(name);
        try {
            command.execute();
        } catch (GitException exception) {
            if (exception.getMessage().isEmpty()) {
                throw new GitException("Can not find property '" + name + "' in repository configuration");
            } else {
                throw exception;
            }
        }
        final List<String> output = command.getLines();
        if (output.isEmpty()) {
            return null;
        }
        return output.get(0);
    }

    @Override
    public List<String> getAll(String name) throws GitException {
        final GetConfigCommand command = new GetConfigCommand(repository).setValue(name).setGetAll(true);
        command.execute();
        return command.getLines();
    }

    @Override
    public List<String> getList() throws GitException {
        final GetConfigCommand command = new GetConfigCommand(repository).setGetList(true);
        command.execute();
        return command.getLines();
    }


    @Override
    public Config set(String name, String value) throws GitException {
        final SetConfigCommand command = new SetConfigCommand(repository);
        command.setValue(name, value);
        command.execute();
        return this;
    }

    @Override
    public Config add(String name, String value) throws GitException {
        final SetConfigCommand command = new SetConfigCommand(repository);
        command.addValue(name, value);
        command.execute();
        return this;
    }

    @Override
    public Config unset(String name) throws GitException {
        final SetConfigCommand command = new SetConfigCommand(repository);
        command.unsetValue(name);
        command.execute();
        return this;
    }
}
