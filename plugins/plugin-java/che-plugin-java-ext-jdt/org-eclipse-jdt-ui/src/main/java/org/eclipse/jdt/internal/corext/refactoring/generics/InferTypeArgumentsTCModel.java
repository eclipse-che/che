/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.generics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.CompilationUnitRange;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types.ArrayType;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types.GenericType;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types.ParameterizedType;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types.TType;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types.TypeEnvironment;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.types.TypeVariable;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.ArrayElementVariable2;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.ArrayTypeVariable2;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.CastVariable2;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.CollectionElementVariable2;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.ConstraintVariable2;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.ITypeConstraint2;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.ImmutableTypeVariable2;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.IndependentTypeVariable2;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.ParameterTypeVariable2;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.ParameterizedTypeVariable2;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.ReturnTypeVariable2;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.SubTypeConstraint2;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.TypeEquivalenceSet;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.TypeVariable2;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints2.VariableVariable2;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;

public class InferTypeArgumentsTCModel {

  protected static final boolean DEBUG =
      Boolean.valueOf(Platform.getDebugOption("org.eclipse.jdt.ui/debug/TypeConstraints"))
          .booleanValue(); // $NON-NLS-1$

  private static final String INDEXED_COLLECTION_ELEMENTS =
      "IndexedCollectionElements"; // $NON-NLS-1$
  private static final String ARRAY_ELEMENT = "ArrayElement"; // $NON-NLS-1$
  private static final String USED_IN = "UsedIn"; // $NON-NLS-1$
  private static final String METHOD_RECEIVER = "MethodReceiver"; // $NON-NLS-1$
  private static final Map<String, CollectionElementVariable2>
      EMPTY_COLLECTION_ELEMENT_VARIABLES_MAP = Collections.emptyMap();

  protected static boolean fStoreToString = DEBUG;

  /** Map from a {@link ConstraintVariable2} to itself. */
  private HashMap<ConstraintVariable2, ConstraintVariable2> fConstraintVariables;
  /** Map from a {@link ITypeConstraint2} to itself. */
  private HashMap<ITypeConstraint2, ITypeConstraint2> fTypeConstraints;

  private Collection<CastVariable2> fCastVariables;

  private HashSet<ConstraintVariable2> fCuScopedConstraintVariables;

  private TypeEnvironment fTypeEnvironment;

  private static final int MAX_TTYPE_CACHE = 1024;
  private Map<String, TType> fTTypeCache =
      new LinkedHashMap<String, TType>(MAX_TTYPE_CACHE, 0.75f, true) {
        private static final long serialVersionUID = 1L;

        @Override
        protected boolean removeEldestEntry(Entry<String, TType> eldest) {
          return size() > MAX_TTYPE_CACHE;
        }
      };

  public InferTypeArgumentsTCModel() {
    fTypeConstraints = new HashMap<ITypeConstraint2, ITypeConstraint2>();
    fConstraintVariables =
        new LinkedHashMap<
            ConstraintVariable2,
            ConstraintVariable2>(); // make iteration independent of hashCode() implementation
    fCastVariables = new ArrayList<CastVariable2>();

    fCuScopedConstraintVariables = new HashSet<ConstraintVariable2>();

    fTypeEnvironment = new TypeEnvironment(true);
  }

  /**
   * Allows for avoiding the creation of SimpleTypeConstraints based on properties of their
   * constituent ConstraintVariables and ConstraintOperators. Can be used to e.g. avoid creation of
   * constraints for assignments between built-in types.
   *
   * @param cv1
   * @param cv2
   * @return <code>true</code> iff the type constraint should really be created
   */
  protected boolean keep(ConstraintVariable2 cv1, ConstraintVariable2 cv2) {
    if ((cv1 == null || cv2 == null)) return false;

    if (cv1.equals(cv2)) {
      if (cv1 == cv2) return false;
      else Assert.isTrue(false);
    }

    if (cv1 instanceof CollectionElementVariable2 || cv2 instanceof CollectionElementVariable2)
      return true;

    if (cv1 instanceof IndependentTypeVariable2 || cv2 instanceof IndependentTypeVariable2)
      return true;

    if (isAGenericType(cv1.getType())) return true;

    if (isAGenericType(cv2.getType())) return true;

    return false;
  }

  /**
   * @param cv
   * @return a List of ITypeConstraint2s where cv is used
   */
  @SuppressWarnings("unchecked")
  public List<ITypeConstraint2> getUsedIn(ConstraintVariable2 cv) {
    Object usedIn = cv.getData(USED_IN);
    if (usedIn == null) return Collections.emptyList();
    else if (usedIn instanceof ArrayList)
      return Collections.unmodifiableList((ArrayList<ITypeConstraint2>) usedIn);
    else return Collections.singletonList((ITypeConstraint2) usedIn);
  }

