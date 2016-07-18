/*
 * *****************************************************************************
 *  Copyright (c) 2012-2016 Codenvy, S.A.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *    Codenvy, S.A. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.plugin.languageserver.shared.model.impl;

import io.typefox.lsapi.LanguageDescription;

import org.eclipse.che.plugin.languageserver.shared.model.ServerConfiguration;

import java.util.List;
import java.util.Objects;

/**
 * @author Anatoliy Bazko
 */
public class ServerConfigurationImpl implements ServerConfiguration {
    private final LanguageDescription languageDescription;
    private final String              license;
    private final List<String>        installScript;

    public ServerConfigurationImpl(LanguageDescription languageDescription, String license, List<String> installScript) {
        this.languageDescription = languageDescription;
        this.license = license;
        this.installScript = installScript;
    }

    @Override
    public LanguageDescription getLanguageDescription() {
        return languageDescription;
    }


    @Override
    public List<String> getInstallScript() {
        return installScript;
    }

    @Override
    public String getLicense() {
        return license;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerConfigurationImpl)) return false;
        ServerConfigurationImpl that = (ServerConfigurationImpl)o;
        return Objects.equals(languageDescription, that.languageDescription) &&
               Objects.equals(license, that.license) &&
               Objects.equals(installScript, that.installScript);
    }

    @Override
    public int hashCode() {
        return Objects.hash(languageDescription, license, installScript);
    }
}
