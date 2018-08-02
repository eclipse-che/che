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
package org.eclipse.che.plugin.languageserver.ide.editor.codeassist.snippet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * A parser for VSCode-style snippets as defined in
 * https://github.com/Microsoft/vscode/blob/0ebd01213a65231f0af8187acaf264243629e4dc/src/vs/editor/contrib/snippet/browser/snippet.md
 *
 * @author Thomas Mäder
 */
public class SnippetParser {
  private enum Token {
    DOLLAR,
    OPEN_BRACE,
    CLOSE_BRACE,
    PIPE,
    COMMA,
    COLON,
    DIGIT,
    OTHER,
    EOF
  }

  private static String escapeChars = "${}\\";
  private static String choiceEscapeChars = escapeChars + "|,:";
  private static String tokenChars = "${}|,:";

  private String text;
  private boolean inChoice;
  private Token token;
  private char value;
  private int pos;

  public SnippetParser(String snippetText) {
    this.text = snippetText;
  }

  public Snippet parse() {
    nextToken();
    return snippet((t) -> false);
  }

  private Snippet snippet(Function<Token, Boolean> stopOnToken) {
    int start = pos;
    List<Expression> expressions = new ArrayList<>();
    while (token != Token.EOF && !stopOnToken.apply(token)) {
      expressions.add(any(stopOnToken));
    }
    return new Snippet(start, pos, expressions);
  }

  private Expression any(Function<Token, Boolean> stopOnToken) {
    if (token == Token.DOLLAR) {
      nextToken();
      return dollarExpression();
    } else {
      int start2 = pos;
      StringBuilder b = new StringBuilder();
      while (token != Token.EOF && token != Token.DOLLAR && !stopOnToken.apply(token)) {
        // text
        b.append(value);
        nextToken();
      }
      return new Text(start2, pos, b.toString());
    }
  }

  private Expression dollarExpression() {
    int start = pos;
    if (token == Token.OPEN_BRACE) {
      nextToken();
      Expression e = bracedExpression();
      nextToken(); // consume closing brace
      return new DollarExpression(start, pos, e, true);
    } else {
      return new DollarExpression(start, pos, unbracedExpression(), false);
    }
  }

  private Expression unbracedExpression() {
    int start = pos;
    if (token == Token.DIGIT) {
      StringBuilder b = new StringBuilder();
      // tabstop
      b.append(value);
      nextToken();
      while (token == Token.DIGIT) {
        b.append(value);
        nextToken();
      }
      return new Placeholder(start, pos, Integer.valueOf(b.toString()), null);
    } else if (Character.isLetter(value) || value == '_') {
      StringBuilder b = new StringBuilder();
      b.append(value);
      // var
      nextToken();
      while (token != Token.EOF && (Character.isLetterOrDigit(value) || value == '_')) {
        b.append(value);
        nextToken();
      }
      return new Variable(start, pos, b.toString(), null);
    } else {
      throw new RuntimeException("unexpected");
    }
  }

  private Expression bracedExpression() {
    int start = pos;
    if (token == Token.DIGIT) {
      // tabstop
      // placeholder
      // choice
      StringBuilder b = new StringBuilder();
      // tabstop
      b.append(value);
      nextToken();
      while (token != Token.EOF && token == Token.DIGIT) {
        b.append(value);
        nextToken();
      }
      if (token == Token.COLON) {
        nextToken();
        Expression value = null;
        if (token == Token.PIPE) {
          value = parseChoice();
        } else {
          value = snippet(t -> t == Token.CLOSE_BRACE);
        }
        return new Placeholder(start, pos, Integer.valueOf(b.toString()), value);
      } else {
        return new Placeholder(start, pos, Integer.valueOf(b.toString()), null);
      }
    } else if (Character.isLetterOrDigit(value) || value == '_') {
      // var
      StringBuilder b = new StringBuilder();
      b.append(value);
      // var
      nextToken();
      while (token != Token.EOF && (Character.isLetterOrDigit(value) || value == '_')) {
        b.append(value);
        nextToken();
      }
      Expression value = null;
      if (token == Token.COLON) {
        nextToken();
        value = snippet(t -> t == Token.CLOSE_BRACE);
      }
      return new Variable(start, pos, b.toString(), value);
    } else {
      throw new RuntimeException("unexpected");
    }
  }

  private Choice parseChoice() {
    int start = pos;
    inChoice = true;
    List<String> choices = new ArrayList<>();
    nextToken(); // consume pipe token
    StringBuilder b = new StringBuilder();
    while (token != Token.EOF && token != Token.PIPE) {
      if (token == Token.COMMA || token == Token.PIPE) {
        choices.add(b.toString());
        b = new StringBuilder();
        nextToken();
      } else if (token != Token.EOF) {
        b.append(value);
        nextToken();
      }
    }
    if (token == Token.PIPE) {
      nextToken();
    }
    choices.add(b.toString());
    inChoice = false;
    return new Choice(start, pos, choices);
  }

  private void nextToken() {
    nextChar();
    if (value == '\\') {
      char nextChar = peekChar(pos + 1);
      String esc = inChoice ? choiceEscapeChars : escapeChars;
      int index = esc.indexOf(nextChar);
      if (index >= 0) {
        token = Token.values()[index];
        nextChar();
      }
    } else {
      int index = tokenChars.indexOf(value);
      if (index >= 0) {
        token = Token.values()[index];
      } else if (Character.isDigit(value)) {
        token = Token.DIGIT;
      } else if (value == (char) -1) {
        token = Token.EOF;
      } else {
        token = Token.OTHER;
      }
    }
  }

  private void nextChar() {
    value = peekChar(pos++);
  }

  private char peekChar(int pos) {
    if (pos >= text.length()) {
      return (char) -1;
    }
    return text.charAt(pos);
  }
}
