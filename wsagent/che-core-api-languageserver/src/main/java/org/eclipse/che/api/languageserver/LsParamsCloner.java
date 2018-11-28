/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.languageserver;

import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.lsp4j.*;

/** Clones some language server request parameters. */
@Singleton
class LsParamsCloner {
  RenameParams clone(RenameParams renameParams) {
    if (renameParams == null) {
      return null;
    }

    TextDocumentIdentifier textDocument = renameParams.getTextDocument();
    String newName = renameParams.getNewName();
    Position position = renameParams.getPosition();

    RenameParams cloned = new RenameParams();
    cloned.setNewName(newName);
    cloned.setPosition(clone(position));
    cloned.setTextDocument(clone(textDocument));

    return cloned;
  }

  DidSaveTextDocumentParams clone(DidSaveTextDocumentParams didSaveTextDocumentParams) {
    if (didSaveTextDocumentParams == null) {
      return null;
    }

    String text = didSaveTextDocumentParams.getText();
    TextDocumentIdentifier textDocument = didSaveTextDocumentParams.getTextDocument();

    DidSaveTextDocumentParams cloned = new DidSaveTextDocumentParams();
    cloned.setText(text);
    cloned.setTextDocument(clone(textDocument));

    return cloned;
  }

  DidCloseTextDocumentParams clone(DidCloseTextDocumentParams didCloseTextDocumentParams) {
    if (didCloseTextDocumentParams == null) {
      return null;
    }

    TextDocumentIdentifier textDocument = didCloseTextDocumentParams.getTextDocument();

    DidCloseTextDocumentParams cloned = new DidCloseTextDocumentParams();
    cloned.setTextDocument(clone(textDocument));

    return cloned;
  }

  DidOpenTextDocumentParams clone(DidOpenTextDocumentParams didOpenTextDocumentParams) {
    if (didOpenTextDocumentParams == null) {
      return null;
    }

    TextDocumentItem textDocument = didOpenTextDocumentParams.getTextDocument();

    DidOpenTextDocumentParams cloned = new DidOpenTextDocumentParams();
    cloned.setTextDocument(clone(textDocument));

    return cloned;
  }

  TextDocumentItem clone(TextDocumentItem textDocumentItem) {
    if (textDocumentItem == null) {
      return null;
    }

    String languageId = textDocumentItem.getLanguageId();
    String text = textDocumentItem.getText();
    String uri = textDocumentItem.getUri();
    int version = textDocumentItem.getVersion();

    TextDocumentItem cloned = new TextDocumentItem();
    cloned.setLanguageId(languageId);
    cloned.setText(text);
    cloned.setUri(uri);
    cloned.setVersion(version);

    return cloned;
  }

  DidChangeTextDocumentParams clone(DidChangeTextDocumentParams didChangeTextDocumentParams) {
    if (didChangeTextDocumentParams == null) {
      return null;
    }

    List<TextDocumentContentChangeEvent> contentChanges =
        didChangeTextDocumentParams.getContentChanges();
    VersionedTextDocumentIdentifier textDocument = didChangeTextDocumentParams.getTextDocument();

    DidChangeTextDocumentParams cloned = new DidChangeTextDocumentParams();
    cloned.setContentChanges(cloneTDCCE(contentChanges));
    cloned.setTextDocument(clone(textDocument));

    return cloned;
  }

  VersionedTextDocumentIdentifier clone(
      VersionedTextDocumentIdentifier versionedTextDocumentIdentifier) {
    if (versionedTextDocumentIdentifier == null) {
      return null;
    }

    Integer version = versionedTextDocumentIdentifier.getVersion();
    String uri = versionedTextDocumentIdentifier.getUri();

    VersionedTextDocumentIdentifier cloned = new VersionedTextDocumentIdentifier();
    cloned.setUri(uri);
    cloned.setVersion(version);

    return cloned;
  }

  List<TextDocumentContentChangeEvent> cloneTDCCE(
      List<TextDocumentContentChangeEvent> textDocumentContentChangeEvents) {
    if (textDocumentContentChangeEvents == null) {
      return null;
    }

    List<TextDocumentContentChangeEvent> cloned =
        new ArrayList<>(textDocumentContentChangeEvents.size());
    textDocumentContentChangeEvents.stream().map(this::clone).forEach(cloned::add);

    return cloned;
  }

  TextDocumentContentChangeEvent clone(
      TextDocumentContentChangeEvent textDocumentContentChangeEvent) {
    if (textDocumentContentChangeEvent == null) {
      return null;
    }

    Range range = textDocumentContentChangeEvent.getRange();
    Integer rangeLength = textDocumentContentChangeEvent.getRangeLength();
    String text = textDocumentContentChangeEvent.getText();

    TextDocumentContentChangeEvent cloned = new TextDocumentContentChangeEvent();
    cloned.setRange(clone(range));
    cloned.setRangeLength(rangeLength);
    cloned.setText(text);

    return cloned;
  }

