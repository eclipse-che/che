/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.ceylon.ide;

import static java.util.Arrays.asList;

import javax.inject.Provider;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;
import org.eclipse.che.plugin.ceylon.shared.Constants;

public class CeylonLanguageDescriptionProvider implements Provider<LanguageDescription> {
  private static final String[] EXTENSIONS = new String[] {Constants.CEYLON_EXT};
  private static final String MIME_TYPE = "text/x-ceylon";

  @Override
  public LanguageDescription get() {
    LanguageDescription description = new LanguageDescription();
    description.setFileExtensions(asList(EXTENSIONS));
    description.setLanguageId(Constants.LANGUAGE_ID);
    description.setMimeType(MIME_TYPE);
    description.setHighlightingConfiguration(
        "[\n"
            + "  { include: \"orion.lib#string_doubleQuote\"},\n"
            + "  { include: \"orion.lib#string_singleQuote\"},\n"
            + "  { include: \"orion.c-like#comment_singleLine\"},\n"
            + "  { include: \"orion.c-like#comment_block\"},\n"
            + "  { include: \"orion.lib#brace_open\"},\n"
            + "  { include: \"orion.lib#brace_close\"},\n"
            + "  { include: \"orion.lib#bracket_open\"},\n"
            + "  { include: \"orion.lib#bracket_close\"},\n"
            + "  { include: \"orion.lib#parenthesis_open\"},\n"
            + "  { include: \"orion.lib#parenthesis_close\"},\n"
            + "  { include: \"orion.lib#operator\"},\n"
            + "  { match: \"^#!/.*$\\n\", name: \"comment.shebang.ceylon\"},\n"
            + "  { match: \"\\\\b(assembly|module|package|import|alias|class|interface|object|"
            + "given|value|assign|void|function|new|of|extends|satisfies|adapts|abstracts|in|out|"
            + "return|break|continue|throw|assert|dynamic|if|else|switch|case|for|while|try|"
            + "catch|finally|then|let|this|outer|super|is|exists|nonempty)\\\\b\", name: \"keyword.control.ceylon\"},\n"
            + "  { match: \"\\\\b(doc|by|license|see|throws|tagged|shared|abstract|formal|default|actual|"
            + "variable|late|native|deprecated|final|sealed|annotation|suppressWarnings|static)\\\\b\", name: \"keyword.other.ceylon\" },\n"
            + "  { match: \"([A-Z][a-zA-Z0-9_]*|\\\\\\\\I[a-zA-Z0-9_]+)\", name: \"entity.name.class.ceylon\" },\n"
            + "  { match: \"([a-z][a-zA-Z0-9_]*|\\\\\\\\i[a-zA-Z0-9_]+)\", name: \"variable.other.ceylon\"},\n"
            + "  { begin: \"\\\"\\\"\\\"\", end: \"\\\"\\\"\\\"\", name: \"string.verbatim.ceylon\"},\n"
            + "  { begin: \"'\", end: \"'\", name: \"string.ceylon\", patterns: [{ match: \"\\\\\\\\.\", name: \"constant.character.escape.ceylon\"}]},\n"
            + "  { begin: \"\\\"\", end: \"\\\"|(``)\", name: \"string.template.head.ceylon\", patterns: [{ match: \"\\\\\\\\.\", name: \"constant.character.escape.ceylon\"}]},\n"
            + "  { begin: \"``\", end: \"\\\"|``\", name: \"string.template.midOrtail.ceylon\" },\n"
            + "  { match: \"\\\\$(([01]+(_[01]+)+)|[01]+)\", name: \"constant.numeric.binary.ceylon\"},\n"
            + "  { match: \"#(([0-9ABCDEF]+(_[0-9ABCDEF]+)+)|[0-9ABCDEF]+)\", name: \"constant.numeric.hexa.ceylon\"},\n"
            + "  { match: \"-?(([0-9]+(_[0-9]+)+)|[0-9]+)\\\\.(([0-9]+(_[0-9]+)+)|[0-9]+)(([eE]-?(([0-9]+(_[0-9]+)+)|[0-9]+))|[kmgtpKMGTP])?\", name: \"constant.numeric.floating.ceylon\"},\n"
            + "  { match: \"-?(([0-9]+(_[0-9]+)+)|[0-9]+)[kmgtpKMGTP]?\", name: \"constant.numeric.decimal.ceylon\"}"
            + "]");

    return description;
  }
}
