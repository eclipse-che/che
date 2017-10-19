/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jdt.javadoc;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Helper needed to get the content of a Javadoc comment.
 *
 * <p>
 *
 * <p>This class is not intended to be subclassed or instantiated by clients.
 *
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 * @since 3.1
 */
public class JavadocContentAccess {

  private JavadocContentAccess() {
    // do not instantiate
  }

  /**
   * Gets a reader for an IMember's Javadoc comment content from the source attachment. The content
   * does contain only the text from the comment without the Javadoc leading star characters.
   * Returns <code>null</code> if the member does not contain a Javadoc comment or if no source is
   * available.
   *
   * @param member The member to get the Javadoc of.
   * @param allowInherited For methods with no (Javadoc) comment, the comment of the overridden
   *     class is returned if <code>allowInherited</code> is <code>true</code>.
   * @return Returns a reader for the Javadoc comment content or <code>null</code> if the member
   *     does not contain a Javadoc comment or if no source is available
   * @throws org.eclipse.jdt.core.JavaModelException is thrown when the elements javadoc can not be
   *     accessed
   */
  public static Reader getContentReader(IMember member, boolean allowInherited)
      throws JavaModelException {
    Reader contentReader = internalGetContentReader(member);
    if (contentReader != null
        || !(allowInherited && (member.getElementType() == IJavaElement.METHOD)))
      return contentReader;
    return findDocInHierarchy((IMethod) member, false, false);
  }

  /**
   * Gets a reader for an IMember's Javadoc comment content from the source attachment. The content
   * does contain only the text from the comment without the Javadoc leading star characters.
   * Returns <code>null</code> if the member does not contain a Javadoc comment or if no source is
   * available.
   *
   * @param member The member to get the Javadoc of.
   * @return Returns a reader for the Javadoc comment content or <code>null</code> if the member
   *     does not contain a Javadoc comment or if no source is available
   * @throws org.eclipse.jdt.core.JavaModelException is thrown when the elements javadoc can not be
   *     accessed
   * @since 3.4
   */
  private static Reader internalGetContentReader(IMember member) throws JavaModelException {
    IBuffer buf = member.getOpenable().getBuffer();
    if (buf == null) {
      return null; // no source attachment found
    }

    ISourceRange javadocRange = member.getJavadocRange();
    if (javadocRange != null) {
      JavaDocCommentReader reader =
          new JavaDocCommentReader(
              buf,
              javadocRange.getOffset(),
              javadocRange.getOffset() + javadocRange.getLength() - 1);
      if (!containsOnlyInheritDoc(reader, javadocRange.getLength())) {
        reader.reset();
        return reader;
      }
    }

    return null;
  }

  /**
   * Checks whether the given reader only returns the inheritDoc tag.
   *
   * @param reader the reader
   * @param length the length of the underlying content
   * @return <code>true</code> if the reader only returns the inheritDoc tag
   * @since 3.2
   */
  private static boolean containsOnlyInheritDoc(Reader reader, int length) {
    char[] content = new char[length];
    try {
      reader.read(content, 0, length);
    } catch (IOException e) {
      return false;
    }
    return new String(content).trim().equals("{@inheritDoc}"); // $NON-NLS-1$
  }

  /**
   * Gets a reader for an IMember's Javadoc comment content from the source attachment. and renders
   * the tags in HTML. Returns <code>null</code> if the member does not contain a Javadoc comment or
   * if no source is available.
   *
   * @param member the member to get the Javadoc of.
   * @param allowInherited for methods with no (Javadoc) comment, the comment of the overridden
   *     class is returned if <code>allowInherited</code> is <code>true</code>
   * @param useAttachedJavadoc if <code>true</code> Javadoc will be extracted from attached Javadoc
   *     if there's no source
   * @return a reader for the Javadoc comment content in HTML or <code>null</code> if the member
   *     does not contain a Javadoc comment or if no source is available
   * @throws org.eclipse.jdt.core.JavaModelException is thrown when the elements Javadoc can not be
   *     accessed
   * @since 3.2
   */
  public static Reader getHTMLContentReader(
      IMember member, boolean allowInherited, boolean useAttachedJavadoc)
      throws JavaModelException {
    Reader contentReader = internalGetContentReader(member);
    if (contentReader != null) return new JavaDoc2HTMLTextReader(contentReader);

    if (useAttachedJavadoc
        && member.getOpenable().getBuffer() == null) { // only if no source available
      String s = member.getAttachedJavadoc(null);
      if (s != null) return new StringReader(s);
    }

    if (allowInherited && (member.getElementType() == IJavaElement.METHOD))
      return findDocInHierarchy((IMethod) member, true, useAttachedJavadoc);

    return null;
  }

  /**
   * Gets a reader for an IMember's Javadoc comment content from the source attachment. and renders
   * the tags in HTML. Returns <code>null</code> if the member does not contain a Javadoc comment or
   * if no source is available.
   *
   * @param member The member to get the Javadoc of.
   * @param allowInherited For methods with no (Javadoc) comment, the comment of the overridden
   *     class is returned if <code>allowInherited</code> is <code>true</code>.
   * @return Returns a reader for the Javadoc comment content in HTML or <code>null</code> if the
   *     member does not contain a Javadoc comment or if no source is available
   * @throws org.eclipse.jdt.core.JavaModelException is thrown when the elements javadoc can not be
   *     accessed
   * @deprecated As of 3.2, replaced by {@link #getHTMLContentReader(org.eclipse.jdt.core.IMember,
   *     boolean, boolean)}
   */
  public static Reader getHTMLContentReader(IMember member, boolean allowInherited)
      throws JavaModelException {
    return getHTMLContentReader(member, allowInherited, false);
  }

  private static Reader findDocInHierarchy(
      IMethod method, boolean isHTML, boolean useAttachedJavadoc) throws JavaModelException {
    // todo
    //		/*
    //		 * Catch ExternalJavaProject in which case
    //		 * no hierarchy can be built.
    //		 */
    //		if (!method.getJavaProject().exists())
    //			return null;
    //
    //		IType type= method.getDeclaringType();
    //		ITypeHierarchy hierarchy= type.newSupertypeHierarchy(null);
    //
    //		MethodOverrideTester tester= new MethodOverrideTester(type, hierarchy);
    //
    //		IType[] superTypes= hierarchy.getAllSupertypes(type);
    //		for (int i= 0; i < superTypes.length; i++) {
    //			IType curr= superTypes[i];
    //			IMethod overridden= tester.findOverriddenMethodInType(curr, method);
    //			if (overridden != null) {
    //				Reader reader;
    //				if (isHTML)
    //					reader= getHTMLContentReader(overridden, false, useAttachedJavadoc);
    //				else
    //					reader= getContentReader(overridden, false);
    //				if (reader != null)
    //					return reader;
    //			}
    //		}
    return null;
  }
}