  DocumentOnTypeFormattingParams clone(
      DocumentOnTypeFormattingParams documentOnTypeFormattingParams) {
    if (documentOnTypeFormattingParams == null) {
      return null;
    }

    String ch = documentOnTypeFormattingParams.getCh();
    Position position = documentOnTypeFormattingParams.getPosition();
    FormattingOptions options = documentOnTypeFormattingParams.getOptions();
    TextDocumentIdentifier textDocument = documentOnTypeFormattingParams.getTextDocument();

    DocumentOnTypeFormattingParams cloned = new DocumentOnTypeFormattingParams();
    cloned.setCh(ch);
    cloned.setPosition(clone(position));
    cloned.setOptions(clone(options));
    cloned.setTextDocument(clone(textDocument));

    return cloned;
  }

  DocumentRangeFormattingParams clone(DocumentRangeFormattingParams documentRangeFormattingParams) {
    if (documentRangeFormattingParams == null) {
      return null;
    }

    Range range = documentRangeFormattingParams.getRange();
    FormattingOptions options = documentRangeFormattingParams.getOptions();
    TextDocumentIdentifier textDocument = documentRangeFormattingParams.getTextDocument();

    DocumentRangeFormattingParams cloned = new DocumentRangeFormattingParams();
    cloned.setRange(clone(range));
    cloned.setOptions(clone(options));
    cloned.setTextDocument(clone(textDocument));

    return cloned;
  }

  DocumentFormattingParams clone(DocumentFormattingParams documentFormattingParams) {
    if (documentFormattingParams == null) {
      return null;
    }

    FormattingOptions options = documentFormattingParams.getOptions();
    TextDocumentIdentifier textDocument = documentFormattingParams.getTextDocument();

    DocumentFormattingParams cloned = new DocumentFormattingParams();
    cloned.setOptions(clone(options));
    cloned.setTextDocument(clone(textDocument));

    return cloned;
  }

  FormattingOptions clone(FormattingOptions formattingOptions) {
    if (formattingOptions == null) {
      return null;
    }

    int tabSize = formattingOptions.getTabSize();
    boolean insertSpaces = formattingOptions.isInsertSpaces();

    FormattingOptions cloned = new FormattingOptions();
    cloned.setInsertSpaces(insertSpaces);
    cloned.setTabSize(tabSize);

    return cloned;
  }

  TextDocumentPositionParams clone(TextDocumentPositionParams textDocumentPositionParams) {
    if (textDocumentPositionParams == null) {
      return null;
    }

    Position position = textDocumentPositionParams.getPosition();
    TextDocumentIdentifier textDocument = textDocumentPositionParams.getTextDocument();

    TextDocumentPositionParams cloned = new TextDocumentPositionParams();
    cloned.setPosition(clone(position));
    cloned.setTextDocument(clone(textDocument));

    return cloned;
  }

  ReferenceParams clone(ReferenceParams referenceParams) {
    if (referenceParams == null) {
      return null;
    }

    ReferenceContext context = referenceParams.getContext();
    Position position = referenceParams.getPosition();
    TextDocumentIdentifier textDocument = referenceParams.getTextDocument();

    ReferenceParams cloned = new ReferenceParams();
    cloned.setContext(clone(context));
    cloned.setPosition(clone(position));
    cloned.setTextDocument(clone(textDocument));

    return cloned;
  }

  ReferenceContext clone(ReferenceContext referenceContext) {
    if (referenceContext == null) {
      return null;
    }

    boolean includeDeclaration = referenceContext.isIncludeDeclaration();

    ReferenceContext cloned = new ReferenceContext();
    cloned.setIncludeDeclaration(includeDeclaration);

    return cloned;
  }

  DocumentSymbolParams clone(DocumentSymbolParams documentSymbolParams) {
    if (documentSymbolParams == null) {
      return null;
    }

    TextDocumentIdentifier textDocument = documentSymbolParams.getTextDocument();

    DocumentSymbolParams cloned = new DocumentSymbolParams();
    cloned.setTextDocument(clone(textDocument));

    return cloned;
  }

  CompletionParams clone(CompletionParams completionParams) {
    if (completionParams == null) {
      return null;
    }

    CompletionContext completionContext = completionParams.getContext();
    Position position = completionParams.getPosition();
    TextDocumentIdentifier textDocument = completionParams.getTextDocument();

    CompletionParams cloned = new CompletionParams();
    cloned.setContext(clone(completionContext));
    cloned.setPosition(clone(position));
    cloned.setTextDocument(clone(textDocument));

    return cloned;
  }

