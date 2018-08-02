/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.java.server.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.ide.ext.java.shared.dto.model.ClassFile;
import org.eclipse.che.ide.ext.java.shared.dto.model.CompilationUnit;
import org.eclipse.che.ide.ext.java.shared.dto.model.Field;
import org.eclipse.che.ide.ext.java.shared.dto.model.ImportDeclaration;
import org.eclipse.che.ide.ext.java.shared.dto.model.Initializer;
import org.eclipse.che.ide.ext.java.shared.dto.model.JavaProject;
import org.eclipse.che.ide.ext.java.shared.dto.model.Method;
import org.eclipse.che.ide.ext.java.shared.dto.model.PackageFragment;
import org.eclipse.che.ide.ext.java.shared.dto.model.PackageFragmentRoot;
import org.eclipse.che.ide.ext.java.shared.dto.model.Type;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IImportContainer;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jdt.ui.StandardJavaElementContentProvider;
import org.eclipse.search.ui.text.AbstractTextSearchResult;

/**
 * Helper class for converting <code>IJavaElement</code> to DTO.
 *
 * @author Evgen Vidolob
 */
public class JavaElementToDtoConverter {

  public static final int LEVEL_TYPE = 1;
  public static final int LEVEL_FILE = 2;
  public static final int LEVEL_PACKAGE = 3;
  public static final int LEVEL_PROJECT = 4;
  private static final int[][] JAVA_ELEMENT_TYPES = {
    {IJavaElement.TYPE},
    {IJavaElement.CLASS_FILE, IJavaElement.COMPILATION_UNIT},
    {IJavaElement.PACKAGE_FRAGMENT},
    {IJavaElement.JAVA_PROJECT, IJavaElement.PACKAGE_FRAGMENT_ROOT},
    {IJavaElement.JAVA_MODEL}
  };
  private static final int[][] RESOURCE_TYPES = {
    {}, {IResource.FILE}, {IResource.FOLDER}, {IResource.PROJECT}, {IResource.ROOT}
  };
  private static final int MAX_LEVEL = JAVA_ELEMENT_TYPES.length - 1;

  private final FastJavaElementProvider contentProvider;
  private Map<Object, Set<Object>> childrens = new HashMap<>();
  private int level;
  private AbstractTextSearchResult result;

  public JavaElementToDtoConverter(AbstractTextSearchResult result) {
    this.result = result;
    contentProvider = new FastJavaElementProvider();
    level = LEVEL_PROJECT;
  }

  public void addElementToProjectHierarchy(IJavaElement element) {
    insert(null, null, element);
  }

  private Object internalGetParent(Object child) {
    return contentProvider.getParent(child);
  }

  private Object getParent(Object child) {
    Object possibleParent = internalGetParent(child);
    if (possibleParent instanceof IJavaElement) {
      IJavaElement javaElement = (IJavaElement) possibleParent;
      for (int j = level; j < MAX_LEVEL + 1; j++) {
        for (int i = 0; i < JAVA_ELEMENT_TYPES[j].length; i++) {
          if (javaElement.getElementType() == JAVA_ELEMENT_TYPES[j][i]) {
            return null;
          }
        }
      }
    } else if (possibleParent instanceof IResource) {
      IResource resource = (IResource) possibleParent;
      for (int j = level; j < MAX_LEVEL + 1; j++) {
        for (int i = 0; i < RESOURCE_TYPES[j].length; i++) {
          if (resource.getType() == RESOURCE_TYPES[j][i]) {
            return null;
          }
        }
      }
    }

    return possibleParent;
  }

  private void insert(Map<Object, Set<Object>> toAdd, Set<Object> toUpdate, Object child) {
    Object parent = getParent(child);
    while (parent != null) {
      if (insertChild(parent, child)) {
        if (toAdd != null) insertInto(parent, child, toAdd);
      } else {
        if (toUpdate != null) toUpdate.add(parent);
        return;
      }
      child = parent;
      parent = getParent(child);
    }
    if (insertChild(result, child)) {
      if (toAdd != null) insertInto(result, child, toAdd);
    }
  }

  private boolean insertChild(Object parent, Object child) {
    return insertInto(parent, child, childrens);
  }

  private boolean insertInto(Object parent, Object child, Map<Object, Set<Object>> map) {
    Set<Object> children = map.get(parent);
    if (children == null) {
      children = new HashSet<>();
      map.put(parent, children);
    }
    return children.add(child);
  }

