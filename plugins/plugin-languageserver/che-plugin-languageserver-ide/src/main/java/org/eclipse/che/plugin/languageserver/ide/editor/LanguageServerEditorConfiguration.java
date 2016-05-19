package org.eclipse.che.plugin.languageserver.ide.editor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.che.ide.api.editor.codeassist.CodeAssistProcessor;
import org.eclipse.che.ide.api.editor.editorconfig.AutoSaveTextEditorConfiguration;
import org.eclipse.che.ide.api.editor.partition.DocumentPartitioner;
import org.eclipse.che.plugin.languageserver.ide.editor.codeassist.LanguageServerCodeAssistProcessor;

import com.google.inject.Inject;

public class LanguageServerEditorConfiguration extends AutoSaveTextEditorConfiguration {

    private LanguageServerCodeAssistProcessor codeAssistProcessor;

    @Inject
    public LanguageServerEditorConfiguration(LanguageServerCodeAssistProcessor codeAssistProcessor) {
        this.codeAssistProcessor = codeAssistProcessor;
    }

    @Override
    public Map<String, CodeAssistProcessor> getContentAssistantProcessors() {
        Map<String, CodeAssistProcessor> map = new HashMap<>();
        map.put(DocumentPartitioner.DEFAULT_CONTENT_TYPE, codeAssistProcessor);
        return map;
    }
}
