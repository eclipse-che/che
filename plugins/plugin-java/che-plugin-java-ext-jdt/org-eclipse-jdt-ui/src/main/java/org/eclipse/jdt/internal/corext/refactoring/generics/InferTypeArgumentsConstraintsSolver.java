/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.generics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types.ArrayType;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types.HierarchyType;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types.TType;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.EnumeratedTypeSet;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.SingletonTypeSet;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSet;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.TypeSetEnvironment;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.ArrayElementVariable2;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.ArrayTypeVariable2;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.CastVariable2;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.CollectionElementVariable2;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.ConstraintVariable2;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.ITypeConstraint2;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.IndependentTypeVariable2;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.TTypes;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.TypeEquivalenceSet;

public class InferTypeArgumentsConstraintsSolver {

  private static class TTypeComparator implements Comparator<TType> {
    public int compare(TType o1, TType o2) {
      return o1.getPrettySignature().compareTo(o2.getPrettySignature());
    }

    public static TTypeComparator INSTANCE = new TTypeComparator();
  }

  private static final String CHOSEN_TYPE = "chosenType"; // $NON-NLS-1$

  private final InferTypeArgumentsTCModel fTCModel;
  private TypeSetEnvironment fTypeSetEnvironment;

  /**
   * The work-list used by the type constraint solver to hold the set of nodes in the constraint
   * graph that remain to be (re-)processed. Entries are <code>ConstraintVariable2</code>s.
   */
  private LinkedList<ConstraintVariable2> fWorkList;

  private InferTypeArgumentsUpdate fUpdate;

  public InferTypeArgumentsConstraintsSolver(InferTypeArgumentsTCModel typeConstraintFactory) {
    fTCModel = typeConstraintFactory;
    fWorkList = new LinkedList<ConstraintVariable2>();
  }

  public InferTypeArgumentsUpdate solveConstraints(IProgressMonitor pm) {
    pm.beginTask("", 2); // $NON-NLS-1$
    fUpdate = new InferTypeArgumentsUpdate();

    ConstraintVariable2[] allConstraintVariables = fTCModel.getAllConstraintVariables();
    if (allConstraintVariables.length == 0) return fUpdate;

    fTypeSetEnvironment = new TypeSetEnvironment(fTCModel.getTypeEnvironment());
    ParametricStructureComputer parametricStructureComputer =
        new ParametricStructureComputer(allConstraintVariables, fTCModel);
    Collection<CollectionElementVariable2> newVars =
        parametricStructureComputer.createElemConstraintVariables();

    ArrayList<ConstraintVariable2> newAllConstraintVariables = new ArrayList<ConstraintVariable2>();
    newAllConstraintVariables.addAll(Arrays.asList(allConstraintVariables));
    newAllConstraintVariables.addAll(newVars);
    allConstraintVariables =
        newAllConstraintVariables.toArray(
            new ConstraintVariable2[newAllConstraintVariables.size()]);

    // loop over all TypeEquivalenceSets and unify the elements from the fElemStructureEnv with the
    // existing TypeEquivalenceSets
    HashSet<TypeEquivalenceSet> allTypeEquivalenceSets = new HashSet<TypeEquivalenceSet>();
    for (int i = 0; i < allConstraintVariables.length; i++) {
      TypeEquivalenceSet typeEquivalenceSet = allConstraintVariables[i].getTypeEquivalenceSet();
      if (typeEquivalenceSet != null) allTypeEquivalenceSets.add(typeEquivalenceSet);
    }
    for (Iterator<TypeEquivalenceSet> iter = allTypeEquivalenceSets.iterator(); iter.hasNext(); ) {
      TypeEquivalenceSet typeEquivalenceSet = iter.next();
      ConstraintVariable2[] contributingVariables = typeEquivalenceSet.getContributingVariables();
      for (int i = 0; i < contributingVariables.length; i++) {
        for (int j = i + 1; j < contributingVariables.length; j++) {
          ConstraintVariable2 first = contributingVariables[i];
          ConstraintVariable2 second = contributingVariables[j];
          fTCModel.createElementEqualsConstraints(first, second); // recursively
        }
      }
    }
    ITypeConstraint2[] allTypeConstraints = fTCModel.getAllTypeConstraints();
    for (int i = 0; i < allTypeConstraints.length; i++) {
      ITypeConstraint2 typeConstraint = allTypeConstraints[i];
      fTCModel.createElementEqualsConstraints(typeConstraint.getLeft(), typeConstraint.getRight());
    }

    initializeTypeEstimates(allConstraintVariables);
    if (pm.isCanceled()) throw new OperationCanceledException();
    fWorkList.addAll(Arrays.asList(allConstraintVariables));
    runSolver(new SubProgressMonitor(pm, 1));
    chooseTypes(allConstraintVariables, new SubProgressMonitor(pm, 1));
    findCastsToRemove(fTCModel.getCastVariables());
    return fUpdate;
  }

