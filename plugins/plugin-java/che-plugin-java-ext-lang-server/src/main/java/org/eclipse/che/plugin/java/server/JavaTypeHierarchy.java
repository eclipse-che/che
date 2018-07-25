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

import static java.util.Collections.emptyList;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.ide.ext.java.shared.dto.ImplementationsDescriptorDTO;
import org.eclipse.che.ide.ext.java.shared.dto.Region;
import org.eclipse.che.ide.ext.java.shared.dto.model.Member;
import org.eclipse.che.ide.ext.java.shared.dto.model.Type;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICodeAssist;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.util.MethodOverrideTester;

/**
 * A type hierarchy provides navigations between a type and its resolved supertypes and subtypes for
 * a specific type or for all types within a region.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class JavaTypeHierarchy {

  @Inject
  public JavaTypeHierarchy() {}

  /**
   * Get all implementations of selected Java Element.
   *
   * @param project opened project
   * @param fqn fully qualified name of the class file
   * @param offset cursor position
   * @return descriptor of the implementations
   * @throws JavaModelException when JavaModel has a failure
   */
  public ImplementationsDescriptorDTO getImplementations(
      IJavaProject project, String fqn, int offset) throws JavaModelException {
    ImplementationsDescriptorDTO implementationDescriptor =
        DtoFactory.newDto(ImplementationsDescriptorDTO.class);

    IJavaElement element = getJavaElement(project, fqn, offset);
    if (element == null) {
      return implementationDescriptor.withImplementations(emptyList());
    }

    List<Type> implementations = new ArrayList<>();

    implementationDescriptor.setImplementations(implementations);

    switch (element.getElementType()) {
      case 7: // type
        findSubTypes(element, implementations);
        implementationDescriptor.setMemberName(element.getElementName());
        break;
      case 9: // method
        findTypesWithSubMethods(element, implementations);
        implementationDescriptor.setMemberName(element.getElementName());
        break;
      default:
        break;
    }

    return implementationDescriptor;
  }

  private IJavaElement getJavaElement(IJavaProject project, String fqn, int offset)
      throws JavaModelException {
    IJavaElement originalElement = null;
    IType type = project.findType(fqn);
    ICodeAssist codeAssist;
    if (type.isBinary()) {
      codeAssist = type.getClassFile();
    } else {
      codeAssist = type.getCompilationUnit();
    }

    IJavaElement[] elements = null;
    if (codeAssist != null) {
      elements = codeAssist.codeSelect(offset, 0);
    }

    if (elements != null && elements.length > 0) {
      originalElement = elements[0];
    }
    return originalElement;
  }

  private void findSubTypes(IJavaElement element, List<Type> implementations)
      throws JavaModelException {
    IType type = (IType) element;
    ITypeHierarchy typeHierarchy = type.newTypeHierarchy(new NullProgressMonitor());
    IType[] implTypes = typeHierarchy.getAllSubtypes(type);

    for (IType implType : implTypes) {
      Type dto = convertToTypeDTO(implType);
      implementations.add(dto);
    }
  }

  private void findTypesWithSubMethods(IJavaElement element, List<Type> implementations)
      throws JavaModelException {
    IMethod selectedMethod = (IMethod) element;
    IType parentType = selectedMethod.getDeclaringType();
    if (parentType == null) {
      return;
    }
    ITypeHierarchy typeHierarchy = parentType.newTypeHierarchy(new NullProgressMonitor());
    IType[] subTypes = typeHierarchy.getAllSubtypes(parentType);

    MethodOverrideTester methodOverrideTester = new MethodOverrideTester(parentType, typeHierarchy);

    for (IType type : subTypes) {
      IMethod method = methodOverrideTester.findOverridingMethodInType(type, selectedMethod);
      if (method == null) {
        continue;
      }
      Type openDeclaration = convertToTypeDTO(type);
      setRange(openDeclaration, method);
      implementations.add(openDeclaration);
    }
  }

  private void setRange(Member member, IMember iMember) throws JavaModelException {
    ISourceRange nameRange = iMember.getNameRange();
    if (iMember.isBinary()) {
      nameRange = iMember.getSourceRange();
    }

    if (nameRange == null) {
      return;
    }

    member.setFileRegion(convertToRegionDTO(iMember.getSourceRange()));
  }

  private Type convertToTypeDTO(IType type) throws JavaModelException {
    Type dto = DtoFactory.newDto(Type.class);
    String typeName = type.getElementName();
    if (typeName.isEmpty()) {
      dto.setElementName("Anonymous in " + type.getParent().getElementName());
    } else {
      dto.setElementName(type.getElementName());
    }
    if (type.isBinary()) {
      dto.setRootPath(type.getFullyQualifiedName());
      dto.setLibId(type.getAncestor(IPackageFragmentRoot.PACKAGE_FRAGMENT_ROOT).hashCode());
      dto.setBinary(true);
    } else {
      dto.setRootPath(type.getPath().toOSString());
      dto.setBinary(false);
    }
    setRange(dto, type);

    return dto;
  }

  private Region convertToRegionDTO(ISourceRange iSourceRange) {
    Region region = DtoFactory.newDto(Region.class);
    return iSourceRange == null
        ? region
        : region.withLength(iSourceRange.getLength()).withOffset(iSourceRange.getOffset());
  }
}
