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
package org.eclipse.che.ide.api.machine;

import org.eclipse.che.api.core.model.machine.Server;
import org.eclipse.che.api.machine.shared.dto.ServerDto;

/**
 * Describe development machine server instance.
 * {@link Server}
 *
 * @author Vitalii Parfonov
 */
public class DevMachineServer implements Server {


        private final String path;
        private final String address;
        private final String protocol;
        private final String ref;
        private final String url;

        public DevMachineServer(ServerDto dto) {
            path = dto.getPath();
            address = dto.getAddress();
            protocol = dto.getProtocol();
            ref = dto.getRef();
            url = dto.getUrl();
        }


        @Override
        public String getRef() {
            return ref;
        }

        @Override
        public String getAddress() {
            return address;
        }

        @Override
        public String getProtocol() {
            return protocol;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public String getUrl() {
            return url;
        }
}
