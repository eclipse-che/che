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
package org.eclipse.che.ide.api.editor.changeintercept.changeintercept;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;

import org.eclipse.che.ide.api.editor.changeintercept.CloseCStyleCommentChangeInterceptor;
import org.eclipse.che.ide.api.editor.changeintercept.TextChange;
import org.eclipse.che.ide.api.editor.document.ReadOnlyDocument;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** Test of the c-style bloc comment close interceptor. */
@RunWith(MockitoJUnitRunner.class)
public class CloseCStyleCommentChangeInterceptorTest {

  @Mock private ReadOnlyDocument document;

  @InjectMocks private CloseCStyleCommentChangeInterceptor interceptor;

  /** The input is a normal /* &#42;&#47; comment without leading spaces. */
  @Ignore
  @Test
  public void testNotFirstLineNoLeadingSpaces() {
    doReturn("").when(document).getLineContent(0);
    doReturn("/*").when(document).getLineContent(1);
    doReturn(" *").when(document).getLineContent(2);
    final TextChange input =
        new TextChange.Builder()
            .from(new TextPosition(1, 2))
            .to(new TextPosition(2, 2))
            .insert("\n *")
            .build();
    final TextChange output = interceptor.processChange(input, document);
    assertNotNull(output);
    final TextChange expected =
        new TextChange.Builder()
            .from(new TextPosition(1, 2))
            .to(new TextPosition(3, 3))
            .insert("\n * \n */")
            .build();
    Assert.assertEquals(expected, output);
  }

  @Ignore
  @Test
  public void testFirstLineNoLeadingSpaces() {
    doReturn("/*").when(document).getLineContent(0);
    doReturn(" *").when(document).getLineContent(1);
    final TextChange input =
        new TextChange.Builder()
            .from(new TextPosition(0, 2))
            .to(new TextPosition(1, 2))
            .insert("\n *")
            .build();
    final TextChange output = interceptor.processChange(input, document);
    assertNotNull(output);
    final TextChange expected =
        new TextChange.Builder()
            .from(new TextPosition(0, 2))
            .to(new TextPosition(2, 3))
            .insert("\n * \n */")
            .build();
    Assert.assertEquals(expected, output);
  }

  @Test
  public void testStartNotEmptyLine() {
    doReturn("s/*").when(document).getLineContent(1);
    final TextChange input =
        new TextChange.Builder()
            .from(new TextPosition(1, 3))
            .to(new TextPosition(2, 2))
            .insert("\n *")
            .build();
    final TextChange output = interceptor.processChange(input, document);
    assertNull(output);
  }

  @Ignore
  @Test
  public void test3LeadingSpaces() {
    testWithLeading("   ");
  }

  @Ignore
  @Test
  public void testLeadingTab() {
    testWithLeading("\t");
  }

  @Ignore
  @Test
  public void testLeadingMixed() {
    testWithLeading(" \t");
  }

  private void testWithLeading(final String lead) {
    doReturn(lead + "/*").when(document).getLineContent(1);
    doReturn(lead + " *").when(document).getLineContent(2);
    final TextChange input =
        new TextChange.Builder()
            .from(new TextPosition(1, 2 + lead.length()))
            .to(new TextPosition(2, 2 + lead.length()))
            .insert("\n" + lead + " *")
            .build();
    final TextChange output = interceptor.processChange(input, document);
    assertNotNull(output);
    final TextChange expected =
        new TextChange.Builder()
            .from(new TextPosition(1, 2 + lead.length()))
            .to(new TextPosition(3, 3 + lead.length()))
            .insert("\n" + lead + " * " + "\n" + lead + " */")
            .build();
    Assert.assertEquals(expected, output);
  }

  @Ignore
  @Test
  public void testAddWithComment() {
    doReturn("/*").when(document).getLineContent(0);
    doReturn("/*").when(document).getLineContent(1);
    doReturn(" *").when(document).getLineContent(2);
    final TextChange input =
        new TextChange.Builder()
            .from(new TextPosition(1, 2))
            .to(new TextPosition(2, 2))
            .insert("\n *")
            .build();
    final TextChange output = interceptor.processChange(input, document);
    assertNull(output);
  }

  @Ignore
  @Test
  public void testJavadocStyleComment() {
    doReturn("/**").when(document).getLineContent(0);
    doReturn(" *").when(document).getLineContent(1);
    final TextChange input =
        new TextChange.Builder()
            .from(new TextPosition(0, 3))
            .to(new TextPosition(1, 2))
            .insert("\n *")
            .build();
    final TextChange output = interceptor.processChange(input, document);
    assertNotNull(output);
    final TextChange expected =
        new TextChange.Builder()
            .from(new TextPosition(0, 3))
            .to(new TextPosition(2, 3))
            .insert("\n * \n */")
            .build();
    Assert.assertEquals(expected, output);
  }

  @Test
  public void testPasteWholeCommentStart() {
    final TextChange input =
        new TextChange.Builder()
            .from(new TextPosition(0, 0))
            .to(new TextPosition(1, 2))
            .insert("/**\n *")
            .build();
    final TextChange output = interceptor.processChange(input, document);
    assertNull(output);
  }

  @Test
  public void testCloseComment() {
    final TextChange input =
        new TextChange.Builder()
            .from(new TextPosition(0, 0))
            .to(new TextPosition(1, 2))
            .insert("/**\n *")
            .build();
    final TextChange output = interceptor.processChange(input, document);
    assertNull(output);
  }
}
