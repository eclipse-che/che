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
package org.eclipse.che.plugin.jsonexample.ide.editor;

import com.google.inject.Inject;

import javax.inject.Provider;
import java.util.Set;

/**
 * Guice Provider for HTML Editor configuration.
 *
 * @author Florent Benoit
 */

public class JsonExampleEditorConfigurationProvider implements Provider<JsonExampleEditorConfiguration> {



    @Inject
    private JsonExampleCodeAssistProcessor jsonExampleCodeAssistProcessor;


    /**
     * Build a new instance of HtmlEditor Configuration
     *
     * @return
     */
    @Override
    public JsonExampleEditorConfiguration get() {
        return new JsonExampleEditorConfiguration(jsonExampleCodeAssistProcessor);
    }
}
