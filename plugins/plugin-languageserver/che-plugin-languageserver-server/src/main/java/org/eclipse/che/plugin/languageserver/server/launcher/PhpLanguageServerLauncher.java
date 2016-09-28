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
package org.eclipse.che.plugin.languageserver.server.launcher;

import io.typefox.lsapi.services.json.JsonBasedLanguageServer;

import com.google.inject.Singleton;

import org.eclipse.che.plugin.languageserver.server.exception.LanguageServerException;
import org.eclipse.che.plugin.languageserver.shared.model.LanguageDescription;
import org.eclipse.che.plugin.languageserver.shared.model.impl.LanguageDescriptionImpl;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Arrays.asList;

/**
 * @author Evgen Vidolob
 * @author Anatolii Bazko
 * @author Kaloyan Raev
 */
@Singleton
public class PhpLanguageServerLauncher extends LanguageServerLauncherTemplate {

    public static final String   LANGUAGE_ID = "php";
    public static final String[] EXTENSIONS  = new String[] {"php"};
    public static final String[] MIME_TYPES  = new String[] {"text/x-php"};

    private static final LanguageDescriptionImpl description;
    private static final String SCRIPT_PATH = "che/ls-php/launch.sh";

    static {
        description = new LanguageDescriptionImpl();
        description.setFileExtensions(asList(EXTENSIONS));
        description.setLanguageId(LANGUAGE_ID);
        description.setMimeTypes(asList(MIME_TYPES));
    }

    @Override
    public LanguageDescription getLanguageDescription() {
        return description;
    }

    @Override
    public boolean isAbleToLaunch() {
        Path launchFile = Paths.get(System.getenv("HOME"), SCRIPT_PATH);
        return launchFile.toFile().exists();
    }

    protected JsonBasedLanguageServer connectToLanguageServer(Process languageServerProcess) {
        JsonBasedLanguageServer languageServer = new JsonBasedLanguageServer();
        languageServer.connect(languageServerProcess.getInputStream(), languageServerProcess.getOutputStream());
        return languageServer;
    }

    protected Process startLanguageServerProcess(String projectPath) throws LanguageServerException {
        Path launchFile = Paths.get(System.getenv("HOME"), SCRIPT_PATH);

        ProcessBuilder processBuilder = new ProcessBuilder(launchFile.toString());
        processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
        try {
            return processBuilder.start();
        } catch (IOException e) {
            throw new LanguageServerException("Can't start PHP language server", e);
        }
    }
}
