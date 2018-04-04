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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.corext.refactoring.Checks;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringArguments;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringAvailabilityTester;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.refactoring.base.JavaStatusContext;
import org.eclipse.jdt.internal.corext.refactoring.util.TextChangeManager;
import org.eclipse.jdt.internal.corext.util.JdtFlags;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.ltk.core.refactoring.GroupCategorySet;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;

public class RenameVirtualMethodProcessor extends RenameMethodProcessor {

  private IMethod fOriginalMethod;
  private boolean fActivationChecked;
  private ITypeHierarchy fCachedHierarchy = null;

  /**
   * Creates a new rename method processor.
   *
   * @param method the method
   */
  public RenameVirtualMethodProcessor(IMethod method) {
    super(method);
    fOriginalMethod = getMethod();
  }

  /**
   * Creates a new rename method processor from arguments
   *
   * @param method the method
   * @param arguments the arguments
   * @param status the resulting status
   */
  public RenameVirtualMethodProcessor(
      IMethod method, JavaRefactoringArguments arguments, RefactoringStatus status) {
    this(method);
    RefactoringStatus initializeStatus = initialize(arguments);
    status.merge(initializeStatus);
    fOriginalMethod = getMethod();
  }

  /*
   * int. not javadoc'd
   *
   * Protected constructor; only called from RenameTypeProcessor. Initializes
   * the method processor with an already resolved top level and ripple
   * methods.
   *
   */
  RenameVirtualMethodProcessor(
      IMethod topLevel,
      IMethod[] ripples,
      TextChangeManager changeManager,
      ITypeHierarchy hierarchy,
      GroupCategorySet categorySet) {
    super(topLevel, changeManager, categorySet);
    fOriginalMethod = getMethod();
    fActivationChecked = true; // is top level
    fCachedHierarchy = hierarchy; // may be null
    setMethodsToRename(ripples);
  }

  public IMethod getOriginalMethod() {
    return fOriginalMethod;
  }

  private ITypeHierarchy getCachedHierarchy(IType declaring, IProgressMonitor monitor)
      throws JavaModelException {
    if (fCachedHierarchy != null && declaring.equals(fCachedHierarchy.getType()))
      return fCachedHierarchy;
    fCachedHierarchy = declaring.newTypeHierarchy(new SubProgressMonitor(monitor, 1));
    return fCachedHierarchy;
  }

  @Override
  public boolean isApplicable() throws CoreException {
    return RefactoringAvailabilityTester.isRenameVirtualMethodAvailable(getMethod());
  }

  // ------------ preconditions -------------

  @Override
  public RefactoringStatus checkInitialConditions(IProgressMonitor monitor) throws CoreException {
    RefactoringStatus result = super.checkInitialConditions(monitor);
    if (result.hasFatalError()) return result;
    try {
      monitor.beginTask("", 3); // $NON-NLS-1$
      if (!fActivationChecked) {
        // the following code may change the method to be changed.
        IMethod method = getMethod();
        fOriginalMethod = method;

        ITypeHierarchy hierarchy = null;
        IType declaringType = method.getDeclaringType();
        if (!declaringType.isInterface())
          hierarchy = getCachedHierarchy(declaringType, new SubProgressMonitor(monitor, 1));

        IMethod topmost = getMethod();
        if (MethodChecks.isVirtual(topmost))
          topmost = MethodChecks.getTopmostMethod(getMethod(), hierarchy, monitor);
        if (topmost != null) initialize(topmost);
        fActivationChecked = true;
      }
    } finally {
      monitor.done();
    }
    return result;
  }

  @Override
  protected RefactoringStatus doCheckFinalConditions(
      IProgressMonitor pm, CheckConditionsContext checkContext) throws CoreException {
    try {
      pm.beginTask("", 9); // $NON-NLS-1$
      RefactoringStatus result = new RefactoringStatus();

      result.merge(super.doCheckFinalConditions(new SubProgressMonitor(pm, 7), checkContext));
      if (result.hasFatalError()) return result;

      final IMethod method = getMethod();
      final IType declaring = method.getDeclaringType();
      final ITypeHierarchy hierarchy = getCachedHierarchy(declaring, new SubProgressMonitor(pm, 1));
      final String name = getNewElementName();
      if (declaring.isInterface()) {
        if (isSpecialCase())
          result.addError(RefactoringCoreMessages.RenameMethodInInterfaceRefactoring_special_case);
        pm.worked(1);
        IMethod[] relatedMethods =
            relatedTypeDeclaresMethodName(new SubProgressMonitor(pm, 1), method, name);
        for (int i = 0; i < relatedMethods.length; i++) {
          IMethod relatedMethod = relatedMethods[i];
          RefactoringStatusContext context = JavaStatusContext.create(relatedMethod);
          result.addError(
              RefactoringCoreMessages.RenameMethodInInterfaceRefactoring_already_defined, context);
        }
      } else {
        if (classesDeclareOverridingNativeMethod(hierarchy.getAllSubtypes(declaring))) {
          result.addError(
              Messages.format(
                  RefactoringCoreMessages.RenameVirtualMethodRefactoring_requieres_renaming_native,
                  new String[] {
                    BasicElementLabels.getJavaElementName(method.getElementName()),
                    "UnsatisfiedLinkError"
                  })); // $NON-NLS-1$
        }

        IMethod[] hierarchyMethods =
            hierarchyDeclaresMethodName(new SubProgressMonitor(pm, 1), hierarchy, method, name);
        for (int i = 0; i < hierarchyMethods.length; i++) {
          IMethod hierarchyMethod = hierarchyMethods[i];
          RefactoringStatusContext context = JavaStatusContext.create(hierarchyMethod);
          if (Checks.compareParamTypes(
              method.getParameterTypes(), hierarchyMethod.getParameterTypes())) {
            result.addError(
                Messages.format(
                    RefactoringCoreMessages.RenameVirtualMethodRefactoring_hierarchy_declares2,
                    BasicElementLabels.getJavaElementName(name)),
                context);
          } else {
            result.addWarning(
                Messages.format(
                    RefactoringCoreMessages.RenameVirtualMethodRefactoring_hierarchy_declares1,
                    BasicElementLabels.getJavaElementName(name)),
                context);
          }
        }
      }
      fCachedHierarchy = null;
      return result;
    } finally {
      pm.done();
    }
  }

