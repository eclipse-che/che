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

package org.eclipse.che.api.installer.server.impl;
import com.google.inject.Singleton;

import org.eclipse.che.api.installer.server.InstallerRegistry;
import org.eclipse.che.api.installer.server.InstallerRegistryService;
import org.eclipse.che.api.installer.server.DtoConverter;
import org.eclipse.che.api.installer.server.exception.InstallerException;
import org.eclipse.che.api.installer.shared.dto.InstallerDto;
import org.eclipse.che.api.installer.shared.model.Installer;
import org.eclipse.che.api.installer.shared.model.InstallerKey;
import org.eclipse.che.api.core.rest.HttpRequestHelper;
import org.eclipse.che.commons.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * Composite registry to use by interesting parties inside "master"
 * It is supposed that it can me configured to use remote registry
 * in this case "registry.installer.remote" property should point to the valid
 * Installer Registry REST service, otherwise LocalInstallerregistry will be used
 *
 * @author gazarenkov
 */
@Singleton
// TODO implement and test it properly
public class InstallerRegistryFacade implements InstallerRegistry {

    private final InstallerRegistryService remote;
    private final LocalInstallerRegistry   local;

    @Inject
    @Nullable
    @Named("registry.installer.remote") String remoteUrl;

    @Inject
    public InstallerRegistryFacade(InstallerRegistryService remote, LocalInstallerRegistry local) {
        this.remote = remote;
        this.local = local;
    }

    @Override
    public Installer getInstaller(InstallerKey installerKey) throws InstallerException {
        if(isRemote()) {
            try {
                return HttpRequestHelper.createJsonRequest(remoteUrl).useGetMethod().
                        setBody(DtoConverter.asDto(installerKey)).request().asDto(InstallerDto.class);
            } catch (Exception e) {
                throw new InstallerException(e.getMessage());
            }
        } else {
            return local.getInstaller(installerKey);
        }
    }

    @Override
    public List<String> getVersions(String id) throws InstallerException {
        //TODO same as getInstaller
        return null;
    }

    @Override
    public Collection<Installer> getInstallers() throws InstallerException {
        ///TODO same as getInstaller
        return null;
    }

    @Override
    public List<Installer> getOrderedInstallers(List<InstallerKey> keys) {
        //TODO same as getInstaller
        return null;
    }

    private boolean isRemote() {
        return remoteUrl != null;
    }
}
