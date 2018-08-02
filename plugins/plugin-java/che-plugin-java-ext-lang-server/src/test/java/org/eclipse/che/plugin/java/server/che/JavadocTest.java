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
import org.eclipse.che.jdt.javadoc.JavaElementLinks;
import org.eclipse.che.jdt.javadoc.JavadocContentAccess2;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/** @author Evgen Vidolob */
// TODO: rework after new Project API
@Ignore
public class JavadocTest extends BaseTest {
  private String urlPart = "http://localhost:8080/ws/java-ca?projectpath=/test&handle=";
  private JavadocFinder finder;

  @Before
  public void createFinder() throws Exception {
    finder = new JavadocFinder(urlPart);
  }

  @Test
  public void binaryObjectDoc() throws JavaModelException {
    IType type = project.findType("java.lang.Object");
    assertThat(type).isNotNull();
    String htmlContent = JavadocContentAccess2.getHTMLContent(type, true, urlPart);
    Assert.assertNotNull(htmlContent);
    assertThat(htmlContent)
        .isNotNull()
        .isNotEmpty()
        .contains("Class <code>Object</code> is the root of the class hierarchy.");
  }

  @Test
  public void findObjectDoc() throws JavaModelException {
    String javadoc = finder.findJavadoc(project, "java.lang.Object", 514);
    Assert.assertNotNull(javadoc);
    assertThat(javadoc)
        .isNotNull()
        .isNotEmpty()
        .contains("Class <code>Object</code> is the root of the class hierarchy.");
  }

  @Test
  public void binaryMethodDoc() throws JavaModelException {
    IType type = project.findType("java.lang.Object");
    assertThat(type).isNotNull();
    IMethod method = type.getMethod("hashCode", null);
    assertThat(method).isNotNull();
    String htmlContent = JavadocContentAccess2.getHTMLContent(method, true, urlPart);
    assertThat(htmlContent)
        .isNotNull()
        .isNotEmpty()
        .contains("Returns a hash code value for the object.");
  }

  @Test
  public void binaryGenericMethodDoc() throws JavaModelException {
    IType type = project.findType("java.util.List");
    assertThat(type).isNotNull();
    IMethod method = type.getMethod("add", new String[] {"TE;"});
    assertThat(method).isNotNull();
    String htmlContent = JavadocContentAccess2.getHTMLContent(method, true, urlPart);
    assertThat(htmlContent)
        .isNotNull()
        .isNotEmpty()
        .contains("Appends the specified element to the end of this list (optional");
  }

  @Test
  public void binaryFieldDoc() throws JavaModelException, URISyntaxException {
    IType type = project.findType("java.util.ArrayList");
    assertThat(type).isNotNull();
    IField field = type.getField("size");
    assertThat(field).isNotNull();
    String htmlContent = JavadocContentAccess2.getHTMLContent(field, true, urlPart);
    assertThat(htmlContent)
        .isNotNull()
        .isNotEmpty()
        .contains("The size of the ArrayList (the number of elements it contains).");
  }

  @Test
  public void binaryGenericObjectDoc() throws JavaModelException {
    IType type = project.findType("java.util.ArrayList");
    assertThat(type).isNotNull();
    String htmlContent = JavadocContentAccess2.getHTMLContent(type, true, urlPart);
    assertThat(htmlContent)
        .isNotNull()
        .isNotEmpty()
        .contains("Resizable-array implementation of the <tt>List</tt> interface.");
  }

  @Test
  public void binaryHandle()
      throws JavaModelException, URISyntaxException, UnsupportedEncodingException {
    testDoc(
        "<java.lang(String.class"
            + JavaElementLinks.LINK_BRACKET_REPLACEMENT
            + "String"
            + JavaElementLinks.LINK_SEPARATOR
            + "java.lang.StringBuffer",
        "A thread-safe, mutable sequence of characters.");
  }

  @Test
  public void binaryHandleMethod()
      throws JavaModelException, URISyntaxException, UnsupportedEncodingException {
    testDoc(
        "<java.nio.charset(CharsetDecoder.class"
            + JavaElementLinks.LINK_BRACKET_REPLACEMENT
            + "CharsetDecoder"
            + JavaElementLinks.LINK_SEPARATOR
            + JavaElementLinks.LINK_SEPARATOR
            + "replaceWith"
            + JavaElementLinks.LINK_SEPARATOR
            + "java.lang.String",
        "Changes this decoder's replacement value.");
  }

  @Test
  public void exceptionMethod()
      throws JavaModelException, URISyntaxException, UnsupportedEncodingException {
    testDoc(
        "<java.lang(Throwable.class"
            + JavaElementLinks.LINK_BRACKET_REPLACEMENT
            + "Throwable~Throwable~Ljava.lang.String;~Ljava.lang.Throwable;~Z~Z"
            + JavaElementLinks.LINK_SEPARATOR
            + JavaElementLinks.LINK_SEPARATOR
            + "getStackTrace",
        "Provides programmatic access to the stack trace information printed by");
  }

  @Test
  public void getContextClassLoadingMethod()
      throws JavaModelException, URISyntaxException, UnsupportedEncodingException {
    testDoc(
        "<java.nio.charset.spi(CharsetProvider.class"
            + JavaElementLinks.LINK_BRACKET_REPLACEMENT
            + "CharsetProvider"
            + JavaElementLinks.LINK_SEPARATOR
            + "java.lang.Thread"
            + JavaElementLinks.LINK_SEPARATOR
            + "getContextClassLoader"
            + JavaElementLinks.LINK_SEPARATOR,
        "Returns the context ClassLoader for this Thread.");
  }

  private void testDoc(String handle, String content) {
    String handl = getHandldeForRtJarStart() + handle;
    String javadoc = finder.findJavadoc4Handle(project, handl);
    assertThat(javadoc).contains(content);
  }
}
