/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.java.testing;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaTestHelper {

  private static final Logger LOG = LoggerFactory.getLogger(JavaTestHelper.class);

  /**
   * Check if a method is a test method.
   *
   * @param method method which should be checked
   * @param compilationUnit parent of the method
   * @param testAnnotation java annotation which describes test method in the test framework
   * @return {@code true} if the method is test method
   */
  public static boolean isTest(
      IMethod method, ICompilationUnit compilationUnit, String testAnnotation) {
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

  private static boolean isImportOfTestAnnotationExist(
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
}
