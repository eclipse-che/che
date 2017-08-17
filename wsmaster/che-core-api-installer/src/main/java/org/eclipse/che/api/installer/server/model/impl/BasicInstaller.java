/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.installer.server.model.impl;

import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.installer.shared.dto.InstallerDto;
import org.eclipse.che.api.installer.shared.model.Installer;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.dto.server.DtoFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

/**
 * Basic implementation of the {@link Installer}.
 *
 * It is supposed that installer descriptor and installer script are located
 * as resources in the jar.
 *
 * If resources aren't found then {@link Installer} won't be initialized.
 *
 * @author Anatolii Bazko
 */
public abstract class BasicInstaller implements Installer {
    private final Installer internal;

    public BasicInstaller(String installerDescriptor, String installerScript) throws IOException {
        internal = readInstallerDescriptor(installerDescriptor, installerScript);
    }

    @Override
    public String getId() {
        return internal.getId();
    }

    @Override
    public String getName() {
        return internal.getName();
    }

    @Override
    public String getVersion() {
        return internal.getVersion();
    }

    @Override
    public String getDescription() {
        return internal.getDescription();
    }

    @Override
    public List<String> getDependencies() {
        return unmodifiableList(internal.getDependencies());
    }

    @Override
    public String getScript() {
        return internal.getScript();
    }

    @Override
    public Map<String, String> getProperties() {
        return unmodifiableMap(internal.getProperties());
    }

    @Override
    public Map<String, ? extends ServerConfig> getServers() {
        return unmodifiableMap(internal.getServers());
    }

    private Installer readInstallerDescriptor(String installerDescriptor, String installerScript) throws IOException {
        InputStream inputStream = readResource(installerDescriptor);
        InstallerDto installer = DtoFactory.getInstance().createDtoFromJson(inputStream, InstallerDto.class);
        return installer.withScript(readInstallerScript(installerScript));
    }

    private String readInstallerScript(String installerScript) throws IOException {
        InputStream inputStream = readResource(installerScript);
        return IoUtil.readStream(inputStream);
    }

    private InputStream readResource(String resource) throws IOException {
        InputStream inputStream = getClass().getResourceAsStream("/" + resource);
        if (inputStream == null) {
            throw new IOException(format("Can't initialize installer. Resource %s not found", resource));
        }
        return inputStream;
    }
}
