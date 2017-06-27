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
package org.eclipse.che.api.installer.server;

import org.eclipse.che.api.installer.shared.dto.InstallerDto;
import org.eclipse.che.api.installer.shared.dto.InstallerKeyDto;
import org.eclipse.che.api.installer.shared.model.Installer;
import org.eclipse.che.api.installer.shared.model.InstallerKey;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * @author Anatolii Bazko
 */
public class DtoConverter {

    public static InstallerDto asDto(Installer installer) {
        return newDto(InstallerDto.class).withId(installer.getId())
                                         .withName(installer.getName())
                                         .withVersion(installer.getVersion())
                                         .withDescription(installer.getDescription())
                                         .withProperties(installer.getProperties())
                                         .withScript(installer.getScript())
                                         .withDependencies(installer.getDependencies());
    }

    public static InstallerKeyDto asDto(InstallerKey key) {
        return newDto(InstallerKeyDto.class).withName(key.getId()).withVersion(key.getVersion());
    }

    private DtoConverter() { }
}
