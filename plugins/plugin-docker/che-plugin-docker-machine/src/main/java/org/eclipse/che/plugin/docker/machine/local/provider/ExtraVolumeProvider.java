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
package org.eclipse.che.plugin.docker.machine.local.provider;

import com.google.inject.Inject;

import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Provides volume configuration of machine for any local directories
 * that a user may want to mount into a dev machine.
 * <br/>machine.server.extra.volume property is optional and provided as
 * /path/on/host:/path/in/container
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class ExtraVolumeProvider implements Provider<String> {

    public static final String CHE_WORKSPACE_VOLUME = "CHE_WORKSPACE_VOLUME";

    @Inject(optional = true)
    @Named("machine.server.extra.volume")
    private String volume;

    @Inject(optional = true)
    @Named("che.workspace.volume")
    private String volumeNew;

    @Override
    public String get() {
    	// Logic:
    	//   If CHE_
    	String cheWorkspaceVolumeVar = System.getenv(CHE_WORKSPACE_VOLUME);
        if (cheWorkspaceVolumeVar != null && !cheWorkspaceVolumeVar.isEmpty()) {
        	return cheWorkspaceVolumeVar;
        } else if (volumeNew != null ){
        	return volumeNew;
        } else {
	        return volume == null ? "" : volume;
        }
    }
}
