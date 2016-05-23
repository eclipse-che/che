package org.eclipse.che.plugin.languageserver.server.dummyimpl;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import com.google.common.io.Files;

import io.typefox.lsapi.CodeActionParams;
import io.typefox.lsapi.CodeLens;
import io.typefox.lsapi.CodeLensParams;
import io.typefox.lsapi.Command;
import io.typefox.lsapi.CompletionItem;
import io.typefox.lsapi.CompletionItemImpl;
import io.typefox.lsapi.Diagnostic;
import io.typefox.lsapi.DiagnosticImpl;
import io.typefox.lsapi.DidChangeTextDocumentParams;
import io.typefox.lsapi.DidCloseTextDocumentParams;
import io.typefox.lsapi.DidOpenTextDocumentParams;
import io.typefox.lsapi.DidSaveTextDocumentParams;
import io.typefox.lsapi.DocumentFormattingParams;
import io.typefox.lsapi.DocumentHighlight;
import io.typefox.lsapi.DocumentOnTypeFormattingParams;
import io.typefox.lsapi.DocumentRangeFormattingParams;
import io.typefox.lsapi.DocumentSymbolParams;
import io.typefox.lsapi.Hover;
import io.typefox.lsapi.Location;
import io.typefox.lsapi.NotificationCallback;
import io.typefox.lsapi.PositionImpl;
import io.typefox.lsapi.PublishDiagnosticsParams;
import io.typefox.lsapi.PublishDiagnosticsParamsImpl;
import io.typefox.lsapi.RangeImpl;
import io.typefox.lsapi.ReferenceParams;
import io.typefox.lsapi.RenameParams;
import io.typefox.lsapi.SignatureHelp;
import io.typefox.lsapi.SymbolInformation;
import io.typefox.lsapi.TextDocumentPositionParams;
import io.typefox.lsapi.TextDocumentService;
import io.typefox.lsapi.TextEdit;
import io.typefox.lsapi.TextEditImpl;
import io.typefox.lsapi.WorkspaceEdit;

public class FooTextDocumentService implements TextDocumentService {
    
    private String rootPath;

    public FooTextDocumentService(String rootPath) {
        this.rootPath = rootPath;
    }

    @Override
    public List<? extends CompletionItem> completion(TextDocumentPositionParams position) {
        List<CompletionItem> result = newArrayList();
        result.add(newCompletionItem("foo", position.getPosition().getLine(), position.getPosition().getCharacter()));
        result.add(newCompletionItem("bar", position.getPosition().getLine(), position.getPosition().getCharacter()));
        return result;
    }
    
    private CompletionItemImpl newCompletionItem(String newText, int line, int character) {
        CompletionItemImpl item = new CompletionItemImpl();
        item.setLabel(newText+" - Inserts "+newText);
        TextEditImpl textEdit = new TextEditImpl();
        textEdit.setNewText(newText);
        textEdit.setRange(newRange(line, character, line, character));
        item.setTextEdit(textEdit);
        return item;
    }
    
    private RangeImpl newRange(int line, int character, int line2, int character2) {
        RangeImpl result = new RangeImpl();
        PositionImpl start = new PositionImpl();
        start.setLine(line);
        start.setCharacter(character);
        result.setStart(start);
        PositionImpl end = new PositionImpl();
        end.setLine(line2);
        end.setCharacter(character2);
        result.setEnd(end);
        return result;
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        validateDocument(params.getTextDocument().getUri());
    }

    private void validateDocument(String uri) {
        File file = new File(rootPath + uri);
        List<String> lines;
        try {
            lines = Files.readLines(file, Charset.defaultCharset());
            PublishDiagnosticsParamsImpl diagnosticsMessage = new PublishDiagnosticsParamsImpl();
            diagnosticsMessage.setUri(uri);
            diagnosticsMessage.setDiagnostics(newArrayList());
            for (int currentLineIdx = 0; currentLineIdx < lines.size(); currentLineIdx++) {
                String line = lines.get(currentLineIdx);
                int indexOf = line.indexOf("Foo");
                final int lineIdx = currentLineIdx;
                if (indexOf != -1) {
                    DiagnosticImpl diagnosticImpl = new DiagnosticImpl();
                    diagnosticImpl.setCode("no.uppercase.foo");
                    diagnosticImpl.setMessage("Please use lower case 'foo'.");
                    diagnosticImpl.setRange(newRange(lineIdx, indexOf, lineIdx, indexOf+3));
                    diagnosticImpl.setSeverity(Diagnostic.SEVERITY_ERROR);
                    diagnosticsMessage.getDiagnostics().add(diagnosticImpl);
                }
            }
            for (NotificationCallback<PublishDiagnosticsParams> callback : publishDiagnosticsCallbacks) {
                callback.call(diagnosticsMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private final List<NotificationCallback<PublishDiagnosticsParams>> publishDiagnosticsCallbacks = newArrayList();
    
    @Override
    public void onPublishDiagnostics(NotificationCallback<PublishDiagnosticsParams> callback) {
        publishDiagnosticsCallbacks.add(callback);
    }

    @Override
    public CompletionItem resolveCompletionItem(CompletionItem unresolved) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Hover hover(TextDocumentPositionParams position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SignatureHelp signatureHelp(TextDocumentPositionParams position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<? extends Location> definition(TextDocumentPositionParams position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<? extends Location> references(ReferenceParams params) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DocumentHighlight documentHighlight(TextDocumentPositionParams position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<? extends SymbolInformation> documentSymbol(DocumentSymbolParams params) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<? extends Command> codeAction(CodeActionParams params) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<? extends CodeLens> codeLens(CodeLensParams params) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CodeLens resolveCodeLens(CodeLens unresolved) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<? extends TextEdit> formatting(DocumentFormattingParams params) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<? extends TextEdit> rangeFormatting(DocumentRangeFormattingParams params) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<? extends TextEdit> onTypeFormatting(DocumentOnTypeFormattingParams params) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WorkspaceEdit rename(RenameParams params) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        // TODO Auto-generated method stub

    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        // TODO Auto-generated method stub

    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        // TODO Auto-generated method stub

    }

}
