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
package org.eclipse.che.plugin.languageserver.server.csharp;

import io.typefox.lsapi.services.json.JsonBasedLanguageServer;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.plugin.languageserver.server.LanguageServerRegistry;
import org.eclipse.che.plugin.languageserver.shared.dto.DtoServerImpls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class CSharpLanguageServerRegistrant {

    private final static Logger LOG = LoggerFactory.getLogger(CSharpLanguageServerRegistrant.class);

    @Inject
    public void registerJsonServer(LanguageServerRegistry registry) {
        try {
            ProcessBuilder dotetRestoreBuilder = new ProcessBuilder("dotnet", "restore");
            dotetRestoreBuilder.directory(new File("/projects/test"));
            Process start = dotetRestoreBuilder.start();
            int i = start.waitFor();
            if (i != 0) {
                throw new IOException("Can't DOTNET restore!!!!!");
            }

            ProcessBuilder languageServerStarter =
                    new ProcessBuilder("node", "--debug=5858", "/projects/lscsharp/node_modules/omnisharp-client/languageserver/server.js");
            languageServerStarter.redirectInput(ProcessBuilder.Redirect.PIPE);
            languageServerStarter.redirectOutput(ProcessBuilder.Redirect.PIPE);
            Process process = languageServerStarter.start();
            JsonBasedLanguageServer languageServer = new JsonBasedLanguageServer();
            languageServer.connect(process.getInputStream(), process.getOutputStream());

            DtoServerImpls.LanguageDescriptionDTOImpl dto = new DtoServerImpls.LanguageDescriptionDTOImpl();
            //same as in VSCode
            dto.setFileExtensions(Arrays.asList("cs", "csx"));
            dto.setLanguageId("csharp");
            dto.setMimeTypes(Collections.singletonList("text/x-csharp"));
            registry.register(languageServer, Collections.singletonList(dto));
        } catch (IOException | InterruptedException e) {
            LOG.error("Can't register CSHARP language server!", e);
        }

    }
}
