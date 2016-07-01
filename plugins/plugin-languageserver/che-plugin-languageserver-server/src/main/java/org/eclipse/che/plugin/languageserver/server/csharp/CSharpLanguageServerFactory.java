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

import io.typefox.lsapi.LanguageDescription;
import io.typefox.lsapi.LanguageDescriptionImpl;
import io.typefox.lsapi.services.json.JsonBasedLanguageServer;

import com.google.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.eclipse.che.plugin.languageserver.server.LanguageServerFactoryTemplate;
import org.eclipse.che.plugin.languageserver.server.exception.LanguageServerException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static java.util.Arrays.asList;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class CSharpLanguageServerFactory extends LanguageServerFactoryTemplate {

    public static final String   LANGUAGE_ID = "csharp";
    public static final String[] EXTENSIONS  = new String[] {"cs", "csx"};
    public static final String[] MIME_TYPES  = new String[] {"text/x-csharp"};

    @Override
    protected Process startLanguageServerProcess(String projectPath) throws LanguageServerException {
        restoreDependencies(projectPath);

        ProcessBuilder processBuilder =
                new ProcessBuilder("node", "--debug=5858", "/projects/lscsharp/node_modules/omnisharp-client/languageserver/server.js");
        processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);

        try {
            return processBuilder.start();
        } catch (IOException e) {
            throw new LanguageServerException("Can't start JSON language server", e);
        }
    }

    private void restoreDependencies(String projectPath) throws LanguageServerException {
        ProcessBuilder processBuilder = new ProcessBuilder("dotnet", "restore");
        processBuilder.directory(new File(projectPath));
        try {
            Process process = processBuilder.start();
            int resultCode = process.waitFor();
            if (resultCode != 0) {
                String err = IOUtils.toString(process.getErrorStream());
                String in = IOUtils.toString(process.getInputStream());
                throw new LanguageServerException("Can't restore dependencies. Error: " + err + ". Output: " + in);
            }
        } catch (IOException | InterruptedException e) {
            throw new LanguageServerException("Can't start JSON language server", e);
        }
    }

    @Override
    protected JsonBasedLanguageServer connectToLanguageServer(Process languageServerProcess) {
        JsonBasedLanguageServer languageServer = new JsonBasedLanguageServer();
        languageServer.connect(languageServerProcess.getInputStream(), languageServerProcess.getOutputStream());
        return languageServer;
    }

    @Override
    public LanguageDescription getLanguageDescription() {
        LanguageDescriptionImpl languageDescription = new LanguageDescriptionImpl();
        languageDescription.setFileExtensions(asList(EXTENSIONS));
        languageDescription.setLanguageId(LANGUAGE_ID);
        languageDescription.setMimeTypes(Arrays.asList(MIME_TYPES));
        return languageDescription;
    }
}
