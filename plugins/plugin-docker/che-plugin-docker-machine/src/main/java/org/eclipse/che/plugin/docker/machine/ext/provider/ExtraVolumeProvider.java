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

import com.google.inject.Inject;

import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Provides volume configuration of machine for any local directories
 * that a user may want to mount into any docker machine.
 *
 * {@code machine.server.extra.volume} property is optional
 * and contains semicolon separated extra volumes to mount, for instance:
 *
 * /path/on/host1:/path/in/container1;/path/on/host2:/path/in/container2
 *
 * @author Alexander Garagatyi
 * @author Anatolii Bazko
 */
@Singleton
public class ExtraVolumeProvider implements Provider<String> {
    @Inject(optional = true)
    @Named("machine.server.extra.volume")
    private String volume;

    @Override
    public String get() {
        return volume == null ? "" : volume;
    }
}
