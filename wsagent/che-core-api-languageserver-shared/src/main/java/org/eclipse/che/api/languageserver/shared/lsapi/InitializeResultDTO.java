/*
 * *****************************************************************************
 *  Copyright (c) 2012-2016 Codenvy, S.A.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *    Codenvy, S.A. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.api.languageserver.shared.lsapi;

import io.typefox.lsapi.InitializeResult;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * @author Anatoliy Bazko
 */
@DTO
public interface InitializeResultDTO extends InitializeResult {

    String getProject();

    void setProject(String project);

    @Override
    ServerCapabilitiesDTO getCapabilities();

    void setCapabilities(ServerCapabilitiesDTO capabilities);

    List<LanguageDescriptionDTO> getSupportedLanguages();

    void setSupportedLanguages(List<LanguageDescriptionDTO> supportedLanguages);
}