  public List<JavaProject> getProjects() throws JavaModelException {
    Set<Object> objects = childrens.get(result);
    List<JavaProject> result = new ArrayList<>();
    if (objects == null) {
      return result;
    }
    for (Object object : objects) {
      JavaProject javaProject = DtoFactory.newDto(JavaProject.class);

      IJavaProject project = (IJavaProject) object;
      javaProject.setName(project.getElementName());
      String path = project.getPath().toOSString();
      javaProject.setPath(path);
      javaProject.setPackageFragmentRoots(getPackageFragmentRoots(object, path));
      result.add(javaProject);
    }
    return result;
  }

  private List<PackageFragmentRoot> getPackageFragmentRoots(Object parent, String projectPath)
      throws JavaModelException {
    List<PackageFragmentRoot> result = new ArrayList<>();
    Set<Object> objects = childrens.get(parent);
    if (objects == null) {
      return result;
    }
    for (Object object : objects) {
      if (object instanceof IPackageFragmentRoot) {
        PackageFragmentRoot root = DtoFactory.newDto(PackageFragmentRoot.class);
        IPackageFragmentRoot fragmentRoot = (IPackageFragmentRoot) object;
        root.setProjectPath(projectPath);
        root.setKind(fragmentRoot.getKind());
        root.setPath(fragmentRoot.getPath().toOSString());
        root.setElementName(fragmentRoot.getElementName());
        root.setHandleIdentifier(fragmentRoot.getHandleIdentifier());
        root.setPackageFragments(getPackageFragments(fragmentRoot, projectPath));
        result.add(root);
      }
    }

    return result;
  }

  private List<PackageFragment> getPackageFragments(IPackageFragmentRoot parent, String projectPath)
      throws JavaModelException {
    List<PackageFragment> result = new ArrayList<>();
    Set<Object> objects = childrens.get(parent);
    if (objects == null) {
      return result;
    }

    for (Object object : objects) {
      if (object instanceof IPackageFragment) {
        IPackageFragment packageFragment = (IPackageFragment) object;
        PackageFragment fragment = DtoFactory.newDto(PackageFragment.class);
        fragment.setProjectPath(projectPath);
        fragment.setPath(packageFragment.getPath().toOSString());
        fragment.setHandleIdentifier(packageFragment.getHandleIdentifier());
        fragment.setElementName(packageFragment.getElementName());
        fragment.setKind(packageFragment.getKind());
        fragment.setDefaultPackage(packageFragment.isDefaultPackage());
        List<CompilationUnit> compilationUnits = new ArrayList<>();
        List<ClassFile> classFiles = new ArrayList<>();
        addCompilationUnitsAndClassFiles(object, compilationUnits, classFiles);
        fragment.setCompilationUnits(compilationUnits);
        fragment.setClassFiles(classFiles);

        result.add(fragment);
      }
    }

    return result;
  }

  private void addCompilationUnitsAndClassFiles(
      Object parent, List<CompilationUnit> compilationUnits, List<ClassFile> classFiles)
      throws JavaModelException {
    Set<Object> childrens = this.childrens.get(parent);
    for (Object children : childrens) {
      if (children instanceof ICompilationUnit) {
        ICompilationUnit unit = (ICompilationUnit) children;
        CompilationUnit compilationUnit = DtoFactory.newDto(CompilationUnit.class);
        compilationUnit.setElementName(unit.getElementName());
        compilationUnit.setProjectPath(unit.getJavaProject().getPath().toOSString());
        compilationUnit.setPath(unit.getResource().getFullPath().toOSString());
        compilationUnit.setHandleIdentifier(unit.getHandleIdentifier());
        compilationUnit.setLabel(
            JavaElementLabels.getElementLabel(unit, JavaElementLabels.ALL_DEFAULT));
        compilationUnit.setImports(getImports(children));
        compilationUnit.setTypes(getTypes(children));
        compilationUnits.add(compilationUnit);
      } else if (children instanceof IClassFile) {
        ClassFile classFile = DtoFactory.newDto(ClassFile.class);
        IClassFile clazz = (IClassFile) children;
        classFile.setHandleIdentifier(clazz.getHandleIdentifier());
        classFile.setElementName(clazz.getElementName());
        classFile.setPath(clazz.getType().getFullyQualifiedName());
        classFile.setLabel(JavaElementLabels.getElementLabel(clazz, JavaElementLabels.ALL_DEFAULT));
        classFile.setProjectPath(clazz.getJavaProject().getPath().toOSString());
        classFile.setType(getTypes(children).get(0));
        classFiles.add(classFile);
      }
    }
  }

  private List<ImportDeclaration> getImports(Object parent) throws JavaModelException {
    List<ImportDeclaration> result = new ArrayList<>();
    Set<Object> objects = childrens.get(parent);
    if (objects == null) {
      return result;
    }

    for (Object object : objects) {
      if (object instanceof IImportContainer) {
        Set<Object> imports = childrens.get(object);
        if (imports == null) {
          continue;
        }
        addImport(result, imports);
      }
    }

    return result;
  }

