/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.util;

import java.util.Map;
import org.eclipse.che.jdt.core.ToolFactory;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;
import org.eclipse.text.edits.TextEdit;

public class CodeFormatterUtil {

  /**
   * Creates a string that represents the given number of indentation units. The returned string can
   * contain tabs and/or spaces depending on the core formatter preferences.
   *
   * @param indentationUnits the number of indentation units to generate
   * @param project the project from which to get the formatter settings, <code>null</code> if the
   *     workspace default should be used
   * @return the indent string
   */
  public static String createIndentString(int indentationUnits, IJavaProject project) {
    Map<String, String> options =
        project != null ? project.getOptions(true) : JavaCore.getOptions();
    return ToolFactory.createCodeFormatter(options).createIndentationString(indentationUnits);
  }

  /**
   * Gets the current tab width.
   *
   * @param project The project where the source is used, used for project specific options or
   *     <code>null</code> if the project is unknown and the workspace default should be used
   * @return The tab width
   */
  public static int getTabWidth(IJavaProject project) {
    /*
     * If the tab-char is SPACE, FORMATTER_INDENTATION_SIZE is not used
     * by the core formatter.
     * We piggy back the visual tab length setting in that preference in
     * that case.
     */
    String key;
    if (JavaCore.SPACE.equals(
        getCoreOption(project, DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR)))
      key = DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE;
    else key = DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE;

    return getCoreOption(project, key, 4);
  }

  /**
   * Returns the current indent width.
   *
   * @param project the project where the source is used or, <code>null</code> if the project is
   *     unknown and the workspace default should be used
   * @return the indent width
   * @since 3.1
   */
  public static int getIndentWidth(IJavaProject project) {
    String key;
    if (DefaultCodeFormatterConstants.MIXED.equals(
        getCoreOption(project, DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR)))
      key = DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE;
    else key = DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE;

    return getCoreOption(project, key, 4);
  }

  /**
   * Returns the possibly <code>project</code>-specific core preference defined under <code>key
   * </code>.
   *
   * @param project the project to get the preference from, or <code>null</code> to get the global
   *     preference
   * @param key the key of the preference
   * @return the value of the preference
   * @since 3.1
   */
  private static String getCoreOption(IJavaProject project, String key) {
    if (project == null) return JavaCore.getOption(key);
    return project.getOption(key, true);
  }

  /**
   * Returns the possibly <code>project</code>-specific core preference defined under <code>key
   * </code>, or <code>def</code> if the value is not a integer.
   *
   * @param project the project to get the preference from, or <code>null</code> to get the global
   *     preference
   * @param key the key of the preference
   * @param def the default value
   * @return the value of the preference
   * @since 3.1
   */
  private static int getCoreOption(IJavaProject project, String key, int def) {
    try {
      return Integer.parseInt(getCoreOption(project, key));
    } catch (NumberFormatException e) {
      return def;
    }
  }

  // transition code

  /**
   * Old API. Consider to use format2 (TextEdit)
   *
   * @param kind Use to specify the kind of the code snippet to format. It can be any of the kind
   *     constants defined in {@link org.eclipse.jdt.core.formatter.CodeFormatter}
   * @param source The source to format
   * @param indentationLevel The initial indentation level, used to shift left/right the entire
   *     source fragment. An initial indentation level of zero or below has no effect.
   * @param lineSeparator The line separator to use in formatted source, if set to <code>null</code>
   *     , then the platform default one will be used.
   * @param project The project from which to retrieve the formatter options from If set to <code>
   *     null</code>, then use the current settings from {@link
   *     org.eclipse.jdt.core.JavaCore#getOptions()}.
   * @return the formatted source string
   */
  public static String format(
      int kind, String source, int indentationLevel, String lineSeparator, IJavaProject project) {
    Map<String, String> options = project != null ? project.getOptions(true) : null;
    return format(kind, source, indentationLevel, lineSeparator, options);
  }

  /**
   * Old API. Consider to use format2 (TextEdit)
   *
   * @param kind Use to specify the kind of the code snippet to format. It can be any of the kind
   *     constants defined in {@link org.eclipse.jdt.core.formatter.CodeFormatter}
   * @param source The source to format
   * @param indentationLevel The initial indentation level, used to shift left/right the entire
   *     source fragment. An initial indentation level of zero or below has no effect.
   * @param lineSeparator The line separator to use in formatted source, if set to <code>null</code>
   *     , then the platform default one will be used.
   * @param options The options map to use for formatting with the default code formatter.
   *     Recognized options are documented on {@link
   *     org.eclipse.jdt.core.JavaCore#getDefaultOptions()}. If set to <code>null</code>, then use
   *     the current settings from {@link org.eclipse.jdt.core.JavaCore#getOptions()}.
   * @return the formatted source string
   */
  public static String format(
      int kind,
      String source,
      int indentationLevel,
      String lineSeparator,
      Map<String, String> options) {
    TextEdit edit = format2(kind, source, indentationLevel, lineSeparator, options);
    if (edit == null) {
      return source;
    } else {
      Document document = new Document(source);
      try {
        edit.apply(document, TextEdit.NONE);
      } catch (BadLocationException e) {
        JavaPlugin.log(e); // bug in the formatter
        Assert.isTrue(
            false,
            "Formatter created edits with wrong positions: " + e.getMessage()); // $NON-NLS-1$
      }
      return document.get();
    }
  }

