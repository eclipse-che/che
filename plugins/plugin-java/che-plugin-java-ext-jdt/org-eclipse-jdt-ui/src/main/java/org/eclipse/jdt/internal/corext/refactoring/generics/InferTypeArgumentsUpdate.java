/**
 * ***************************************************************************** Copyright (c) 2005,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.generics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.CastVariable2;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.CollectionElementVariable2;

public class InferTypeArgumentsUpdate {
  public static class CuUpdate {
    private List<CollectionElementVariable2> fDeclarations =
        new ArrayList<CollectionElementVariable2>();
    private List<CastVariable2> fCastsToRemove = new ArrayList<CastVariable2>();

    public List<CollectionElementVariable2> getDeclarations() {
      return fDeclarations;
    }

    public List<CastVariable2> getCastsToRemove() {
      return fCastsToRemove;
    }
  }

  private HashMap<ICompilationUnit, CuUpdate> fUpdates = new HashMap<ICompilationUnit, CuUpdate>();

  public HashMap<ICompilationUnit, CuUpdate> getUpdates() {
    return fUpdates;
  }

  public void addDeclaration(CollectionElementVariable2 elementCv) {
    ICompilationUnit cu = elementCv.getCompilationUnit();
    if (cu == null) return;
    CuUpdate update = getUpdate(cu);
    update.fDeclarations.add(elementCv);
  }

  public void addCastToRemove(CastVariable2 castCv) {
    ICompilationUnit cu = castCv.getCompilationUnit();
    CuUpdate update = getUpdate(cu);
    update.fCastsToRemove.add(castCv);
  }

  private CuUpdate getUpdate(ICompilationUnit cu) {
    Assert.isNotNull(cu);
    Object obj = fUpdates.get(cu);
    CuUpdate update;
    if (obj == null) {
      update = new CuUpdate();
      fUpdates.put(cu, update);
    } else {
      update = (CuUpdate) obj;
    }
    return update;
  }
}
