/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.typeconstraints2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

public class TypeEquivalenceSet {

  private ConstraintVariable2[] fVariables;
  private ITypeSet fTypeEstimate;

  public TypeEquivalenceSet(ConstraintVariable2 first, ConstraintVariable2 second) {
    fVariables = new ConstraintVariable2[] {first, second};
  }

  public TypeEquivalenceSet(ConstraintVariable2 variable) {
    fVariables = new ConstraintVariable2[] {variable};
  }

  public void add(ConstraintVariable2 variable) {
    for (int i = 0; i < fVariables.length; i++) if (fVariables[i] == variable) return;

    int length = fVariables.length;
    ConstraintVariable2[] newElements = new ConstraintVariable2[length + 1];
    System.arraycopy(fVariables, 0, newElements, 0, length);
    newElements[length] = variable;
    fVariables = newElements;
  }

  public ConstraintVariable2[] getContributingVariables() {
    return fVariables;
  }

  public void addAll(ConstraintVariable2[] variables) {
    if (fVariables.length * variables.length > 100) {
      LinkedHashSet<ConstraintVariable2> result =
          new LinkedHashSet<ConstraintVariable2>(fVariables.length + variables.length);
      result.addAll(Arrays.asList(fVariables));
      result.addAll(Arrays.asList(variables));
      fVariables = result.toArray(new ConstraintVariable2[result.size()]);

    } else {
      List<ConstraintVariable2> elements = Arrays.asList(fVariables);
      ArrayList<ConstraintVariable2> result =
          new ArrayList<ConstraintVariable2>(fVariables.length + variables.length);
      result.addAll(elements);
      for (int i = 0; i < variables.length; i++) {
        ConstraintVariable2 right = variables[i];
        if (!result.contains(right)) result.add(right);
      }
      fVariables = result.toArray(new ConstraintVariable2[result.size()]);
    }
  }

  public void setTypeEstimate(ITypeSet estimate) {
    fTypeEstimate = estimate;
  }

  public ITypeSet getTypeEstimate() {
    return fTypeEstimate;
  }

  @Override
  public String toString() {
    StringBuffer result = new StringBuffer();
    if (fVariables.length > 0) result.append(fVariables[0].toString());
    for (int i = 1; i < fVariables.length; i++) {
      result.append(" =^= \n"); // $NON-NLS-1$
      result.append(fVariables[i].toString());
    }
    return result.toString();
  }
}
