package org.eclipse.che.plugin.languageserver.ide.editor;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.annotation.AnnotationModel;
import org.eclipse.che.ide.api.editor.document.Document;
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
//                annotationModel.clear();
                Document document = ((TextEditor) openedEditor).getDocument();
                collector.beginReporting();
                try {
                    for (DiagnosticDTO diagnostic : diagnosticsMessage.getDiagnostics()) {
//                    Annotation annotation = new Annotation(true);
//                    annotation.setText(diagnostic.getMessage());
//                    // TODO generalize id for error and warning. Needs fix in org.eclipse.che.ide.editor.orion.client.OrionEditorWidget.getSeverity(String)
//                    annotation.setType(diagnostic.getSeverity() == Diagnostic.SEVERITY_ERROR ? "org.eclipse.jdt.ui.error" : "org.eclipse.jdt.ui.warning");
//
//                    RangeDTO range = diagnostic.getRange();
//                    int startIndex = document.getIndexFromPosition(new TextPosition(range.getStart().getLine(), range.getStart().getCharacter()));
//                    int endIndex = document.getIndexFromPosition(new TextPosition(range.getEnd().getLine(), range.getEnd().getCharacter()));
//                    annotationModel.addAnnotation(annotation, new Position(startIndex, endIndex - startIndex));
                        collector.acceptDiagnostic(diagnostic);
                    }
                } finally {
                    collector.endReporting();
                }
            }
        }
    }

}
