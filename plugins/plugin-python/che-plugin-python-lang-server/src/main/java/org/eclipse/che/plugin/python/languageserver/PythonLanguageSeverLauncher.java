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
package org.eclipse.che.plugin.python.languageserver;

import io.typefox.lsapi.services.LanguageServer;
import io.typefox.lsapi.services.json.JsonBasedLanguageServer;
import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncherTemplate;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;
import org.eclipse.che.api.languageserver.shared.model.impl.LanguageDescriptionImpl;
import org.eclipse.che.plugin.python.shared.ProjectAttributes;

import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static java.util.Arrays.asList;

/**
 * Launches language server for Python
 */
@Singleton
public class PythonLanguageSeverLauncher extends LanguageServerLauncherTemplate {

    private static final String[] EXTENSIONS  = new String[] {ProjectAttributes.PYTHON_EXT};
    private static final String[] MIME_TYPES  = new String[] {"text/x-python"};
    private static final LanguageDescriptionImpl description;

    private final Path launchScript;

    static {
        description = new LanguageDescriptionImpl();
        description.setFileExtensions(asList(EXTENSIONS));
        description.setLanguageId(ProjectAttributes.PYTHON_ID);
        description.setMimeTypes(Arrays.asList(MIME_TYPES));
    }

    public PythonLanguageSeverLauncher() {
        launchScript = Paths.get(System.getenv("HOME"), "che/ls-python/launch.sh");
    }

    @Override
    public LanguageDescription getLanguageDescription() {
        return description;
    }

    @Override
    public boolean isAbleToLaunch() {
        return launchScript.toFile().exists();
    }

    @Override
    protected Process startLanguageServerProcess(String projectPath) throws LanguageServerException {
        ProcessBuilder processBuilder = new ProcessBuilder(launchScript.toString());
        processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);

        try {
            return processBuilder.start();
        } catch (IOException e) {
            throw new LanguageServerException("Can't start CSharp language server", e);
        }
    }

    @Override
    protected LanguageServer connectToLanguageServer(Process languageServerProcess) throws LanguageServerException {
        JsonBasedLanguageServer languageServer = new JsonBasedLanguageServer();
        languageServer.connect(languageServerProcess.getInputStream(), languageServerProcess.getOutputStream());
        return languageServer;
    }
}
