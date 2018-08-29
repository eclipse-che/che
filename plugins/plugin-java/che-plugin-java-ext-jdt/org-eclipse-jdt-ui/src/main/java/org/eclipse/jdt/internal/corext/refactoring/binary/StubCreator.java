/**
 * ***************************************************************************** Copyright (c) 2006,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.binary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.corext.refactoring.Checks;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;

public class StubCreator {

  /** The internal string buffer */
  protected StringBuffer fBuffer;

  /** Should stubs for private member be generated as well? */
  protected final boolean fStubInvisible;

  public StubCreator(final boolean stubInvisible) {
    fStubInvisible = stubInvisible;
  }

  protected void appendEnumConstants(final IType type) throws JavaModelException {
    final IField[] fields = type.getFields();
    final List<IField> list = new ArrayList<IField>(fields.length);
    for (int index = 0; index < fields.length; index++) {
      final IField field = fields[index];
      if (Flags.isEnum(field.getFlags())) list.add(field);
    }
    for (int index = 0; index < list.size(); index++) {
      if (index > 0) fBuffer.append(","); // $NON-NLS-1$
      fBuffer.append(list.get(index).getElementName());
    }
    fBuffer.append(";"); // $NON-NLS-1$
  }

  protected void appendExpression(final String signature) {
    appendExpression(signature, false);
  }

  protected void appendExpression(String signature, boolean castNull) {
    switch (signature.charAt(0)) {
      case Signature.C_BOOLEAN:
        fBuffer.append("false"); // $NON-NLS-1$
        break;
      case Signature.C_BYTE:
      case Signature.C_CHAR:
      case Signature.C_DOUBLE:
      case Signature.C_FLOAT:
      case Signature.C_INT:
      case Signature.C_LONG:
      case Signature.C_SHORT:
        fBuffer.append("0"); // $NON-NLS-1$
        break;
      default:
        if (castNull) {
          fBuffer.append("("); // $NON-NLS-1$
          fBuffer.append(Signature.toString(signature));
          fBuffer.append(")"); // $NON-NLS-1$
        }
        fBuffer.append("null"); // $NON-NLS-1$
        break;
    }
  }

  protected void appendFieldDeclaration(final IField field) throws JavaModelException {
    appendFlags(field);
    fBuffer.append(" "); // $NON-NLS-1$
    final String signature = field.getTypeSignature();
    fBuffer.append(Signature.toString(signature));
    fBuffer.append(" "); // $NON-NLS-1$
    fBuffer.append(field.getElementName());
    if (Flags.isFinal(field.getFlags())) {
      fBuffer.append("="); // $NON-NLS-1$
      appendExpression(signature);
    }
    fBuffer.append(";"); // $NON-NLS-1$
  }

  protected void appendFlags(final IMember member) throws JavaModelException {
    if (member instanceof IAnnotatable)
      for (IAnnotation annotation : ((IAnnotatable) member).getAnnotations()) {
        appendAnnotation(annotation);
      }

    int flags = member.getFlags();
    final int kind = member.getElementType();
    if (kind == IJavaElement.TYPE) {
      flags &= ~Flags.AccSuper;
      final IType type = (IType) member;
      if (!type.isMember()) flags &= ~Flags.AccPrivate;
      if (Flags.isEnum(flags)) flags &= ~Flags.AccAbstract;
    }
    if (Flags.isEnum(flags)) flags &= ~Flags.AccFinal;
    if (kind == IJavaElement.METHOD) {
      flags &= ~Flags.AccVarargs;
      flags &= ~Flags.AccBridge;
    }
    if (flags != 0) fBuffer.append(Flags.toString(flags));
  }

  private void appendAnnotation(IAnnotation annotation) throws JavaModelException {
    String name = annotation.getElementName();
    if (!fStubInvisible && name.startsWith("sun.")) // $NON-NLS-1$
    return; // skip Sun-internal annotations

    fBuffer.append('@');
    fBuffer.append(name);
    fBuffer.append('(');

    IMemberValuePair[] memberValuePairs = annotation.getMemberValuePairs();
    for (IMemberValuePair pair : memberValuePairs) {
      fBuffer.append(pair.getMemberName());
      fBuffer.append('=');
      appendAnnotationValue(pair.getValue(), pair.getValueKind());
      fBuffer.append(',');
    }
    if (memberValuePairs.length > 0) fBuffer.deleteCharAt(fBuffer.length() - 1);

    fBuffer.append(')').append('\n');
  }

  private void appendAnnotationValue(Object value, int valueKind) throws JavaModelException {
    if (value instanceof Object[]) {
      Object[] objects = (Object[]) value;
      fBuffer.append('{');
      for (Object object : objects) {
        appendAnnotationValue(object, valueKind);
        fBuffer.append(',');
      }
      if (objects.length > 0) fBuffer.deleteCharAt(fBuffer.length() - 1);
      fBuffer.append('}');

    } else {
      switch (valueKind) {
        case IMemberValuePair.K_ANNOTATION:
          appendAnnotation((IAnnotation) value);
          break;
        case IMemberValuePair.K_STRING:
          fBuffer.append('"').append(value).append('"');
          break;

        default:
          fBuffer.append(value);
          break;
      }
    }
  }

  protected void appendMembers(final IType type, final IProgressMonitor monitor)
      throws JavaModelException {
    try {
      monitor.beginTask(RefactoringCoreMessages.StubCreationOperation_creating_type_stubs, 1);
      final IJavaElement[] children = type.getChildren();
      for (int index = 0; index < children.length; index++) {
        final IMember child = (IMember) children[index];
        final int flags = child.getFlags();
        final boolean isPrivate = Flags.isPrivate(flags);
        final boolean isDefault = !Flags.isPublic(flags) && !Flags.isProtected(flags) && !isPrivate;
        final boolean stub = fStubInvisible || (!isPrivate && !isDefault);
        if (child instanceof IType) {
          if (stub) appendTypeDeclaration((IType) child, new SubProgressMonitor(monitor, 1));
        } else if (child instanceof IField) {
          if (stub && !Flags.isEnum(flags) && !Flags.isSynthetic(flags))
            appendFieldDeclaration((IField) child);
        } else if (child instanceof IMethod) {
          final IMethod method = (IMethod) child;
          final String name = method.getElementName();
          if (method.getDeclaringType().isEnum()) {
            final int count = method.getNumberOfParameters();
            if (count == 0 && "values".equals(name)) // $NON-NLS-1$
            continue;
            if (count == 1
                && "valueOf".equals(name)
                && "Ljava.lang.String;"
                    .equals(method.getParameterTypes()[0])) // $NON-NLS-1$ //$NON-NLS-2$
            continue;
            if (method.isConstructor()) continue;
          }
          boolean skip = !stub || name.equals("<clinit>"); // $NON-NLS-1$
          if (method.isConstructor()) skip = false;
          skip = skip || Flags.isSynthetic(flags) || Flags.isBridge(flags);
          if (!skip) appendMethodDeclaration(method);
        }
        fBuffer.append("\n"); // $NON-NLS-1$
      }
    } finally {
      monitor.done();
    }
  }

  @SuppressWarnings("boxing")
  protected void appendMethodBody(final IMethod method) throws JavaModelException {
    if (method.isConstructor()) {
      final IType declaringType = method.getDeclaringType();
      String superSignature = declaringType.getSuperclassTypeSignature();
      if (superSignature != null) {
        superSignature = Signature.getTypeErasure(superSignature);
        final IType superclass =
            declaringType
                .getJavaProject()
                .findType(
                    Signature.getSignatureQualifier(superSignature),
                    Signature.getSignatureSimpleName(superSignature));
        if (superclass != null) {
          final IMethod[] superMethods = superclass.getMethods();

          // collect super constructors by parameter count
          Map<Integer, List<IMethod>> superConstructorsByParamCount =
              new TreeMap<Integer, List<IMethod>>();
          boolean multi = false;
          IMethod superConstructor = null;
          for (int i = 0; i < superMethods.length; i++) {
            IMethod superMethod = superMethods[i];
            if (superMethod.isConstructor()
                && !Flags.isPrivate(superMethod.getFlags())
                && !(Flags.isPackageDefault(superMethod.getFlags())
                    && !declaringType
                        .getPackageFragment()
                        .equals(superclass.getPackageFragment()))) {
              int paramCount = superMethod.getNumberOfParameters();
              if (paramCount == 0) {
                superConstructor = superMethod;
                break;
              }
              List<IMethod> constructors = superConstructorsByParamCount.get(paramCount);
              if (constructors == null) {
                constructors = new ArrayList<IMethod>();
                superConstructorsByParamCount.put(paramCount, constructors);
              }
              constructors.add(superMethod);
            }
          }
          if (superConstructor == null && superConstructorsByParamCount.size() > 0) {
            // look for constructors without exceptions and without parameters
            done:
            for (List<IMethod> constructors : superConstructorsByParamCount.values()) {
              for (IMethod constructor : constructors) {
                if (constructor.getExceptionTypes().length == 0) {
                  superConstructor = constructor;
                  multi = constructors.size() != 1;
                  if (multi) break;
                  else break done;
                }
                if (superConstructor == null) {
                  superConstructor = constructor;
                  multi = constructors.size() != 1;
                }
              }
            }
            if (superConstructor == null) {
              // give up, get first
              superConstructor = superConstructorsByParamCount.values().iterator().next().get(0);
              multi = true;
            }
          }
          if (superConstructor != null) {
            final String[] superParameters = superConstructor.getParameterTypes();
            final int paramLength = superParameters.length;
            fBuffer.append("super("); // $NON-NLS-1$
            if (paramLength != 0) {
              for (int index = 0; index < paramLength; index++) {
                if (index > 0) fBuffer.append(","); // $NON-NLS-1$
                appendExpression(superParameters[index], multi);
              }
            }
            fBuffer.append(");"); // $NON-NLS-1$
          }
        }
      }
    } else {
      String returnType = method.getReturnType();
      if (!Signature.SIG_VOID.equals(returnType)) {
        fBuffer.append("return "); // $NON-NLS-1$
        appendExpression(returnType);
        fBuffer.append(";"); // $NON-NLS-1$
      }
    }
  }

  protected void appendMethodDeclaration(final IMethod method) throws JavaModelException {
    appendFlags(method);
    fBuffer.append(" "); // $NON-NLS-1$
    final ITypeParameter[] parameters = method.getTypeParameters();
    if (parameters.length > 0) {
      appendTypeParameters(parameters);
      fBuffer.append(" "); // $NON-NLS-1$
    }
    final String returnType = method.getReturnType();
    if (!method.isConstructor()) {
      fBuffer.append(Signature.toString(returnType));
      fBuffer.append(" "); // $NON-NLS-1$
    }
    fBuffer.append(method.getElementName());
    fBuffer.append("("); // $NON-NLS-1$
    final String[] parameterTypes = method.getParameterTypes();
    final int flags = method.getFlags();
    final boolean varargs = Flags.isVarargs(flags);
    final int parameterLength = parameterTypes.length;
    for (int index = 0; index < parameterLength; index++) {
      if (index > 0) fBuffer.append(","); // $NON-NLS-1$
      fBuffer.append(Signature.toString(parameterTypes[index]));
      if (varargs && index == parameterLength - 1) {
        final int length = fBuffer.length();
        if (length >= 2 && fBuffer.indexOf("[]", length - 2) >= 0) // $NON-NLS-1$
        fBuffer.setLength(length - 2);
        fBuffer.append("..."); // $NON-NLS-1$
      }
      fBuffer.append(" "); // $NON-NLS-1$
      appendMethodParameterName(method, index);
    }
    fBuffer.append(")"); // $NON-NLS-1$
    final String[] exceptionTypes = method.getExceptionTypes();
    final int exceptionLength = exceptionTypes.length;
    if (exceptionLength > 0) fBuffer.append(" throws "); // $NON-NLS-1$
    for (int index = 0; index < exceptionLength; index++) {
      if (index > 0) fBuffer.append(","); // $NON-NLS-1$
      fBuffer.append(Signature.toString(exceptionTypes[index]));
    }
    if (Flags.isAbstract(flags) || Flags.isNative(flags)) fBuffer.append(";"); // $NON-NLS-1$
    else {
      fBuffer.append("{\n"); // $NON-NLS-1$
      appendMethodBody(method);
      fBuffer.append("}"); // $NON-NLS-1$
    }
  }

  /**
   * Appends a parameter name
   *
   * @param method the method
   * @param index the index of the parameter
   */
  protected void appendMethodParameterName(IMethod method, int index) {
    fBuffer.append("a"); // $NON-NLS-1$
    fBuffer.append(index);
  }

  protected void appendSuperInterfaceTypes(final IType type) throws JavaModelException {
    final String[] signatures = type.getSuperInterfaceTypeSignatures();
    if (signatures.length > 0) {
      if (type.isInterface()) fBuffer.append(" extends "); // $NON-NLS-1$
      else fBuffer.append(" implements "); // $NON-NLS-1$
    }
    for (int index = 0; index < signatures.length; index++) {
      if (index > 0) fBuffer.append(","); // $NON-NLS-1$
      fBuffer.append(Signature.toString(signatures[index]));
    }
  }

  protected void appendTopLevelType(final IType type, IProgressMonitor subProgressMonitor)
      throws JavaModelException {
    String packageName = type.getPackageFragment().getElementName();
    if (packageName.length() > 0) {
      fBuffer.append("package "); // $NON-NLS-1$
      fBuffer.append(packageName);
      fBuffer.append(";\n"); // $NON-NLS-1$
    }
    appendTypeDeclaration(type, subProgressMonitor);
  }

  protected void appendTypeDeclaration(final IType type, final IProgressMonitor monitor)
      throws JavaModelException {
    try {
      monitor.beginTask(RefactoringCoreMessages.StubCreationOperation_creating_type_stubs, 1);
      if (type.isAnnotation()) {
        appendFlags(type);
        fBuffer.append(" @interface "); // $NON-NLS-1$
        fBuffer.append(type.getElementName());
        fBuffer.append("{\n"); // $NON-NLS-1$
        appendMembers(type, new SubProgressMonitor(monitor, 1));
        fBuffer.append("}"); // $NON-NLS-1$
      } else if (type.isInterface()) {
        appendFlags(type);
        fBuffer.append(" interface "); // $NON-NLS-1$
        fBuffer.append(type.getElementName());
        appendTypeParameters(type.getTypeParameters());
        appendSuperInterfaceTypes(type);
        fBuffer.append("{\n"); // $NON-NLS-1$
        appendMembers(type, new SubProgressMonitor(monitor, 1));
        fBuffer.append("}"); // $NON-NLS-1$
      } else if (type.isClass()) {
        appendFlags(type);
        fBuffer.append(" class "); // $NON-NLS-1$
        fBuffer.append(type.getElementName());
        appendTypeParameters(type.getTypeParameters());
        final String signature = type.getSuperclassTypeSignature();
        if (signature != null) {
          fBuffer.append(" extends "); // $NON-NLS-1$
          fBuffer.append(Signature.toString(signature));
        }
        appendSuperInterfaceTypes(type);
        fBuffer.append("{\n"); // $NON-NLS-1$
        appendMembers(type, new SubProgressMonitor(monitor, 1));
        fBuffer.append("}"); // $NON-NLS-1$
      } else if (type.isEnum()) {
        appendFlags(type);
        fBuffer.append(" enum "); // $NON-NLS-1$
        fBuffer.append(type.getElementName());
        appendSuperInterfaceTypes(type);
        fBuffer.append("{\n"); // $NON-NLS-1$
        appendEnumConstants(type);
        appendMembers(type, new SubProgressMonitor(monitor, 1));
        fBuffer.append("}"); // $NON-NLS-1$
      }
    } finally {
      monitor.done();
    }
  }

  protected void appendTypeParameters(final ITypeParameter[] parameters) throws JavaModelException {
    final int length = parameters.length;
    if (length > 0) fBuffer.append("<"); // $NON-NLS-1$
    for (int index = 0; index < length; index++) {
      if (index > 0) fBuffer.append(","); // $NON-NLS-1$
      final ITypeParameter parameter = parameters[index];
      fBuffer.append(parameter.getElementName());
      final String[] bounds = parameter.getBounds();
      final int size = bounds.length;
      if (size > 0) fBuffer.append(" extends "); // $NON-NLS-1$
      for (int offset = 0; offset < size; offset++) {
        if (offset > 0) fBuffer.append(" & "); // $NON-NLS-1$
        fBuffer.append(bounds[offset]);
      }
    }
    if (length > 0) fBuffer.append(">"); // $NON-NLS-1$
  }

  /**
   * Creates and returns a stub for the given top-level type.
   *
   * @param topLevelType the top-level type
   * @param monitor the progress monitor, can be <code>null</code>
   * @return the source stub
   * @throws JavaModelException if this element does not exist or if an exception occurs while
   *     accessing its corresponding resource
   */
  public String createStub(IType topLevelType, IProgressMonitor monitor) throws JavaModelException {
    Assert.isTrue(Checks.isTopLevel(topLevelType));
    if (monitor == null) monitor = new NullProgressMonitor();

    fBuffer = new StringBuffer(2046);
    appendTopLevelType(topLevelType, monitor);
    String result = fBuffer.toString();
    fBuffer = null;
    return result;
  }
}