  private void initializeTypeEstimates(ConstraintVariable2[] allConstraintVariables) {
    for (int i = 0; i < allConstraintVariables.length; i++) {
      ConstraintVariable2 cv = allConstraintVariables[i];
      // TODO: not necessary for types that are not used in a TypeConstraint but only as type in
      // CollectionElementVariable
      // TODO: handle nested element variables; see ParametricStructureComputer.createAndInitVars()
      TypeEquivalenceSet set = cv.getTypeEquivalenceSet();
      if (set == null) {
        set = new TypeEquivalenceSet(cv);
        set.setTypeEstimate(createInitialEstimate(cv));
        cv.setTypeEquivalenceSet(set);
      } else {
        TypeSet typeEstimate = (TypeSet) cv.getTypeEstimate();
        if (typeEstimate == null) {
          ConstraintVariable2[] cvs = set.getContributingVariables();
          typeEstimate = fTypeSetEnvironment.getUniverseTypeSet();
          for (int j = 0;
              j < cvs.length;
              j++) // TODO: optimize: just try to find an immutable CV; if not found, use Universe
          typeEstimate = typeEstimate.intersectedWith(createInitialEstimate(cvs[j]));
          set.setTypeEstimate(typeEstimate);
        }
      }
    }
  }

  private TypeSet createInitialEstimate(ConstraintVariable2 cv) {
    // TODO: check assumption: only immutable CVs have a type
    //		ParametricStructure parametricStructure= fElemStructureEnv.elemStructure(cv);
    //		if (parametricStructure != null && parametricStructure !=
    // ParametricStructureComputer.ParametricStructure.NONE) {
    //			return SubTypesOfSingleton.create(parametricStructure.getBase());
    //		}

    TType type = cv.getType();
    if (type == null) {
      return fTypeSetEnvironment.getUniverseTypeSet();

    } else if (cv instanceof IndependentTypeVariable2) {
      return fTypeSetEnvironment.getUniverseTypeSet();
      // TODO: solve problem with recursive bounds
      //			TypeVariable tv= (TypeVariable) type;
      //			TType[] bounds= tv.getBounds();
      //			TypeSet result= SubTypesOfSingleton.create(bounds[0].getErasure());
      //			for (int i= 1; i < bounds.length; i++) {
      //				result= result.intersectedWith(SubTypesOfSingleton.create(bounds[i].getErasure()));
      //			}
      //			return result;

    } else if (cv instanceof ArrayTypeVariable2) {
      return fTypeSetEnvironment.getUniverseTypeSet();
    } else if (cv instanceof ArrayElementVariable2) {
      if (cv.getType() != null && cv.getType().isTypeVariable()) {
        return fTypeSetEnvironment.getUniverseTypeSet();
      } else {
        return new SingletonTypeSet(type, fTypeSetEnvironment);
      }

    } else if (type.isVoidType()) {
      return fTypeSetEnvironment.getEmptyTypeSet();
    } else {
      return new SingletonTypeSet(type, fTypeSetEnvironment);
    }
  }

  private void runSolver(SubProgressMonitor pm) {
    pm.beginTask("", fWorkList.size() * 3); // $NON-NLS-1$
    while (!fWorkList.isEmpty()) {
      // Get a variable whose type estimate has changed
      ConstraintVariable2 cv = fWorkList.removeFirst();
      List<ITypeConstraint2> usedIn = fTCModel.getUsedIn(cv);
      processConstraints(usedIn);
      pm.worked(1);
      if (pm.isCanceled()) throw new OperationCanceledException();
    }
    pm.done();
  }

  /**
   * Given a list of <code>ITypeConstraint2</code>s that all refer to a given <code>
   * ConstraintVariable2</code> (whose type bound has presumably just changed), process each <code>
   * ITypeConstraint</code>, propagating the type bound across the constraint as needed.
   *
   * @param usedIn the <code>List</code> of <code>ITypeConstraint2</code>s to process
   */
  private void processConstraints(List<ITypeConstraint2> usedIn) {
    Iterator<ITypeConstraint2> iter = usedIn.iterator();
    while (iter.hasNext()) {
      ITypeConstraint2 tc = iter.next();

      maintainSimpleConstraint(tc);
      // TODO: prune tcs which cannot cause further changes
      // Maybe these should be pruned after a special first loop over all ConstraintVariables,
      // Since this can only happen once for every CV in the work list.
      //				if (isConstantConstraint(stc))
      //					fTypeConstraintFactory.removeUsedIn(stc, changedCv);
    }
  }

