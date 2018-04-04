/**
 * ***************************************************************************** Copyright (c)
 * 2012-2014 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jdt.dom;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.jdt.internal.core.JavaModelStatus;
import org.eclipse.jdt.internal.core.util.Messages;
import org.eclipse.jdt.internal.core.util.Util;

/** @author Evgen Vidolob */
public class JavaConventions {

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
          IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Messages.convention_unit_nullName, null);
    }
    if (!Util.isJavaLikeFileName(name)) {
      return new Status(
          IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Messages.convention_unit_notJavaName, null);
    }
    String identifier;
    int index;
    index = name.lastIndexOf('.');
    if (index == -1) {
      return new Status(
          IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Messages.convention_unit_notJavaName, null);
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
    //        IStatus status = ResourcesPlugin.getWorkspace().validateName(name, IResource.FILE);
    //        if (!status.isOK()) {
    //            return status;
    //        }
    return JavaModelStatus.VERIFIED_OK;
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
          IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Messages.convention_classFile_nullName, null);
    }
    if (!org.eclipse.jdt.internal.compiler.util.Util.isClassFileName(name)) {
      return new Status(
          IStatus.ERROR,
          JavaCore.PLUGIN_ID,
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
          JavaCore.PLUGIN_ID,
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
    //        IStatus status = ResourcesPlugin.getWorkspace().validateName(name, IResource.FILE);
    //        if (!status.isOK()) {
    //            return status;
    //        }
    return JavaModelStatus.VERIFIED_OK;
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
          JavaCore.PLUGIN_ID,
          -1,
          Messages.bind(Messages.convention_illegalIdentifier, id),
          null);
    }
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
}
