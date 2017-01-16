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
package org.eclipse.che.ide.ext.web.html.editor;

import org.eclipse.che.ide.api.editor.codeassist.DefaultChainedCodeAssistProcessor;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Set;

/**
 * Allows to chain code assist processor for the default given content type.
 * It will delegate to sub processors.
 *
 * @author Florent Benoit
 */
@Singleton
public class DefaultCodeAssistProcessor extends DefaultChainedCodeAssistProcessor {

    /**
     * HTML code assist processors.(as it's optional it can't be in constructor)
     */
    @Inject(optional = true)
    protected void injectProcessors(Set<HTMLCodeAssistProcessor> htmlCodeAssistProcessors) {
        setProcessors(htmlCodeAssistProcessors);
    }

}
