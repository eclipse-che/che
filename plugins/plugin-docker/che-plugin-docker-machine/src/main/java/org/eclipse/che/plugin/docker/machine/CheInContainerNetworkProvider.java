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
package org.eclipse.che.plugin.docker.machine;

import org.eclipse.che.commons.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.Set;

/**
 * Provides network that all containers of workspaces in Che should join to properly communicate with Che server.
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class CheInContainerNetworkProvider implements Provider<Set<String>> {

    private Set<String> networks;

    @Inject
    public CheInContainerNetworkProvider(@Nullable @Named("che.docker.network") String cheMasterNetwork) {
        if (cheMasterNetwork == null) {
            networks = Collections.emptySet();
        } else {
            networks = Collections.singleton(cheMasterNetwork);
        }
    }

    @Override
    public Set<String> get() {
        return networks;
    }
}