  /**
   * Creates edits that describe how to format the given string. Returns <code>null</code> if the
   * code could not be formatted for the given kind.
   *
   * @param kind Use to specify the kind of the code snippet to format. It can be any of the kind
   *     constants defined in {@link org.eclipse.jdt.core.formatter.CodeFormatter}
   * @param source The source to format
   * @param offset The given offset to start recording the edits (inclusive).
   * @param length the given length to stop recording the edits (exclusive).
   * @param indentationLevel The initial indentation level, used to shift left/right the entire
   *     source fragment. An initial indentation level of zero or below has no effect.
   * @param lineSeparator The line separator to use in formatted source, if set to <code>null</code>
   *     , then the platform default one will be used.
   * @param options The options map to use for formatting with the default code formatter.
   *     Recognized options are documented on {@link
   *     org.eclipse.jdt.core.JavaCore#getDefaultOptions()}. If set to <code>null</code>, then use
   *     the current settings from {@link org.eclipse.jdt.core.JavaCore#getOptions()}.
   * @return an TextEdit describing the changes required to format source
   * @throws IllegalArgumentException If the offset and length are not inside the string, a
   *     IllegalArgumentException is thrown.
   */
  public static TextEdit format2(
      int kind,
      String source,
      int offset,
      int length,
      int indentationLevel,
      String lineSeparator,
      Map<String, String> options) {
    if (offset < 0 || length < 0 || offset + length > source.length()) {
      throw new IllegalArgumentException(
          "offset or length outside of string. offset: "
              + offset
              + ", length: "
              + length
              + ", string size: "
              + source.length()); // $NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
    }
    return ToolFactory.createCodeFormatter(options)
        .format(kind, source, offset, length, indentationLevel, lineSeparator);
  }

  /**
   * Creates edits that describe how to format the given string. Returns <code>null</code> if the
   * code could not be formatted for the given kind.
   *
   * @param kind Use to specify the kind of the code snippet to format. It can be any of the kind
   *     constants defined in {@link org.eclipse.jdt.core.formatter.CodeFormatter}
   * @param source The source to format
   * @param indentationLevel The initial indentation level, used to shift left/right the entire
   *     source fragment. An initial indentation level of zero or below has no effect.
   * @param lineSeparator The line separator to use in formatted source, if set to <code>null</code>
   *     , then the platform default one will be used.
   * @param options The options map to use for formatting with the default code formatter.
   *     Recognized options are documented on {@link
   *     org.eclipse.jdt.core.JavaCore#getDefaultOptions()}. If set to <code>null</code>, then use
   *     the current settings from {@link org.eclipse.jdt.core.JavaCore#getOptions()}.
   * @return an TextEdit describing the changes required to format source
   * @throws IllegalArgumentException If the offset and length are not inside the string, a
   *     IllegalArgumentException is thrown.
   */
  public static TextEdit format2(
      int kind,
      String source,
      int indentationLevel,
      String lineSeparator,
      Map<String, String> options) {
    return format2(kind, source, 0, source.length(), indentationLevel, lineSeparator, options);
  }

  /**
   * Creates edits that describe how to re-format the given string. This method should be used for
   * formatting existing code. Returns <code>null</code> if the code could not be formatted for the
   * given kind.
   *
   * @param kind Use to specify the kind of the code snippet to format. It can be any of the kind
   *     constants defined in {@link org.eclipse.jdt.core.formatter.CodeFormatter}
   * @param source The source to format
   * @param offset The given offset to start recording the edits (inclusive).
   * @param length the given length to stop recording the edits (exclusive).
   * @param indentationLevel The initial indentation level, used to shift left/right the entire
   *     source fragment. An initial indentation level of zero or below has no effect.
   * @param lineSeparator The line separator to use in formatted source, if set to <code>null</code>
   *     , then the platform default one will be used.
   * @param options The options map to use for formatting with the default code formatter.
   *     Recognized options are documented on {@link
   *     org.eclipse.jdt.core.JavaCore#getDefaultOptions()}. If set to <code>null</code>, then use
   *     the current settings from {@link org.eclipse.jdt.core.JavaCore#getOptions()}.
   * @return an TextEdit describing the changes required to format source
   * @throws IllegalArgumentException If the offset and length are not inside the string, a
   *     IllegalArgumentException is thrown.
   */
  public static TextEdit reformat(
      int kind,
      String source,
      int offset,
      int length,
      int indentationLevel,
      String lineSeparator,
      Map<String, String> options) {
    if (offset < 0 || length < 0 || offset + length > source.length()) {
      throw new IllegalArgumentException(
          "offset or length outside of string. offset: "
              + offset
              + ", length: "
              + length
              + ", string size: "
              + source.length()); // $NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
    }
    return ToolFactory.createCodeFormatter(options, ToolFactory.M_FORMAT_EXISTING)
        .format(kind, source, offset, length, indentationLevel, lineSeparator);
  }

