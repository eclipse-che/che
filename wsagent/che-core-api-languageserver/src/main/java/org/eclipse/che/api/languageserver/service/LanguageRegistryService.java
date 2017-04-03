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
package org.eclipse.che.api.languageserver.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.languageserver.DtoConverter;
import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.registry.LanguageServerDescription;
import org.eclipse.che.api.languageserver.registry.LanguageServerRegistry;
import org.eclipse.che.api.languageserver.registry.LanguageServerRegistryImpl;
import org.eclipse.che.api.languageserver.shared.lsapi.InitializeResultDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.LanguageDescriptionDTO;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.eclipse.che.api.languageserver.DtoConverter.asDto;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

@Singleton
@Path("languageserver")
public class LanguageRegistryService {

    private final LanguageServerRegistry registry;

    @Inject
    public LanguageRegistryService(LanguageServerRegistry registry) {
        this.registry = registry;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("supported")
    public List<LanguageDescriptionDTO> getSupportedLanguages() {
        return registry.getSupportedLanguages().stream().map(DtoConverter::asDto).collect(toList());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("registered")
    public List<InitializeResultDTO> getRegisteredLanguages() {
        List<InitializeResultDTO> result= new ArrayList<>();
        registry.getInitializedLanguages().entrySet().forEach(entry -> {
            List<LanguageServerDescription> serverDescriptions = entry.getValue();
            for (LanguageServerDescription serverDescription : serverDescriptions) {
                List<LanguageDescriptionDTO> languageDescriptionDTOs = Collections
                                .singletonList(asDto(serverDescription.getLanguageDescription()));

                InitializeResultDTO dto = newDto(InitializeResultDTO.class);
                dto.setProject(entry.getKey().substring(LanguageServerRegistryImpl.PROJECT_FOLDER_PATH.length()));
                dto.setSupportedLanguages(languageDescriptionDTOs);
                dto.setCapabilities(asDto(serverDescription.getInitializeResult().getCapabilities()));
                result.add(dto);
            }

        });
        return result;

    }

    @POST
    @Path("initialize")
    public void initialize(@QueryParam("path") String path) throws LanguageServerException {
        // in most cases starts new LS if not already started
        registry.findServer(TextDocumentService.prefixURI(path));
    }
}
