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
package org.eclipse.che.plugin.docker.machine.ext.provider;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Provides volumes configuration of machine for local storage
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class LocalStorageDockerVolumePathProvider implements Provider<String> {
    @Inject
    @Named("che.conf.storage")
    private String localStoragePath;

    @Override
    public String get() {
        return localStoragePath + ":/local-storage";
    }
}
