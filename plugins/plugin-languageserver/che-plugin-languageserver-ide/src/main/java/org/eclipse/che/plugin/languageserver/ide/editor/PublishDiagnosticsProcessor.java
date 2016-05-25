package org.eclipse.che.plugin.languageserver.ide.editor;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.annotation.AnnotationModel;
import org.eclipse.che.ide.api.editor.editorconfig.TextEditorConfiguration;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.plugin.languageserver.shared.lsapi.DiagnosticDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.PublishDiagnosticsParamsDTO;

@Singleton
public class PublishDiagnosticsProcessor {
    
    private final EditorAgent editorAgent;
    
    @Inject 
    public PublishDiagnosticsProcessor(EditorAgent editorAgent) {
        this.editorAgent = editorAgent;
    }

    public void processDiagnostics(PublishDiagnosticsParamsDTO diagnosticsMessage) {
        EditorPartPresenter openedEditor = editorAgent.getOpenedEditor(new Path(diagnosticsMessage.getUri()));
        //TODO add markers
        if (openedEditor == null)
            return;
        if (openedEditor instanceof TextEditor) {
            TextEditorConfiguration editorConfiguration = ((TextEditor) openedEditor).getConfiguration();
            AnnotationModel annotationModel = editorConfiguration.getAnnotationModel();
            if (annotationModel != null && annotationModel instanceof DiagnosticCollector) {
                DiagnosticCollector collector = (DiagnosticCollector)annotationModel;
                collector.beginReporting();
                try {
                    for (DiagnosticDTO diagnostic : diagnosticsMessage.getDiagnostics()) {
                        collector.acceptDiagnostic(diagnostic);
                    }
                } finally {
                    collector.endReporting();
                }
            }
        }
    }

}
