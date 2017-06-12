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
import org.eclipse.che.api.languageserver.registry.InitializedLanguageServer;
import org.eclipse.che.api.languageserver.registry.LSOperation;
import org.eclipse.che.api.languageserver.registry.LanguageServerRegistry;
import org.eclipse.che.api.languageserver.registry.LanguageServerRegistryImpl;
import org.eclipse.che.api.languageserver.server.dto.DtoServerImpls.SymbolInformationDto;
import org.eclipse.che.api.languageserver.shared.model.ExtendedWorkspaceSymbolParams;
import org.eclipse.lsp4j.SymbolInformation;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.eclipse.che.api.languageserver.service.TextDocumentServiceUtils.removePrefixUri;
import static org.eclipse.che.api.languageserver.service.TextDocumentServiceUtils.truish;

/**
 * REST API for the workspace/* services defined in
 * https://github.com/Microsoft/vscode-languageserver-protocol Dispatches onto
 * the {@link LanguageServerRegistryImpl}.
 *
 * @author Evgen Vidolob
 */
@Singleton
@Path("languageserver/workspace")
public class WorkspaceService {
    private LanguageServerRegistry registry;

    @Inject
    public WorkspaceService(LanguageServerRegistry registry) {
        this.registry = registry;
    }

    @POST
    @Path("symbol")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<? extends SymbolInformationDto> symbol(ExtendedWorkspaceSymbolParams workspaceSymbolParams)
                    throws ExecutionException, InterruptedException, LanguageServerException {
        List<SymbolInformationDto> result = new ArrayList<>();
        List<InitializedLanguageServer> servers = registry.getApplicableLanguageServers(workspaceSymbolParams.getFileUri()).stream()
                        .flatMap(Collection::stream).collect(Collectors.toList());
        LanguageServerRegistryImpl.doInParallel(servers, new LSOperation<InitializedLanguageServer, List<? extends SymbolInformation>>() {

            @Override
            public boolean canDo(InitializedLanguageServer element) {
                return truish(element.getInitializeResult().getCapabilities().getWorkspaceSymbolProvider());
            }

            @Override
            public CompletableFuture<List<? extends SymbolInformation>> start(InitializedLanguageServer element) {
                return element.getServer().getWorkspaceService().symbol(workspaceSymbolParams);
            }

            @Override
            public boolean handleResult(InitializedLanguageServer element, List<? extends SymbolInformation> locations) {
                locations.forEach(o -> {
                    o.getLocation().setUri(removePrefixUri(o.getLocation().getUri()));
                    result.add(new SymbolInformationDto(o));
                });
                return true;
            }
        }, 10000);
        return result;
    }
}
