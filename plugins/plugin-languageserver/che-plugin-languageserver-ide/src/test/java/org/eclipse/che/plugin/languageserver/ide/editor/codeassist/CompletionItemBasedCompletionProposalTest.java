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
package org.eclipse.che.plugin.languageserver.ide.editor.codeassist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwtmockito.GwtMockitoTestRunner;
import java.util.Collections;
import org.eclipse.che.api.languageserver.shared.model.ExtendedCompletionItem;
import org.eclipse.che.ide.api.editor.codeassist.Completion;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.link.HasLinkedMode;
import org.eclipse.che.ide.api.editor.text.LinearRange;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.plugin.languageserver.ide.LanguageServerResources;
import org.eclipse.che.plugin.languageserver.ide.service.TextDocumentServiceClient;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextEdit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

/** */
@RunWith(GwtMockitoTestRunner.class)
public class CompletionItemBasedCompletionProposalTest {

  @Mock private HasLinkedMode editor;
  @Mock private TextDocumentServiceClient documentServiceClient;
  @Mock private LanguageServerResources resources;
  @Mock private Icon icon;
  @Mock private ServerCapabilities serverCapabilities;
  @Mock private ExtendedCompletionItem completionItem;
  @Mock private CompletionOptions completionOptions;
  @Mock private Document document;
  @Mock private CompletionItem completion;

  private CompletionItemBasedCompletionProposal proposal;

  @Before
  public void setUp() throws Exception {
    proposal =
        new CompletionItemBasedCompletionProposal(
            editor,
            completionItem,
            "",
            documentServiceClient,
            resources,
            icon,
            serverCapabilities,
            Collections.emptyList(),
            0);
    when(completionItem.getItem()).thenReturn(completion);
  }

  @Test
  public void shouldReturnNotNullCompletion() throws Exception {
    when(serverCapabilities.getCompletionProvider()).thenReturn(completionOptions);
    when(completionOptions.getResolveProvider()).thenReturn(false);

    Completion[] completions = new Completion[1];
    proposal.getCompletion(completion -> completions[0] = completion);

    assertNotNull(completions[0]);
  }

  @Test
  public void shouldUseInsertText() throws Exception {
    when(serverCapabilities.getCompletionProvider()).thenReturn(completionOptions);
    when(completionOptions.getResolveProvider()).thenReturn(false);

    when(document.getCursorPosition()).thenReturn(new TextPosition(0, 5));
    when(completion.getInsertText()).thenReturn("foo");

    Completion[] completions = new Completion[1];
    proposal.getCompletion(completion -> completions[0] = completion);

    completions[0].apply(document);

    verify(document).getCursorPosition();
    verify(document, times(1)).replace(eq(0), eq(5), eq(0), eq(5), eq("foo"));
    verify(document, times(1)).replace(anyInt(), anyInt(), anyInt(), anyInt(), anyString());
  }

  @Test
  public void shouldUseLabelIfInsertTextIsNull() throws Exception {
    when(serverCapabilities.getCompletionProvider()).thenReturn(completionOptions);
    when(completionOptions.getResolveProvider()).thenReturn(false);

    when(document.getCursorPosition()).thenReturn(new TextPosition(0, 5));
    when(completion.getInsertText()).thenReturn(null);
    when(completion.getLabel()).thenReturn("bar");

    Completion[] completions = new Completion[1];
    proposal.getCompletion(completion -> completions[0] = completion);

    completions[0].apply(document);

    verify(document).getCursorPosition();
    verify(document, times(1)).replace(eq(0), eq(5), eq(0), eq(5), eq("bar"));
    verify(document, times(1)).replace(anyInt(), anyInt(), anyInt(), anyInt(), anyString());
  }

  @Test
  public void shouldUseTextEditFirst() throws Exception {
    TextEdit textEdit = mock(TextEdit.class);
    Range range = mock(Range.class);
    Position startPosition = mock(Position.class);
    Position endPosition = mock(Position.class);

    when(serverCapabilities.getCompletionProvider()).thenReturn(completionOptions);
    when(completionOptions.getResolveProvider()).thenReturn(false);

    when(document.getCursorPosition()).thenReturn(new TextPosition(0, 5));
    when(completion.getInsertText()).thenReturn("foo");
    when(completion.getLabel()).thenReturn("bar");
    when(completion.getTextEdit()).thenReturn(textEdit);

    when(textEdit.getRange()).thenReturn(range);
    when(textEdit.getNewText()).thenReturn("fooBar");

    when(range.getStart()).thenReturn(startPosition);
    when(range.getEnd()).thenReturn(endPosition);

    when(startPosition.getLine()).thenReturn(1);
    when(startPosition.getCharacter()).thenReturn(5);

    when(endPosition.getLine()).thenReturn(1);
    when(endPosition.getCharacter()).thenReturn(5);

    Completion[] completions = new Completion[1];
    proposal.getCompletion(completion -> completions[0] = completion);

    completions[0].apply(document);

    verify(document, times(1)).replace(eq(1), eq(5), eq(1), eq(5), eq("fooBar"));
    verify(document, times(1)).replace(anyInt(), anyInt(), anyInt(), anyInt(), anyString());
  }

  @Test
  public void shouldPlaceCursorInRightPositionWithTextEdit() throws Exception {
    TextEdit textEdit = mock(TextEdit.class);
    Range range = mock(Range.class);
    Position startPosition = mock(Position.class);
    Position endPosition = mock(Position.class);

    when(serverCapabilities.getCompletionProvider()).thenReturn(completionOptions);
    when(completionOptions.getResolveProvider()).thenReturn(false);

    when(document.getCursorPosition()).thenReturn(new TextPosition(0, 5));
    when(completion.getInsertText()).thenReturn("foo");
    when(completion.getLabel()).thenReturn("bar");
    when(completion.getTextEdit()).thenReturn(textEdit);

    when(textEdit.getRange()).thenReturn(range);
    when(textEdit.getNewText()).thenReturn("fooBar");

    when(range.getStart()).thenReturn(startPosition);
    when(range.getEnd()).thenReturn(endPosition);

    when(startPosition.getLine()).thenReturn(1);
    when(startPosition.getCharacter()).thenReturn(5);

    when(endPosition.getLine()).thenReturn(1);
    when(endPosition.getCharacter()).thenReturn(5);

    when(document.getIndexFromPosition(any())).thenReturn(5);

    Completion[] completions = new Completion[1];
    proposal.getCompletion(completion -> completions[0] = completion);

    completions[0].apply(document);
    LinearRange selection = completions[0].getSelection(document);

    assertEquals(11, selection.getStartOffset());
    assertEquals(0, selection.getLength());
  }

  @Test
  public void shouldPlaceCursorInRightPositionWithInsertedText() throws Exception {

    when(serverCapabilities.getCompletionProvider()).thenReturn(completionOptions);
    when(completionOptions.getResolveProvider()).thenReturn(false);

    when(document.getCursorPosition()).thenReturn(new TextPosition(0, 5));
    when(completion.getInsertText()).thenReturn("foo");

    when(document.getIndexFromPosition(any())).thenReturn(5);

    Completion[] completions = new Completion[1];
    proposal.getCompletion(completion -> completions[0] = completion);

    completions[0].apply(document);
    LinearRange selection = completions[0].getSelection(document);

    assertEquals(8, selection.getStartOffset());
    assertEquals(0, selection.getLength());
  }
}
