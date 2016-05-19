package org.eclipse.che.plugin.languageserver.ide;

import org.eclipse.che.ide.api.editor.EditorRegistry;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.plugin.languageserver.ide.editor.LanguageServerEditorProvider;

import com.google.inject.Inject;

@Extension(title = "LanguageServer")
public class LanguageServerExtension {

    @Inject
    protected void configureFileTypes(FileTypeRegistry fileTypeRegistry, LanguageServerResources resources,
            final EditorRegistry editorRegistry, final LanguageServerEditorProvider editorProvider) {
        // TODO the file types need to be retrieved from the server. Ideally we
        // would listen on messages when new language servers get registered.
        FileType fileType = new FileType(resources.file(), "foo");
        fileTypeRegistry.registerFileType(fileType);
        // register editor provider
        editorRegistry.registerDefaultEditor(fileType, editorProvider);
    }
}
