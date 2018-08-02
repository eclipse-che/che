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
package org.eclipse.che.ide.ext.java.client;

import static org.eclipse.che.ide.runtime.IStatus.ERROR;
import static org.eclipse.che.ide.runtime.Status.OK_STATUS;

import com.google.gwt.regexp.shared.RegExp;
import org.eclipse.che.ide.runtime.IStatus;
import org.eclipse.che.ide.runtime.Status;

/**
 * A collection of methods for Java-specific things.
 *
 * @author Artem Zatsarynnyi
 */
public class JavaUtils {

  private static final String INT = "^int$|^int\\..*|.*\\.int\\..*|.*\\.int$";
  private static final String ABSTRACT =
      "^abstract$|^abstract\\..*|.*\\.abstract\\..*|.*\\.abstract$";
  private static final String ASSERT = "^assert$|^assert\\..*|.*\\.assert\\..*|.*\\.assert$";
  private static final String BOOLEAN = "^boolean$|^boolean\\..*|.*\\.boolean\\..*|.*\\.boolean$";
  private static final String BREAK = "^break$|^break\\..*|.*\\.break\\..*|.*\\.break$";
  private static final String BYTE = "^byte$|^byte\\..*|.*\\.byte\\..*|.*\\.byte$";
  private static final String CASE = "^case$|^case\\..*|.*\\.case\\..*|.*\\.case$";
  private static final String CATCH = "^catch$|^catch\\..*|.*\\.catch\\..*|.*\\.catch$";
  private static final String CHAR = "^char$|^char\\..*|.*\\.char\\..*|.*\\.char$";
  private static final String CLASS = "^class$|^class\\..*|.*\\.class\\..*|.*\\.class$";
  private static final String CONST = "^const$|^const\\..*|.*\\.const\\..*|.*\\.const$";
  private static final String CONTINUE =
      "^continue$|^continue\\..*|.*\\.continue\\..*|.*\\.continue$";
  private static final String DEFAULT = "^default$|^default\\..*|.*\\.default\\..*|.*\\.default$";
  private static final String DO = "^do$|^do\\..*|.*\\.do\\..*|.*\\.do$";
  private static final String DOUBLE = "^double$|^double\\..*|.*\\.double\\..*|.*\\.double$";
  private static final String ELSE = "^else$|^else\\..*|.*\\.else\\..*|.*\\.else$";
  private static final String ENUM = "^enum$|^enum\\..*|.*\\.enum\\..*|.*\\.enum$";
  private static final String EXTENDS = "^extends$|^extends\\..*|.*\\.extends\\..*|.*\\.extends$";
  private static final String FINAL = "^final$|^final\\..*|.*\\.final\\..*|.*\\.final$";
  private static final String FINALLY = "^finally$|^finally\\..*|.*\\.finally\\..*|.*\\.finally$";
  private static final String FLOAT = "^float$|^float\\..*|.*\\.float\\..*|.*\\.float$";
  private static final String FOR = "^for$|^for\\..*|.*\\.for\\..*|.*\\.for$";
  private static final String GOTO = "^goto$|^goto\\..*|.*\\.goto\\..*|.*\\.goto$";
  private static final String IMPORT = "^import$|^import\\..*|.*\\.import\\..*|.*\\.import$";
  private static final String INSTANCE_OF =
      "^instanceof$|^instanceof\\..*|.*\\.instanceof\\..*|.*\\.instanceof$";
  private static final String NEW = "^new$|^new\\..*|.*\\.new\\..*|.*\\.new$";
  private static final String PACKAGE = "^package$|^package\\..*|.*\\.package\\..*|.*\\.package$";
  private static final String PRIVATE = "^private$|^private\\..*|.*\\.private\\..*|.*\\.private$";
  private static final String PROTECTED =
      "^protected$|^protected\\..*|.*\\.protected\\..*|.*\\.protected$";
  private static final String PUBLIC = "^public$|^public\\..*|.*\\.public\\..*|.*\\.public$";
  private static final String RETURN = "^return$|^return\\..*|.*\\.return\\..*|.*\\.return$";
  private static final String SHORT = "^short$|^short\\..*|.*\\.short\\..*|.*\\.short$";
  private static final String STATIC = "^static$|^static\\..*|.*\\.static\\..*|.*\\.static$";
  private static final String STRICTFP =
      "^strictfp$|^strictfp\\..*|.*\\.strictfp\\..*|.*\\.strictfp$";
  private static final String SUPER = "^super$|^super\\..*|.*\\.super\\..*|.*\\.super$";
  private static final String SWITCH = "^switch$|^switch\\..*|.*\\.switch\\..*|.*\\.switch$";
  private static final String SYNCHRONIZED =
      "^synchronized$|^synchronized\\..*|.*\\.synchronized\\..*|.*\\.synchronized$";
  private static final String THIS = "^this$|^this\\..*|.*\\.this\\..*|.*\\.this$";
  private static final String THROW = "^throw$|^throw\\..*|.*\\.throw\\..*|.*\\.throw$";
  private static final String THROWS = "^throws$|^throws\\..*|.*\\.throws\\..*|.*\\.throws$";
  private static final String TRANSIENT =
      "^transient$|^transient\\..*|.*\\.transient\\..*|.*\\.transient$";
  private static final String TRY = "^try$|^try\\..*|.*\\.try\\..*|.*\\.try$";
  private static final String VOID = "^void$|^void\\..*|.*\\.void\\..*|.*\\.void$";
  private static final String VOLATILE =
      "^volatile$|^volatile\\..*|.*\\.volatile\\..*|.*\\.volatile$";
  private static final String WHILE = "^while$|^while\\..*|.*\\.while\\..*|.*\\.while$";
  private static final String IF = "^if$|^if\\..*|.*\\.if\\..*|.*\\.if$";
  private static final String IMPLEMENTS =
      "^implements$|^implements\\..*|.*\\.implements\\..*|.*\\.implements$";
  private static final String INTERFACE =
      "^interface$|^interface\\..*|.*\\.interface\\..*|.*\\.interface$";
  private static final String LONG = "^long$|^long\\..*|.*\\.long\\..*|.*\\.long$";
  private static final String NATIVE = "^native$|^native\\..*|.*\\.native\\..*|.*\\.native$";
  private static final String RESERVED_WORDS =
      "(?!"
          + ABSTRACT
          + "|"
          + ASSERT
          + "|"
          + BOOLEAN
          + "|"
          + BREAK
          + "|"
          + BYTE
          + "|"
          + CASE
          + "|"
          + CATCH
          + "|"
          + CHAR
          + "|"
          + CLASS
          + "|"
          + CONST
          + "|"
          + CONTINUE
          + "|"
          + DEFAULT
          + "|"
          + DO
          + "|"
          + DOUBLE
          + "|"
          + ELSE
          + "|"
          + ENUM
          + "|"
          + EXTENDS
          + "|"
          + FINAL
          + "|"
          + FINALLY
          + "|"
          + FLOAT
          + "|"
          + FOR
          + "|"
          + GOTO
          + "|"
          + IF
          + "|"
          + IMPLEMENTS
          + "|"
          + IMPORT
          + "|"
          + INSTANCE_OF
          + "|"
          + INT
          + "|"
          + INTERFACE
          + "|"
          + LONG
          + "|"
          + NATIVE
          + "|"
          + NEW
          + "|"
          + PACKAGE
          + "|"
          + PRIVATE
          + "|"
          + PROTECTED
          + "|"
          + PUBLIC
          + "|"
          + RETURN
          + "|"
          + SHORT
          + "|"
          + STATIC
          + "|"
          + STRICTFP
          + "|"
          + SUPER
          + "|"
          + SWITCH
          + "|"
          + SYNCHRONIZED
          + "|"
          + THIS
          + "|"
          + THROW
          + "|"
          + THROWS
          + "|"
          + TRANSIENT
          + "|"
          + TRY
          + "|"
          + VOID
          + "|"
          + VOLATILE
          + "|"
          + WHILE
          + ")";
  private static final String IDENTIFIER_FIRST_SYMBOL = "[a-zA-Z_]+";
  private static final String IDENTIFIER_NOT_FIRST_SYMBOL = "(?:\\d*[a-zA-Z_]*)";
  private static final String JAVA_IDENTIFIER =
      "(^"
          + "(?:"
          + IDENTIFIER_FIRST_SYMBOL
          + IDENTIFIER_NOT_FIRST_SYMBOL
          + "*)"
          + "(?:"
          + "\\."
          + IDENTIFIER_FIRST_SYMBOL
          + IDENTIFIER_NOT_FIRST_SYMBOL
          + "*)"
          + "*$)";
  public static final RegExp JAVA_PACKAGE_FQN_PATTERN =
      RegExp.compile(RESERVED_WORDS + JAVA_IDENTIFIER);
  public static final RegExp JAVA_CLASS_NAME_PATTERN =
      RegExp.compile(
          RESERVED_WORDS
              + "(^"
              + "(?:"
              + IDENTIFIER_FIRST_SYMBOL
              + IDENTIFIER_NOT_FIRST_SYMBOL
              + "*)*$)");

