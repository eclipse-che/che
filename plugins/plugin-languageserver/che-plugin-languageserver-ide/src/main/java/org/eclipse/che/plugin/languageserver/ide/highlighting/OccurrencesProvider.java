package org.eclipse.che.plugin.languageserver.ide.highlighting;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.che.api.languageserver.shared.lsapi.DocumentHighlightDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.TextDocumentPositionParamsDTO;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.JsPromise;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.editor.orion.client.OrionOccurrencesHandler;
import org.eclipse.che.ide.editor.orion.client.jso.OrionOccurrenceContextOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionOccurrenceOverlay;
import org.eclipse.che.plugin.languageserver.ide.editor.LanguageServerEditorConfiguration;
import org.eclipse.che.plugin.languageserver.ide.service.TextDocumentServiceClient;
import org.eclipse.che.plugin.languageserver.ide.util.DtoBuildHelper;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Provides occurrences highlights for the Orion Editor.
 * 
 * @author Xavier Coulon, Red Hat
 */
@Singleton
public class OccurrencesProvider implements OrionOccurrencesHandler {

	private static final Logger LOGGER = Logger.getLogger(OccurrencesProvider.class.getName());
	private final EditorAgent               editorAgent;
    private final TextDocumentServiceClient client;
    private final DtoBuildHelper            helper;

    /**
     * Constructor.
     * @param editorAgent
     * @param client
     * @param helper
     */
    @Inject
    public OccurrencesProvider(EditorAgent editorAgent, TextDocumentServiceClient client, DtoBuildHelper helper) {
        this.editorAgent = editorAgent;
        this.client = client;
        this.helper = helper;
    }

    @Override
    public JsPromise<OrionOccurrenceOverlay[]> computeOccurrences(
    		OrionOccurrenceContextOverlay context) {
        final EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
        if (activeEditor == null || !(activeEditor instanceof TextEditor)) {
            return null;
        }
        final TextEditor editor = ((TextEditor)activeEditor);
        if (!(editor.getConfiguration() instanceof LanguageServerEditorConfiguration)) {
            return null;
        }
        final LanguageServerEditorConfiguration configuration = (LanguageServerEditorConfiguration)editor.getConfiguration();
        if (configuration.getServerCapabilities().isDocumentHighlightProvider() == null || !configuration.getServerCapabilities().isDocumentHighlightProvider()) {
            return null;
        }
        final Document document = editor.getDocument();
        final TextDocumentPositionParamsDTO paramsDTO = helper.createTDPP(document, context.getStart());
        // FIXME: the result should be a Promise<List<DocumentHighlightDTO>> but the typefox API returns a single DocumentHighlightDTO
        Promise<DocumentHighlightDTO> promise = client.documentHighlight(paramsDTO);
        Promise<OrionOccurrenceOverlay[]> then = promise.then(new Function<DocumentHighlightDTO, OrionOccurrenceOverlay[]>() {
            @Override
            public OrionOccurrenceOverlay[] apply(DocumentHighlightDTO highlight) throws FunctionException {
            	if(highlight == null) {
            		return new OrionOccurrenceOverlay[0];
            	}
            	final OrionOccurrenceOverlay[] occurrences = new OrionOccurrenceOverlay[1];
        		final OrionOccurrenceOverlay occurrence = OrionOccurrenceOverlay.create();
						// FIXME: this assumes that the language server will
						// compute a range based on 'line 1', ie, the whole
						// file content is on line 1 and the location to
						// highlight is given by the 'character' position
						// only.
        		occurrence.setStart(highlight.getRange().getStart().getCharacter());
        		occurrence.setEnd(highlight.getRange().getEnd().getCharacter() + 1);
        		occurrences[0] = occurrence;
                return occurrences;
            }
        });
        return (JsPromise<OrionOccurrenceOverlay[]>)then;

    }
}
