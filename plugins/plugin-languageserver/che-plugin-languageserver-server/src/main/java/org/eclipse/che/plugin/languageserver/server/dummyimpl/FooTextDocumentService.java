package org.eclipse.che.plugin.languageserver.server.dummyimpl;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import io.typefox.lsapi.CodeActionParams;
import io.typefox.lsapi.CodeLens;
import io.typefox.lsapi.CodeLensParams;
import io.typefox.lsapi.Command;
import io.typefox.lsapi.CompletionItem;
import io.typefox.lsapi.CompletionItemImpl;
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

    @Override
    public List<? extends CompletionItem> completion(TextDocumentPositionParams position) {
        List<CompletionItem> result = newArrayList();
        CompletionItemImpl item = new CompletionItemImpl();
        item.setLabel("foo - Inserts Foo");
        item.setTextEdit(new TextEditImpl() {
            {
                setNewText("foo");
                setRange(new RangeImpl() {
                    {
                        setStart(new PositionImpl() {
                            {
                                setLine(position.getPosition().getLine());
                                setCharacter(position.getPosition().getCharacter());
                            }
                        });
                        setEnd(new PositionImpl() {
                            {
                                setLine(position.getPosition().getLine());
                                setCharacter(position.getPosition().getCharacter());
                            }
                        });
                    }
                });
            }
        });
        result.add(item);
        CompletionItemImpl item2 = new CompletionItemImpl();
        item2.setLabel("bar - Inserts Bar");
        item2.setTextEdit(new TextEditImpl() {
            {
                setNewText("bar");
                setRange(new RangeImpl() {
                    {
                        setStart(new PositionImpl() {
                            {
                                setLine(position.getPosition().getLine());
                                setCharacter(position.getPosition().getCharacter());
                            }
                        });
                        setEnd(new PositionImpl() {
                            {
                                setLine(position.getPosition().getLine());
                                setCharacter(position.getPosition().getCharacter());
                            }
                        });
                    }
                });
            }
        });
        result.add(item2);
        return result;
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
    public void didChange(DidChangeTextDocumentParams params) {
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

    @Override
    public void onPublishDiagnostics(NotificationCallback<PublishDiagnosticsParams> callback) {
        // TODO Auto-generated method stub

    }

}
