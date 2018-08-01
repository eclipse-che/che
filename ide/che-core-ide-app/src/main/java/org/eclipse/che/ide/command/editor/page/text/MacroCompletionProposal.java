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
package org.eclipse.che.ide.command.editor.page.text;

import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import java.util.List;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.editor.codeassist.Completion;
import org.eclipse.che.ide.api.editor.codeassist.CompletionProposal;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.text.LinearRange;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.macro.Macro;
import org.eclipse.che.ide.filters.Match;

/** Completion proposal for {@link Macro} that will insert {@link Macro#getName()} value. */
class MacroCompletionProposal implements CompletionProposal {

  private final Macro macro;
  private final List<Match> matches;
  private Resources resources;
  private int offset;
  private int length;

  MacroCompletionProposal(
      Macro macro, List<Match> matches, Resources resources, int offset, int length) {
    this.macro = macro;
    this.matches = matches;
    this.resources = resources;
    this.offset = offset;
    this.length = length;
  }

  @Override
  public void getAdditionalProposalInfo(AsyncCallback<Widget> callback) {
    String documentation = macro.getDescription();
    if (documentation == null || documentation.trim().isEmpty()) {
      documentation = "No documentation found.";
    }

    Label label = new Label(documentation);
    label.setWordWrap(true);
    label.getElement().getStyle().setFontSize(13, Style.Unit.PX);
    label.getElement().getStyle().setMarginLeft(4, Style.Unit.PX);
    label.setSize("100%", "100%");
    callback.onSuccess(label);
  }

  @Override
  public String getDisplayString() {
    SafeHtmlBuilder builder = new SafeHtmlBuilder();

    String label = macro.getName();
    int pos = 0;
    for (Match highlight : matches) {
      if (highlight.getStart() == highlight.getEnd()) {
        continue;
      }

      if (pos < highlight.getStart()) {
        appendPlain(builder, label.substring(pos, highlight.getStart()));
      }

      appendHighlighted(builder, label.substring(highlight.getStart(), highlight.getEnd()));
      pos = highlight.getEnd();
    }

    if (pos < label.length()) {
      appendPlain(builder, label.substring(pos));
    }

    return builder.toSafeHtml().asString();
  }

  private void appendPlain(SafeHtmlBuilder builder, String text) {
    builder.appendEscaped(text);
  }

  private void appendHighlighted(SafeHtmlBuilder builder, String text) {
    builder.appendHtmlConstant(
        "<span class=\"" + resources.coreCss().foundPhraseHighlight() + "\">");
    builder.appendEscaped(text);
    builder.appendHtmlConstant("</span>");
  }

  @Override
  public Icon getIcon() {
    return null;
  }

  @Override
  public void getCompletion(final CompletionCallback callback) {
    callback.onCompletion(
        new Completion() {
          @Override
          public void apply(Document document) {
            document.replace(offset, length, macro.getName());
          }

          @Override
          public LinearRange getSelection(Document document) {
            LinearRange.PartialLinearRange start =
                LinearRange.createWithStart(offset + macro.getName().length());
            return start.andLength(0);
          }
        });
  }

  public Macro getMacro() {
    return macro;
  }
}
