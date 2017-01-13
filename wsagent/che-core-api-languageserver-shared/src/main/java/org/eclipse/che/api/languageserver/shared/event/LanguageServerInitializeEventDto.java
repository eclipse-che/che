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
package org.eclipse.che.api.languageserver.shared.event;

import org.eclipse.che.api.languageserver.shared.lsapi.LanguageDescriptionDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.ServerCapabilitiesDTO;
import org.eclipse.che.dto.shared.DTO;

/**
 * @author Anatoliy Bazko
 */
@DTO
public interface LanguageServerInitializeEventDto {

    String getProjectPath();

    void setProjectPath(String projectPath);

    LanguageDescriptionDTO getSupportedLanguages();

    void setSupportedLanguages(LanguageDescriptionDTO supportedLanguages);

    ServerCapabilitiesDTO getServerCapabilities();

    void setServerCapabilities(ServerCapabilitiesDTO serverCapabilities);
}
