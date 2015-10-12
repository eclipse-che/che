/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.deploy;

import org.eclipse.che.api.core.util.SystemInfo;

import javax.inject.Provider;

/**
 * Provider api.endpoint url. This is used to make calls to che api from containers.
 * It may depend if we use boot2docker or not.
 *
 * @author Sergii Kabashniuk
 */
public class ApiEndpointProvider implements Provider<String> {
    @Override
    public String get() {
        if (SystemInfo.isMacOS() || SystemInfo.isWindows()) {
            return "http://192.168.99.1:8080/che/api";
        }
        return "http://172.17.42.1:8080/che/api";
    }
}