  public void newCu() {
    pruneUnusedCuScopedCvs();
    fCuScopedConstraintVariables.clear();
    fTTypeCache.clear();
  }

  private void pruneUnusedCuScopedCvs() {
    for (Iterator<ConstraintVariable2> iter = fCuScopedConstraintVariables.iterator();
        iter.hasNext(); ) {
      ConstraintVariable2 cv = iter.next();
      pruneCvIfUnused(cv);
    }
  }

  private boolean pruneCvIfUnused(ConstraintVariable2 cv) {
    if (getUsedIn(cv).size() != 0) return false;

    if (cv.getTypeEquivalenceSet() != null) {
      if (cv.getTypeEquivalenceSet().getContributingVariables().length > 0) return false;
    }

    ArrayElementVariable2 arrayElementVariable = getArrayElementVariable(cv);
    if (arrayElementVariable != null && !pruneCvIfUnused(arrayElementVariable)) return false;

    Map<String, CollectionElementVariable2> elementVariables = getElementVariables(cv);
    for (Iterator<CollectionElementVariable2> iter = elementVariables.values().iterator();
        iter.hasNext(); ) {
      CollectionElementVariable2 elementVariable = iter.next();
      if (!pruneCvIfUnused(elementVariable)) return false;
    }

    fConstraintVariables.remove(cv);
    return true;
  }

  public ConstraintVariable2[] getAllConstraintVariables() {
    ConstraintVariable2[] result = new ConstraintVariable2[fConstraintVariables.size()];
    int i = 0;
    for (Iterator<ConstraintVariable2> iter = fConstraintVariables.keySet().iterator();
        iter.hasNext();
        i++) result[i] = iter.next();
    return result;
  }

  public ITypeConstraint2[] getAllTypeConstraints() {
    Set<ITypeConstraint2> typeConstraints = fTypeConstraints.keySet();
    return typeConstraints.toArray(new ITypeConstraint2[typeConstraints.size()]);
  }

  public CastVariable2[] getCastVariables() {
    return fCastVariables.toArray(new CastVariable2[fCastVariables.size()]);
  }

  /**
   * Controls calculation and storage of information for more readable toString() messages.
   *
   * <p><em>Warning: This method is for testing purposes only and should not be called except from
   * unit tests.</em>
   *
   * @param store <code>true</code> iff information for toString() should be stored
   */
  public static void setStoreToString(boolean store) {
    fStoreToString = store;
  }

  public void createSubtypeConstraint(ConstraintVariable2 cv1, ConstraintVariable2 cv2) {
    if (!keep(cv1, cv2)) return;

    ConstraintVariable2 storedCv1 = storedCv(cv1);
    ConstraintVariable2 storedCv2 = storedCv(cv2);
    ITypeConstraint2 typeConstraint = new SubTypeConstraint2(storedCv1, storedCv2);

    Object storedTc = fTypeConstraints.get(typeConstraint);
    if (storedTc == null) {
      fTypeConstraints.put(typeConstraint, typeConstraint);
    } else {
      typeConstraint = (ITypeConstraint2) storedTc;
    }

    registerCvWithTc(storedCv1, typeConstraint);
    registerCvWithTc(storedCv2, typeConstraint);
  }

  private ConstraintVariable2 storedCv(ConstraintVariable2 cv) {
    Object stored = fConstraintVariables.get(cv);
    if (stored == null) {
      fConstraintVariables.put(cv, cv);
      return cv;
    } else {
      return (ConstraintVariable2) stored;
    }
  }

  private void registerCvWithTc(ConstraintVariable2 storedCv, ITypeConstraint2 typeConstraint) {
    Object usedIn = storedCv.getData(USED_IN);
    if (usedIn == null) {
      storedCv.setData(USED_IN, typeConstraint);
    } else if (usedIn instanceof ArrayList) {
      @SuppressWarnings("unchecked")
      ArrayList<ITypeConstraint2> usedInList = (ArrayList<ITypeConstraint2>) usedIn;
      usedInList.add(typeConstraint);
    } else {
      ArrayList<ITypeConstraint2> usedInList = new ArrayList<ITypeConstraint2>(2);
      usedInList.add((ITypeConstraint2) usedIn);
      usedInList.add(typeConstraint);
      storedCv.setData(USED_IN, usedInList);
    }
  }

