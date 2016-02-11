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
import java.util.Hashtable;
import java.util.Map;

/**
 * @author Roman Nikitenko
 */
public class CheCodeFormatterInitializer {

    @SuppressWarnings("unchecked")
    public void initializeDefaultPreferences() {
        Map<String, String> codeFormatterDefaultSettings = CheCodeFormatterOptions.getDefaultFormatSettings();
        Hashtable<String, String> options = JavaModelManager.getJavaModelManager().getOptions();
        options.putAll(codeFormatterDefaultSettings);
        JavaModelManager.getJavaModelManager().setOptions(options);
    }
}
