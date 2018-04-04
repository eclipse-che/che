/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.rename;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;

public class RefactoringScanner {

  private static int NO_MATCH = 0;
  private static int MATCH_QUALIFIED = 1;
  private static int MATCH_UNQUALIFIED = 2;

  public static class TextMatch {

    private int fStartPosition;
    private boolean fQualified;

    private TextMatch(int startPosition, boolean qualified) {
      fStartPosition = startPosition;
      fQualified = qualified;
    }

    /** @return the offset where the unqualified name starts */
    public int getStartPosition() {
      return fStartPosition;
    }

    public boolean isQualified() {
      return fQualified;
    }
  }

  private final String fName;
  private final String fQualifier;

  private IScanner fScanner;
  private Set<TextMatch> fMatches; // Set<TextMatch>

  public RefactoringScanner(String name, String qualifier) {
    Assert.isNotNull(name);
    Assert.isNotNull(qualifier);
    fName = name;
    fQualifier = qualifier;
  }

  public void scan(ICompilationUnit cu) throws JavaModelException {
    char[] chars = cu.getBuffer().getCharacters();
    fMatches = new HashSet<TextMatch>();
    fScanner = ToolFactory.createScanner(true, true, false, true);
    fScanner.setSource(chars);

    //		IImportContainer importContainer= cu.getImportContainer();
    //		if (importContainer.exists())
    //			fNoFlyZone= importContainer.getSourceRange();
    //		else
    //			fNoFlyZone= null;

    doScan();
    fScanner = null;
  }

  /**
   * Scan the given text.
   *
   * <p><strong>NOTE:</strong> Use only for testing.
   *
   * @param text the text
   */
  public void scan(String text) {
    char[] chars = text.toCharArray();
    fMatches = new HashSet<TextMatch>();
    fScanner = ToolFactory.createScanner(true, true, false, true);
    fScanner.setSource(chars);
    doScan();
    fScanner = null;
  }

  private void doScan() {
    try {
      int token = fScanner.getNextToken();
      while (token != ITerminalSymbols.TokenNameEOF) {
        switch (token) {
          case ITerminalSymbols.TokenNameStringLiteral:
          case ITerminalSymbols.TokenNameCOMMENT_JAVADOC:
          case ITerminalSymbols.TokenNameCOMMENT_LINE:
          case ITerminalSymbols.TokenNameCOMMENT_BLOCK:
            parseCurrentToken();
        }
        token = fScanner.getNextToken();
      }
    } catch (InvalidInputException e) {
      // ignore
    }
  }

  private static boolean isWholeWord(String value, int from, int to) {
    if (from > 0) {
      char ch = value.charAt(from - 1);
      if (Character.isLetterOrDigit(ch) || ch == '_') {
        return false;
      }
    }
    if (to < value.length()) {
      char ch = value.charAt(to);
      if (Character.isLetterOrDigit(ch) || ch == '_') {
        return false;
      }
    }
    return true;
  }

  private void parseCurrentToken() {
    // only works for references without whitespace
    String value = new String(fScanner.getRawTokenSource());
    int start = fScanner.getCurrentTokenStartPosition();
    int index = value.indexOf(fName);
    while (index != -1) {
      if (isWholeWord(value, index, index + fName.length())) {
        int ok = isQualifierOK(value, index);
        if (ok > NO_MATCH) addMatch(start + index, ok);
      }
      index = value.indexOf(fName, index + 1);
    }
  }

  private int isQualifierOK(String value, int nameStart) {
    // only works for references without whitespace
    int qualifierAfter = nameStart - 1;
    if (qualifierAfter < 0)
      // there is absolutely nothing before the name itself in the string
      return MATCH_UNQUALIFIED;

    char charBeforeName = value.charAt(qualifierAfter);
    if (!isQualifierSeparator(charBeforeName))
      // the char before the name is not a # or . - should not get here anyway
      return MATCH_UNQUALIFIED; // NO_MATCH ?

    boolean canFinish = charBeforeName == '#';
    // work through the qualifier from back to front
    for (int i = 0; i < fQualifier.length(); i++) {
      int qualifierCharPos = qualifierAfter - 1 - i;
      if (qualifierCharPos < 0)
        // the position does not exist, return OK if last read char was a non-separator
        return canFinish ? MATCH_UNQUALIFIED : NO_MATCH;

      char qualifierChar = value.charAt(qualifierCharPos);
      char goalQualifierChar = fQualifier.charAt(fQualifier.length() - 1 - i);
      if (qualifierChar != goalQualifierChar)
        // the chars do not match. return OK if last read char was a non-separator and the current
        // one a non-qualifier
        return (canFinish && !isQualifierPart(qualifierChar)) ? MATCH_UNQUALIFIED : NO_MATCH;

      canFinish = !isQualifierSeparator(qualifierChar);
    }
    int beforeQualifierPos = qualifierAfter - fQualifier.length() - 1;
    if (beforeQualifierPos >= 0) {
      char beforeQualifierChar = value.charAt(beforeQualifierPos);
      if (Character.isJavaIdentifierPart(beforeQualifierChar)) {
        return NO_MATCH;
      }
      if (isQualifierSeparator(beforeQualifierChar)) {
        if (beforeQualifierPos > 0) {
          /*
           * https://bugs.eclipse.org/bugs/show_bug.cgi?id=142508 :
           * If the character before the qualifier separator is not
           * an identifier part, then accept the match.
           */
          char precedingOne = value.charAt(beforeQualifierPos - 1);
          if (Character.isJavaIdentifierPart(precedingOne)) {
            return NO_MATCH;
          }
        }
      }
      return MATCH_QUALIFIED;
    }
    return MATCH_QUALIFIED;
  }

  private boolean isQualifierPart(char ch) {
    return Character.isJavaIdentifierPart(ch) || isQualifierSeparator(ch);
  }

  private boolean isQualifierSeparator(char c) {
    return ".#".indexOf(c) != -1; // $NON-NLS-1$
  }

  private void addMatch(int matchStart, int matchCode) {
    fMatches.add(new TextMatch(matchStart, matchCode == MATCH_QUALIFIED));
  }

  /** @return Set of TextMatch */
  public Set<TextMatch> getMatches() {
    return fMatches;
  }
}
