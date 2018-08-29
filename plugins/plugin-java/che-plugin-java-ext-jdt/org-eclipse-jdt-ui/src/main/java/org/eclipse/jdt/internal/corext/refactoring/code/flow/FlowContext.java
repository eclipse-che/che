/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.code.flow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TryStatement;

public class FlowContext {

  private static class Enum {}

  public static final Enum MERGE = new Enum();
  public static final Enum ARGUMENTS = new Enum();
  public static final Enum RETURN_VALUES = new Enum();

  private int fStart;
  private int fLength;
  private boolean fConsiderAccessMode;
  private boolean fLoopReentranceMode;
  private Enum fComputeMode;
  private IVariableBinding[] fLocals;
  private List<List<CatchClause>> fExceptionStack;

  private static final List<CatchClause> EMPTY_CATCH_CLAUSE = new ArrayList<CatchClause>(0);

  public FlowContext(int start, int length) {
    fStart = start;
    fLength = length;
    fExceptionStack = new ArrayList<List<CatchClause>>(3);
  }

  public void setConsiderAccessMode(boolean b) {
    fConsiderAccessMode = b;
  }

  public void setComputeMode(Enum mode) {
    fComputeMode = mode;
  }

  void setLoopReentranceMode(boolean b) {
    fLoopReentranceMode = b;
  }

  int getArrayLength() {
    return fLength;
  }

  int getStartingIndex() {
    return fStart;
  }

  boolean considerAccessMode() {
    return fConsiderAccessMode;
  }

  boolean isLoopReentranceMode() {
    return fLoopReentranceMode;
  }

  boolean computeMerge() {
    return fComputeMode == MERGE;
  }

  boolean computeArguments() {
    return fComputeMode == ARGUMENTS;
  }

  boolean computeReturnValues() {
    return fComputeMode == RETURN_VALUES;
  }

  public IVariableBinding getLocalFromId(int id) {
    return getLocalFromIndex(id - fStart);
  }

  public IVariableBinding getLocalFromIndex(int index) {
    if (fLocals == null || index > fLocals.length) return null;
    return fLocals[index];
  }

  public int getIndexFromLocal(IVariableBinding local) {
    if (fLocals == null) return -1;
    for (int i = 0; i < fLocals.length; i++) {
      if (fLocals[i] == local) return i;
    }
    return -1;
  }

  void manageLocal(IVariableBinding local) {
    if (fLocals == null) fLocals = new IVariableBinding[fLength];
    fLocals[local.getVariableId() - fStart] = local;
  }

  // ---- Exception handling --------------------------------------------------------

  void pushExcptions(TryStatement node) {
    List<CatchClause> catchClauses = node.catchClauses();
    if (catchClauses == null) catchClauses = EMPTY_CATCH_CLAUSE;
    fExceptionStack.add(catchClauses);
  }

  void popExceptions() {
    Assert.isTrue(fExceptionStack.size() > 0);
    fExceptionStack.remove(fExceptionStack.size() - 1);
  }

  boolean isExceptionCaught(ITypeBinding excpetionType) {
    for (Iterator<List<CatchClause>> exceptions = fExceptionStack.iterator();
        exceptions.hasNext(); ) {
      for (Iterator<CatchClause> catchClauses = exceptions.next().iterator();
          catchClauses.hasNext(); ) {
        SingleVariableDeclaration caughtException = catchClauses.next().getException();
        IVariableBinding binding = caughtException.resolveBinding();
        if (binding == null) continue;
        ITypeBinding caughtype = binding.getType();
        while (caughtype != null) {
          if (caughtype == excpetionType) return true;
          caughtype = caughtype.getSuperclass();
        }
      }
    }
    return false;
  }
}