  /**
   * Creates edits that describe how to re-format the regions in the given string. This method
   * should be used for formatting existing code. Returns <code>null</code> if the code could not be
   * formatted for the given kind.
   *
   * <p>No region in <code>regions</code> must overlap with any other region in <code>regions</code>
   * . Each region must be within source. There must be at least one region. Regions must be sorted
   * by their offsets, smaller offset first.
   *
   * @param kind Use to specify the kind of the code snippet to format. It can be any of
   *     K_EXPRESSION, K_STATEMENTS, K_CLASS_BODY_DECLARATIONS, K_COMPILATION_UNIT, K_UNKNOWN
   * @param source The source to format
   * @param regions a set of regions in the string to format
   * @param indentationLevel The initial indentation level, used to shift left/right the entire
   *     source fragment. An initial indentation level of zero or below has no effect.
   * @param lineSeparator The line separator to use in formatted source, if set to <code>null</code>
   *     , then the platform default one will be used.
   * @param options The options map to use for formatting with the default code formatter.
   *     Recognized options are documented on {@link
   *     org.eclipse.jdt.core.JavaCore#getDefaultOptions()}. If set to <code>null</code>, then use
   *     the current settings from {@link org.eclipse.jdt.core.JavaCore#getOptions()}.
   * @return an TextEdit describing the changes required to format source
   * @throws IllegalArgumentException if there is no region, a region overlaps with another region,
   *     or the regions are not sorted in the ascending order.
   * @since 3.4
   */
  public static TextEdit reformat(
      int kind,
      String source,
      IRegion[] regions,
      int indentationLevel,
      String lineSeparator,
      Map<String, String> options) {
    return ToolFactory.createCodeFormatter(options, ToolFactory.M_FORMAT_EXISTING)
        .format(kind, source, regions, indentationLevel, lineSeparator);
  }

  /**
   * Creates edits that describe how to re-format the given string. This method should be used for
   * formatting existing code. Returns <code>null</code> if the code could not be formatted for the
   * given kind.
   *
   * @param kind Use to specify the kind of the code snippet to format. It can be any of the kind
   *     constants defined in {@link org.eclipse.jdt.core.formatter.CodeFormatter}
   * @param source The source to format
   * @param indentationLevel The initial indentation level, used to shift left/right the entire
   *     source fragment. An initial indentation level of zero or below has no effect.
   * @param lineSeparator The line separator to use in formatted source, if set to <code>null</code>
   *     , then the platform default one will be used.
   * @param options The options map to use for formatting with the default code formatter.
   *     Recognized options are documented on {@link
   *     org.eclipse.jdt.core.JavaCore#getDefaultOptions()}. If set to <code>null</code>, then use
   *     the current settings from {@link org.eclipse.jdt.core.JavaCore#getOptions()}.
   * @return an TextEdit describing the changes required to format source
   * @throws IllegalArgumentException If the offset and length are not inside the string, a
   *     IllegalArgumentException is thrown.
   */
  public static TextEdit reformat(
      int kind,
      String source,
      int indentationLevel,
      String lineSeparator,
      Map<String, String> options) {
    return reformat(kind, source, 0, source.length(), indentationLevel, lineSeparator, options);
  }

