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
package org.eclipse.che.plugin.java.testing;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import com.google.inject.Singleton;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IRegion;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Class which finds test classes and test methods for java test frameworks. */
@Singleton
public class JavaTestFinder {
  private static final Logger LOG = LoggerFactory.getLogger(JavaTestFinder.class);

  /**
   * Finds test method related to the cursor position.
   *
   * @param compilationUnit compilation unit of class
   * @param cursorOffset cursor position
   * @return declaration of test method which should be ran. (Example:
   *     full.qualified.name.of.Class#methodName)
   */
  public List<String> findTestMethodDeclaration(
      ICompilationUnit compilationUnit, int cursorOffset) {
    IType primaryType = compilationUnit.findPrimaryType();
    String qualifiedName = primaryType.getFullyQualifiedName();
    try {
      IJavaElement element = compilationUnit.getElementAt(cursorOffset);
      if (element instanceof IMethod) {
        IMethod method = (IMethod) element;
        qualifiedName = qualifiedName + '#' + method.getElementName();
      }
    } catch (JavaModelException e) {
      LOG.debug("Can't read a method.", e);
    }
    return singletonList(qualifiedName);
  }

  /**
   * Finds test class declaration.
   *
   * @param compilationUnit compilation unit of class
   * @return declaration of test class which should be ran.
   */
  public List<String> findTestClassDeclaration(ICompilationUnit compilationUnit) {
    IType primaryType = compilationUnit.findPrimaryType();
    return singletonList(primaryType.getFullyQualifiedName());
  }

  /**
   * Finds test classes in package.
   *
   * @param javaProject java project
   * @param packagePath package path
   * @param testMethodAnnotation java annotation which describes test method in the test framework
   * @param testClassAnnotation java annotation which describes test class in the test framework
   * @return list of test classes which should be ran.
   */
  public List<String> findClassesInPackage(
      IJavaProject javaProject,
      String packagePath,
      String testMethodAnnotation,
      String testClassAnnotation) {
    IPackageFragment packageFragment = null;
    try {
      packageFragment = javaProject.findPackageFragment(new Path(packagePath));
    } catch (JavaModelException e) {
      LOG.info("Can't find package.", e);
    }
    return packageFragment == null
        ? emptyList()
        : findClassesInContainer(packageFragment, testMethodAnnotation, testClassAnnotation);
  }

  /**
   * Finds test classes in project.
   *
   * @param project java project
   * @param testMethodAnnotation java annotation which describes test method in the test framework
   * @param testClassAnnotation java annotation which describes test class in the test framework
   * @return list of test classes which should be ran.
   */
  public List<String> findClassesInProject(
      IJavaProject project, String testMethodAnnotation, String testClassAnnotation) {
    return findClassesInContainer(project, testMethodAnnotation, testClassAnnotation);
  }

  /**
   * Check if a method is test method.
   *
   * @param method method which should be checked
   * @param compilationUnit parent of the method
   * @param testAnnotation java annotation which describes test method in the test framework
   * @return {@code true} if the method is test method
   */
  public boolean isTest(IMethod method, ICompilationUnit compilationUnit, String testAnnotation) {
    try {
      IAnnotation[] annotations = method.getAnnotations();
      IAnnotation test = null;
      for (IAnnotation annotation : annotations) {
        String annotationElementName = annotation.getElementName();
        if ("Test".equals(annotationElementName)) {
          test = annotation;
          break;
        }
        if (testAnnotation.equals(annotationElementName)) {
          return true;
        }
      }
      return test != null && isImportOfTestAnnotationExist(compilationUnit, testAnnotation);
    } catch (JavaModelException e) {
      LOG.info("Can't read method's annotations.", e);
      return false;
    }
  }

  private boolean isImportOfTestAnnotationExist(
      ICompilationUnit compilationUnit, String testAnnotation) {
    try {
      IImportDeclaration[] imports = compilationUnit.getImports();
      for (IImportDeclaration importDeclaration : imports) {
        String elementName = importDeclaration.getElementName();
        if (testAnnotation.equals(elementName)) {
          return true;
        }
        if (importDeclaration.isOnDemand()
            && testAnnotation.startsWith(
                elementName.substring(0, elementName.length() - 3))) { // remove .*
          return true;
        }
      }
    } catch (JavaModelException e) {
      LOG.info("Can't read class imports.", e);
      return false;
    }
    return false;
  }

  private List<String> findClassesInContainer(
      IJavaElement container, String testMethodAnnotation, String testClassAnnotation) {
    List<String> result = new LinkedList<>();
    IRegion region = getRegion(container);
    try {
      ITypeHierarchy hierarchy = JavaCore.newTypeHierarchy(region, null, null);
      IType[] allClasses = hierarchy.getAllClasses();

      // search for all types with references to RunWith and Test and all subclasses
      HashSet<IType> candidates = new HashSet<>(allClasses.length);
      SearchRequestor requestor = new AnnotationSearchRequestor(hierarchy, candidates);

      IJavaSearchScope scope =
          SearchEngine.createJavaSearchScope(allClasses, IJavaSearchScope.SOURCES);
      int matchRule = SearchPattern.R_CASE_SENSITIVE;

      SearchPattern testPattern =
          SearchPattern.createPattern(
              testMethodAnnotation,
              IJavaSearchConstants.ANNOTATION_TYPE,
              IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE,
              matchRule);

      SearchPattern runWithPattern =
          isNullOrEmpty(testClassAnnotation)
              ? testPattern
              : SearchPattern.createPattern(
                  testClassAnnotation,
                  IJavaSearchConstants.ANNOTATION_TYPE,
                  IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE,
                  matchRule);

      SearchPattern annotationsPattern = SearchPattern.createOrPattern(runWithPattern, testPattern);
      SearchParticipant[] searchParticipants =
          new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()};
      new SearchEngine().search(annotationsPattern, searchParticipants, scope, requestor, null);

      // find all classes in the region
      for (IType candidate : candidates) {
        if (isAccessibleClass(candidate)
            && !Flags.isAbstract(candidate.getFlags())
            && region.contains(candidate)) {
          result.add(candidate.getFullyQualifiedName());
        }
      }
    } catch (CoreException e) {

      LOG.info("Can't build project hierarchy.", e);
    }

    return result;
  }

  private IRegion getRegion(IJavaElement element) {
    IRegion result = JavaCore.newRegion();
    if (element.getElementType() == IJavaElement.JAVA_PROJECT) {
      // for projects only add the contained source folders
      try {
        IPackageFragmentRoot[] packageFragmentRoots =
            ((IJavaProject) element).getPackageFragmentRoots();
        for (IPackageFragmentRoot packageFragmentRoot : packageFragmentRoots) {
          if (!packageFragmentRoot.isArchive()) {
            result.add(packageFragmentRoot);
          }
        }
      } catch (JavaModelException e) {
        LOG.info("Can't read source folders.", e);
      }
    } else {
      result.add(element);
    }
    return result;
  }

  private static boolean isAccessibleClass(IType type) throws JavaModelException {
    int flags = type.getFlags();
    if (Flags.isInterface(flags)) {
      return false;
    }
    IJavaElement parent = type.getParent();
    while (true) {
      if (parent instanceof ICompilationUnit || parent instanceof IClassFile) {
        return true;
      }
      if (!(parent instanceof IType) || !Flags.isStatic(flags) || !Flags.isPublic(flags)) {
        return false;
      }
      flags = ((IType) parent).getFlags();
      parent = parent.getParent();
    }
  }
}
