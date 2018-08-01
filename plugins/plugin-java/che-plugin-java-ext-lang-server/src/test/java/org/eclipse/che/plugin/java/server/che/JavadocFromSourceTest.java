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
package org.eclipse.che.plugin.java.server.che;

import static org.fest.assertions.Assertions.assertThat;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import org.eclipse.che.jdt.JavadocFinder;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.junit.Ignore;
import org.junit.Test;

/** @author Evgen Vidolob */
// TODO: rework after new Project API
@Ignore
public class JavadocFromSourceTest extends BaseTest {

  private JavadocFinder finder = new JavadocFinder("test");

  @Test
  public void testJavadoc4Class() throws Exception {
    String javadoc = finder.findJavadoc(project, "org.eclipse.che.test.MyClass", 686);
    assertThat(javadoc).isNotNull().contains("Test javadoc for class");
  }

  @Test
  public void testJavadoc4Method() throws Exception {
    String javadoc = finder.findJavadoc(project, "org.eclipse.che.test.MyClass", 759);
    assertThat(javadoc).isNotNull().contains("My test method javadoc;");
  }

  @Test
  public void testJavadoc4StaticMethod() throws Exception {
    String javadoc = finder.findJavadoc(project, "org.eclipse.che.test.MyClass", 1359);
    assertThat(javadoc)
        .isNotNull()
        .contains("Verifies that the specified name is valid for our service");
  }

  @Test
  public void testJavadoc4Field() throws Exception {
    String javadoc = finder.findJavadoc(project, "org.eclipse.che.test.MyClass", 862);
    assertThat(javadoc).isNotNull().contains("My test field javadoc.");
  }

  @Test
  public void testResolveMethodsParam() throws Exception {
    IJavaElement element =
        project.findElement(
            "Lorg/eclipse/che/test/MyClass;.isValidName(Ljava.lang.String;)Z", null);
    assertThat(element).isNotNull().isInstanceOf(IMethod.class);
    assertThat(element.getElementName()).isEqualTo("isValidName");
  }

  @Test
  public void testResolveBinaryMethodsParam() throws Exception {
    IJavaElement element =
        project.findElement("Ljava/lang/String;.endsWith(Ljava.lang.String;)Z", null);
    assertThat(element).isNotNull().isInstanceOf(IMethod.class);
    assertThat(element.getElementName()).isEqualTo("endsWith");
  }

  @Test
  public void methodHandleWithParam()
      throws JavaModelException, URISyntaxException, UnsupportedEncodingException {
    JavadocFinder finder = new JavadocFinder("test");
    String javadoc = finder.findJavadoc(project, "org.eclipse.che.test.MyClass", 1639);
    assertThat(javadoc).isNotNull().contains("Method with param and exception");
  }
}