  public void createEqualsConstraint(
      ConstraintVariable2 leftElement, ConstraintVariable2 rightElement) {
    if (leftElement == null || rightElement == null) return;

    TypeEquivalenceSet leftSet = leftElement.getTypeEquivalenceSet();
    TypeEquivalenceSet rightSet = rightElement.getTypeEquivalenceSet();
    if (leftSet == null) {
      if (rightSet == null) {
        TypeEquivalenceSet set = new TypeEquivalenceSet(leftElement, rightElement);
        leftElement.setTypeEquivalenceSet(set);
        rightElement.setTypeEquivalenceSet(set);
      } else {
        rightSet.add(leftElement);
        leftElement.setTypeEquivalenceSet(rightSet);
      }
    } else {
      if (rightSet == null) {
        leftSet.add(rightElement);
        rightElement.setTypeEquivalenceSet(leftSet);
      } else if (leftSet == rightSet) {
        return;
      } else {
        ConstraintVariable2[] cvs = rightSet.getContributingVariables();
        leftSet.addAll(cvs);
        for (int i = 0; i < cvs.length; i++) cvs[i].setTypeEquivalenceSet(leftSet);
      }
    }
  }

  public TType createTType(ITypeBinding typeBinding) {
    String key = typeBinding.getKey();
    TType cached = fTTypeCache.get(key);
    if (cached != null) return cached;
    TType type = fTypeEnvironment.create(typeBinding);
    fTTypeCache.put(key, type);
    return type;
  }

  private TType getBoxedType(ITypeBinding typeBinding, Expression expression) {
    if (typeBinding == null) return null;

    if (!typeBinding.isPrimitive()) return createTType(typeBinding);

    if (expression == null || !expression.resolveBoxing()) return null;

    ITypeBinding boxed = Bindings.getBoxedTypeBinding(typeBinding, expression.getAST());
    return createTType(boxed);
  }

  public VariableVariable2 makeVariableVariable(IVariableBinding variableBinding) {
    if (variableBinding == null) return null;
    TType type = getBoxedType(variableBinding.getType(), /*no boxing*/ null);
    if (type == null) return null;
    VariableVariable2 cv = new VariableVariable2(type, variableBinding);
    VariableVariable2 storedCv = (VariableVariable2) storedCv(cv);
    if (storedCv == cv) {
      if (!variableBinding.isField() || Modifier.isPrivate(variableBinding.getModifiers()))
        fCuScopedConstraintVariables.add(storedCv);
      makeElementVariables(storedCv, type);
      makeArrayElementVariable(storedCv);
      if (fStoreToString)
        storedCv.setData(ConstraintVariable2.TO_STRING, '[' + variableBinding.getName() + ']');
    }
    return storedCv;
  }

  public VariableVariable2 makeDeclaredVariableVariable(
      IVariableBinding variableBinding, ICompilationUnit cu) {
    VariableVariable2 cv = makeVariableVariable(variableBinding);
    if (cv == null) return null;
    cv.setCompilationUnit(cu);
    return cv;
  }

  public TypeVariable2 makeTypeVariable(Type type) {
    ICompilationUnit cu = RefactoringASTParser.getCompilationUnit(type);
    TType ttype = getBoxedType(type.resolveBinding(), /*no boxing*/ null);
    if (ttype == null) return null;

    CompilationUnitRange range = new CompilationUnitRange(cu, type);
    TypeVariable2 typeVariable = new TypeVariable2(ttype, range);
    TypeVariable2 storedCv = (TypeVariable2) storedCv(typeVariable);
    if (storedCv == typeVariable) {
      fCuScopedConstraintVariables.add(storedCv);
      if (isAGenericType(ttype)) makeElementVariables(storedCv, ttype);
      makeArrayElementVariable(storedCv);
      if (fStoreToString) storedCv.setData(ConstraintVariable2.TO_STRING, type.toString());
    }
    return storedCv;
  }

  public IndependentTypeVariable2 makeIndependentTypeVariable(TypeVariable type) {
    IndependentTypeVariable2 cv = new IndependentTypeVariable2(type);
    IndependentTypeVariable2 storedCv = (IndependentTypeVariable2) storedCv(cv);
    if (cv == storedCv) {
      fCuScopedConstraintVariables.add(storedCv);
      //			if (isAGenericType(typeBinding)) // would lead to infinite recursion!
      //				makeElementVariables(storedCv, typeBinding);
      if (fStoreToString)
        storedCv.setData(
            ConstraintVariable2.TO_STRING,
            "IndependentType(" + type.getPrettySignature() + ")"); // $NON-NLS-1$ //$NON-NLS-2$
    }
    return storedCv;
  }

  public ParameterizedTypeVariable2 makeParameterizedTypeVariable(ITypeBinding typeBinding) {
    if (typeBinding == null) return null;
    TType type = createTType(typeBinding);
    return makeParameterizedTypeVariable(type);
  }

  private ParameterizedTypeVariable2 makeParameterizedTypeVariable(TType type) {
    Assert.isTrue(isAGenericType(type));

    ParameterizedTypeVariable2 cv = new ParameterizedTypeVariable2(type);
    ParameterizedTypeVariable2 storedCv = (ParameterizedTypeVariable2) storedCv(cv);
    if (cv == storedCv) {
      fCuScopedConstraintVariables.add(storedCv);
      makeElementVariables(storedCv, type);
      if (fStoreToString)
        storedCv.setData(
            ConstraintVariable2.TO_STRING,
            "ParameterizedType(" + type.getPrettySignature() + ")"); // $NON-NLS-1$ //$NON-NLS-2$
    }
    return storedCv;
  }

