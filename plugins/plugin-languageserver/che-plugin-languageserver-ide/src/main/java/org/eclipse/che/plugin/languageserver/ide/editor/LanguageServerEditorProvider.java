package org.eclipse.che.plugin.languageserver.ide.editor;

import javax.inject.Inject;

import org.eclipse.che.ide.api.editor.defaulteditor.AbstractTextEditorProvider;
import org.eclipse.che.ide.api.editor.editorconfig.TextEditorConfiguration;

import com.google.inject.Provider;

public class LanguageServerEditorProvider extends AbstractTextEditorProvider {

    private Provider<LanguageServerEditorConfiguration> editorConfigurationProvider;

    @Inject
    public LanguageServerEditorProvider(final Provider<LanguageServerEditorConfiguration> editorConfigurationProvider) {
        this.editorConfigurationProvider = editorConfigurationProvider;
    }

    @Override
    public String getId() {
        return "LanguageServerEditor";
    }

    @Override
    public String getDescription() {
        return "Code Editor";
    }

    @Override
    protected TextEditorConfiguration getEditorConfiguration() {
        return editorConfigurationProvider.get();
    }
}
