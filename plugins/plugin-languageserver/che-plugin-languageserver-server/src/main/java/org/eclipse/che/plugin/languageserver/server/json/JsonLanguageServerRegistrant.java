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
package org.eclipse.che.plugin.languageserver.server.json;

import io.typefox.lsapi.services.json.JsonBasedLanguageServer;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.plugin.languageserver.server.LanguageServerRegistry;
import org.eclipse.che.plugin.languageserver.shared.dto.DtoServerImpls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class JsonLanguageServerRegistrant {

    private final static Logger LOG = LoggerFactory.getLogger(JsonLanguageServerRegistrant.class);

    @Inject
    public void registerJsonServer(LanguageServerRegistry registry) {
        try {
            ProcessBuilder languageServerStarter = new ProcessBuilder("node", "/projects/vscode-json-server/server.js");
            languageServerStarter.redirectInput(ProcessBuilder.Redirect.PIPE);
            languageServerStarter.redirectOutput(ProcessBuilder.Redirect.PIPE);
            Process process = languageServerStarter.start();
            JsonBasedLanguageServer languageServer = new JsonBasedLanguageServer();
            languageServer.connect(process.getInputStream(), process.getOutputStream());

            DtoServerImpls.LanguageDescriptionDTOImpl dto = new DtoServerImpls.LanguageDescriptionDTOImpl();
            //same as in VSCode
            dto.setFileExtensions(Arrays.asList("json", "bowerrc", "jshintrc", "jscsrc", "eslintrc", "babelrc"));
            dto.setLanguageId("json");
            dto.setMimeTypes(Collections.singletonList("application/json"));
            registry.register(languageServer, Collections.singletonList(dto));
        } catch (IOException e) {
            LOG.error("Can't register JSON language server!", e);
        }

    }
}
