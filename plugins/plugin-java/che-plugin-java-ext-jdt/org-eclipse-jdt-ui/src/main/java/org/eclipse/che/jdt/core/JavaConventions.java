/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jdt.core;

import java.util.StringTokenizer;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.ScannerHelper;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.jdt.internal.core.JavaModelStatus;
import org.eclipse.jdt.internal.core.util.Messages;

/**
 * Provides methods for checking Java-specific conventions such as name syntax.
 *
 * <p>This class provides static methods and constants only.
 *
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class JavaConventions {

  private static final char DOT = '.';
  private static final String PACKAGE_INFO = new String(TypeConstants.PACKAGE_INFO_NAME);
  private static final Scanner SCANNER =
      new Scanner(
          false /*comment*/,
          true /*whitespace*/,
          false /*nls*/,
          ClassFileConstants.JDK1_3 /*sourceLevel*/,
          null /*taskTag*/,
          null /*taskPriorities*/,
          true /*taskCaseSensitive*/);

  private JavaConventions() {
    // Not instantiable
  }

  /**
   * Returns whether the given package fragment root paths are considered to overlap.
   *
   * <p>Two root paths overlap if one is a prefix of the other, or they point to the same location.
   * However, a JAR is allowed to be nested in a root.
   *
   * @param rootPath1 the first root path
   * @param rootPath2 the second root path
   * @return true if the given package fragment root paths are considered to overlap, false
   *     otherwise
   * @deprecated Overlapping roots are allowed in 2.1
   */
  public static boolean isOverlappingRoots(IPath rootPath1, IPath rootPath2) {
    if (rootPath1 == null || rootPath2 == null) {
      return false;
    }
    return rootPath1.isPrefixOf(rootPath2) || rootPath2.isPrefixOf(rootPath1);
  }

  /*
   * Returns the current identifier extracted by the scanner (without unicode
   * escapes) from the given id and for the given source and compliance levels.
   * Returns <code>null</code> if the id was not valid
   */
  private static synchronized char[] scannedIdentifier(
      String id, String sourceLevel, String complianceLevel) {
    if (id == null) {
      return null;
    }
    // Set scanner for given source and compliance levels
    SCANNER.sourceLevel =
        sourceLevel == null
            ? ClassFileConstants.JDK1_3
            : CompilerOptions.versionToJdkLevel(sourceLevel);
    SCANNER.complianceLevel =
        complianceLevel == null
            ? ClassFileConstants.JDK1_3
            : CompilerOptions.versionToJdkLevel(complianceLevel);

    try {
      SCANNER.setSource(id.toCharArray());
      int token = SCANNER.scanIdentifier();
      if (token != TerminalTokens.TokenNameIdentifier) return null;
      if (SCANNER.currentPosition
          == SCANNER.eofPosition) { // to handle case where we had an ArrayIndexOutOfBoundsException
        try {
          return SCANNER.getCurrentIdentifierSource();
        } catch (ArrayIndexOutOfBoundsException e) {
          return null;
        }
      } else {
        return null;
      }
    } catch (InvalidInputException e) {
      return null;
    }
  }

  /**
   * Validate the given compilation unit name.
   *
   * <p>A compilation unit name must obey the following rules:
   *
   * <ul>
   *   <li>it must not be null
   *   <li>it must be suffixed by a dot ('.') followed by one of the {@link
   *       org.eclipse.jdt.core.JavaCore#getJavaLikeExtensions() Java-like extensions}
   *   <li>its prefix must be a valid identifier
   *   <li>it must not contain any characters or substrings that are not valid on the file system on
   *       which workspace root is located.
   * </ul>
   *
   * @param name the name of a compilation unit
   * @return a status object with code <code>IStatus.OK</code> if the given name is valid as a
   *     compilation unit name, otherwise a status object indicating what is wrong with the name
   * @deprecated Use {@link #validateCompilationUnitName(String id, String sourceLevel, String
   *     complianceLevel)} instead
   */
  public static IStatus validateCompilationUnitName(String name) {
    return validateCompilationUnitName(
        name, CompilerOptions.VERSION_1_3, CompilerOptions.VERSION_1_3);
  }

  /**
   * Validate the given compilation unit name for the given source and compliance levels.
   *
   * <p>A compilation unit name must obey the following rules:
   *
   * <ul>
   *   <li>it must not be null
   *   <li>it must be suffixed by a dot ('.') followed by one of the {@link
   *       org.eclipse.jdt.core.JavaCore#getJavaLikeExtensions() Java-like extensions}
   *   <li>its prefix must be a valid identifier
   *   <li>it must not contain any characters or substrings that are not valid on the file system on
   *       which workspace root is located.
   * </ul>
   *
   * @param name the name of a compilation unit
   * @param sourceLevel the source level
   * @param complianceLevel the compliance level
   * @return a status object with code <code>IStatus.OK</code> if the given name is valid as a
   *     compilation unit name, otherwise a status object indicating what is wrong with the name
   * @since 3.3
   */
  public static IStatus validateCompilationUnitName(
      String name, String sourceLevel, String complianceLevel) {
    if (name == null) {
      return new Status(
          IStatus.ERROR,
          org.eclipse.jdt.core.JavaCore.PLUGIN_ID,
          -1,
          Messages.convention_unit_nullName,
          null);
    }
    if (!org.eclipse.jdt.internal.core.util.Util.isJavaLikeFileName(name)) {
      return new Status(
          IStatus.ERROR,
          org.eclipse.jdt.core.JavaCore.PLUGIN_ID,
          -1,
          Messages.convention_unit_notJavaName,
          null);
    }
    String identifier;
    int index;
    index = name.lastIndexOf('.');
    if (index == -1) {
      return new Status(
          IStatus.ERROR,
          org.eclipse.jdt.core.JavaCore.PLUGIN_ID,
          -1,
          Messages.convention_unit_notJavaName,
          null);
    }
    identifier = name.substring(0, index);
    // JSR-175 metadata strongly recommends "package-info.java" as the
    // file in which to store package annotations and
    // the package-level spec (replaces package.html)
    if (!identifier.equals(PACKAGE_INFO)) {
      IStatus status = validateIdentifier(identifier, sourceLevel, complianceLevel);
      if (!status.isOK()) {
        return status;
      }
    }
    //		IStatus status = ResourcesPlugin.getWorkspace().validateName(name, IResource.FILE);
    //		if (!status.isOK()) {
    //			return status;
    //		}
    return JavaModelStatus.VERIFIED_OK;
  }

  /**
   * Validate the given .class file name.
   *
   * <p>A .class file name must obey the following rules:
   *
   * <ul>
   *   <li>it must not be null
   *   <li>it must include the <code>".class"</code> suffix
   *   <li>its prefix must be a valid identifier
   *   <li>it must not contain any characters or substrings that are not valid on the file system on
   *       which workspace root is located.
   * </ul>
   *
   * @param name the name of a .class file
   * @return a status object with code <code>IStatus.OK</code> if the given name is valid as a
   *     .class file name, otherwise a status object indicating what is wrong with the name
   * @since 2.0
   * @deprecated Use {@link #validateClassFileName(String id, String sourceLevel, String
   *     complianceLevel)} instead
   */
  public static IStatus validateClassFileName(String name) {
    return validateClassFileName(name, CompilerOptions.VERSION_1_3, CompilerOptions.VERSION_1_3);
  }

  /**
   * Validate the given .class file name for the given source and compliance levels.
   *
   * <p>A .class file name must obey the following rules:
   *
   * <ul>
   *   <li>it must not be null
   *   <li>it must include the <code>".class"</code> suffix
   *   <li>its prefix must be a valid identifier
   *   <li>it must not contain any characters or substrings that are not valid on the file system on
   *       which workspace root is located.
   * </ul>
   *
   * @param name the name of a .class file
   * @param sourceLevel the source level
   * @param complianceLevel the compliance level
   * @return a status object with code <code>IStatus.OK</code> if the given name is valid as a
   *     .class file name, otherwise a status object indicating what is wrong with the name
   * @since 3.3
   */
  public static IStatus validateClassFileName(
      String name, String sourceLevel, String complianceLevel) {
    if (name == null) {
      return new Status(
          IStatus.ERROR,
          org.eclipse.jdt.core.JavaCore.PLUGIN_ID,
          -1,
          Messages.convention_classFile_nullName,
          null);
    }
    if (!org.eclipse.jdt.internal.compiler.util.Util.isClassFileName(name)) {
      return new Status(
          IStatus.ERROR,
          org.eclipse.jdt.core.JavaCore.PLUGIN_ID,
          -1,
          Messages.convention_classFile_notClassFileName,
          null);
    }
    String identifier;
    int index;
    index = name.lastIndexOf('.');
    if (index == -1) {
      return new Status(
          IStatus.ERROR,
          org.eclipse.jdt.core.JavaCore.PLUGIN_ID,
          -1,
          Messages.convention_classFile_notClassFileName,
          null);
    }
    identifier = name.substring(0, index);
    // JSR-175 metadata strongly recommends "package-info.java" as the
    // file in which to store package annotations and
    // the package-level spec (replaces package.html)
    if (!identifier.equals(PACKAGE_INFO)) {
      IStatus status = validateIdentifier(identifier, sourceLevel, complianceLevel);
      if (!status.isOK()) {
        return status;
      }
    }
    //		IStatus status = ResourcesPlugin.getWorkspace().validateName(name, IResource.FILE);
    //		if (!status.isOK()) {
    //			return status;
    //		}
    return JavaModelStatus.VERIFIED_OK;
  }

  /**
   * Validate the given field name.
   *
   * <p>Syntax of a field name corresponds to VariableDeclaratorId (JLS2 8.3). For example, <code>
   * "x"</code>.
   *
   * @param name the name of a field
   * @return a status object with code <code>IStatus.OK</code> if the given name is valid as a field
   *     name, otherwise a status object indicating what is wrong with the name
   * @deprecated Use {@link #validateFieldName(String id, String sourceLevel, String
   *     complianceLevel)} instead
   */
  public static IStatus validateFieldName(String name) {
    return validateIdentifier(name, CompilerOptions.VERSION_1_3, CompilerOptions.VERSION_1_3);
  }

  /**
   * Validate the given field name for the given source and compliance levels.
   *
   * <p>Syntax of a field name corresponds to VariableDeclaratorId (JLS2 8.3). For example, <code>
   * "x"</code>.
   *
   * @param name the name of a field
   * @param sourceLevel the source level
   * @param complianceLevel the compliance level
   * @return a status object with code <code>IStatus.OK</code> if the given name is valid as a field
   *     name, otherwise a status object indicating what is wrong with the name
   * @since 3.3
   */
  public static IStatus validateFieldName(String name, String sourceLevel, String complianceLevel) {
    return validateIdentifier(name, sourceLevel, complianceLevel);
  }

  /**
   * Validate the given Java identifier. The identifier must not have the same spelling as a Java
   * keyword, boolean literal (<code>"true"</code>, <code>"false"</code>), or null literal (<code>
   * "null"</code>). See section 3.8 of the <em>Java Language Specification, Second Edition</em>
   * (JLS2). A valid identifier can act as a simple type name, method name or field name.
   *
   * @param id the Java identifier
   * @return a status object with code <code>IStatus.OK</code> if the given identifier is a valid
   *     Java identifier, otherwise a status object indicating what is wrong with the identifier
   * @deprecated Use {@link #validateIdentifier(String id, String sourceLevel, String
   *     complianceLevel)} instead
   */
  public static IStatus validateIdentifier(String id) {
    return validateIdentifier(id, CompilerOptions.VERSION_1_3, CompilerOptions.VERSION_1_3);
  }

  /**
   * Validate the given Java identifier for the given source and compliance levels The identifier
   * must not have the same spelling as a Java keyword, boolean literal (<code>"true"</code>, <code>
   * "false"</code>), or null literal (<code>"null"</code>). See section 3.8 of the <em>Java
   * Language Specification, Second Edition</em> (JLS2). A valid identifier can act as a simple type
   * name, method name or field name.
   *
   * @param id the Java identifier
   * @param sourceLevel the source level
   * @param complianceLevel the compliance level
   * @return a status object with code <code>IStatus.OK</code> if the given identifier is a valid
   *     Java identifier, otherwise a status object indicating what is wrong with the identifier
   * @since 3.3
   */
  public static IStatus validateIdentifier(String id, String sourceLevel, String complianceLevel) {
    if (scannedIdentifier(id, sourceLevel, complianceLevel) != null) {
      return JavaModelStatus.VERIFIED_OK;
    } else {
      return new Status(
          IStatus.ERROR,
          org.eclipse.jdt.core.JavaCore.PLUGIN_ID,
          -1,
          Messages.bind(Messages.convention_illegalIdentifier, id),
          null);
    }
  }

  /**
   * Validate the given import declaration name.
   *
   * <p>The name of an import corresponds to a fully qualified type name or an on-demand package
   * name as defined by ImportDeclaration (JLS2 7.5). For example, <code>"java.util.*"</code> or
   * <code>"java.util.Hashtable"</code>.
   *
   * @param name the import declaration
   * @return a status object with code <code>IStatus.OK</code> if the given name is valid as an
   *     import declaration, otherwise a status object indicating what is wrong with the name
   * @deprecated Use {@link #validateImportDeclaration(String id, String sourceLevel, String
   *     complianceLevel)} instead
   */
  public static IStatus validateImportDeclaration(String name) {
    return validateImportDeclaration(
        name, CompilerOptions.VERSION_1_3, CompilerOptions.VERSION_1_3);
  }

  /**
   * Validate the given import declaration name for the given source and compliance levels.
   *
   * <p>The name of an import corresponds to a fully qualified type name or an on-demand package
   * name as defined by ImportDeclaration (JLS2 7.5). For example, <code>"java.util.*"</code> or
   * <code>"java.util.Hashtable"</code>.
   *
   * @param name the import declaration
   * @param sourceLevel the source level
   * @param complianceLevel the compliance level
   * @return a status object with code <code>IStatus.OK</code> if the given name is valid as an
   *     import declaration, otherwise a status object indicating what is wrong with the name
   * @since 3.3
   */
  public static IStatus validateImportDeclaration(
      String name, String sourceLevel, String complianceLevel) {
    if (name == null || name.length() == 0) {
      return new Status(
          IStatus.ERROR,
          org.eclipse.jdt.core.JavaCore.PLUGIN_ID,
          -1,
          Messages.convention_import_nullImport,
          null);
    }
    if (name.charAt(name.length() - 1) == '*') {
      if (name.charAt(name.length() - 2) == '.') {
        return validatePackageName(
            name.substring(0, name.length() - 2), sourceLevel, complianceLevel);
      } else {
        return new Status(
            IStatus.ERROR,
            org.eclipse.jdt.core.JavaCore.PLUGIN_ID,
            -1,
            Messages.convention_import_unqualifiedImport,
            null);
      }
    }
    return validatePackageName(name, sourceLevel, complianceLevel);
  }

  /**
   * Validate the given Java type name, either simple or qualified. For example, <code>
   * "java.lang.Object"</code>, or <code>"Object"</code>.
   *
   * <p>
   *
   * @param name the name of a type
   * @return a status object with code <code>IStatus.OK</code> if the given name is valid as a Java
   *     type name, a status with code <code>IStatus.WARNING</code> indicating why the given name is
   *     discouraged, otherwise a status object indicating what is wrong with the name
   * @deprecated Use {@link #validateJavaTypeName(String id, String sourceLevel, String
   *     complianceLevel)} instead
   */
  public static IStatus validateJavaTypeName(String name) {
    return validateJavaTypeName(
        name, org.eclipse.jdt.core.JavaCore.VERSION_1_3, org.eclipse.jdt.core.JavaCore.VERSION_1_3);
  }

  /**
   * Validate the given Java type name, either simple or qualified, for the given source and
   * compliance levels.
   *
   * <p>For example, <code>"java.lang.Object"</code>, or <code>"Object"</code>.
   *
   * <p>The source level and compliance level values should be taken from the constant defined
   * inside {@link org.eclipse.jdt.core.JavaCore} class. The constants are named <code>
   * JavaCore#VERSION_1_x</code>, x being set between '1' and '8'.
   *
   * @param name the name of a type
   * @param sourceLevel the source level
   * @param complianceLevel the compliance level
   * @return a status object with code <code>IStatus.OK</code> if the given name is valid as a Java
   *     type name, a status with code <code>IStatus.WARNING</code> indicating why the given name is
   *     discouraged, otherwise a status object indicating what is wrong with the name
   * @since 3.3
   * @see org.eclipse.jdt.core.JavaCore#VERSION_1_1
   * @see org.eclipse.jdt.core.JavaCore#VERSION_1_2
   * @see org.eclipse.jdt.core.JavaCore#VERSION_1_3
   * @see org.eclipse.jdt.core.JavaCore#VERSION_1_4
   * @see org.eclipse.jdt.core.JavaCore#VERSION_1_5
   * @see org.eclipse.jdt.core.JavaCore#VERSION_1_6
   * @see org.eclipse.jdt.core.JavaCore#VERSION_1_7
   * @see org.eclipse.jdt.core.JavaCore#VERSION_1_8
   */
  public static IStatus validateJavaTypeName(
      String name, String sourceLevel, String complianceLevel) {
    if (name == null) {
      return new Status(
          IStatus.ERROR,
          org.eclipse.jdt.core.JavaCore.PLUGIN_ID,
          -1,
          Messages.convention_type_nullName,
          null);
    }
    String trimmed = name.trim();
    if (!name.equals(trimmed)) {
      return new Status(
          IStatus.ERROR,
          org.eclipse.jdt.core.JavaCore.PLUGIN_ID,
          -1,
          Messages.convention_type_nameWithBlanks,
          null);
    }
    int index = name.lastIndexOf('.');
    char[] scannedID;
    if (index == -1) {
      // simple name
      scannedID = scannedIdentifier(name, sourceLevel, complianceLevel);
    } else {
      // qualified name
      String pkg = name.substring(0, index).trim();
      IStatus status = validatePackageName(pkg, sourceLevel, complianceLevel);
      if (!status.isOK()) {
        return status;
      }
      String type = name.substring(index + 1).trim();
      scannedID = scannedIdentifier(type, sourceLevel, complianceLevel);
    }

    if (scannedID != null) {
      //			IStatus status = ResourcesPlugin.getWorkspace().validateName(new String(scannedID),
      // IResource.FILE);
      //			if (!status.isOK()) {
      //				return status;
      //			}
      if (CharOperation.contains('$', scannedID)) {
        return new Status(
            IStatus.WARNING,
            org.eclipse.jdt.core.JavaCore.PLUGIN_ID,
            -1,
            Messages.convention_type_dollarName,
            null);
      }
      if ((scannedID.length > 0 && ScannerHelper.isLowerCase(scannedID[0]))) {
        return new Status(
            IStatus.WARNING,
            org.eclipse.jdt.core.JavaCore.PLUGIN_ID,
            -1,
            Messages.convention_type_lowercaseName,
            null);
      }
      return JavaModelStatus.VERIFIED_OK;
    } else {
      return new Status(
          IStatus.ERROR,
          org.eclipse.jdt.core.JavaCore.PLUGIN_ID,
          -1,
          Messages.bind(Messages.convention_type_invalidName, name),
          null);
    }
  }

  /**
   * Validate the given method name. The special names "&lt;init&gt;" and "&lt;clinit&gt;" are not
   * valid.
   *
   * <p>The syntax for a method name is defined by Identifier of MethodDeclarator (JLS2 8.4). For
   * example "println".
   *
   * @param name the name of a method
   * @return a status object with code <code>IStatus.OK</code> if the given name is valid as a
   *     method name, otherwise a status object indicating what is wrong with the name
   * @deprecated Use {@link #validateMethodName(String id, String sourceLevel, String
   *     complianceLevel)} instead
   */
  public static IStatus validateMethodName(String name) {
    return validateMethodName(name, CompilerOptions.VERSION_1_3, CompilerOptions.VERSION_1_3);
  }

  /**
   * Validate the given method name for the given source and compliance levels. The special names
   * "&lt;init&gt;" and "&lt;clinit&gt;" are not valid.
   *
   * <p>The syntax for a method name is defined by Identifier of MethodDeclarator (JLS2 8.4). For
   * example "println".
   *
   * @param name the name of a method
   * @param sourceLevel the source level
   * @param complianceLevel the compliance level
   * @return a status object with code <code>IStatus.OK</code> if the given name is valid as a
   *     method name, otherwise a status object indicating what is wrong with the name
   * @since 3.3
   */
  public static IStatus validateMethodName(
      String name, String sourceLevel, String complianceLevel) {
    return validateIdentifier(name, sourceLevel, complianceLevel);
  }

  /**
   * Validate the given package name.
   *
   * <p>The syntax of a package name corresponds to PackageName as defined by PackageDeclaration
   * (JLS2 7.4). For example, <code>"java.lang"</code>.
   *
   * <p>Note that the given name must be a non-empty package name (that is, attempting to validate
   * the default package will return an error status.) Also it must not contain any characters or
   * substrings that are not valid on the file system on which workspace root is located.
   *
   * @param name the name of a package
   * @return a status object with code <code>IStatus.OK</code> if the given name is valid as a
   *     package name, otherwise a status object indicating what is wrong with the name
   * @deprecated Use {@link #validatePackageName(String id, String sourceLevel, String
   *     complianceLevel)} instead
   */
  public static IStatus validatePackageName(String name) {
    return validatePackageName(name, CompilerOptions.VERSION_1_3, CompilerOptions.VERSION_1_3);
  }

  /**
   * Validate the given package name for the given source and compliance levels.
   *
   * <p>The syntax of a package name corresponds to PackageName as defined by PackageDeclaration
   * (JLS2 7.4). For example, <code>"java.lang"</code>.
   *
   * <p>Note that the given name must be a non-empty package name (that is, attempting to validate
   * the default package will return an error status.) Also it must not contain any characters or
   * substrings that are not valid on the file system on which workspace root is located.
   *
   * @param name the name of a package
   * @param sourceLevel the source level
   * @param complianceLevel the compliance level
   * @return a status object with code <code>IStatus.OK</code> if the given name is valid as a
   *     package name, otherwise a status object indicating what is wrong with the name
   * @since 3.3
   */
  public static IStatus validatePackageName(
      String name, String sourceLevel, String complianceLevel) {

    if (name == null) {
      return new Status(
          IStatus.ERROR,
          org.eclipse.jdt.core.JavaCore.PLUGIN_ID,
          -1,
          Messages.convention_package_nullName,
          null);
    }
    int length;
    if ((length = name.length()) == 0) {
      return new Status(
          IStatus.ERROR,
          org.eclipse.jdt.core.JavaCore.PLUGIN_ID,
          -1,
          Messages.convention_package_emptyName,
          null);
    }
    if (name.charAt(0) == DOT || name.charAt(length - 1) == DOT) {
      return new Status(
          IStatus.ERROR,
          org.eclipse.jdt.core.JavaCore.PLUGIN_ID,
          -1,
          Messages.convention_package_dotName,
          null);
    }
    if (CharOperation.isWhitespace(name.charAt(0))
        || CharOperation.isWhitespace(name.charAt(name.length() - 1))) {
      return new Status(
          IStatus.ERROR,
          org.eclipse.jdt.core.JavaCore.PLUGIN_ID,
          -1,
          Messages.convention_package_nameWithBlanks,
          null);
    }
    int dot = 0;
    while (dot != -1 && dot < length - 1) {
      if ((dot = name.indexOf(DOT, dot + 1)) != -1
          && dot < length - 1
          && name.charAt(dot + 1) == DOT) {
        return new Status(
            IStatus.ERROR,
            org.eclipse.jdt.core.JavaCore.PLUGIN_ID,
            -1,
            Messages.convention_package_consecutiveDotsName,
            null);
      }
    }
    //		IWorkspace workspace = ResourcesPlugin.getWorkspace();
    StringTokenizer st = new StringTokenizer(name, "."); // $NON-NLS-1$
    boolean firstToken = true;
    IStatus warningStatus = null;
    while (st.hasMoreTokens()) {
      String typeName = st.nextToken();
      typeName = typeName.trim(); // grammar allows spaces
      char[] scannedID = scannedIdentifier(typeName, sourceLevel, complianceLevel);
      if (scannedID == null) {
        return new Status(
            IStatus.ERROR,
            org.eclipse.jdt.core.JavaCore.PLUGIN_ID,
            -1,
            Messages.bind(Messages.convention_illegalIdentifier, typeName),
            null);
      }
      //			IStatus status = workspace.validateName(new String(scannedID), IResource.FOLDER);
      //			if (!status.isOK()) {
      //				return status;
      //			}
      if (firstToken && scannedID.length > 0 && ScannerHelper.isUpperCase(scannedID[0])) {
        if (warningStatus == null) {
          warningStatus =
              new Status(
                  IStatus.WARNING,
                  org.eclipse.jdt.core.JavaCore.PLUGIN_ID,
                  -1,
                  Messages.convention_package_uppercaseName,
                  null);
        }
      }
      firstToken = false;
    }
    if (warningStatus != null) {
      return warningStatus;
    }
    return JavaModelStatus.VERIFIED_OK;
  }

  /**
   * Validate a given classpath and output location for a project, using the following rules:
   *
   * <ul>
   *   <li>Classpath entries cannot collide with each other; that is, all entry paths must be
   *       unique.
   *   <li>The project output location path cannot be null, must be absolute and located inside the
   *       project.
   *   <li>Specific output locations (specified on source entries) can be null, if not they must be
   *       located inside the project,
   *   <li>A project entry cannot refer to itself directly (that is, a project cannot prerequisite
   *       itself).
   *   <li>Classpath entries or output locations cannot coincide or be nested in each other, except
   *       for the following scenarios listed below:
   *       <ul>
   *         <li>A source folder can coincide with its own output location, in which case this
   *             output can then contain library archives. However, a specific output location
   *             cannot coincide with any library or a distinct source folder than the one referring
   *             to it.<br>
   *             Note: Since 3.8, this behavior can be overridden by configuring {@link
   *             org.eclipse.jdt.core.JavaCore#CORE_OUTPUT_LOCATION_OVERLAPPING_ANOTHER_SOURCE}
   *         <li>A source/library folder can be nested in any source folder as long as the nested
   *             folder is excluded from the enclosing one.
   *         <li>An output location can be nested in a source folder, if the source folder coincides
   *             with the project itself, or if the output location is excluded from the source
   *             folder.
   *       </ul>
   * </ul>
   *
   * Note that the classpath entries are not validated automatically. Only bound variables or
   * containers are considered in the checking process (this allows to perform a consistency check
   * on a classpath which has references to yet non existing projects, folders, ...).
   *
   * <p>This validation is intended to anticipate classpath issues prior to assigning it to a
   * project. In particular, it will automatically be performed during the classpath setting
   * operation (if validation fails, the classpath setting will not complete).
   *
   * <p>
   *
   * @param javaProject the given java project
   * @param rawClasspath the given classpath
   * @param projectOutputLocation the given output location
   * @return a status object with code <code>IStatus.OK</code> if the given classpath and output
   *     location are compatible, otherwise a status object indicating what is wrong with the
   *     classpath or output location
   * @since 2.0
   */
  public static IJavaModelStatus validateClasspath(
      IJavaProject javaProject, IClasspathEntry[] rawClasspath, IPath projectOutputLocation) {

    //		return ClasspathEntry.validateClasspath(javaProject, rawClasspath, projectOutputLocation);
    throw new UnsupportedOperationException();
  }

  /**
   * Returns a Java model status describing the problem related to this classpath entry if any, a
   * status object with code <code>IStatus.OK</code> if the entry is fine (that is, if the given
   * classpath entry denotes a valid element to be referenced onto a classpath).
   *
   * @param project the given java project
   * @param entry the given classpath entry
   * @param checkSourceAttachment a flag to determine if source attachment should be checked
   * @return a java model status describing the problem related to this classpath entry if any, a
   *     status object with code <code>IStatus.OK</code> if the entry is fine
   * @since 2.0
   */
  public static IJavaModelStatus validateClasspathEntry(
      IJavaProject project, IClasspathEntry entry, boolean checkSourceAttachment) {
    //		return ClasspathEntry.validateClasspathEntry(project, entry, checkSourceAttachment,
    // false/*not referred by container*/);
    throw new UnsupportedOperationException();
  }

  /**
   * Validate the given type variable name.
   *
   * <p>Syntax of a type variable name corresponds to a Java identifier (JLS3 4.3). For example,
   * <code>"E"</code>.
   *
   * @param name the name of a type variable
   * @return a status object with code <code>IStatus.OK</code> if the given name is valid as a type
   *     variable name, otherwise a status object indicating what is wrong with the name
   * @since 3.1
   * @deprecated Use {@link #validateTypeVariableName(String id, String sourceLevel, String
   *     complianceLevel)} instead
   */
  public static IStatus validateTypeVariableName(String name) {
    return validateIdentifier(name, CompilerOptions.VERSION_1_3, CompilerOptions.VERSION_1_3);
  }

  /**
   * Validate the given type variable name for the given source and compliance levels.
   *
   * <p>Syntax of a type variable name corresponds to a Java identifier (JLS3 4.3). For example,
   * <code>"E"</code>.
   *
   * @param name the name of a type variable
   * @param sourceLevel the source level
   * @param complianceLevel the compliance level
   * @return a status object with code <code>IStatus.OK</code> if the given name is valid as a type
   *     variable name, otherwise a status object indicating what is wrong with the name
   * @since 3.3
   */
  public static IStatus validateTypeVariableName(
      String name, String sourceLevel, String complianceLevel) {
    return validateIdentifier(name, sourceLevel, complianceLevel);
  }

  /**
   * Validate that all compiler options of the given project match keys and values described in
   * {@link org.eclipse.jdt.core.JavaCore#getDefaultOptions()} method.
   *
   * @param javaProject the given java project
   * @param inheritJavaCoreOptions inherit project options from JavaCore or not.
   * @return a status object with code <code>IStatus.OK</code> if all project compiler options are
   *     valid, otherwise a status object indicating what is wrong with the keys and their value.
   * @since 3.1 TODO (frederic) finalize for all possible options (JavaCore,
   *     DefaultCodeFormatterOptions, AssistOptions) and open to API
   */
  /*
  public static IStatus validateCompilerOptions(IJavaProject javaProject, boolean inheritJavaCoreOptions)	  {
  	return validateCompilerOptions(javaProject.getOptions(inheritJavaCoreOptions));
  }
  */

  /**
   * Validate that all compiler options of the given project match keys and values described in
   * {@link org.eclipse.jdt.core.JavaCore#getDefaultOptions()} method.
   *
   * @param compilerOptions Map of options
   * @return a status object with code <code>IStatus.OK</code> if all compiler options are valid,
   *     otherwise a status object indicating what is wrong with the keys and their value.
   * @since 3.1
   */
  /*
  public static IStatus validateCompilerOptions(Map compilerOptions)	  {

  	// Get current options
  	String compliance = (String) compilerOptions.get(JavaCore.COMPILER_COMPLIANCE);
  	String source = (String) compilerOptions.get(JavaCore.COMPILER_SOURCE);
  	String target = (String) compilerOptions.get(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM);
  	if (compliance == null && source == null && target == null) {
  		return JavaModelStatus.VERIFIED_OK; // default is OK
  	}

  	// Initialize multi-status
  	List errors = new ArrayList();

  	// Set default for compliance if necessary (not set on project and not inherited...)
  	if (compliance == null) {
  		compliance = JavaCore.getOption(JavaCore.COMPILER_COMPLIANCE);
  	}

  	// Verify compliance level value and set source and target default if necessary
  	long complianceLevel = 0;
  	long sourceLevel = 0;
  	long targetLevel = 0;
  	if (JavaCore.VERSION_1_3.equals(compliance)) {
  		complianceLevel = ClassFileConstants.JDK1_3;
  		if (source == null) {
  			source = JavaCore.VERSION_1_3;
  			sourceLevel = ClassFileConstants.JDK1_3;
  		}
  		if (target == null) {
  			target = JavaCore.VERSION_1_1;
  			targetLevel = ClassFileConstants.JDK1_1;
  		}
  	} else if (JavaCore.VERSION_1_4.equals(compliance)) {
  		complianceLevel = ClassFileConstants.JDK1_4;
  		if (source == null) {
  			source = JavaCore.VERSION_1_3;
  			sourceLevel = ClassFileConstants.JDK1_3;
  		}
  		if (target == null) {
  			target = JavaCore.VERSION_1_2;
  			targetLevel = ClassFileConstants.JDK1_2;
  		}
  	} else if (JavaCore.VERSION_1_5.equals(compliance)) {
  		complianceLevel = ClassFileConstants.JDK1_5;
  		if (source == null) {
  			source = JavaCore.VERSION_1_5;
  			sourceLevel = ClassFileConstants.JDK1_5;
  		}
  		if (target == null) {
  			target = JavaCore.VERSION_1_5;
  			targetLevel = ClassFileConstants.JDK1_5;
  		}
  	} else {
  		// compliance is not valid
  		errors.add(new JavaModelStatus(IStatus.ERROR, Util.bind("convention.compiler.invalidCompilerOption", compliance==null?"":compliance, JavaCore.COMPILER_COMPLIANCE))); //$NON-NLS-1$ //$NON-NLS-2$
  	}

  	// Verify source value and set default for target if necessary
  	 if (JavaCore.VERSION_1_4.equals(source)) {
  		sourceLevel = ClassFileConstants.JDK1_4;
  		if (target == null) {
  			target = JavaCore.VERSION_1_4;
  			targetLevel = ClassFileConstants.JDK1_4;
  		}
  	} else if (JavaCore.VERSION_1_5.equals(source)) {
  		sourceLevel = ClassFileConstants.JDK1_5;
  		if (target == null) {
  			target = JavaCore.VERSION_1_5;
  			targetLevel = ClassFileConstants.JDK1_5;
  		}
  	} else if (JavaCore.VERSION_1_3.equals(source)) {
  		sourceLevel = ClassFileConstants.JDK1_3;
  	} else {
  		// source is not valid
  		errors.add(new JavaModelStatus(IStatus.ERROR, Util.bind("convention.compiler.invalidCompilerOption", source==null?"":source, JavaCore.COMPILER_SOURCE))); //$NON-NLS-1$ //$NON-NLS-2$
  	}

  	// Verify target value
  	 if (targetLevel == 0) {
  		 targetLevel = CompilerOptions.versionToJdkLevel(target);
  		 if (targetLevel == 0) {
  			// target is not valid
  			errors.add(new JavaModelStatus(IStatus.ERROR, Util.bind("convention.compiler.invalidCompilerOption", target==null?"":target, JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM))); //$NON-NLS-1$ //$NON-NLS-2$
  		 }
  	}

  	// Check and set compliance/source/target compatibilities (only if they have valid values)
  	if (complianceLevel != 0 && sourceLevel != 0 && targetLevel != 0) {
  		// target must be 1.5 if source is 1.5
  		if (sourceLevel >= ClassFileConstants.JDK1_5 && targetLevel < ClassFileConstants.JDK1_5) {
  			errors.add(new JavaModelStatus(IStatus.ERROR, Util.bind("convention.compiler.incompatibleTargetForSource", target, JavaCore.VERSION_1_5))); //$NON-NLS-1$
  		}
     		else
  	   		// target must be 1.4 if source is 1.4
     			if (sourceLevel >= ClassFileConstants.JDK1_4 && targetLevel < ClassFileConstants.JDK1_4) {
  				errors.add(new JavaModelStatus(IStatus.ERROR, Util.bind("convention.compiler.incompatibleTargetForSource", target, JavaCore.VERSION_1_4))); //$NON-NLS-1$
     		}
  		// target cannot be greater than compliance level
  		if (complianceLevel < targetLevel){
  			errors.add(new JavaModelStatus(IStatus.ERROR, Util.bind("convention.compiler.incompatibleComplianceForTarget", compliance, JavaCore.VERSION_1_4))); //$NON-NLS-1$
  		}
  		// compliance must be 1.5 if source is 1.5
  		if (source.equals(JavaCore.VERSION_1_5) && complianceLevel < ClassFileConstants.JDK1_5) {
  			errors.add(new JavaModelStatus(IStatus.ERROR, Util.bind("convention.compiler.incompatibleComplianceForSource", compliance, JavaCore.VERSION_1_5))); //$NON-NLS-1$
  		} else
  			// compliance must be 1.4 if source is 1.4
  			if (source.equals(JavaCore.VERSION_1_4) && complianceLevel < ClassFileConstants.JDK1_4) {
  				errors.add(new JavaModelStatus(IStatus.ERROR, Util.bind("convention.compiler.incompatibleComplianceForSource", compliance, JavaCore.VERSION_1_4))); //$NON-NLS-1$
  		}
  	}

  	// Return status
  	int size = errors.size();
  	switch (size) {
  		case 0:
  			return JavaModelStatus.VERIFIED_OK;
  		case 1:
  			return (IStatus) errors.get(0);
  		default:
  			IJavaModelStatus[] allStatus = new IJavaModelStatus[size];
  			errors.toArray(allStatus);
  			return JavaModelStatus.newMultiStatus(allStatus);
  	}
  }
  */
}