  private void maintainSimpleConstraint(ITypeConstraint2 stc) {
    ConstraintVariable2 left = stc.getLeft();
    ConstraintVariable2 right = stc.getRight();

    TypeEquivalenceSet leftSet = left.getTypeEquivalenceSet();
    TypeEquivalenceSet rightSet = right.getTypeEquivalenceSet();
    TypeSet leftEstimate = (TypeSet) leftSet.getTypeEstimate();
    TypeSet rightEstimate = (TypeSet) rightSet.getTypeEstimate();

    if (leftEstimate.isUniverse() && rightEstimate.isUniverse()) return; // nothing to do

    if (leftEstimate.equals(rightEstimate)) return; // nothing to do

    TypeSet lhsSuperTypes = leftEstimate.superTypes();
    TypeSet rhsSubTypes = rightEstimate.subTypes();

    if (!rhsSubTypes.containsAll(leftEstimate)) {
      TypeSet xsection = leftEstimate.intersectedWith(rhsSubTypes);

      //			if (xsection.isEmpty()) // too bad, but this can happen
      //				throw new IllegalStateException("Type estimate set is now empty for LHS in " + left + "
      // <= " + right + "; estimates were " + leftEstimate + " <= " + rightEstimate); //$NON-NLS-1$
      // //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

      leftSet.setTypeEstimate(xsection);
      fWorkList.addAll(Arrays.asList(leftSet.getContributingVariables()));
    }
    if (!lhsSuperTypes.containsAll(rightEstimate)) {
      TypeSet xsection = rightEstimate.intersectedWith(lhsSuperTypes);

      //			if (xsection.isEmpty())
      //				throw new IllegalStateException("Type estimate set is now empty for RHS in " + left + "
      // <= " + right + "; estimates were " + leftEstimate + " <= " + rightEstimate); //$NON-NLS-1$
      // //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

      rightSet.setTypeEstimate(xsection);
      fWorkList.addAll(Arrays.asList(rightSet.getContributingVariables()));
    }
  }

  private void chooseTypes(ConstraintVariable2[] allConstraintVariables, SubProgressMonitor pm) {
    pm.beginTask("", allConstraintVariables.length); // $NON-NLS-1$
    for (int i = 0; i < allConstraintVariables.length; i++) {
      ConstraintVariable2 cv = allConstraintVariables[i];

      TypeEquivalenceSet set = cv.getTypeEquivalenceSet();
      if (set == null)
        continue; // TODO: should not happen iff all unused constraint variables got pruned
      // TODO: should calculate only once per EquivalenceRepresentative; can throw away estimate
      // TypeSet afterwards
      TType type =
          chooseSingleType((TypeSet) cv.getTypeEstimate()); // TODO: is null for Universe TypeSet
      setChosenType(cv, type);

      if (cv instanceof CollectionElementVariable2) {
        CollectionElementVariable2 elementCv = (CollectionElementVariable2) cv;
        fUpdate.addDeclaration(elementCv);
      }

      pm.worked(1);
      if (pm.isCanceled()) throw new OperationCanceledException();
    }
    pm.done();
  }

  private TType chooseSingleType(TypeSet typeEstimate) {
    if (typeEstimate.isUniverse() || typeEstimate.isEmpty()) {
      return null;

    } else if (typeEstimate.hasUniqueLowerBound()) {
      return typeEstimate.uniqueLowerBound();

    } else {
      EnumeratedTypeSet lowerBound = typeEstimate.lowerBound().enumerate();
      ArrayList<TType> interfaceCandidates = null;
      for (Iterator<TType> iter = lowerBound.iterator(); iter.hasNext(); ) {
        TType type = iter.next();
        if (!type.isInterface()) {
          return type;
        } else {
          if (interfaceCandidates == null) interfaceCandidates = new ArrayList<TType>(2);
          interfaceCandidates.add(type);
        }
      }

      if (interfaceCandidates == null || interfaceCandidates.size() == 0) {
        return null;
      } else if (interfaceCandidates.size() == 1) {
        return interfaceCandidates.get(0);
      } else {
        ArrayList<TType> nontaggingCandidates = getNonTaggingInterfaces(interfaceCandidates);
        if (nontaggingCandidates.size() != 0) {
          return Collections.min(nontaggingCandidates, TTypeComparator.INSTANCE);
        } else {
          return Collections.min(interfaceCandidates, TTypeComparator.INSTANCE);
        }
      }
    }
  }