  private JavaUtils() {}

  /**
   * Checks if the given name is valid compilation unit name. Throws {@link IllegalStateException}
   * if the specified name isn't a valid Java compilation unit name.
   *
   * <p>A compilation unit name must obey the following rules:
   *
   * <ul>
   *   <li>it must not be null
   *   <li>it must be suffixed by a dot ('.') followed by one of the java like extension
   *   <li>its prefix must be a valid identifier
   * </ul>
   *
   * @param name name to check
   * @throws IllegalStateException with a detail message that describes what is wrong with the
   *     specified name
   */
  public static void checkCompilationUnitName(String name) throws IllegalStateException {
    IStatus status = validateCompilationUnitName(name);
    if (status.getSeverity() == ERROR) {
      throw new IllegalStateException(status.getMessage());
    }
  }

  /**
   * Checks if the specified text is a valid compilation unit name.
   *
   * @param name the text to check
   * @return <code>true</code> if the specified text is a valid compilation unit name, <code>false
   *     </code> otherwise
   */
  public static boolean isValidCompilationUnitName(String name) {
    IStatus status = validateCompilationUnitName(name);
    switch (status.getSeverity()) {
      case Status.WARNING:
      case Status.OK:
        return true;
      default:
        return false;
    }
  }

  /**
   * Checks if the given package name is a valid package name. Throws {@link IllegalStateException}
   * if the specified name isn't a valid Java package name.
   *
   * <p>The syntax of a package name corresponds to PackageName as defined by PackageDeclaration
   * (JLS2 7.4). For example, <code>"java.lang"</code>.
   *
   * <p>
   *
   * @param name name of the package
   * @throws IllegalStateException with a detail message that describes what is wrong with the
   *     specified name
   */
  public static void checkPackageName(String name) throws IllegalStateException {
    IStatus status = validatePackageName(name);
    if (status.getSeverity() == ERROR) {
      throw new IllegalStateException(status.getMessage());
    }
  }

  /**
   * Checks if the specified text is a valid package name.
   *
   * @param name the text to check
   * @return <code>true</code> if the specified text is a valid package name, <code>false</code>
   *     otherwise
   */
  public static boolean isValidPackageName(String name) {
    IStatus status = validatePackageName(name);
    switch (status.getSeverity()) {
      case Status.WARNING:
      case Status.OK:
        return true;
      default:
        return false;
    }
  }

  private static IStatus validateCompilationUnitName(String name) {
    return JAVA_CLASS_NAME_PATTERN.test(name)
        ? OK_STATUS
        : new Status(ERROR, "unknown", null, null);
  }

  private static IStatus validatePackageName(String name) {
    return JAVA_PACKAGE_FQN_PATTERN.test(name)
        ? OK_STATUS
        : new Status(ERROR, "unknown", null, null);
  }
}
