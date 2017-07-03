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

import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.registry.LanguageServerDescription;
import org.eclipse.che.api.languageserver.registry.LanguageServerRegistry;
import org.eclipse.che.api.languageserver.registry.LanguageServerRegistryImpl;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.ExtendedInitializeResultDto;
import org.eclipse.che.api.languageserver.shared.ProjectLangugageKey;
import org.eclipse.che.api.languageserver.shared.model.ExtendedInitializeResult;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

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
    public List<LanguageDescription> getSupportedLanguages() {
        return registry.getSupportedLanguages();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("registered")
    public List<ExtendedInitializeResultDto> getRegisteredLanguages() {
        return registry.getInitializedLanguages()
                       .entrySet()
                       .stream()
                       .map(entry -> {
                           ProjectLangugageKey projectExtensionKey = entry.getKey();
                           LanguageServerDescription serverDescription = entry.getValue();


                           ExtendedInitializeResult dto = new ExtendedInitializeResult();
                           dto.setProject(
                                   projectExtensionKey.getProject().substring(LanguageServerRegistryImpl.PROJECT_FOLDER_PATH.length()));
                           dto.setSupportedLanguages(Collections.singletonList(serverDescription.getLanguageDescription()));
                           dto.setCapabilities(serverDescription.getInitializeResult().getCapabilities());
                           return new DtoServerImpls.ExtendedInitializeResultDto(dto);
                       })
                       .collect(toList());

    }

    @POST
    @Path("initialize")
    public void initialize(@QueryParam("path") String path) throws LanguageServerException {
        //in most cases starts new LS if not already started
        registry.findServer(TextDocumentServiceUtils.prefixURI(path));
    }
}