  /**
   * Creates edits that describe how to format the given string. The given node is used to infer the
   * kind to use to format the string. Consider to use {@link #format2(int, String, int, String,
   * java.util.Map)} if the kind is already known. Returns <code>null</code> if the code could not
   * be formatted for the given kind.
   *
   * @param node Use to infer the kind of the code snippet to format.
   * @param source The source to format
   * @param indentationLevel The initial indentation level, used to shift left/right the entire
   *     source fragment. An initial indentation level of zero or below has no effect.
   * @param lineSeparator The line separator to use in formatted source, if set to <code>null</code>
   *     , then the platform default one will be used.
   * @param options The options map to use for formatting with the default code formatter.
   *     Recognized options are documented on {@link
   *     org.eclipse.jdt.core.JavaCore#getDefaultOptions()}. If set to <code>null</code>, then use
   *     the current settings from {@link org.eclipse.jdt.core.JavaCore#getOptions()}.
   * @return an TextEdit describing the changes required to format source
   * @throws IllegalArgumentException If the offset and length are not inside the string, a
   *     IllegalArgumentException is thrown.
   */
  public static TextEdit format2(
      ASTNode node,
      String source,
      int indentationLevel,
      String lineSeparator,
      Map<String, String> options) {
    int code;
    String prefix = ""; // $NON-NLS-1$
    String suffix = ""; // $NON-NLS-1$
    if (node instanceof Statement) {
      code = CodeFormatter.K_STATEMENTS;
      if (node.getNodeType() == ASTNode.SWITCH_CASE) {
        prefix = "switch(1) {"; // $NON-NLS-1$
        suffix = "}"; // $NON-NLS-1$
        code = CodeFormatter.K_STATEMENTS;
      }
    } else if (node instanceof Expression
        && node.getNodeType() != ASTNode.VARIABLE_DECLARATION_EXPRESSION) {
      code = CodeFormatter.K_EXPRESSION;
    } else if (node instanceof BodyDeclaration) {
      code = CodeFormatter.K_CLASS_BODY_DECLARATIONS;
    } else {
      switch (node.getNodeType()) {
        case ASTNode.ARRAY_TYPE:
        case ASTNode.PARAMETERIZED_TYPE:
        case ASTNode.PRIMITIVE_TYPE:
        case ASTNode.QUALIFIED_TYPE:
        case ASTNode.SIMPLE_TYPE:
          suffix = " x;"; // $NON-NLS-1$
          code = CodeFormatter.K_CLASS_BODY_DECLARATIONS;
          break;
        case ASTNode.WILDCARD_TYPE:
          prefix = "A<"; // $NON-NLS-1$
          suffix = "> x;"; // $NON-NLS-1$
          code = CodeFormatter.K_CLASS_BODY_DECLARATIONS;
          break;
        case ASTNode.COMPILATION_UNIT:
          code = CodeFormatter.K_COMPILATION_UNIT;
          break;
        case ASTNode.VARIABLE_DECLARATION_EXPRESSION:
        case ASTNode.SINGLE_VARIABLE_DECLARATION:
          suffix = ";"; // $NON-NLS-1$
          code = CodeFormatter.K_STATEMENTS;
          break;
        case ASTNode.VARIABLE_DECLARATION_FRAGMENT:
          prefix = "A "; // $NON-NLS-1$
          suffix = ";"; // $NON-NLS-1$
          code = CodeFormatter.K_STATEMENTS;
          break;
        case ASTNode.PACKAGE_DECLARATION:
        case ASTNode.IMPORT_DECLARATION:
          suffix = "\nclass A {}"; // $NON-NLS-1$
          code = CodeFormatter.K_COMPILATION_UNIT;
          break;
        case ASTNode.JAVADOC:
          suffix = "void foo();"; // $NON-NLS-1$
          code = CodeFormatter.K_CLASS_BODY_DECLARATIONS;
          break;
        case ASTNode.CATCH_CLAUSE:
          prefix = "try {}"; // $NON-NLS-1$
          code = CodeFormatter.K_STATEMENTS;
          break;
        case ASTNode.ANONYMOUS_CLASS_DECLARATION:
          prefix = "new A()"; // $NON-NLS-1$
          suffix = ";"; // $NON-NLS-1$
          code = CodeFormatter.K_STATEMENTS;
          break;
        case ASTNode.MEMBER_VALUE_PAIR:
          prefix = "@Author("; // $NON-NLS-1$
          suffix = ") class x {}"; // $NON-NLS-1$
          code = CodeFormatter.K_COMPILATION_UNIT;
          break;
        case ASTNode.MODIFIER:
          suffix = " class x {}"; // $NON-NLS-1$
          code = CodeFormatter.K_COMPILATION_UNIT;
          break;
        case ASTNode.TYPE_PARAMETER:
          prefix = "class X<"; // $NON-NLS-1$
          suffix = "> {}"; // $NON-NLS-1$
          code = CodeFormatter.K_COMPILATION_UNIT;
          break;
        case ASTNode.MEMBER_REF:
        case ASTNode.METHOD_REF:
        case ASTNode.METHOD_REF_PARAMETER:
        case ASTNode.TAG_ELEMENT:
        case ASTNode.TEXT_ELEMENT:
          // Javadoc formatting not yet supported:
          return null;
        default:
          // Assert.isTrue(false, "Node type not covered: " + node.getClass().getName());
          // //$NON-NLS-1$
          return null;
      }
    }

    String concatStr = prefix + source + suffix;
    TextEdit edit =
        format2(
            code,
            concatStr,
            prefix.length(),
            source.length(),
            indentationLevel,
            lineSeparator,
            options);
    if (edit != null && prefix.length() > 0) {
      edit.moveTree(-prefix.length());
    }
    return edit;
  }
}
