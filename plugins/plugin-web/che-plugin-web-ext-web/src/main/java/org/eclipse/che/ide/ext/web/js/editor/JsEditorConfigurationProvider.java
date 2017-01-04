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
package org.eclipse.che.ide.ext.web.js.editor;

import java.util.Set;

import javax.inject.Provider;

import org.eclipse.che.ide.ext.web.html.editor.AutoEditStrategyFactory;
import com.google.inject.Inject;

/**
 * Provider for JS Editor configuration.
 * 
 * @author Florent Benoit
 */

public class JsEditorConfigurationProvider implements Provider<JsEditorConfiguration> {

    /**
     * Auto Edit strategies
     */
    @Inject(optional = true)
    private Set<AutoEditStrategyFactory> autoEditStrategyFactories;

    @Inject
    private DefaultCodeAssistProcessor chainedCodeAssistProcessor;

    /**
     * Build a new instance of JS Editor configuration
     * 
     * @return
     */
    @Override
    public JsEditorConfiguration get() {
        return new JsEditorConfiguration(autoEditStrategyFactories, chainedCodeAssistProcessor);
    }
}