  public ArrayTypeVariable2 makeArrayTypeVariable(ITypeBinding typeBinding) {
    if (typeBinding == null) return null;
    TType type = createTType(typeBinding);
    return makeArrayTypeVariable((ArrayType) type);
  }

  private ArrayTypeVariable2 makeArrayTypeVariable(ArrayType type) {
    ArrayTypeVariable2 cv = new ArrayTypeVariable2(type);
    ArrayTypeVariable2 storedCv = (ArrayTypeVariable2) storedCv(cv);
    if (cv == storedCv) {
      fCuScopedConstraintVariables.add(storedCv);
      makeArrayElementVariable(storedCv);
      if (fStoreToString)
        storedCv.setData(
            ConstraintVariable2.TO_STRING,
            "ArrayType(" + type.getPrettySignature() + ")"); // $NON-NLS-1$ //$NON-NLS-2$
    }
    return storedCv;
  }

  public ParameterTypeVariable2 makeParameterTypeVariable(
      IMethodBinding methodBinding, int parameterIndex) {
    if (methodBinding == null) return null;
    TType type =
        getBoxedType(methodBinding.getParameterTypes()[parameterIndex], /*no boxing*/ null);
    if (type == null) return null;

    ParameterTypeVariable2 cv = new ParameterTypeVariable2(type, parameterIndex, methodBinding);
    ParameterTypeVariable2 storedCv = (ParameterTypeVariable2) storedCv(cv);
    if (storedCv == cv) {
      if (methodBinding.getDeclaringClass().isLocal()
          || Modifier.isPrivate(methodBinding.getModifiers())) fCuScopedConstraintVariables.add(cv);
      makeElementVariables(storedCv, type);
      makeArrayElementVariable(storedCv);
      if (fStoreToString)
        storedCv.setData(
            ConstraintVariable2.TO_STRING,
            "[Parameter("
                + parameterIndex
                + ","
                + Bindings.asString(methodBinding)
                + ")]"); // $NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    return storedCv;
  }

  /**
   * Make a ParameterTypeVariable2 from a method declaration. The constraint variable is always
   * stored if it passes the type filter.
   *
   * @param methodBinding
   * @param parameterIndex
   * @param cu
   * @return the ParameterTypeVariable2, or <code>null</code>
   */
  public ParameterTypeVariable2 makeDeclaredParameterTypeVariable(
      IMethodBinding methodBinding, int parameterIndex, ICompilationUnit cu) {
    if (methodBinding == null) return null;
    ParameterTypeVariable2 cv = makeParameterTypeVariable(methodBinding, parameterIndex);
    if (cv == null) return null;
    cv.setCompilationUnit(cu);
    return cv;
  }

  public ReturnTypeVariable2 makeReturnTypeVariable(IMethodBinding methodBinding) {
    if (methodBinding == null) return null;
    TType returnType = getBoxedType(methodBinding.getReturnType(), /*no boxing*/ null);
    if (returnType == null) return null;

    ReturnTypeVariable2 cv = new ReturnTypeVariable2(returnType, methodBinding);
    ReturnTypeVariable2 storedCv = (ReturnTypeVariable2) storedCv(cv);
    if (cv == storedCv) {
      makeElementVariables(storedCv, returnType);
      makeArrayElementVariable(storedCv);
      if (fStoreToString)
        storedCv.setData(
            ConstraintVariable2.TO_STRING,
            "[ReturnType(" + Bindings.asString(methodBinding) + ")]"); // $NON-NLS-1$ //$NON-NLS-2$
    }
    return storedCv;
  }

  public ReturnTypeVariable2 makeDeclaredReturnTypeVariable(
      IMethodBinding methodBinding, ICompilationUnit cu) {
    if (methodBinding == null) return null;
    ReturnTypeVariable2 cv = makeReturnTypeVariable(methodBinding);
    if (cv == null) return null;

    cv.setCompilationUnit(cu);
    if (methodBinding.getDeclaringClass().isLocal()) fCuScopedConstraintVariables.add(cv);
    return cv;
  }

  public ImmutableTypeVariable2 makeImmutableTypeVariable(
      ITypeBinding typeBinding, Expression expression) {
    //		Assert.isTrue(! typeBinding.isGenericType()); // see JDT/Core bug 80472
    TType type = getBoxedType(typeBinding, expression);
    if (type == null) return null;
    return makeImmutableTypeVariable(type);
  }

  public ImmutableTypeVariable2 makeImmutableTypeVariable(TType type) {
    ImmutableTypeVariable2 cv = new ImmutableTypeVariable2(type);
    ImmutableTypeVariable2 storedCv = (ImmutableTypeVariable2) storedCv(cv);
    if (cv == storedCv) {
      makeFixedElementVariables(storedCv, type);
      makeArrayElementVariable(storedCv);
    }
    return storedCv;
  }

  public static boolean isAGenericType(TType type) {
    return type.isGenericType()
        || type.isParameterizedType()
        || (type.isRawType() && type.getTypeDeclaration().isGenericType());
  }

  public static boolean isAGenericType(ITypeBinding type) {
    return type.isGenericType()
        || type.isParameterizedType()
        || (type.isRawType() && type.getTypeDeclaration().isGenericType());
  }

  public CastVariable2 makeCastVariable(
      CastExpression castExpression, ConstraintVariable2 expressionCv) {
    ITypeBinding typeBinding = castExpression.resolveTypeBinding();
    ICompilationUnit cu = RefactoringASTParser.getCompilationUnit(castExpression);
    CompilationUnitRange range = new CompilationUnitRange(cu, castExpression);
    CastVariable2 castCv = new CastVariable2(createTType(typeBinding), range, expressionCv);
    fCastVariables.add(castCv);
    return castCv;
  }

  public TypeEnvironment getTypeEnvironment() {
    return fTypeEnvironment;
  }

  public CollectionElementVariable2 getElementVariable(
      ConstraintVariable2 constraintVariable, ITypeBinding typeVariable) {
    Assert.isTrue(typeVariable.isTypeVariable()); // includes null check
    HashMap<String, CollectionElementVariable2> typeVarToElementVars =
        getIndexedCollectionElements(constraintVariable);
    if (typeVarToElementVars == null) return null;
    return typeVarToElementVars.get(typeVariable.getKey());
  }

  public Map<String, CollectionElementVariable2> getElementVariables(
      ConstraintVariable2 constraintVariable) {
    Map<String, CollectionElementVariable2> elementVariables =
        getIndexedCollectionElements(constraintVariable);
    if (elementVariables == null) return EMPTY_COLLECTION_ELEMENT_VARIABLES_MAP;
    else return elementVariables;
  }

  public ArrayElementVariable2 getArrayElementVariable(ConstraintVariable2 constraintVariable) {
    return (ArrayElementVariable2) constraintVariable.getData(ARRAY_ELEMENT);
  }

  private void setArrayElementVariable(
      ConstraintVariable2 constraintVariable, ArrayElementVariable2 arrayElementVariable) {
    constraintVariable.setData(ARRAY_ELEMENT, arrayElementVariable);
  }

  public void makeArrayElementVariable(ConstraintVariable2 constraintVariable2) {
    if (constraintVariable2.getType() == null || !constraintVariable2.getType().isArrayType())
      return;

    ArrayElementVariable2 storedArrayElementVariable = getArrayElementVariable(constraintVariable2);
    if (storedArrayElementVariable != null) return;

    ArrayElementVariable2 arrayElementCv = new ArrayElementVariable2(constraintVariable2);
    arrayElementCv = (ArrayElementVariable2) storedCv(arrayElementCv);
    setArrayElementVariable(constraintVariable2, arrayElementCv);

    makeArrayElementVariable(arrayElementCv); // recursive
  }

  public void makeElementVariables(ConstraintVariable2 expressionCv, TType type) {
    if (isAGenericType(type)) {
      GenericType genericType = (GenericType) type.getTypeDeclaration();
      TType[] typeParameters = genericType.getTypeParameters();
      for (int i = 0; i < typeParameters.length; i++) {
        TypeVariable typeVariable = (TypeVariable) typeParameters[i];
        makeElementVariable(expressionCv, typeVariable, i);
        if (typeVariable.getBounds().length != 0) {
          // TODO: create subtype constraints for bounds
        }
      }
    }
    makeElementVariablesFromSupertypes(expressionCv, type.getTypeDeclaration());
  }

  private void makeElementVariablesFromSupertypes(ConstraintVariable2 expressionCv, TType type) {
    TType superclass = type.getSuperclass();
    if (superclass != null) {
      makeSupertypeElementVariables(expressionCv, superclass);
    }

    TType[] interfaces = type.getInterfaces();
    for (int i = 0; i < interfaces.length; i++) {
      makeSupertypeElementVariables(expressionCv, interfaces[i]);
    }
  }

  private void makeSupertypeElementVariables(ConstraintVariable2 expressionCv, TType supertype) {
    if (supertype.isParameterizedType() || supertype.isRawType()) {
      TType[] typeArguments = null;
      if (supertype.isParameterizedType()) {
        typeArguments = ((ParameterizedType) supertype).getTypeArguments();
      }
      TypeVariable[] typeParameters =
          ((GenericType) supertype.getTypeDeclaration()).getTypeParameters();
      for (int i = 0; i < typeParameters.length; i++) {
        TypeVariable typeParameter = typeParameters[i];
        TType referenceTypeArgument;
        if (typeArguments == null) { // raw type
          referenceTypeArgument = typeParameter.getErasure();
        } else {
          referenceTypeArgument = typeArguments[i];
        }
        if (referenceTypeArgument.isTypeVariable()) {
          CollectionElementVariable2 referenceTypeArgumentCv =
              getElementVariable(expressionCv, (TypeVariable) referenceTypeArgument);
          if (referenceTypeArgumentCv != null) {
            setElementVariable(expressionCv, referenceTypeArgumentCv, typeParameter);
            continue;
          }
        }
        makeElementVariable(
            expressionCv,
            typeParameter,
            CollectionElementVariable2.NOT_DECLARED_TYPE_VARIABLE_INDEX);
      }
    }
    makeElementVariablesFromSupertypes(expressionCv, supertype);
  }

  public void makeFixedElementVariables(ConstraintVariable2 expressionCv, TType type) {
    if (isAGenericType(type)) {
      GenericType genericType = (GenericType) type.getTypeDeclaration();
      TType[] typeParameters = genericType.getTypeParameters();
      TType[] typeArguments = null;
      if (type.isParameterizedType()) typeArguments = ((ParameterizedType) type).getTypeArguments();

      for (int i = 0; i < typeParameters.length; i++) {
        TypeVariable typeVariable = (TypeVariable) typeParameters[i];
        CollectionElementVariable2 elementCv = makeElementVariable(expressionCv, typeVariable, i);
        TType referenceTypeArgument;
        if (typeArguments == null) { // raw type
          continue; // do not consider
        } else {
          referenceTypeArgument = typeArguments[i];
        }
        createEqualsConstraint(elementCv, makeImmutableTypeVariable(referenceTypeArgument));
        //				if (typeVariable.getBounds().length != 0) {
        //					//TODO: create subtype constraints for bounds
        //				}
      }
    }
    makeFixedElementVariablesFromSupertypes(expressionCv, type.getTypeDeclaration());
  }

  private void makeFixedElementVariablesFromSupertypes(
      ConstraintVariable2 expressionCv, TType type) {
    TType superclass = type.getSuperclass();
    if (superclass != null) makeFixedSupertypeElementVariables(expressionCv, superclass);

    TType[] interfaces = type.getInterfaces();
    for (int i = 0; i < interfaces.length; i++)
      makeFixedSupertypeElementVariables(expressionCv, interfaces[i]);
  }

  private void makeFixedSupertypeElementVariables(
      ConstraintVariable2 expressionCv, TType supertype) {
    if (supertype.isParameterizedType() || supertype.isRawType()) {
      TType[] typeArguments = null;
      if (supertype.isParameterizedType())
        typeArguments = ((ParameterizedType) supertype).getTypeArguments();

      TypeVariable[] typeParameters =
          ((GenericType) supertype.getTypeDeclaration()).getTypeParameters();
      for (int i = 0; i < typeParameters.length; i++) {
        TypeVariable typeParameter = typeParameters[i];
        TType referenceTypeArgument;
        if (typeArguments == null) { // raw type
          continue; // do not consider
        } else {
          referenceTypeArgument = typeArguments[i];
        }
        if (referenceTypeArgument.isTypeVariable()) {
          CollectionElementVariable2 referenceTypeArgumentCv =
              getElementVariable(expressionCv, (TypeVariable) referenceTypeArgument);
          setElementVariable(expressionCv, referenceTypeArgumentCv, typeParameter);
        } else {
          CollectionElementVariable2 elementCv =
              makeElementVariable(
                  expressionCv,
                  typeParameter,
                  CollectionElementVariable2.NOT_DECLARED_TYPE_VARIABLE_INDEX);
          createEqualsConstraint(elementCv, makeImmutableTypeVariable(referenceTypeArgument));
        }
      }
    }
    makeFixedElementVariablesFromSupertypes(expressionCv, supertype);
  }

  /**
   * Create equality constraints between generic type variables of expressionCv and referenceCv. For
   * example, the generic interface <code>java.lang.Iterable&lt;E&gt;</code> defines a method <code>
   * Iterator&lt;E&gt; iterator()</code>. Given
   *
   * <ul>
   *   <li>an expressionCv of a subtype of <code>Iterable</code>,
   *   <li>a referenceCv of a subtype of <code>Iterator</code>, and
   *   <li>a reference binding of the Iterable#iterator()'s return type (the parameterized type
   *       <code>Iterator&lt;E&gt;</code>),
   * </ul>
   *
   * this method creates an equality constraint between the type variable E in expressionCV and the
   * type variable E in referenceCV.
   *
   * @param expressionCv the type constraint variable of an expression
   * @param methodTypeVariables
   * @param referenceCv the type constraint variable of a type reference
   * @param reference the declared type reference
   */
  public void createTypeVariablesEqualityConstraints(
      ConstraintVariable2 expressionCv,
      Map<String, IndependentTypeVariable2> methodTypeVariables,
      ConstraintVariable2 referenceCv,
      TType reference) {
    if (reference.isParameterizedType() || reference.isRawType()) {
      TType[] referenceTypeArguments = null;
      if (reference.isParameterizedType()) {
        referenceTypeArguments = ((ParameterizedType) reference).getTypeArguments();
      }
      TType[] referenceTypeParameters =
          ((GenericType) reference.getTypeDeclaration()).getTypeParameters();
      for (int i = 0; i < referenceTypeParameters.length; i++) {
        TypeVariable referenceTypeParameter = (TypeVariable) referenceTypeParameters[i];
        TType referenceTypeArgument;
        if (referenceTypeArguments == null)
          referenceTypeArgument = referenceTypeParameter.getErasure();
        else referenceTypeArgument = referenceTypeArguments[i];
        if (referenceTypeArgument.isTypeVariable()) {
          ConstraintVariable2 referenceTypeArgumentCv =
              getElementTypeCv(referenceTypeArgument, expressionCv, methodTypeVariables);
          CollectionElementVariable2 referenceTypeParametersCv =
              getElementVariable(referenceCv, referenceTypeParameter);
          createEqualsConstraint(referenceTypeArgumentCv, referenceTypeParametersCv);
        } else if (referenceTypeArgument.isWildcardType()) {
          ConstraintVariable2 referenceTypeArgumentCv =
              makeImmutableTypeVariable(fTypeEnvironment.VOID); // block it for now (bug 106174)
          CollectionElementVariable2 referenceTypeParametersCv =
              getElementVariable(referenceCv, referenceTypeParameter);
          createEqualsConstraint(referenceTypeArgumentCv, referenceTypeParametersCv);

          //					WildcardType wildcardType= (WildcardType) referenceTypeArgument;
          //					if (wildcardType.isUnboundWildcardType()) {
          //						ConstraintVariable2 referenceTypeArgumentCv=
          // makeImmutableTypeVariable(wildcardType);
          //						CollectionElementVariable2 referenceTypeParametersCv=
          // getElementVariable(referenceCv, referenceTypeParameter);
          //						createEqualsConstraint(referenceTypeArgumentCv, referenceTypeParametersCv);
          //					} else if (wildcardType.isSuperWildcardType() &&
          // wildcardType.getBound().isTypeVariable()) {
          //						ConstraintVariable2 referenceTypeArgumentBoundCv=
          // getElementTypeCv(wildcardType.getBound(), expressionCv, methodTypeVariables);
          //						CollectionElementVariable2 referenceTypeParametersCv=
          // getElementVariable(referenceCv, referenceTypeParameter);
          //						//TODO: need *strict* subtype constraint?
          //						createSubtypeConstraint(referenceTypeParametersCv, referenceTypeArgumentBoundCv);
          //					}
          // else: TODO

          //				} else if (referenceTypeArgument.isParameterizedType()) {
          //					//TODO: nested containers
          //					ParameterizedType parameterizedType= (ParameterizedType) referenceTypeArgument;
          //					ParameterizedTypeVariable2 parameterizedTypeCv=
          // makeParameterizedTypeVariable(parameterizedType.getTypeDeclaration());
          //					CollectionElementVariable2 referenceTypeParametersCv=
          // getElementVariable(referenceCv, referenceTypeParameter);
          //					createEqualsConstraint(parameterizedTypeCv, referenceTypeParametersCv);
          //					createElementEqualsConstraints(parameterizedTypeCv, referenceTypeParametersCv);
        } else {
          // TODO
        }
      }

    } else if (reference.isArrayType()) {
      TType elementType = ((ArrayType) reference).getElementType();
      if (elementType.isRawType()) elementType = elementType.getErasure();
      ConstraintVariable2 elementTypeCv =
          getElementTypeCv(elementType, expressionCv, methodTypeVariables);
      ArrayElementVariable2 arrayElementTypeCv = getArrayElementVariable(referenceCv);
      createEqualsConstraint(elementTypeCv, arrayElementTypeCv);
    }
  }

  private ConstraintVariable2 getElementTypeCv(
      TType elementType,
      ConstraintVariable2 expressionCv,
      Map<String, IndependentTypeVariable2> methodTypeVariables) {
    if (elementType.isTypeVariable()) {
      ConstraintVariable2 elementTypeCv = methodTypeVariables.get(elementType.getBindingKey());
      if (elementTypeCv != null) return elementTypeCv;
      if (expressionCv != null) return getElementVariable(expressionCv, (TypeVariable) elementType);
    }
    return null;
  }

  private CollectionElementVariable2 makeElementVariable(
      ConstraintVariable2 expressionCv,
      TypeVariable typeVariable,
      int declarationTypeVariableIndex) {
    if (expressionCv == null) return null;

    CollectionElementVariable2 storedElementVariable =
        getElementVariable(expressionCv, typeVariable);
    if (storedElementVariable != null) return storedElementVariable;

    CollectionElementVariable2 cv =
        new CollectionElementVariable2(expressionCv, typeVariable, declarationTypeVariableIndex);
    cv = (CollectionElementVariable2) storedCv(cv);
    setElementVariable(expressionCv, cv, typeVariable);
    return cv;
  }

  @SuppressWarnings("unchecked")
  private static HashMap<String, CollectionElementVariable2> getIndexedCollectionElements(
      ConstraintVariable2 constraintVariable) {
    return (HashMap<String, CollectionElementVariable2>)
        constraintVariable.getData(INDEXED_COLLECTION_ELEMENTS);
  }

  private static void setElementVariable(
      ConstraintVariable2 typeConstraintVariable,
      CollectionElementVariable2 elementVariable,
      TypeVariable typeVariable) {
    HashMap<String, CollectionElementVariable2> keyToElementVar =
        getIndexedCollectionElements(typeConstraintVariable);
    String key = typeVariable.getBindingKey();
    if (keyToElementVar == null) {
      keyToElementVar = new HashMap<String, CollectionElementVariable2>();
      typeConstraintVariable.setData(INDEXED_COLLECTION_ELEMENTS, keyToElementVar);
    } else {
      Object existingElementVar = keyToElementVar.get(key);
      if (existingElementVar != null) {
        Assert.isTrue(existingElementVar == elementVariable);
      }
    }
    keyToElementVar.put(key, elementVariable);
  }

  public CollectionElementVariable2 getElementVariable(
      ConstraintVariable2 constraintVariable, TypeVariable typeVariable) {
    Assert.isTrue(typeVariable.isTypeVariable()); // includes null check
    HashMap<String, CollectionElementVariable2> typeVarToElementVars =
        getIndexedCollectionElements(constraintVariable);
    if (typeVarToElementVars == null) return null;
    return typeVarToElementVars.get(typeVariable.getBindingKey());
  }

  public void createElementEqualsConstraints(
      ConstraintVariable2 cv, ConstraintVariable2 initializerCv) {
    internalCreateElementEqualsConstraints(cv, initializerCv, false);
  }

  public void createAssignmentElementConstraints(
      ConstraintVariable2 cv, ConstraintVariable2 initializerCv) {
    internalCreateElementEqualsConstraints(cv, initializerCv, true);
  }

  private void internalCreateElementEqualsConstraints(
      ConstraintVariable2 cv, ConstraintVariable2 initializerCv, boolean isAssignment) {
    if (cv == null || initializerCv == null) return;

    Map<String, CollectionElementVariable2> leftElements = getElementVariables(cv);
    Map<String, CollectionElementVariable2> rightElements = getElementVariables(initializerCv);
    for (Iterator<Entry<String, CollectionElementVariable2>> leftIter =
            leftElements.entrySet().iterator();
        leftIter.hasNext(); ) {
      Entry<String, CollectionElementVariable2> leftEntry = leftIter.next();
      String leftTypeVariableKey = leftEntry.getKey();
      CollectionElementVariable2 rightElementVariable = rightElements.get(leftTypeVariableKey);
      if (rightElementVariable != null) {
        CollectionElementVariable2 leftElementVariable = leftEntry.getValue();
        createEqualsConstraint(leftElementVariable, rightElementVariable);
        internalCreateElementEqualsConstraints(
            leftElementVariable, rightElementVariable, false); // recursive
      }
    }

    ArrayElementVariable2 leftArrayElement = getArrayElementVariable(cv);
    ArrayElementVariable2 rightArrayElement = getArrayElementVariable(initializerCv);
    if (leftArrayElement != null && rightArrayElement != null) {
      if (isAssignment) createSubtypeConstraint(rightArrayElement, leftArrayElement);
      else createEqualsConstraint(leftArrayElement, rightArrayElement);
      internalCreateElementEqualsConstraints(
          leftArrayElement, rightArrayElement, false); // recursive
    }
  }

  /**
   * @return the receiver of the method invocation this expressionVariable depends on, or null iff
   *     no receiver is available. If the receiver stayed raw, then the method return type cannot be
   *     substituted.
   */
  public ConstraintVariable2 getMethodReceiverCv(ConstraintVariable2 expressionVariable) {
    return (ConstraintVariable2) expressionVariable.getData(METHOD_RECEIVER);
  }

  public void setMethodReceiverCV(
      ConstraintVariable2 expressionVariable, ConstraintVariable2 methodReceiverCV) {
    expressionVariable.setData(METHOD_RECEIVER, methodReceiverCV);
  }
}
