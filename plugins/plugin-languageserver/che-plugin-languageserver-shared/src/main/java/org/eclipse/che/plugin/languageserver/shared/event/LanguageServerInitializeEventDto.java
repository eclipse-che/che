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
package org.eclipse.che.plugin.languageserver.shared.event;

import org.eclipse.che.dto.shared.DTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.LanguageDescriptionDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.ServerCapabilitiesDTO;

/**
 * @author Anatoliy Bazko
 */
@DTO
public interface LanguageServerInitializeEventDto {

    LanguageDescriptionDTO getSupportedLanguages();

    void setSupportedLanguages(LanguageDescriptionDTO supportedLanguages);

    ServerCapabilitiesDTO getServerCapabilities();

    void setServerCapabilities(ServerCapabilitiesDTO serverCapabilities);
}
