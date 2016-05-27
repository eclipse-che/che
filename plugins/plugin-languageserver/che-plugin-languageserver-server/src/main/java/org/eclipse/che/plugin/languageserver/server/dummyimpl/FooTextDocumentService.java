package org.eclipse.che.plugin.languageserver.server.dummyimpl;

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
import io.typefox.lsapi.Position;
import io.typefox.lsapi.PositionImpl;
import io.typefox.lsapi.PublishDiagnosticsParams;
import io.typefox.lsapi.PublishDiagnosticsParamsImpl;
import io.typefox.lsapi.RangeImpl;
import io.typefox.lsapi.ReferenceParams;
import io.typefox.lsapi.RenameParams;
import io.typefox.lsapi.SignatureHelp;
import io.typefox.lsapi.SymbolInformation;
import io.typefox.lsapi.TextDocumentContentChangeEvent;
import io.typefox.lsapi.TextDocumentPositionParams;
import io.typefox.lsapi.TextDocumentService;
import io.typefox.lsapi.TextEdit;
import io.typefox.lsapi.TextEditImpl;
import io.typefox.lsapi.WorkspaceEdit;

import com.google.common.base.Splitter;
import com.google.common.io.Files;
import com.google.gson.JsonElement;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

public class FooTextDocumentService implements TextDocumentService {

    private final List<NotificationCallback<PublishDiagnosticsParams>> publishDiagnosticsCallbacks = newArrayList();
    private String rootPath;
    private Map<String, Document> openDocuments = newHashMap();

    private TextDocumentPositionParams lastDocumentPosition;
    private Map<String, String> lastCompletionMap = newHashMap();

    public FooTextDocumentService(String rootPath) {
        this.rootPath = rootPath;
    }

    @Override
    public List<? extends CompletionItem> completion(TextDocumentPositionParams position) {
        lastDocumentPosition = position;
        lastCompletionMap.clear();
        List<CompletionItem> result = newArrayList();
        result.add(newCompletionItem("foo", position.getPosition().getLine(), position.getPosition().getCharacter()));
        result.add(newCompletionItem("bar", position.getPosition().getLine(), position.getPosition().getCharacter()));
        return result;
    }

    private CompletionItemImpl newCompletionItem(String newText, int line, int character) {
        CompletionItemImpl item = new CompletionItemImpl();
        item.setLabel(newText + " - Inserts " + newText);
        String data = generateUID();
        item.setData(data);
        lastCompletionMap.put(data, newText);
//        TextEditImpl textEdit = new TextEditImpl();
//        textEdit.setNewText(newText);
//        textEdit.setRange(newRange(line, character, line, character));
//        item.setTextEdit(textEdit);
        return item;
    }

    private String generateUID() {
        return UUID.randomUUID().toString();
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
        String uri = params.getTextDocument().getUri();
        Document doc = openDocuments.get(uri);
        if (doc == null || doc.version > params.getTextDocument().getVersion()) {
            return;
        }
        if (doc.version + 1 < params.getTextDocument().getVersion()) {
            // missed a change.
            File file = new File(rootPath + uri);
            String contents;
            try {
                contents = Files.toString(file, Charset.defaultCharset());
                openDocuments.put(uri, new Document(params.getTextDocument().getVersion(), contents));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            List<? extends TextDocumentContentChangeEvent> changes = params.getContentChanges();
            for (TextDocumentContentChangeEvent change : changes) {
                doc.apply(change);
            }
            doc.version = params.getTextDocument().getVersion();
        }
        validateDocument(uri);
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        File file = new File(rootPath + uri);
        String contents;
        try {
            contents = Files.toString(file, Charset.defaultCharset());
            openDocuments.put(uri, new Document(params.getTextDocument().getVersion(), contents));
            validateDocument(uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        openDocuments.remove(params.getTextDocument().getUri());
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
    }

    private void validateDocument(String uri) {
        Document document = openDocuments.get(uri);
        List<String> lines = Splitter.on('\n').splitToList(document.contents);
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
                diagnosticImpl.setRange(newRange(lineIdx, indexOf, lineIdx, indexOf + 3));
                diagnosticImpl.setSeverity(Diagnostic.SEVERITY_ERROR);
                diagnosticsMessage.getDiagnostics().add(diagnosticImpl);
            }
        }
        for (NotificationCallback<PublishDiagnosticsParams> callback : publishDiagnosticsCallbacks) {
            callback.call(diagnosticsMessage);
        }
    }

    @Override
    public void onPublishDiagnostics(NotificationCallback<PublishDiagnosticsParams> callback) {
        publishDiagnosticsCallbacks.add(callback);
    }

    @Override
    public CompletionItem resolveCompletionItem(CompletionItem unresolved) {
        CompletionItemImpl item = new CompletionItemImpl();
        item.setLabel(unresolved.getLabel());
        item.setDetail(unresolved.getDetail());
        item.setDocumentation("Doc!");
        item.setKind(unresolved.getKind());
        item.setFilterText(unresolved.getFilterText());
        TextEditImpl textEdit = new TextEditImpl();
        //TODO need to throw exception here
        String text = "Can't find insert string";
        if(unresolved.getData() instanceof JsonElement){
            text = lastCompletionMap.get(((JsonElement)unresolved.getData()).getAsString());
        }
        textEdit.setNewText(text);
        textEdit.setRange(newRange(lastDocumentPosition.getPosition().getLine(),
                                   lastDocumentPosition.getPosition().getCharacter(),
                                   lastDocumentPosition.getPosition().getLine(),
                                   lastDocumentPosition.getPosition().getCharacter()));
        item.setTextEdit(textEdit);
        return item;
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

    private static class Document {
        int    version;
        String contents;

        public Document(int version, String contents) {
            this.version = version;
            this.contents = contents;
        }

        public void apply(TextDocumentContentChangeEvent change) {
            int start = getOffSet(change.getRange().getStart());
            int end = getOffSet(change.getRange().getEnd());
            if (end < start) {
                end = start;
            }
            if (start > -1)
                this.contents = contents.substring(0, start) + change.getText() + contents.substring(end);
        }

        public int getOffSet(Position pos) {
            char[] charArray = contents.toCharArray();
            int line = 0;
            int character = 0;
            for (int i = 0; i < charArray.length; i++) {
                char c = charArray[i];
                if (c == '\n') {
                    line++;
                    character = 0;
                } else {
                    character++;
                }
                if (line == pos.getLine() && character == pos.getCharacter()) {
                    return i + 1;
                }
            }
            return -1;
        }
    }

}
