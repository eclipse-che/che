/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.ui;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.util.Strings;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementLabelComposer;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.osgi.util.TextProcessor;

/**
 * <code>JavaElementLabels</code> provides helper methods to render names of Java elements.
 *
 * @since 3.1
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class JavaElementLabels {

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
   * IType#isResolved()}. If resolved information is available, types will be rendered with type
   * parameters of the instantiated type. Resolved methods render with the parameter types of the
   * method instance. <code>Vector&lt;String&gt;.get(String)</code>
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

  /** Default post qualify options (All except Root and Package) */
  public static final long DEFAULT_POST_QUALIFIED =
      new Long(
              F_POST_QUALIFIED
                  | M_POST_QUALIFIED
                  | I_POST_QUALIFIED
                  | T_POST_QUALIFIED
                  | TP_POST_QUALIFIED
                  | D_POST_QUALIFIED
                  | CF_POST_QUALIFIED
                  | CU_POST_QUALIFIED)
          .longValue();

  /** User-readable string for separating post qualified names (e.g. " - "). */
  public static final String CONCAT_STRING =
      " - "; // JavaUIMessages.JavaElementLabels_concat_string;
  /** User-readable string for separating list items (e.g. ", "). */
  public static final String COMMA_STRING = ", "; // JavaUIMessages.JavaElementLabels_comma_string;
  /** User-readable string for separating the return type (e.g. " : "). */
  public static final String DECL_STRING =
      " : "; // JavaUIMessages.JavaElementLabels_declseparator_string;
  /**
   * User-readable string for concatenating categories (e.g. " ").
   *
   * @since 3.5
   */
  public static final String CATEGORY_SEPARATOR_STRING =
      " "; // JavaUIMessages.JavaElementLabels_category_separator_string;
  /** User-readable string for ellipsis ("..."). */
  public static final String ELLIPSIS_STRING = "..."; // $NON-NLS-1$
  /** User-readable string for the default package name (e.g. "(default package)"). */
  public static final String DEFAULT_PACKAGE =
      "(default package)"; // JavaUIMessages.JavaElementLabels_default_package;

  //	private static final Styler DECORATIONS_STYLE = StyledString.DECORATIONS_STYLER;

  private JavaElementLabels() {}

  /**
   * Returns the label of the given object. The object must be of type {@link IJavaElement} or adapt
   * to {@link IWorkbenchAdapter}. If the element type is not known, the empty string is returned.
   * The returned label is BiDi-processed with {@link TextProcessor#process(String, String)}.
   *
   * @param obj object to get the label for
   * @param flags the rendering flags
   * @return the label or the empty string if the object type is not supported
   */
  public static String getTextLabel(Object obj, long flags) {
    if (obj instanceof IJavaElement) {
      return getElementLabel((IJavaElement) obj, flags);

    } else if (obj instanceof IResource) {
      return BasicElementLabels.getResourceName((IResource) obj);
      //
      //		} else if (obj instanceof ClassPathContainer) {
      //			ClassPathContainer container = (ClassPathContainer)obj;
      //			IPath containerPath = container.getClasspathEntry().getPath();
      //			try {
      //				return getContainerEntryLabel(containerPath, container.getJavaProject());
      //			} catch (JavaModelException e) {
      //				return BasicElementLabels.getPathLabel(containerPath, false);
      //			}
      //
      //		} else if (obj instanceof IStorage) {
      //			return BasicElementLabels.getResourceName(((IStorage)obj).getName());
      //
      //		} else if (obj instanceof IAdaptable) {
      //			IWorkbenchAdapter wbadapter =
      // (IWorkbenchAdapter)((IAdaptable)obj).getAdapter(IWorkbenchAdapter.class);
      //			if (wbadapter != null) {
      //				return Strings.markLTR(wbadapter.getLabel(obj));
      //			}
    }
    return ""; // $NON-NLS-1$
  }

  /**
   * Returns the styled label of the given object. The object must be of type {@link IJavaElement}
   * or adapt to {@link IWorkbenchAdapter}. If the element type is not known, the empty string is
   * returned. The returned label is BiDi-processed with {@link TextProcessor#process(String,
   * String)}.
   *
   * @param obj object to get the label for
   * @param flags the rendering flags
   * @return the label or the empty string if the object type is not supported
   * @since 3.4
   */
  public static StyledString getStyledTextLabel(Object obj, long flags) {
    if (obj instanceof IJavaElement) {
      return getStyledElementLabel((IJavaElement) obj, flags);

    } else if (obj instanceof IResource) {
      return getStyledResourceLabel((IResource) obj);

      //		} else if (obj instanceof ClassPathContainer) {
      //			ClassPathContainer container= (ClassPathContainer) obj;
      //			return getStyledContainerEntryLabel(container.getClasspathEntry().getPath(),
      // container.getJavaProject());
      //
      //		} else if (obj instanceof IStorage) {
      //			return getStyledStorageLabel((IStorage) obj);
      //
      //		} else if (obj instanceof IAdaptable) {
      //			IWorkbenchAdapter wbadapter= (IWorkbenchAdapter)
      // ((IAdaptable)obj).getAdapter(IWorkbenchAdapter.class);
      //			if (wbadapter != null) {
      //				return Strings.markLTR(new StyledString(wbadapter.getLabel(obj)));
      //			}
    }
    return new StyledString();
  }

  /**
   * Returns the styled string for the given resource. The returned label is BiDi-processed with
   * {@link TextProcessor#process(String, String)}.
   *
   * @param resource the resource
   * @return the styled string
   * @since 3.4
   */
  private static StyledString getStyledResourceLabel(IResource resource) {
    StyledString result = new StyledString(resource.getName());
    return Strings.markLTR(result);
  }

  /**
   * Returns the styled string for the given storage. The returned label is BiDi-processed with
   * {@link TextProcessor#process(String, String)}.
   *
   * @param storage the storage
   * @return the styled string
   * @since 3.4
   */
  private static StyledString getStyledStorageLabel(IStorage storage) {
    StyledString result = new StyledString(storage.getName());
    return Strings.markLTR(result);
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
    return Strings.markJavaElementLabelLTR(result.toString());
  }

  /**
   * Returns the styled label for a Java element with the flags as defined by this class.
   *
   * @param element the element to render
   * @param flags the rendering flags
   * @return the label of the Java element
   * @since 3.4
   */
  public static StyledString getStyledElementLabel(IJavaElement element, long flags) {
    StyledString result = new StyledString();
    getElementLabel(element, flags, result);
    return Strings.markJavaElementLabelLTR(result);
  }

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
   * Returns the styled label for a Java element with the flags as defined by this class.
   *
   * @param element the element to render
   * @param flags the rendering flags
   * @param result the buffer to append the resulting label to
   * @since 3.4
   */
  public static void getElementLabel(IJavaElement element, long flags, StyledString result) {
    new JavaElementLabelComposer(result).appendElementLabel(element, flags);
  }

  /**
   * Appends the label for a method to a {@link StringBuffer}. Considers the M_* flags.
   *
   * @param method the element to render
   * @param flags the rendering flags. Flags with names starting with 'M_' are considered.
   * @param buf the buffer to append the resulting label to
   */
  public static void getMethodLabel(IMethod method, long flags, StringBuffer buf) {
    new JavaElementLabelComposer(buf).appendMethodLabel(method, flags);
  }

  /**
   * Appends the label for a method to a {@link StyledString}. Considers the M_* flags.
   *
   * @param method the element to render
   * @param flags the rendering flags. Flags with names starting with 'M_' are considered.
   * @param result the buffer to append the resulting label to
   * @since 3.4
   */
  public static void getMethodLabel(IMethod method, long flags, StyledString result) {
    new JavaElementLabelComposer(result).appendMethodLabel(method, flags);
  }

  /**
   * Appends the label for a field to a {@link StringBuffer}. Considers the F_* flags.
   *
   * @param field the element to render
   * @param flags the rendering flags. Flags with names starting with 'F_' are considered.
   * @param buf the buffer to append the resulting label to
   */
  public static void getFieldLabel(IField field, long flags, StringBuffer buf) {
    new JavaElementLabelComposer(buf).appendFieldLabel(field, flags);
  }

  /**
   * Appends the style label for a field to a {@link StyledString}. Considers the F_* flags.
   *
   * @param field the element to render
   * @param flags the rendering flags. Flags with names starting with 'F_' are considered.
   * @param result the buffer to append the resulting label to
   * @since 3.4
   */
  public static void getFieldLabel(IField field, long flags, StyledString result) {
    new JavaElementLabelComposer(result).appendFieldLabel(field, flags);
  }

  /**
   * Appends the label for a local variable to a {@link StringBuffer}.
   *
   * @param localVariable the element to render
   * @param flags the rendering flags. Flags with names starting with 'F_' are considered.
   * @param buf the buffer to append the resulting label to
   */
  public static void getLocalVariableLabel(
      ILocalVariable localVariable, long flags, StringBuffer buf) {
    new JavaElementLabelComposer(buf).appendLocalVariableLabel(localVariable, flags);
  }

  /**
   * Appends the styled label for a local variable to a {@link StyledString}.
   *
   * @param localVariable the element to render
   * @param flags the rendering flags. Flags with names starting with 'F_' are considered.
   * @param result the buffer to append the resulting label to
   * @since 3.4
   */
  public static void getLocalVariableLabel(
      ILocalVariable localVariable, long flags, StyledString result) {
    new JavaElementLabelComposer(result).appendLocalVariableLabel(localVariable, flags);
  }

  /**
   * Appends the label for a initializer to a {@link StringBuffer}. Considers the I_* flags.
   *
   * @param initializer the element to render
   * @param flags the rendering flags. Flags with names starting with 'I_' are considered.
   * @param buf the buffer to append the resulting label to
   */
  public static void getInitializerLabel(IInitializer initializer, long flags, StringBuffer buf) {
    new JavaElementLabelComposer(buf).appendInitializerLabel(initializer, flags);
  }

  /**
   * Appends the label for a initializer to a {@link StyledString}. Considers the I_* flags.
   *
   * @param initializer the element to render
   * @param flags the rendering flags. Flags with names starting with 'I_' are considered.
   * @param result the buffer to append the resulting label to
   * @since 3.4
   */
  public static void getInitializerLabel(
      IInitializer initializer, long flags, StyledString result) {
    new JavaElementLabelComposer(result).appendInitializerLabel(initializer, flags);
  }

  /**
   * Appends the label for a type to a {@link StringBuffer}. Considers the T_* flags.
   *
   * @param type the element to render
   * @param flags the rendering flags. Flags with names starting with 'T_' are considered.
   * @param buf the buffer to append the resulting label to
   */
  public static void getTypeLabel(IType type, long flags, StringBuffer buf) {
    new JavaElementLabelComposer(buf).appendTypeLabel(type, flags);
  }

  /**
   * Appends the label for a type to a {@link StyledString}. Considers the T_* flags.
   *
   * @param type the element to render
   * @param flags the rendering flags. Flags with names starting with 'T_' are considered.
   * @param result the buffer to append the resulting label to
   * @since 3.4
   */
  public static void getTypeLabel(IType type, long flags, StyledString result) {
    new JavaElementLabelComposer(result).appendTypeLabel(type, flags);
  }

  /**
   * Appends the label for a type parameter to a {@link StringBuffer}. Considers the TP_* flags.
   *
   * @param typeParameter the element to render
   * @param flags the rendering flags. Flags with names starting with 'TP_' are considered.
   * @param buf the buffer to append the resulting label to
   * @since 3.5
   */
  public static void getTypeParameterLabel(
      ITypeParameter typeParameter, long flags, StringBuffer buf) {
    new JavaElementLabelComposer(buf).appendTypeParameterLabel(typeParameter, flags);
  }

  /**
   * Appends the label for a type parameter to a {@link StyledString}. Considers the TP_* flags.
   *
   * @param typeParameter the element to render
   * @param flags the rendering flags. Flags with names starting with 'TP_' are considered.
   * @param result the buffer to append the resulting label to
   * @since 3.5
   */
  public static void getTypeParameterLabel(
      ITypeParameter typeParameter, long flags, StyledString result) {
    new JavaElementLabelComposer(result).appendTypeParameterLabel(typeParameter, flags);
  }

  /**
   * Appends the label for a import container, import or package declaration to a {@link
   * StringBuffer}. Considers the D_* flags.
   *
   * @param declaration the element to render
   * @param flags the rendering flags. Flags with names starting with 'D_' are considered.
   * @param buf the buffer to append the resulting label to
   */
  public static void getDeclarationLabel(IJavaElement declaration, long flags, StringBuffer buf) {
    new JavaElementLabelComposer(buf).appendDeclarationLabel(declaration, flags);
  }

  /**
   * Appends the label for a import container, import or package declaration to a {@link
   * StyledString}. Considers the D_* flags.
   *
   * @param declaration the element to render
   * @param flags the rendering flags. Flags with names starting with 'D_' are considered.
   * @param result the buffer to append the resulting label to
   * @since 3.4
   */
  public static void getDeclarationLabel(
      IJavaElement declaration, long flags, StyledString result) {
    new JavaElementLabelComposer(result).appendDeclarationLabel(declaration, flags);
  }

  /**
   * Appends the label for a class file to a {@link StringBuffer}. Considers the CF_* flags.
   *
   * @param classFile the element to render
   * @param flags the rendering flags. Flags with names starting with 'CF_' are considered.
   * @param buf the buffer to append the resulting label to
   */
  public static void getClassFileLabel(IClassFile classFile, long flags, StringBuffer buf) {
    new JavaElementLabelComposer(buf).appendClassFileLabel(classFile, flags);
  }

  /**
   * Appends the label for a class file to a {@link StyledString}. Considers the CF_* flags.
   *
   * @param classFile the element to render
   * @param flags the rendering flags. Flags with names starting with 'CF_' are considered.
   * @param result the buffer to append the resulting label to
   * @since 3.4
   */
  public static void getClassFileLabel(IClassFile classFile, long flags, StyledString result) {
    new JavaElementLabelComposer(result).appendClassFileLabel(classFile, flags);
  }

  /**
   * Appends the label for a compilation unit to a {@link StringBuffer}. Considers the CU_* flags.
   *
   * @param cu the element to render
   * @param flags the rendering flags. Flags with names starting with 'CU_' are considered.
   * @param buf the buffer to append the resulting label to
   */
  public static void getCompilationUnitLabel(ICompilationUnit cu, long flags, StringBuffer buf) {
    new JavaElementLabelComposer(buf).appendCompilationUnitLabel(cu, flags);
  }

  /**
   * Appends the label for a compilation unit to a {@link StyledString}. Considers the CU_* flags.
   *
   * @param cu the element to render
   * @param flags the rendering flags. Flags with names starting with 'CU_' are considered.
   * @param result the buffer to append the resulting label to
   * @since 3.4
   */
  public static void getCompilationUnitLabel(ICompilationUnit cu, long flags, StyledString result) {
    new JavaElementLabelComposer(result).appendCompilationUnitLabel(cu, flags);
  }

  /**
   * Appends the label for a package fragment to a {@link StringBuffer}. Considers the P_* flags.
   *
   * @param pack the element to render
   * @param flags the rendering flags. Flags with names starting with P_' are considered.
   * @param buf the buffer to append the resulting label to
   */
  public static void getPackageFragmentLabel(IPackageFragment pack, long flags, StringBuffer buf) {
    new JavaElementLabelComposer(buf).appendPackageFragmentLabel(pack, flags);
  }

  /**
   * Appends the label for a package fragment to a {@link StyledString}. Considers the P_* flags.
   *
   * @param pack the element to render
   * @param flags the rendering flags. Flags with names starting with P_' are considered.
   * @param result the buffer to append the resulting label to
   * @since 3.4
   */
  public static void getPackageFragmentLabel(
      IPackageFragment pack, long flags, StyledString result) {
    new JavaElementLabelComposer(result).appendPackageFragmentLabel(pack, flags);
  }

  /**
   * Appends the label for a package fragment root to a {@link StringBuffer}. Considers the ROOT_*
   * flags.
   *
   * @param root the element to render
   * @param flags the rendering flags. Flags with names starting with ROOT_' are considered.
   * @param buf the buffer to append the resulting label to
   */
  public static void getPackageFragmentRootLabel(
      IPackageFragmentRoot root, long flags, StringBuffer buf) {
    new JavaElementLabelComposer(buf).appendPackageFragmentRootLabel(root, flags);
  }

  /**
   * Appends the label for a package fragment root to a {@link StyledString}. Considers the ROOT_*
   * flags.
   *
   * @param root the element to render
   * @param flags the rendering flags. Flags with names starting with ROOT_' are considered.
   * @param result the buffer to append the resulting label to
   * @since 3.4
   */
  public static void getPackageFragmentRootLabel(
      IPackageFragmentRoot root, long flags, StyledString result) {
    new JavaElementLabelComposer(result).appendPackageFragmentRootLabel(root, flags);
  }

  /**
   * Returns the label of a classpath container. The returned label is BiDi-processed with {@link
   * TextProcessor#process(String, String)}.
   *
   * @param containerPath the path of the container
   * @param project the project the container is resolved in
   * @return the label of the classpath container
   * @throws JavaModelException when resolving of the container failed
   */
  public static String getContainerEntryLabel(IPath containerPath, IJavaProject project)
      throws JavaModelException {
    IClasspathContainer container = JavaCore.getClasspathContainer(containerPath, project);
    if (container != null) {
      return Strings.markLTR(container.getDescription());
    }
    ClasspathContainerInitializer initializer =
        JavaCore.getClasspathContainerInitializer(containerPath.segment(0));
    if (initializer != null) {
      return Strings.markLTR(initializer.getDescription(containerPath, project));
    }
    return BasicElementLabels.getPathLabel(containerPath, false);
  }

  /**
   * Returns the styled label of a classpath container. The returned label is BiDi-processed with
   * {@link TextProcessor#process(String, String)}.
   *
   * @param containerPath the path of the container
   * @param project the project the container is resolved in
   * @return the label of the classpath container
   * @since 3.4
   */
  public static StyledString getStyledContainerEntryLabel(
      IPath containerPath, IJavaProject project) {
    try {
      IClasspathContainer container = JavaCore.getClasspathContainer(containerPath, project);
      String description = null;
      if (container != null) {
        description = container.getDescription();
      }
      if (description == null) {
        ClasspathContainerInitializer initializer =
            JavaCore.getClasspathContainerInitializer(containerPath.segment(0));
        if (initializer != null) {
          description = initializer.getDescription(containerPath, project);
        }
      }
      if (description != null) {
        StyledString str = new StyledString(description);
        //				if (containerPath.segmentCount() > 0 &&
        // JavaRuntime.JRE_CONTAINER.equals(containerPath.segment(0))) {
        //					int index= description.indexOf('[');
        //					if (index != -1) {
        //						str.setStyle(index, description.length() - index, DECORATIONS_STYLE);
        //					}
        //				}
        return Strings.markLTR(str);
      }
    } catch (JavaModelException e) {
      // ignore
    }
    return new StyledString(BasicElementLabels.getPathLabel(containerPath, false));
  }
}