  CompletionContext clone(CompletionContext completionContext) {
    if (completionContext == null) {
      return null;
    }

    CompletionTriggerKind triggerKind = completionContext.getTriggerKind();
    String triggerCharacter = completionContext.getTriggerCharacter();

    CompletionContext cloned = new CompletionContext();
    cloned.setTriggerCharacter(triggerCharacter);
    cloned.setTriggerKind(triggerKind);

    return cloned;
  }

  CodeActionParams clone(CodeActionParams codeActParams) {
    if (codeActParams == null) {
      return null;
    }

    CodeActionContext context = codeActParams.getContext();
    Range range = codeActParams.getRange();
    TextDocumentIdentifier textDocument = codeActParams.getTextDocument();

    CodeActionParams cloned = new CodeActionParams();
    cloned.setContext(clone(context));
    cloned.setRange(clone(range));
    cloned.setTextDocument(clone(textDocument));

    return cloned;
  }

  TextDocumentIdentifier clone(TextDocumentIdentifier textDocumentIdentifier) {
    if (textDocumentIdentifier == null) {
      return null;
    }

    String uri = textDocumentIdentifier.getUri();

    TextDocumentIdentifier cloned = new TextDocumentIdentifier();
    cloned.setUri(uri);

    return cloned;
  }

  CodeActionContext clone(CodeActionContext codeActionContext) {
    if (codeActionContext == null) {
      return null;
    }

    List<Diagnostic> diagnostics = codeActionContext.getDiagnostics();
    List<String> only = codeActionContext.getOnly();

    CodeActionContext cloned = new CodeActionContext();
    cloned.setDiagnostics(cloneD(diagnostics));
    cloned.setOnly(only == null ? null : new ArrayList<>(only));

    return cloned;
  }

  List<Diagnostic> cloneD(List<Diagnostic> diagnostics) {
    if (diagnostics == null) {
      return null;
    }

    List<Diagnostic> cloned = new ArrayList<>(diagnostics.size());
    diagnostics.stream().map(this::clone).forEach(cloned::add);

    return cloned;
  }

  Diagnostic clone(Diagnostic diagnostic) {
    if (diagnostic == null) {
      return null;
    }

    String code = diagnostic.getCode();
    String message = diagnostic.getMessage();
    Range range = diagnostic.getRange();
    List<DiagnosticRelatedInformation> relatedInformation = diagnostic.getRelatedInformation();
    DiagnosticSeverity severity = diagnostic.getSeverity();
    String source = diagnostic.getSource();

    Diagnostic cloned = new Diagnostic();

    cloned.setCode(code);
    cloned.setMessage(message);
    cloned.setRange(clone(range));
    cloned.setRelatedInformation(cloneDRI(relatedInformation));
    cloned.setSeverity(severity);
    cloned.setSource(source);

    return cloned;
  }

  List<DiagnosticRelatedInformation> cloneDRI(
      List<DiagnosticRelatedInformation> diagnosticRelatedInformations) {
    if (diagnosticRelatedInformations == null) {
      return null;
    }

    List<DiagnosticRelatedInformation> cloned =
        new ArrayList<>(diagnosticRelatedInformations.size());
    diagnosticRelatedInformations.stream().map(this::clone).forEach(cloned::add);

    return cloned;
  }

  DiagnosticRelatedInformation clone(DiagnosticRelatedInformation diagnosticRelatedInformation) {
    if (diagnosticRelatedInformation == null) {
      return null;
    }

    Location location = diagnosticRelatedInformation.getLocation();
    String message = diagnosticRelatedInformation.getMessage();

    DiagnosticRelatedInformation cloned = new DiagnosticRelatedInformation();
    cloned.setLocation(clone(location));
    cloned.setMessage(message);

    return cloned;
  }

  Location clone(Location location) {
    if (location == null) {
      return null;
    }

    Range range = location.getRange();
    String uri = location.getUri();

    Location cloned = new Location();
    cloned.setRange(clone(range));
    cloned.setUri(uri);

    return cloned;
  }

  Range clone(Range range) {
    if (range == null) {
      return null;
    }
    Position start = range.getStart();
    Position end = range.getEnd();

    Range cloned = new Range();
    cloned.setStart(clone(start));
    cloned.setEnd(clone(end));

    return cloned;
  }

  Position clone(Position position) {
    if (position == null) {
      return null;
    }

    int line = position.getLine();
    int character = position.getCharacter();

    Position cloned = new Position();
    cloned.setLine(line);
    cloned.setCharacter(character);

    return cloned;
  }
}
