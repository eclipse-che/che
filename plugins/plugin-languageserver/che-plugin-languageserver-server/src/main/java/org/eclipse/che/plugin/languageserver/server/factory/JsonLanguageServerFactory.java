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
package org.eclipse.che.plugin.languageserver.server.factory;

import io.typefox.lsapi.LanguageDescription;
import io.typefox.lsapi.LanguageDescriptionImpl;
import io.typefox.lsapi.services.json.JsonBasedLanguageServer;

import com.google.inject.Singleton;

import org.eclipse.che.plugin.languageserver.server.exception.LanguageServerException;

import java.io.IOException;
import java.util.Arrays;

import static java.util.Arrays.asList;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class JsonLanguageServerFactory extends LanguageServerFactoryTemplate {

    public static final String   LANGUAGE_ID = "json";
    public static final String[] EXTENSIONS  = new String[] {"json", "bowerrc", "jshintrc", "jscsrc", "eslintrc", "babelrc"};
    public static final String[] MIME_TYPES  = new String[] {"application/json"};

    @Override
    public LanguageDescription getLanguageDescription() {
        LanguageDescriptionImpl languageDescription = new LanguageDescriptionImpl();
        languageDescription.setFileExtensions(asList(EXTENSIONS));
        languageDescription.setLanguageId(LANGUAGE_ID);
        languageDescription.setMimeTypes(Arrays.asList(MIME_TYPES));
        return languageDescription;
    }

    protected JsonBasedLanguageServer connectToLanguageServer(Process languageServerProcess) {
        JsonBasedLanguageServer languageServer = new JsonBasedLanguageServer();
        languageServer.connect(languageServerProcess.getInputStream(), languageServerProcess.getOutputStream());
        return languageServer;
    }

    protected Process startLanguageServerProcess(String projectPath) throws LanguageServerException {
        ProcessBuilder processBuilder = new ProcessBuilder("node", "/projects/vscode-json-server/server.js");
        processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
        try {
            return processBuilder.start();
        } catch (IOException e) {
            throw new LanguageServerException("Can't start JSON language server", e);
        }
    }
}
