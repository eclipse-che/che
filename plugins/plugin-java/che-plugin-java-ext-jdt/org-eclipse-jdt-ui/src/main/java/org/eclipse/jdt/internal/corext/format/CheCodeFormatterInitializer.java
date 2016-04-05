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

import static com.google.common.io.Files.readLines;

/**
 * @author Roman Nikitenko
 */
public class CheCodeFormatterInitializer {

    @SuppressWarnings("unchecked")
    public void initializePreferences(String settingsDir, String preferencesFileName) throws IOException {
        Map<String, String> codeFormatterDefaultSettings = CheCodeFormatterOptions.getDefaultFormatSettings();
        Hashtable<String, String> options = JavaModelManager.getJavaModelManager().getOptions();

        File preferencesFile = Paths.get(settingsDir, preferencesFileName).toFile();
        if (preferencesFile.exists()) {
            for (String fileLine : readLines(preferencesFile, Charset.defaultCharset())) {
                String fileProperty = fileLine.substring(0, fileLine.indexOf("="));
                if (!options.containsKey(fileProperty)) {
                    options.put(fileProperty, "");
                }
                options.keySet()
                       .stream()
                       .filter(fileLine::contains)
                       .forEach(property -> options.replace(property, fileLine.substring(fileLine.indexOf("=") + 1)));
            }
        }

        options.putAll(codeFormatterDefaultSettings);
        JavaModelManager.getJavaModelManager().setOptions(options);
    }
}
