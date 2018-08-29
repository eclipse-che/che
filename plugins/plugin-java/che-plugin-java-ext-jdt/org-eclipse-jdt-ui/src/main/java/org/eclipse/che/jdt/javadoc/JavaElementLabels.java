/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jdt.javadoc;

import org.eclipse.jdt.core.IJavaElement;

public class JavaElementLabels {

  public static final String DECL_STRING = "\\ :\\ ";
  public static final String CATEGORY_SEPARATOR_STRING = "\\ ";
  public static final String DEFAULT_PACKAGE = "(default package)";
  /** Method names contain parameter types. e.g. <code>foo(int)</code> */
  public static final long M_PARAMETER_TYPES = 1L << 0;
  /** Method names contain parameter names. e.g. <code>foo(index)</code> */
  public static final long M_PARAMETER_NAMES = 1L << 1;
  /**
   * Method labels contain parameter annotations. E.g. <code>foo(@NonNull int)</code>. This flag is
   * only valid if {@link #M_PARAMETER_NAMES} or {@link #M_PARAMETER_TYPES} is also set.
   *
   * @since 3.8
   */
  public static final long M_PARAMETER_ANNOTATIONS = 1L << 52;
  /** Method names contain type parameters prepended. e.g. <code>&lt;A&gt; foo(A index)</code> */
  public static final long M_PRE_TYPE_PARAMETERS = 1L << 2;
  /** Method names contain type parameters appended. e.g. <code>foo(A index) &lt;A&gt;</code> */
  public static final long M_APP_TYPE_PARAMETERS = 1L << 3;
  /** Method names contain thrown exceptions. e.g. <code>foo throws IOException</code> */
  public static final long M_EXCEPTIONS = 1L << 4;
  /** Method names contain return type (appended) e.g. <code>foo : int</code> */
  public static final long M_APP_RETURNTYPE = 1L << 5;
  /** Method names contain return type (appended) e.g. <code>int foo</code> */
  public static final long M_PRE_RETURNTYPE = 1L << 6;
  /** Method names are fully qualified. e.g. <code>java.util.Vector.size</code> */
  public static final long M_FULLY_QUALIFIED = 1L << 7;
  /** Method names are post qualified. e.g. <code>size - java.util.Vector</code> */
  public static final long M_POST_QUALIFIED = 1L << 8;
  /** Initializer names are fully qualified. e.g. <code>java.util.Vector.{ ... }</code> */
  public static final long I_FULLY_QUALIFIED = 1L << 10;
  /** Type names are post qualified. e.g. <code>{ ... } - java.util.Map</code> */
  public static final long I_POST_QUALIFIED = 1L << 11;
  /** Field names contain the declared type (appended) e.g. <code>fHello : int</code> */
  public static final long F_APP_TYPE_SIGNATURE = 1L << 14;
  /** Field names contain the declared type (prepended) e.g. <code>int fHello</code> */
  public static final long F_PRE_TYPE_SIGNATURE = 1L << 15;
  /** Fields names are fully qualified. e.g. <code>java.lang.System.out</code> */
  public static final long F_FULLY_QUALIFIED = 1L << 16;
  /** Fields names are post qualified. e.g. <code>out - java.lang.System</code> */
  public static final long F_POST_QUALIFIED = 1L << 17;
  /** Type names are fully qualified. e.g. <code>java.util.Map.Entry</code> */
  public static final long T_FULLY_QUALIFIED = 1L << 18;
  /** Type names are type container qualified. e.g. <code>Map.Entry</code> */
  public static final long T_CONTAINER_QUALIFIED = 1L << 19;
  /** Type names are post qualified. e.g. <code>Entry - java.util.Map</code> */
  public static final long T_POST_QUALIFIED = 1L << 20;
  /** Type names contain type parameters. e.g. <code>Map&lt;S, T&gt;</code> */
  public static final long T_TYPE_PARAMETERS = 1L << 21;
  /**
   * Type parameters are post qualified. e.g. <code>K - java.util.Map.Entry</code>
   *
   * @since 3.5
   */
  public static final long TP_POST_QUALIFIED = 1L << 22;
  /**
   * Declarations (import container / declaration, package declaration) are qualified. e.g. <code>
   * java.util.Vector.class/import container</code>
   */
  public static final long D_QUALIFIED = 1L << 24;
  /**
   * Declarations (import container / declaration, package declaration) are post qualified. e.g.
   * <code>import container - java.util.Vector.class</code>
   */
  public static final long D_POST_QUALIFIED = 1L << 25;
  /** Class file names are fully qualified. e.g. <code>java.util.Vector.class</code> */
  public static final long CF_QUALIFIED = 1L << 27;
  /** Class file names are post qualified. e.g. <code>Vector.class - java.util</code> */
  public static final long CF_POST_QUALIFIED = 1L << 28;
  /** Compilation unit names are fully qualified. e.g. <code>java.util.Vector.java</code> */
  public static final long CU_QUALIFIED = 1L << 31;
  /** Compilation unit names are post qualified. e.g. <code>Vector.java - java.util</code> */
  public static final long CU_POST_QUALIFIED = 1L << 32;
  /** Package names are qualified. e.g. <code>MyProject/src/java.util</code> */
  public static final long P_QUALIFIED = 1L << 35;
  /** Package names are post qualified. e.g. <code>java.util - MyProject/src</code> */
  public static final long P_POST_QUALIFIED = 1L << 36;
  /**
   * Package names are abbreviated if {@link
   * PreferenceConstants#APPEARANCE_ABBREVIATE_PACKAGE_NAMES} is <code>true</code> and/or compressed
   * if {@link PreferenceConstants#APPEARANCE_COMPRESS_PACKAGE_NAMES} is <code>true</code>.
   */
  public static final long P_COMPRESSED = 1L << 37;
  /**
   * Package Fragment Roots contain variable name if from a variable. e.g. <code>
   * JRE_LIB - c:\java\lib\rt.jar</code>
   */
  public static final long ROOT_VARIABLE = 1L << 40;
  /**
   * Package Fragment Roots contain the project name if not an archive (prepended). e.g. <code>
   * MyProject/src</code>
   */
  public static final long ROOT_QUALIFIED = 1L << 41;
  /**
   * Package Fragment Roots contain the project name if not an archive (appended). e.g. <code>
   * src - MyProject</code>
   */
  public static final long ROOT_POST_QUALIFIED = 1L << 42;
  /**
   * Add root path to all elements except Package Fragment Roots and Java projects. e.g. <code>
   * java.lang.Vector - C:\java\lib\rt.jar</code> Option only applies to getElementLabel
   */
  public static final long APPEND_ROOT_PATH = 1L << 43;
  /**
   * Add root path to all elements except Package Fragment Roots and Java projects. e.g. <code>
   * C:\java\lib\rt.jar - java.lang.Vector</code> Option only applies to getElementLabel
   */
  public static final long PREPEND_ROOT_PATH = 1L << 44;
  /**
   * Post qualify referenced package fragment roots. For example <code>jdt.jar - org.eclipse.jdt.ui
   * </code> if the jar is referenced from another project.
   */
  public static final long REFERENCED_ROOT_POST_QUALIFIED = 1L << 45;
  /**
   * Specifies to use the resolved information of a IType, IMethod or IField. See {@link
   * org.eclipse.jdt.core.IType#isResolved()}. If resolved information is available, types will be
   * rendered with type parameters of the instantiated type. Resolved methods render with the
   * parameter types of the method instance. <code>Vector&lt;String&gt;.get(String)</code>
   */
  public static final long USE_RESOLVED = 1L << 48;
  /**
   * Specifies to apply color styles to labels. This flag only applies to methods taking or
   * returning a {@link StyledString}.
   *
   * @since 3.4
   */
  public static final long COLORIZE = 1L << 55;
  /**
   * Prepend first category (if any) to field.
   *
   * @since 3.2
   */
  public static final long F_CATEGORY = 1L << 49;
  /**
   * Prepend first category (if any) to method.
   *
   * @since 3.2
   */
  public static final long M_CATEGORY = 1L << 50;
  /**
   * Prepend first category (if any) to type.
   *
   * @since 3.2
   */
  public static final long T_CATEGORY = 1L << 51;
  /**
   * Show category for all elements.
   *
   * @since 3.2
   */
  public static final long ALL_CATEGORY =
      new Long(F_CATEGORY | M_CATEGORY | T_CATEGORY).longValue();
  /** Qualify all elements */
  public static final long ALL_FULLY_QUALIFIED =
      new Long(
              F_FULLY_QUALIFIED
                  | M_FULLY_QUALIFIED
                  | I_FULLY_QUALIFIED
                  | T_FULLY_QUALIFIED
                  | D_QUALIFIED
                  | CF_QUALIFIED
                  | CU_QUALIFIED
                  | P_QUALIFIED
                  | ROOT_QUALIFIED)
          .longValue();
  /** Post qualify all elements */
  public static final long ALL_POST_QUALIFIED =
      new Long(
              F_POST_QUALIFIED
                  | M_POST_QUALIFIED
                  | I_POST_QUALIFIED
                  | T_POST_QUALIFIED
                  | TP_POST_QUALIFIED
                  | D_POST_QUALIFIED
                  | CF_POST_QUALIFIED
                  | CU_POST_QUALIFIED
                  | P_POST_QUALIFIED
                  | ROOT_POST_QUALIFIED)
          .longValue();
  /** Default options (M_PARAMETER_TYPES, M_APP_TYPE_PARAMETERS & T_TYPE_PARAMETERS enabled) */
  public static final long ALL_DEFAULT =
      new Long(M_PARAMETER_TYPES | M_APP_TYPE_PARAMETERS | T_TYPE_PARAMETERS).longValue();
  /** Default qualify options (All except Root and Package) */
  public static final long DEFAULT_QUALIFIED =
      new Long(
              F_FULLY_QUALIFIED
                  | M_FULLY_QUALIFIED
                  | I_FULLY_QUALIFIED
                  | T_FULLY_QUALIFIED
                  | D_QUALIFIED
                  | CF_QUALIFIED
                  | CU_QUALIFIED)
          .longValue();
  /** User-readable string for ellipsis ("..."). */
  public static final String ELLIPSIS_STRING = "..."; // $NON-NLS-1$

  public static String COMMA_STRING = ",\\ ";
  public static String CONCAT_STRING = "\\ -\\ ";

  /**
   * Returns the label for a Java element with the flags as defined by this class.
   *
   * @param element the element to render
   * @param flags the rendering flags
   * @param buf the buffer to append the resulting label to
   */
  public static void getElementLabel(IJavaElement element, long flags, StringBuffer buf) {
    new JavaElementLabelComposer(buf).appendElementLabel(element, flags);
  }

  /**
   * Returns the label for a Java element with the flags as defined by this class.
   *
   * @param element the element to render
   * @param flags the rendering flags
   * @return the label of the Java element
   */
  public static String getElementLabel(IJavaElement element, long flags) {
    StringBuffer result = new StringBuffer();
    getElementLabel(element, flags, result);
    return result.toString(); // Strings.markJavaElementLabelLTR(result.toString());
  }
}
