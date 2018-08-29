/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.search;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.FieldDeclarationMatch;
import org.eclipse.jdt.core.search.FieldReferenceMatch;
import org.eclipse.jdt.core.search.LocalVariableDeclarationMatch;
import org.eclipse.jdt.core.search.LocalVariableReferenceMatch;
import org.eclipse.jdt.core.search.MethodReferenceMatch;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.search.ui.text.AbstractTextSearchResult;

public class NewSearchResultCollector extends SearchRequestor {
  private AbstractTextSearchResult fSearch;
  private boolean fIgnorePotentials;

  public NewSearchResultCollector(AbstractTextSearchResult search, boolean ignorePotentials) {
    super();
    fSearch = search;
    fIgnorePotentials = ignorePotentials;
  }

  @Override
  public void acceptSearchMatch(SearchMatch match) throws CoreException {
    IJavaElement enclosingElement = (IJavaElement) match.getElement();
    if (enclosingElement != null) {
      if (fIgnorePotentials && (match.getAccuracy() == SearchMatch.A_INACCURATE)) return;
      boolean isWriteAccess = false;
      boolean isReadAccess = false;
      if (match instanceof FieldReferenceMatch) {
        FieldReferenceMatch fieldRef = ((FieldReferenceMatch) match);
        isWriteAccess = fieldRef.isWriteAccess();
        isReadAccess = fieldRef.isReadAccess();
      } else if (match instanceof FieldDeclarationMatch) {
        isWriteAccess = true;
      } else if (match instanceof LocalVariableReferenceMatch) {
        LocalVariableReferenceMatch localVarRef = ((LocalVariableReferenceMatch) match);
        isWriteAccess = localVarRef.isWriteAccess();
        isReadAccess = localVarRef.isReadAccess();
      } else if (match instanceof LocalVariableDeclarationMatch) {
        isWriteAccess = true;
      }
      boolean isSuperInvocation = false;
      if (match instanceof MethodReferenceMatch) {
        MethodReferenceMatch methodRef = (MethodReferenceMatch) match;
        isSuperInvocation = methodRef.isSuperInvocation();
      }
      fSearch.addMatch(
          new JavaElementMatch(
              enclosingElement,
              match.getRule(),
              match.getOffset(),
              match.getLength(),
              match.getAccuracy(),
              isReadAccess,
              isWriteAccess,
              match.isInsideDocComment(),
              isSuperInvocation));
    }
  }

  @Override
  public void beginReporting() {}

  @Override
  public void endReporting() {}

  @Override
  public void enterParticipant(SearchParticipant participant) {}

  @Override
  public void exitParticipant(SearchParticipant participant) {}
}