  // ---- Interface checks -------------------------------------

  private IMethod[] relatedTypeDeclaresMethodName(
      IProgressMonitor pm, IMethod method, String newName) throws CoreException {
    try {
      Set<IMethod> result = new HashSet<IMethod>();
      Set<IType> types = getRelatedTypes();
      pm.beginTask("", types.size()); // $NON-NLS-1$
      for (Iterator<IType> iter = types.iterator(); iter.hasNext(); ) {
        final IMethod found = Checks.findMethod(method, iter.next());
        final IType declaring = found.getDeclaringType();
        result.addAll(
            Arrays.asList(
                hierarchyDeclaresMethodName(
                    new SubProgressMonitor(pm, 1),
                    declaring.newTypeHierarchy(new SubProgressMonitor(pm, 1)),
                    found,
                    newName)));
      }
      return result.toArray(new IMethod[result.size()]);
    } finally {
      pm.done();
    }
  }

  private boolean isSpecialCase() throws CoreException {
    String[] noParams = new String[0];
    String[] specialNames =
        new String[] {
          "toString",
          "toString",
          "toString",
          "toString",
          "equals", // $NON-NLS-5$ //$NON-NLS-4$ //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
          "equals",
          "getClass",
          "getClass",
          "hashCode",
          "notify", // $NON-NLS-5$ //$NON-NLS-4$ //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
          "notifyAll",
          "wait",
          "wait",
          "wait"
        }; // $NON-NLS-4$ //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
    String[][] specialParamTypes =
        new String[][] {
          noParams,
          noParams,
          noParams,
          noParams,
          {"QObject;"},
          {"Qjava.lang.Object;"},
          noParams,
          noParams, // $NON-NLS-2$ //$NON-NLS-1$
          noParams,
          noParams,
          noParams,
          {Signature.SIG_LONG, Signature.SIG_INT},
          {Signature.SIG_LONG},
          noParams
        };
    String[] specialReturnTypes =
        new String[] {
          "QString;",
          "QString;",
          "Qjava.lang.String;",
          "Qjava.lang.String;", // $NON-NLS-4$ //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
          Signature.SIG_BOOLEAN,
          Signature.SIG_BOOLEAN,
          "QClass;",
          "Qjava.lang.Class;", // $NON-NLS-2$ //$NON-NLS-1$
          Signature.SIG_INT,
          Signature.SIG_VOID,
          Signature.SIG_VOID,
          Signature.SIG_VOID,
          Signature.SIG_VOID,
          Signature.SIG_VOID
        };
    Assert.isTrue(
        (specialNames.length == specialParamTypes.length)
            && (specialParamTypes.length == specialReturnTypes.length));
    for (int i = 0; i < specialNames.length; i++) {
      if (specialNames[i].equals(getNewElementName())
          && Checks.compareParamTypes(getMethod().getParameterTypes(), specialParamTypes[i])
          && !specialReturnTypes[i].equals(getMethod().getReturnType())) {
        return true;
      }
    }
    return false;
  }

  private Set<IType> getRelatedTypes() {
    Set<IMethod> methods = getMethodsToRename();
    Set<IType> result = new HashSet<IType>(methods.size());
    for (Iterator<IMethod> iter = methods.iterator(); iter.hasNext(); ) {
      result.add(iter.next().getDeclaringType());
    }
    return result;
  }

  // ---- Class checks -------------------------------------

  private boolean classesDeclareOverridingNativeMethod(IType[] classes) throws CoreException {
    for (int i = 0; i < classes.length; i++) {
      IMethod[] methods = classes[i].getMethods();
      for (int j = 0; j < methods.length; j++) {
        if ((!methods[j].equals(getMethod()))
            && (JdtFlags.isNative(methods[j]))
            && (null != Checks.findSimilarMethod(getMethod(), new IMethod[] {methods[j]})))
          return true;
      }
    }
    return false;
  }

  public String getDelegateUpdatingTitle(boolean plural) {
    if (plural) return RefactoringCoreMessages.DelegateMethodCreator_keep_original_renamed_plural;
    else return RefactoringCoreMessages.DelegateMethodCreator_keep_original_renamed_singular;
  }
}
