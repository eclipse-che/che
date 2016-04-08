/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.corext.format;

import org.eclipse.jdt.internal.core.JavaModelManager;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.io.Files.readLines;

/**
 * @author Roman Nikitenko
 * @author Igor Vinokur
 */
public class CheCodeFormatterInitializer {

    @SuppressWarnings("unchecked")
    public void initializePreferences(String settingsDir, String preferencesFileName) throws IOException {
        Map<String, String> codeFormatterSettings = CheCodeFormatterOptions.getDefaultFormatSettings();
        Hashtable<String, String> options = JavaModelManager.getJavaModelManager().getOptions();

        File preferencesFile = Paths.get(settingsDir, preferencesFileName).toFile();
        if (preferencesFile.exists()) {

            Map<String, String> fileOptions = readLines(preferencesFile, Charset.defaultCharset())
                    .stream()
                    .map(fileLine -> fileLine.split("="))
                    .collect(Collectors.toMap(elements -> elements[0], elements -> elements[1]));

            codeFormatterSettings.putAll(fileOptions);
        }

        options.putAll(codeFormatterSettings);
        JavaModelManager.getJavaModelManager().setOptions(options);
    }
}
