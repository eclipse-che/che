/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.java.server;

import static org.eclipse.jdt.core.IJavaElement.METHOD;

import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.ide.ext.java.shared.dto.model.MethodParameters;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaElementLabels;

/**
 * The class provides business logic which allows to find hints of method's parameters. Also it
 * finds methods parameter hints in super classes. The class represents founded parameters as
 * ordinary string. Parameters in string separated by coma from each other. Example:
 *
 * <pre>
 *      class Test{
 *          void x(int x, double y);
 *      }
 *
 *      class Test2 extends Test {
 *          void x(String y);
 *      }
 *
 *      Test2 test2 = new Test2();
 *      test2.x();
 * </pre>
 *
 * <p>When we call method {@link ParametersHints#findHints(IJavaProject, String, int, int)} for
 * test2.x() we will get following: ________ int x, double y String y --------
 *
 * @author Dmitry Shnurenko
 */
@Singleton
public class ParametersHints {

  public List<MethodParameters> findHints(
      IJavaProject project, String fqn, int offset, int lineStartOffset) throws JavaModelException {
    IType type = project.findType(fqn);
    if (type.isBinary()) {
      return Collections.emptyList();
    }

    IJavaElement element = getSelectedElement(type, offset, lineStartOffset);
    if (element == null) {
      return Collections.emptyList();
    }

    IJavaElement parent = element.getParent();
    if (!(parent instanceof IType) || !(element.getElementType() == METHOD)) {
      return Collections.emptyList();
    }

    List<MethodParameters> result = new ArrayList<>();
    findHintsRecursive(element, parent, result);

    return result;
  }

  private IJavaElement getSelectedElement(IType type, int offset, int lineStartOffset)
      throws JavaModelException {
    if (offset <= lineStartOffset) {
      return null;
    }
    ICompilationUnit compilationUnit = type.getCompilationUnit();
    IJavaElement[] javaElements = compilationUnit.codeSelect(offset, 0);

    if (javaElements.length == 0) {
      return getSelectedElement(type, --offset, lineStartOffset);
    }

    IJavaElement element = javaElements[0];

    if (element.getElementType() == METHOD) {
      return element;
    }

    return getSelectedElement(type, --offset, lineStartOffset);
  }

  private void findHintsRecursive(
      IJavaElement method, IJavaElement parent, List<MethodParameters> result)
      throws JavaModelException {
    findHints(method, parent, result);

    IType type = (IType) parent;
    ITypeHierarchy typeHierarchy = type.newTypeHierarchy(new NullProgressMonitor());
    IType[] superTypes = typeHierarchy.getAllSupertypes(type);

    for (IType iType : superTypes) {
      findHintsRecursive(method, iType, result);
    }
  }

  private void findHints(IJavaElement method, IJavaElement parent, List<MethodParameters> result)
      throws JavaModelException {
    String methodName = method.getElementName();

    for (IMethod iMethod : ((IType) parent).getMethods()) {
      int methodFlag = iMethod.getFlags();
      if (Flags.isPrivate(methodFlag) || !methodName.equals(iMethod.getElementName())) {
        continue;
      }

      MethodParameters methodParameters = DtoFactory.newDto(MethodParameters.class);

      String parameters = getMethodParametersAsString(iMethod);

      methodParameters.setMethodName(methodName);
      methodParameters.setParameters(parameters);

      if (!result.contains(methodParameters)) {
        result.add(methodParameters);
      }
    }
  }

  private String getMethodParametersAsString(IMethod method) throws JavaModelException {
    ILocalVariable[] parameters = method.getParameters();

    int paramsLength = parameters.length;
    int index = 0;

    StringBuffer buffer = new StringBuffer();

    for (ILocalVariable parameter : parameters) {
      JavaElementLabels.getLocalVariableLabel(
          parameter, JavaElementLabels.F_PRE_TYPE_SIGNATURE, buffer);
      index++;

      if (index < paramsLength) {
        buffer.append(", ");
      }
    }

    return buffer.toString();
  }
}
