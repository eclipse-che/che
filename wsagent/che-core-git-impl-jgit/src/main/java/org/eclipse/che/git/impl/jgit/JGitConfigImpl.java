/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *   SAP           - implementation
 *******************************************************************************/
package org.eclipse.che.git.impl.jgit;

import org.eclipse.che.api.git.Config;
import org.eclipse.che.api.git.exception.GitException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static java.lang.System.lineSeparator;

/**
 * JGit implementation for Che's git configuration.
 * 
 * @author Tareq Sharafy (tareq.sha@sap.com)
 */
class JGitConfigImpl extends Config {

    private final String CONFIGURATION_NOT_FOUND_MESSAGE = "Can not find property '%s' in Git configuration settings.";

    private final Repository repository;

    JGitConfigImpl(Repository repository) throws GitException {
        super(repository.getDirectory());
        this.repository = repository;
    }

    @Override
    public String get(String name) throws GitException {
        ConfigKey key = parseName(name);
        String value = repository.getConfig().getString(key.section, key.subsection, key.name);
        if (value == null) {
            throw new GitException(format(CONFIGURATION_NOT_FOUND_MESSAGE, name));
        }
        return value;
    }

    @Override
    public List<String> getAll(String name) throws GitException {
        ConfigKey key = parseName(name);
        String[] values = repository.getConfig().getStringList(key.section, key.subsection, key.name);
        if (values == null || values.length == 0) {
            throw new GitException(format(CONFIGURATION_NOT_FOUND_MESSAGE, name));
        }
        return Arrays.asList(values);
    }

    @Override
    public List<String> getList() throws GitException {
        List<String> results = new ArrayList<>();
        // Iterate all sections and subsections, printing all values
        StoredConfig config = repository.getConfig();
        for (String section : config.getSections()) {
            for (String subsection : config.getSubsections(section)) {
                Set<String> names = config.getNames(section, subsection);
                addConfigValues(section, subsection, names, results);
            }
            Set<String> names = config.getNames(section);
            addConfigValues(section, null, names, results);
        }
        return results;
    }

    private void addConfigValues(String section, String subsection, Set<String> names, List<String> output) {
        StringBuilder builder = new StringBuilder();
        for (String name : names) {
            builder.setLength(0);
            builder.append(section);
            builder.append('.');
            if (subsection != null) {
                builder.append(subsection);
                builder.append('.');
            }
            builder.append(name);
            builder.append('=');
            String firstPart = builder.toString();
            String[] values = repository.getConfig().getStringList(section, subsection, name);
            for (String value : values) {
                output.add(firstPart + value);
            }
        }
    }

    @Override
    public Config set(String name, String value) throws GitException {
        ConfigKey key = parseName(name);
        repository.getConfig().setString(key.section, key.subsection, key.name, value);
        try {
            this.repository.getConfig().save();
        } catch (IOException e) {
            throw new GitException(e.getMessage(), e);
        }
        return this;
    }

    @Override
    public Config add(String name, String value) throws GitException {
        return set(name, value);
    }

    @Override
    public Config unset(String name) throws GitException {
        ConfigKey key = parseName(name);
        repository.getConfig().unset(key.section, key.subsection, key.name);
        try {
            this.repository.getConfig().save();
        } catch (IOException e) {
            throw new GitException(e.getMessage(), e);
        }
        return this;
    }

    private static class ConfigKey {
        String section;
        String subsection;
        String name;
    }

    private static ConfigKey parseName(String name) throws GitException {
        ConfigKey key = new ConfigKey();
        // Split the qualified name
        String[] parts = name.split("\\.");
        switch (parts.length) {
        case 1:
            throw new GitException("error: key does not contain a section: " + name + lineSeparator());
        case 2:
            key.section = parts[0];
            key.name = parts[1];
            break;
        case 3:
            key.section = parts[0];
            key.subsection = parts[1];
            key.name = parts[2];
            break;
        default:
            throw new GitException("Invalid configuration key " + name);
        }
        return key;
    }

}
