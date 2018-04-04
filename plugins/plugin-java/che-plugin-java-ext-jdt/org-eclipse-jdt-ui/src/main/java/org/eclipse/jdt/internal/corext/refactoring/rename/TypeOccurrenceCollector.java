/**
 * ***************************************************************************** Copyright (c) 2007,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.rename;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.internal.corext.refactoring.CuCollectingSearchRequestor;
import org.eclipse.jdt.internal.corext.refactoring.base.ReferencesInBinaryContext;

public class TypeOccurrenceCollector extends CuCollectingSearchRequestor {

  private final String fOldName;
  private final String fOldQualifiedName;

  public TypeOccurrenceCollector(IType type) {
    this(type, null);
  }

  public TypeOccurrenceCollector(IType type, ReferencesInBinaryContext binaryRefs) {
    super(binaryRefs);
    fOldName = type.getElementName();
    fOldQualifiedName = type.getFullyQualifiedName('.');
  }

  @Override
  public void acceptSearchMatch(ICompilationUnit unit, SearchMatch match) throws CoreException {
    collectMatch(acceptSearchMatch2(unit, match));
  }

  public SearchMatch acceptSearchMatch2(ICompilationUnit unit, SearchMatch match)
      throws CoreException {
    int start = match.getOffset();
    int length = match.getLength();

    // unqualified:
    String matchText = unit.getBuffer().getText(start, length);
    if (fOldName.equals(matchText)) {
      return match;
    }

    // (partially) qualified:
    if (fOldQualifiedName.endsWith(matchText)) {
      // e.g. rename B and p.A.B ends with match A.B
      int simpleNameLenght = fOldName.length();
      match.setOffset(start + length - simpleNameLenght);
      match.setLength(simpleNameLenght);
      return match;
    }

    // Not a standard reference -- use scanner to find last identifier token:
    IScanner scanner = getScanner(unit);
    scanner.setSource(matchText.toCharArray());
    int simpleNameStart = -1;
    int simpleNameEnd = -1;
    try {
      int token = scanner.getNextToken();
      while (token != ITerminalSymbols.TokenNameEOF) {
        if (token == ITerminalSymbols.TokenNameIdentifier) {
          simpleNameStart = scanner.getCurrentTokenStartPosition();
          simpleNameEnd = scanner.getCurrentTokenEndPosition();
        }
        token = scanner.getNextToken();
      }
    } catch (InvalidInputException e) {
      // ignore
    }
    if (simpleNameStart != -1) {
      match.setOffset(start + simpleNameStart);
      match.setLength(simpleNameEnd + 1 - simpleNameStart);
    }
    return match;
  }
}
