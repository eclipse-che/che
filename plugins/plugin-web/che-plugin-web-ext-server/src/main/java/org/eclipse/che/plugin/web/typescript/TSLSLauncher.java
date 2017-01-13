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
package org.eclipse.che.plugin.web.typescript;

import io.typefox.lsapi.services.LanguageServer;
import io.typefox.lsapi.services.json.JsonBasedLanguageServer;

import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncherTemplate;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;
import org.eclipse.che.api.languageserver.shared.model.impl.LanguageDescriptionImpl;
import org.eclipse.che.plugin.web.shared.Constants;

import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Arrays.asList;

/**
 * Launcher for TypeScript Language Server
 */
@Singleton
public class TSLSLauncher extends LanguageServerLauncherTemplate {
    private static final String[] EXTENSIONS  = new String[] {Constants.TS_EXT};
    private static final String[] MIME_TYPES  = new String[] {Constants.TS_MIME_TYPE};
    private static final LanguageDescriptionImpl description;

    private final Path launchScript;

    public TSLSLauncher() {
        launchScript =  Paths.get(System.getenv("HOME"), "che/ls-typescript/launch.sh");
    }

    @Override
    protected Process startLanguageServerProcess(String projectPath) throws LanguageServerException {
        ProcessBuilder processBuilder = new ProcessBuilder(launchScript.toString());
        processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
        try {
            return processBuilder.start();
        } catch (IOException e) {
            throw new LanguageServerException("Can't start TypeScript language server", e);
        }
    }

    @Override
    protected LanguageServer connectToLanguageServer(Process languageServerProcess) throws LanguageServerException {
        JsonBasedLanguageServer languageServer = new JsonBasedLanguageServer();
        languageServer.connect(languageServerProcess.getInputStream(), languageServerProcess.getOutputStream());
        return languageServer;
    }

    @Override
    public LanguageDescription getLanguageDescription() {
        return description;
    }

    @Override
    public boolean isAbleToLaunch() {
        return Files.exists(launchScript);
    }

    static {
        description = new LanguageDescriptionImpl();
        description.setFileExtensions(asList(EXTENSIONS));
        description.setLanguageId(Constants.TS_LANG);
        description.setMimeTypes(asList(MIME_TYPES));
        description.setHighlightingConfiguration("[\n" +
                                                 "  {\"include\":\"orion.js\"},\n" +
                                                 "  {\"match\":\"\\\\b(?:constructor|declare|module)\\\\b\",\"name\" :\"keyword.operator.typescript\"},\n" +
                                                 "  {\"match\":\"\\\\b(?:any|boolean|number|string)\\\\b\",\"name\" : \"storage.type.typescript\"}\n" +
                                                 "]");
    }
}