  private static final int MAX_CACHE = 1024;
  private Map<TType, Boolean> fInterfaceTaggingCache =
      new LinkedHashMap<TType, Boolean>(MAX_CACHE, 0.75f, true) {
        private static final long serialVersionUID = 1L;

        @Override
        protected boolean removeEldestEntry(Map.Entry<TType, Boolean> eldest) {
          return size() > MAX_CACHE;
        }
      };

  private ArrayList<TType> getNonTaggingInterfaces(ArrayList<TType> interfaceCandidates) {
    ArrayList<TType> unresolvedTypes = new ArrayList<TType>();
    ArrayList<TType> nonTagging = new ArrayList<TType>();

    for (int i = 0; i < interfaceCandidates.size(); i++) {
      TType interf = interfaceCandidates.get(i);
      Object isTagging = fInterfaceTaggingCache.get(interf);
      if (isTagging == null) unresolvedTypes.add(interf);
      else if (isTagging == Boolean.FALSE) nonTagging.add(interf);
    }

    if (unresolvedTypes.size() != 0) {
      TType[] interfaces = unresolvedTypes.toArray(new TType[unresolvedTypes.size()]);
      for (int i = 0; i < interfaces.length; i++) {
        TType interf = interfaces[i];
        if (isTaggingInterface(interf)) {
          fInterfaceTaggingCache.put(interf, Boolean.TRUE);
        } else {
          fInterfaceTaggingCache.put(interf, Boolean.FALSE);
          nonTagging.add(interf);
        }
      }
    }

    return nonTagging;
  }

  private static boolean isTaggingInterface(TType interf) {
    if (interf instanceof HierarchyType) {
      try {
        return ((HierarchyType) interf).getJavaElementType().getMethods().length == 0;
      } catch (JavaModelException e) {
        // assume it's not
      }
    }
    return false;
  }

  private void findCastsToRemove(CastVariable2[] castVariables) {
    for (int i = 0; i < castVariables.length; i++) {
      CastVariable2 castCv = castVariables[i];
      ConstraintVariable2 expressionVariable = castCv.getExpressionVariable();
      TType chosenType = InferTypeArgumentsConstraintsSolver.getChosenType(expressionVariable);
      TType castType = castCv.getType();
      TType expressionType = expressionVariable.getType();
      if (chosenType != null && TTypes.canAssignTo(chosenType, castType)) {
        if (chosenType.equals(expressionType))
          continue; // The type has not changed. Don't remove the cast, since it could be
        // there to get access to default-visible members or to
        // unify types of conditional expressions.
        fUpdate.addCastToRemove(castCv);

      } else if (expressionVariable instanceof ArrayTypeVariable2
          && castType.isArrayType()) { // bug 97258
        ArrayElementVariable2 arrayElementCv = fTCModel.getArrayElementVariable(expressionVariable);
        if (arrayElementCv == null) continue;
        TType chosenArrayElementType =
            InferTypeArgumentsConstraintsSolver.getChosenType(arrayElementCv);
        if (chosenArrayElementType != null
            && TTypes.canAssignTo(
                chosenArrayElementType, ((ArrayType) castType).getComponentType())) {
          if (expressionType instanceof ArrayType
              && chosenArrayElementType.equals(((ArrayType) expressionType).getComponentType()))
            continue; // The type has not changed. Don't remove the cast, since it could be
          // there to unify types of conditional expressions.
          fUpdate.addCastToRemove(castCv);
        }
      }
    }
  }

  public static TType getChosenType(ConstraintVariable2 cv) {
    TType type = (TType) cv.getData(CHOSEN_TYPE);
    if (type != null) return type;
    TypeEquivalenceSet set = cv.getTypeEquivalenceSet();
    if (set == null) { // TODO: should not have to set this here. Clean up when caching chosen type
      return null;
      //			// no representative == no restriction
      //			set= new TypeEquivalenceSet(cv);
      //			set.setTypeEstimate(TypeUniverseSet.create());
      //			cv.setTypeEquivalenceSet(set);
    }
    return cv.getTypeEstimate().chooseSingleType();
  }

  private static void setChosenType(ConstraintVariable2 cv, TType type) {
    cv.setData(CHOSEN_TYPE, type);
  }
}
