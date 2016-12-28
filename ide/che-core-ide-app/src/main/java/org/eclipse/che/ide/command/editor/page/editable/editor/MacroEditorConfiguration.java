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
package org.eclipse.che.ide.command.editor.page.editable.editor;

import org.eclipse.che.ide.api.editor.codeassist.CodeAssistProcessor;
import org.eclipse.che.ide.api.editor.editorconfig.DefaultTextEditorConfiguration;
import org.eclipse.che.ide.api.editor.partition.DocumentPartitioner;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * Editor configuration for macro editor
 */
public class MacroEditorConfiguration extends DefaultTextEditorConfiguration {

    private MacroCodeAssistProcessor codeAssistProcessor;

    @Inject
    public MacroEditorConfiguration(MacroCodeAssistProcessor codeAssistProcessor) {
        this.codeAssistProcessor = codeAssistProcessor;
    }

    @Override
    public Map<String, CodeAssistProcessor> getContentAssistantProcessors() {
        Map<String, CodeAssistProcessor> map = new HashMap<>();
        map.put(DocumentPartitioner.DEFAULT_CONTENT_TYPE, codeAssistProcessor);
        return map;
    }
}