  private void addImport(List<ImportDeclaration> result, Set<Object> imports)
      throws JavaModelException {
    for (Object im : imports) {
      if (im instanceof IImportDeclaration) {
        IImportDeclaration dec = (IImportDeclaration) im;
        ImportDeclaration importDeclaration = DtoFactory.newDto(ImportDeclaration.class);
        importDeclaration.setFlags(dec.getFlags());
        importDeclaration.setHandleIdentifier(dec.getHandleIdentifier());
        importDeclaration.setElementName(dec.getElementName());
        result.add(importDeclaration);
      }
    }
  }

  private List<Type> getTypes(Object parent) throws JavaModelException {
    List<Type> result = new ArrayList<>();
    Set<Object> objects = childrens.get(parent);
    if (objects == null) {
      return result;
    }

    for (Object object : objects) {
      if (object instanceof IType) {
        IType type = (IType) object;
        Type dtoType = DtoFactory.newDto(Type.class);
        dtoType.setElementName(type.getElementName());
        dtoType.setLabel(JavaElementLabels.getElementLabel(type, JavaElementLabels.ALL_DEFAULT));
        dtoType.setHandleIdentifier(type.getHandleIdentifier());
        dtoType.setFlags(type.getFlags());
        dtoType.setTypes(getTypes(object));
        dtoType.setFields(getFields(object));
        dtoType.setMethods(getMethods(object));
        dtoType.setInitializers(getInitializes(object));
        if (parent instanceof ITypeRoot) {
          IType primaryType = ((ITypeRoot) parent).findPrimaryType();
          dtoType.setPrimary(type.equals(primaryType));
        } else {
          dtoType.setPrimary(false);
        }
        result.add(dtoType);
      }
    }

    return result;
  }

  private List<Initializer> getInitializes(Object parent) throws JavaModelException {
    List<Initializer> result = new ArrayList<>();
    Set<Object> objects = childrens.get(parent);
    if (objects == null) {
      return result;
    }

    for (Object object : objects) {
      if (object instanceof IInitializer) {
        IInitializer initializer = (IInitializer) object;
        Initializer init = DtoFactory.newDto(Initializer.class);
        init.setElementName(initializer.getElementName());
        init.setHandleIdentifier(initializer.getHandleIdentifier());
        init.setFlags(initializer.getFlags());
        init.setLabel(
            JavaElementLabels.getElementLabel(initializer, JavaElementLabels.ALL_DEFAULT));
        result.add(init);
      }
    }
    return result;
  }

  private List<Method> getMethods(Object parent) throws JavaModelException {
    List<Method> result = new ArrayList<>();
    Set<Object> objects = childrens.get(parent);
    if (objects == null) {
      return result;
    }

    for (Object object : objects) {
      if (object instanceof IMethod) {
        IMethod method = (IMethod) object;
        Method met = DtoFactory.newDto(Method.class);
        met.setHandleIdentifier(method.getHandleIdentifier());
        met.setFlags(method.getFlags());
        met.setElementName(method.getElementName());
        met.setLabel(JavaElementLabels.getElementLabel(method, JavaElementLabels.ALL_DEFAULT));
        result.add(met);
      }
    }

    return result;
  }

  private List<Field> getFields(Object parent) throws JavaModelException {
    List<Field> result = new ArrayList<>();
    Set<Object> objects = childrens.get(parent);
    if (objects == null) {
      return result;
    }

    for (Object object : objects) {
      if (object instanceof IField) {
        IField iField = (IField) object;
        Field field = DtoFactory.newDto(Field.class);
        field.setElementName(iField.getElementName());
        field.setHandleIdentifier(iField.getHandleIdentifier());
        field.setFlags(iField.getFlags());
        field.setLabel(JavaElementLabels.getElementLabel(iField, JavaElementLabels.ALL_DEFAULT));
        result.add(field);
      }
    }
    return result;
  }

  static class FastJavaElementProvider extends StandardJavaElementContentProvider {
    @Override
    public Object getParent(Object element) {
      Object parent = internalGetParent(element);
      if (parent == null && element instanceof IAdaptable) {
        IAdaptable adaptable = (IAdaptable) element;
        Object javaElement = adaptable.getAdapter(IJavaElement.class);
        if (javaElement != null) {
          parent = internalGetParent(javaElement);
        } else {
          Object resource = adaptable.getAdapter(IResource.class);
          if (resource != null) {
            parent = internalGetParent(resource);
          }
        }
      }
      return parent;
    }
  }
}
