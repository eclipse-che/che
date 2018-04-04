/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation Alex Blewitt -
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=168954
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.fix;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.ui.fix.UnimplementedCodeCleanUp;
import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.jface.preference.IPreferenceStore;

public class CleanUpConstants {

  /** Constant for default options kind for clean up. */
  public static final int DEFAULT_CLEAN_UP_OPTIONS = 1;

  /** Constant for default options kind for save actions. */
  public static final int DEFAULT_SAVE_ACTION_OPTIONS = 2;

  /**
   * Format Java Source Code <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String FORMAT_SOURCE_CODE = "cleanup.format_source_code"; // $NON-NLS-1$

  /**
   * If true then only changed regions are formatted on save. Only has an effect if {@link
   * #FORMAT_SOURCE_CODE} is TRUE <br>
   * <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.4
   */
  public static final String FORMAT_SOURCE_CODE_CHANGES_ONLY =
      "cleanup.format_source_code_changes_only"; // $NON-NLS-1$

  /**
   * Format comments. Specify which comment with:<br>
   * {@link #FORMAT_JAVADOC}<br>
   * {@link #FORMAT_MULTI_LINE_COMMENT}<br>
   * {@link #FORMAT_SINGLE_LINE_COMMENT} <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   * @deprecated replaced by {@link #FORMAT_SOURCE_CODE}
   */
  public static final String FORMAT_COMMENT = "cleanup.format_comment"; // $NON-NLS-1$

  /**
   * Format single line comments. Only has an effect if {@link #FORMAT_COMMENT} is TRUE <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   * @deprecated replaced by {@link
   *     DefaultCodeFormatterConstants#FORMATTER_COMMENT_FORMAT_LINE_COMMENT}
   */
  public static final String FORMAT_SINGLE_LINE_COMMENT =
      "cleanup.format_single_line_comment"; // $NON-NLS-1$

  /**
   * Format multi line comments. Only has an effect if {@link #FORMAT_COMMENT} is TRUE <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   * @deprecated replaced by {@link
   *     DefaultCodeFormatterConstants#FORMATTER_COMMENT_FORMAT_BLOCK_COMMENT}
   */
  public static final String FORMAT_MULTI_LINE_COMMENT =
      "cleanup.format_multi_line_comment"; // $NON-NLS-1$

  /**
   * Format javadoc comments. Only has an effect if {@link #FORMAT_COMMENT} is TRUE <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   * @deprecated replaced by {@link
   *     DefaultCodeFormatterConstants#FORMATTER_COMMENT_FORMAT_JAVADOC_COMMENT}
   */
  public static final String FORMAT_JAVADOC = "cleanup.format_javadoc"; // $NON-NLS-1$

  /**
   * Removes trailing whitespace in compilation units<br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String FORMAT_REMOVE_TRAILING_WHITESPACES =
      "cleanup.remove_trailing_whitespaces"; // $NON-NLS-1$

  /**
   * Removes trailing whitespace in compilation units on all lines<br>
   * Only has an effect if {@link #FORMAT_REMOVE_TRAILING_WHITESPACES} is TRUE <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String FORMAT_REMOVE_TRAILING_WHITESPACES_ALL =
      "cleanup.remove_trailing_whitespaces_all"; // $NON-NLS-1$

  /**
   * Removes trailing whitespace in compilation units on all lines which contain an other characters
   * then whitespace<br>
   * Only has an effect if {@link #FORMAT_REMOVE_TRAILING_WHITESPACES} is TRUE <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String FORMAT_REMOVE_TRAILING_WHITESPACES_IGNORE_EMPTY =
      "cleanup.remove_trailing_whitespaces_ignore_empty";
  // $NON-NLS-1$

  /**
   * Correct indentation in compilation units on all lines <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.4
   */
  public static final String FORMAT_CORRECT_INDENTATION =
      "cleanup.correct_indentation"; // $NON-NLS-1$

  /**
   * Controls access qualifiers for instance fields. For detailed settings use<br>
   * {@link #MEMBER_ACCESSES_NON_STATIC_FIELD_USE_THIS_ALWAYS}<br>
   * {@link #MEMBER_ACCESSES_NON_STATIC_FIELD_USE_THIS_IF_NECESSARY} <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String MEMBER_ACCESSES_NON_STATIC_FIELD_USE_THIS =
      "cleanup.use_this_for_non_static_field_access"; // $NON-NLS-1$

  /**
   * Adds a 'this' qualifier to field accesses.
   *
   * <p>Example:
   *
   * <pre>
   *                     int fField;
   *                     void foo() {fField= 10;} -&gt; void foo() {this.fField= 10;}
   * </pre>
   *
   * Only has an effect if {@link #MEMBER_ACCESSES_NON_STATIC_FIELD_USE_THIS} is TRUE <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String MEMBER_ACCESSES_NON_STATIC_FIELD_USE_THIS_ALWAYS =
      "cleanup.always_use_this_for_non_static_field_access";
  // $NON-NLS-1$

  /**
   * Removes 'this' qualifier to field accesses.
   *
   * <p>Example:
   *
   * <pre>
   *                     int fField;
   *                     void foo() {this.fField= 10;} -&gt; void foo() {fField= 10;}
   * </pre>
   *
   * Only has an effect if {@link #MEMBER_ACCESSES_NON_STATIC_FIELD_USE_THIS} is TRUE <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String MEMBER_ACCESSES_NON_STATIC_FIELD_USE_THIS_IF_NECESSARY =
      "cleanup.use_this_for_non_static_field_access_only_if_necessary"; // $NON-NLS-1$

  /**
   * Controls access qualifiers for instance methods. For detailed settings use<br>
   * {@link #MEMBER_ACCESSES_NON_STATIC_METHOD_USE_THIS_ALWAYS}<br>
   * {@link #MEMBER_ACCESSES_NON_STATIC_METHOD_USE_THIS_IF_NECESSARY} <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String MEMBER_ACCESSES_NON_STATIC_METHOD_USE_THIS =
      "cleanup.use_this_for_non_static_method_access"; // $NON-NLS-1$

  /**
   * Adds a 'this' qualifier to method accesses.
   *
   * <p>Example:
   *
   * <pre>
   *                     int method(){};
   *                     void foo() {method()} -&gt; void foo() {this.method();}
   * </pre>
   *
   * Only has an effect if {@link #MEMBER_ACCESSES_NON_STATIC_METHOD_USE_THIS} is TRUE <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String MEMBER_ACCESSES_NON_STATIC_METHOD_USE_THIS_ALWAYS =
      "cleanup.always_use_this_for_non_static_method_access";
  // $NON-NLS-1$

  /**
   * Removes 'this' qualifier from field accesses.
   *
   * <p>Example:
   *
   * <pre>
   *                     int fField;
   *                     void foo() {this.fField= 10;} -&gt; void foo() {fField= 10;}
   * </pre>
   *
   * Only has an effect if {@link #MEMBER_ACCESSES_NON_STATIC_METHOD_USE_THIS} is TRUE <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String MEMBER_ACCESSES_NON_STATIC_METHOD_USE_THIS_IF_NECESSARY =
      "cleanup.use_this_for_non_static_method_access_only_if_necessary"; // $NON-NLS-1$

  /**
   * Controls access qualifiers for static members. For detailed settings use<br>
   * {@link #MEMBER_ACCESSES_STATIC_QUALIFY_WITH_DECLARING_CLASS_FIELD}<br>
   * {@link #MEMBER_ACCESSES_STATIC_QUALIFY_WITH_DECLARING_CLASS_INSTANCE_ACCESS}<br>
   * {@link #MEMBER_ACCESSES_STATIC_QUALIFY_WITH_DECLARING_CLASS_METHOD}<br>
   * {@link #MEMBER_ACCESSES_STATIC_QUALIFY_WITH_DECLARING_CLASS_SUBTYPE_ACCESS} <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String MEMBER_ACCESSES_STATIC_QUALIFY_WITH_DECLARING_CLASS =
      "cleanup.qualify_static_member_accesses_with_declaring_class"; // $NON-NLS-1$

  /**
   * Qualify static field accesses with declaring type.
   *
   * <p>Example:
   *
   * <pre>
   *                   class E {
   *                     public static int i;
   *                     void foo() {i= 10;} -&gt; void foo() {E.i= 10;}
   *                   }
   * </pre>
   *
   * <br>
   * Only has an effect if {@link #MEMBER_ACCESSES_STATIC_QUALIFY_WITH_DECLARING_CLASS} is TRUE <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String MEMBER_ACCESSES_STATIC_QUALIFY_WITH_DECLARING_CLASS_FIELD =
      "cleanup.qualify_static_field_accesses_with_declaring_class"; // $NON-NLS-1$

  /**
   * Qualifies static method accesses with declaring type.
   *
   * <p>Example:
   *
   * <pre>
   *                   class E {
   *                     public static int m();
   *                     void foo() {m();} -&gt; void foo() {E.m();}
   *                   }
   * </pre>
   *
   * Only has an effect if {@link #MEMBER_ACCESSES_STATIC_QUALIFY_WITH_DECLARING_CLASS} is TRUE <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String MEMBER_ACCESSES_STATIC_QUALIFY_WITH_DECLARING_CLASS_METHOD =
      "cleanup.qualify_static_method_accesses_with_declaring_class"; // $NON-NLS-1$

  /**
   * Changes indirect accesses to static members to direct ones.
   *
   * <p>Example:
   *
   * <pre>
   *                   class E {public static int i;}
   *                   class ESub extends E {
   *                     void foo() {ESub.i= 10;} -&gt; void foo() {E.i= 10;}
   *                   }
   * </pre>
   *
   * Only has an effect if {@link #MEMBER_ACCESSES_STATIC_QUALIFY_WITH_DECLARING_CLASS} is TRUE <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String MEMBER_ACCESSES_STATIC_QUALIFY_WITH_DECLARING_CLASS_SUBTYPE_ACCESS =
      "cleanup.qualify_static_member_accesses_through_subtypes_with_declaring_class"; // $NON-NLS-1$

  /**
   * Changes non static accesses to static members to static accesses.
   *
   * <p>Example:
   *
   * <pre>
   *                   class E {
   *                     public static int i;
   *                     void foo() {(new E()).i= 10;} -&gt; void foo() {E.i= 10;}
   *                   }
   * </pre>
   *
   * Only has an effect if {@link #MEMBER_ACCESSES_STATIC_QUALIFY_WITH_DECLARING_CLASS} is TRUE <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String MEMBER_ACCESSES_STATIC_QUALIFY_WITH_DECLARING_CLASS_INSTANCE_ACCESS =
      "cleanup.qualify_static_member_accesses_through_instances_with_declaring_class"; // $NON-NLS-1$

  /**
   * Controls the usage of blocks around single control statement bodies. For detailed settings use
   * <br>
   * {@link #CONTROL_STATMENTS_USE_BLOCKS_ALWAYS}<br>
   * {@link #CONTROL_STATMENTS_USE_BLOCKS_NEVER}<br>
   * {@link #CONTROL_STATMENTS_USE_BLOCKS_NO_FOR_RETURN_AND_THROW} <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String CONTROL_STATEMENTS_USE_BLOCKS = "cleanup.use_blocks"; // $NON-NLS-1$

  /**
   * Adds block to control statement body if the body is not a block.
   *
   * <p>Example:
   *
   * <pre>
   *                   	 if (b) foo(); -&gt; if (b) {foo();}
   * </pre>
   *
   * Only has an effect if {@link #CONTROL_STATEMENTS_USE_BLOCKS} is TRUE <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String CONTROL_STATMENTS_USE_BLOCKS_ALWAYS =
      "cleanup.always_use_blocks"; // $NON-NLS-1$

  /**
   * Remove unnecessary blocks in control statement bodies if they contain a single return or throw
   * statement.
   *
   * <p>Example:
   *
   * <pre>
   *                     if (b) {return;} -&gt; if (b) return;
   * </pre>
   *
   * Only has an effect if {@link #CONTROL_STATEMENTS_USE_BLOCKS} is TRUE <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String CONTROL_STATMENTS_USE_BLOCKS_NO_FOR_RETURN_AND_THROW =
      "cleanup.use_blocks_only_for_return_and_throw";
  // $NON-NLS-1$

  /**
   * Remove unnecessary blocks in control statement bodies.
   *
   * <p>Example:
   *
   * <pre>
   *                     if (b) {foo();} -&gt; if (b) foo();
   * </pre>
   *
   * Only has an effect if {@link #CONTROL_STATEMENTS_USE_BLOCKS} is TRUE <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String CONTROL_STATMENTS_USE_BLOCKS_NEVER =
      "cleanup.never_use_blocks"; // $NON-NLS-1$

  /**
   * Convert for loops to enhanced for loops.
   *
   * <p>Example:
   *
   * <pre>
   *                   for (int i = 0; i &lt; array.length; i++) {} -&gt; for (int element : array) {}
   * </pre>
   *
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String CONTROL_STATMENTS_CONVERT_FOR_LOOP_TO_ENHANCED =
      "cleanup.convert_to_enhanced_for_loop"; // $NON-NLS-1$

  /**
   * Controls the usage of parentheses in expressions. For detailed settings use<br>
   * {@link #EXPRESSIONS_USE_PARENTHESES_ALWAYS}<br>
   * {@link #EXPRESSIONS_USE_PARENTHESES_NEVER}<br>
   * <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String EXPRESSIONS_USE_PARENTHESES =
      "cleanup.use_parentheses_in_expressions"; // $NON-NLS-1$

  /**
   * Add paranoiac parentheses around conditional expressions.
   *
   * <p>Example:
   *
   * <pre>
   *                   boolean b= i &gt; 10 &amp;&amp; i &lt; 100 || i &gt; 20;
   *                   -&gt;
   *                   boolean b= ((i &gt; 10) &amp;&amp; (i &lt; 100)) || (i &gt; 20);
   * </pre>
   *
   * Only has an effect if {@link #EXPRESSIONS_USE_PARENTHESES} is TRUE <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String EXPRESSIONS_USE_PARENTHESES_ALWAYS =
      "cleanup.always_use_parentheses_in_expressions"; // $NON-NLS-1$

  /**
   * Remove unnecessary parenthesis around conditional expressions.
   *
   * <p>Example:
   *
   * <pre>
   *                   boolean b= ((i &gt; 10) &amp;&amp; (i &lt; 100)) || (i &gt; 20);
   *                   -&gt;
   *                   boolean b= i &gt; 10 &amp;&amp; i &lt; 100 || i &gt; 20;
   * </pre>
   *
   * Only has an effect if {@link #EXPRESSIONS_USE_PARENTHESES} is TRUE <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String EXPRESSIONS_USE_PARENTHESES_NEVER =
      "cleanup.never_use_parentheses_in_expressions"; // $NON-NLS-1$

  /**
   * Controls the usage of 'final' modifier for variable declarations. For detailed settings use:
   * <br>
   * {@link #VARIABLE_DECLARATIONS_USE_FINAL_LOCAL_VARIABLES}<br>
   * {@link #VARIABLE_DECLARATIONS_USE_FINAL_PARAMETERS}<br>
   * {@link #VARIABLE_DECLARATIONS_USE_FINAL_PRIVATE_FIELDS} <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String VARIABLE_DECLARATIONS_USE_FINAL =
      "cleanup.make_variable_declarations_final"; // $NON-NLS-1$

  /**
   * Add a final modifier to private fields where possible i.e.:
   *
   * <pre>
   *                   private int field= 0; -&gt; private final int field= 0;
   * </pre>
   *
   * Only has an effect if {@link #VARIABLE_DECLARATIONS_USE_FINAL} is TRUE <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String VARIABLE_DECLARATIONS_USE_FINAL_PRIVATE_FIELDS =
      "cleanup.make_private_fields_final"; // $NON-NLS-1$

  /**
   * Add a final modifier to method parameters where possible i.e.:
   *
   * <pre>
   *                   void foo(int i) {} -&gt; void foo(final int i) {}
   * </pre>
   *
   * Only has an effect if {@link #VARIABLE_DECLARATIONS_USE_FINAL} is TRUE <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String VARIABLE_DECLARATIONS_USE_FINAL_PARAMETERS =
      "cleanup.make_parameters_final"; // $NON-NLS-1$

  /**
   * Add a final modifier to local variables where possible i.e.:
   *
   * <pre>
   *                   int i= 0; -&gt; final int i= 0;
   * </pre>
   *
   * Only has an effect if {@link #VARIABLE_DECLARATIONS_USE_FINAL} is TRUE <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String VARIABLE_DECLARATIONS_USE_FINAL_LOCAL_VARIABLES =
      "cleanup.make_local_variable_final"; // $NON-NLS-1$

  /**
   * Controls conversion between lambda expressions and anonymous class creations. For detailed
   * settings, use {@link #USE_LAMBDA} or {@link #USE_ANONYMOUS_CLASS_CREATION}
   *
   * <p>Possible values: {TRUE, FALSE}
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String CONVERT_FUNCTIONAL_INTERFACES =
      "cleanup.convert_functional_interfaces"; // $NON-NLS-1$

  /**
   * Replaces anonymous class creations with lambda expressions where possible in Java 8 source.
   *
   * <p>Possible values: {TRUE, FALSE}
   *
   * <p>Only has an effect if {@link #CONVERT_FUNCTIONAL_INTERFACES} is TRUE.
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.10
   */
  public static final String USE_LAMBDA = "cleanup.use_lambda"; // $NON-NLS-1$

  /**
   * Replaces lambda expressions with anonymous class creations.
   *
   * <p>Possible values: {TRUE, FALSE}
   *
   * <p>Only has an effect if {@link #CONVERT_FUNCTIONAL_INTERFACES} is TRUE.
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.10
   */
  public static final String USE_ANONYMOUS_CLASS_CREATION =
      "cleanup.use_anonymous_class_creation"; // $NON-NLS-1$

  /**
   * Adds type parameters to raw type references.
   *
   * <p>Example:
   *
   * <pre>
   *                   List l; -&gt; List&lt;Object&gt; l;
   * </pre>
   *
   * Possible values: {TRUE, FALSE}
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String VARIABLE_DECLARATION_USE_TYPE_ARGUMENTS_FOR_RAW_TYPE_REFERENCES =
      "cleanup.use_arguments_for_raw_type_references"; // $NON-NLS-1$

  /**
   * Removes unused imports. <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String REMOVE_UNUSED_CODE_IMPORTS =
      "cleanup.remove_unused_imports"; // $NON-NLS-1$

  /**
   * Controls the removal of unused private members. For detailed settings use:<br>
   * {@link #REMOVE_UNUSED_CODE_PRIVATE_CONSTRUCTORS}<br>
   * {@link #REMOVE_UNUSED_CODE_PRIVATE_FELDS}<br>
   * {@link #REMOVE_UNUSED_CODE_PRIVATE_METHODS}<br>
   * {@link #REMOVE_UNUSED_CODE_PRIVATE_TYPES} <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String REMOVE_UNUSED_CODE_PRIVATE_MEMBERS =
      "cleanup.remove_unused_private_members"; // $NON-NLS-1$

  /**
   * Removes unused private types. <br>
   * Only has an effect if {@link #REMOVE_UNUSED_CODE_PRIVATE_MEMBERS} is TRUE <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String REMOVE_UNUSED_CODE_PRIVATE_TYPES =
      "cleanup.remove_unused_private_types"; // $NON-NLS-1$

  /**
   * Removes unused private constructors. <br>
   * Only has an effect if {@link #REMOVE_UNUSED_CODE_PRIVATE_MEMBERS} is TRUE <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String REMOVE_UNUSED_CODE_PRIVATE_CONSTRUCTORS =
      "cleanup.remove_private_constructors"; // $NON-NLS-1$

  /**
   * Removes unused private fields. <br>
   * Only has an effect if {@link #REMOVE_UNUSED_CODE_PRIVATE_MEMBERS} is TRUE <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String REMOVE_UNUSED_CODE_PRIVATE_FELDS =
      "cleanup.remove_unused_private_fields"; // $NON-NLS-1$

  /**
   * Removes unused private methods. <br>
   * Only has an effect if {@link #REMOVE_UNUSED_CODE_PRIVATE_MEMBERS} is TRUE <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String REMOVE_UNUSED_CODE_PRIVATE_METHODS =
      "cleanup.remove_unused_private_methods"; // $NON-NLS-1$

  /**
   * Removes unused local variables. <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String REMOVE_UNUSED_CODE_LOCAL_VARIABLES =
      "cleanup.remove_unused_local_variables"; // $NON-NLS-1$

  /**
   * Removes unused casts. <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String REMOVE_UNNECESSARY_CASTS =
      "cleanup.remove_unnecessary_casts"; // $NON-NLS-1$

  /**
   * Remove unnecessary '$NON-NLS$' tags.
   *
   * <p>Example:
   *
   * <pre>
   * String s; //$NON-NLS-1$ -&gt; String s;
   * </pre>
   *
   * <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String REMOVE_UNNECESSARY_NLS_TAGS =
      "cleanup.remove_unnecessary_nls_tags"; // $NON-NLS-1$

  /**
   * Controls the usage of type arguments. For detailed settings use<br>
   * {@link #INSERT_INFERRED_TYPE_ARGUMENTS}<br>
   * {@link #REMOVE_REDUNDANT_TYPE_ARGUMENTS}<br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.10
   */
  public static final String USE_TYPE_ARGUMENTS = "cleanup.use_type_arguments"; // $NON-NLS-1$

  /**
   * Insert inferred type arguments for diamonds.<br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.10
   */
  public static final String INSERT_INFERRED_TYPE_ARGUMENTS =
      "cleanup.insert_inferred_type_arguments"; // $NON-NLS-1$

  /**
   * Removes redundant type arguments from class instance creations and creates a diamond.<br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.10
   */
  public static final String REMOVE_REDUNDANT_TYPE_ARGUMENTS =
      "cleanup.remove_redundant_type_arguments"; // $NON-NLS-1$

  /**
   * Controls whether missing annotations should be added to the code. For detailed settings use:
   * <br>
   * {@link #ADD_MISSING_ANNOTATIONS_DEPRECATED}<br>
   * {@value #ADD_MISSING_ANNOTATIONS_OVERRIDE} <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String ADD_MISSING_ANNOTATIONS =
      "cleanup.add_missing_annotations"; // $NON-NLS-1$

  /**
   * Add '@Override' annotation in front of overriding methods.
   *
   * <p>Example:
   *
   * <pre>
   *                   class E1 {void foo();}
   *                   class E2 extends E1 {
   *                   	 void foo(); -&gt;  @Override void foo();
   *                   }
   * </pre>
   *
   * Only has an effect if {@link #ADD_MISSING_ANNOTATIONS} is TRUE <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String ADD_MISSING_ANNOTATIONS_OVERRIDE =
      "cleanup.add_missing_override_annotations"; // $NON-NLS-1$

  /**
   * Add '@Override' annotation in front of methods that override or implement a superinterface
   * method.
   *
   * <p>Example:
   *
   * <pre>
   *                   interface I {void foo();}
   *                   class E implements I {
   *                   	 void foo(); -&gt;  @Override void foo();
   *                   }
   * </pre>
   *
   * Only has an effect if {@link #ADD_MISSING_ANNOTATIONS} and {@link
   * #ADD_MISSING_ANNOTATIONS_OVERRIDE} are TRUE and the compiler compliance is 1.6 or higher.<br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.6
   */
  public static final String ADD_MISSING_ANNOTATIONS_OVERRIDE_FOR_INTERFACE_METHOD_IMPLEMENTATION =
      "cleanup.add_missing_override_annotations_interface_methods"; // $NON-NLS-1$

  /**
   * Add '@Deprecated' annotation in front of deprecated members.
   *
   * <p>Example:
   *
   * <pre>
   *                         /**@deprecated* /
   *                        int i;
   *                    -&gt;
   *                         /**@deprecated* /
   *                         &#064;Deprecated
   *                        int i;
   * </pre>
   *
   * Only has an effect if {@link #ADD_MISSING_ANNOTATIONS} is TRUE <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String ADD_MISSING_ANNOTATIONS_DEPRECATED =
      "cleanup.add_missing_deprecated_annotations"; // $NON-NLS-1$

  /**
   * Controls whether missing serial version ids should be added to the code. For detailed settings
   * use:<br>
   * {@link #ADD_MISSING_SERIAL_VERSION_ID_DEFAULT}<br>
   * {@link #ADD_MISSING_SERIAL_VERSION_ID_GENERATED} <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String ADD_MISSING_SERIAL_VERSION_ID =
      "cleanup.add_serial_version_id"; // $NON-NLS-1$

  /**
   * Adds a generated serial version id to subtypes of java.io.Serializable and
   * java.io.Externalizable
   *
   * <p>public class E implements Serializable {} -> public class E implements Serializable {
   * private static final long serialVersionUID = 4381024239L; } <br>
   * Only has an effect if {@link #ADD_MISSING_SERIAL_VERSION_ID} is TRUE <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String ADD_MISSING_SERIAL_VERSION_ID_GENERATED =
      "cleanup.add_generated_serial_version_id"; // $NON-NLS-1$

  /**
   * Adds a default serial version it to subtypes of java.io.Serializable and java.io.Externalizable
   *
   * <p>public class E implements Serializable {} -> public class E implements Serializable {
   * private static final long serialVersionUID = 1L; } <br>
   * Only has an effect if {@link #ADD_MISSING_SERIAL_VERSION_ID} is TRUE <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String ADD_MISSING_SERIAL_VERSION_ID_DEFAULT =
      "cleanup.add_default_serial_version_id"; // $NON-NLS-1$

  /**
   * Add '$NON-NLS$' tags to non externalized strings.
   *
   * <p>Example:
   *
   * <pre>
   *                   	 String s= &quot;&quot;; -&gt; String s= &quot;&quot;; //$NON-NLS-1$
   * </pre>
   *
   * <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String ADD_MISSING_NLS_TAGS = "cleanup.add_missing_nls_tags"; // $NON-NLS-1$

  /**
   * If true the imports are organized while cleaning up code.
   *
   * <p>Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String ORGANIZE_IMPORTS = "cleanup.organize_imports"; // $NON-NLS-1$

  /**
   * Should members be sorted? <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see #SORT_MEMBERS_ALL
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String SORT_MEMBERS = "cleanup.sort_members"; // $NON-NLS-1$

  /**
   * If sorting members, should fields, enum constants and initializers also be sorted? <br>
   * This has only an effect if {@link #SORT_MEMBERS} is also enabled. <br>
   * <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see #SORT_MEMBERS
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.3
   */
  public static final String SORT_MEMBERS_ALL = "cleanup.sort_members_all"; // $NON-NLS-1$

  /**
   * If enabled method stubs are added to all non abstract classes which require to implement some
   * methods. <br>
   * Possible values: {TRUE, FALSE}<br>
   * <br>
   *
   * @see CleanUpOptions#TRUE
   * @see CleanUpOptions#FALSE
   * @since 3.4
   */
  public static final String ADD_MISSING_METHODES = "cleanup.add_missing_methods"; // $NON-NLS-1$

  /**
   * Should the Clean Up Wizard be shown when executing the Clean Up Action? <br>
   * <br>
   * Possible values: {<code><b>true</b></code>, <code><b>false</b></code>} <br>
   * Default value: <code><b>true</b></code><br>
   * <br>
   *
   * @since 3.3
   */
  public static final String SHOW_CLEAN_UP_WIZARD = "cleanup.showwizard"; // $NON-NLS-1$

  /**
   * A key to a serialized string in the <code>InstanceScope</code> containing all the profiles.<br>
   * Following code snippet can load the profiles:
   *
   * <pre>
   * List profiles= new ProfileStore(CLEANUP_PROFILES, new CleanUpVersioner()).readProfiles(InstanceScope.INSTANCE);
   * </pre>
   *
   * @since 3.3
   */
  public static final String CLEANUP_PROFILES = "org.eclipse.jdt.ui.cleanupprofiles"; // $NON-NLS-1$

  /**
   * Stores the id of the clean up profile used when executing clean up.<br>
   * <br>
   * Possible values: String value<br>
   * Default value: {@link #DEFAULT_PROFILE} <br>
   *
   * @since 3.3
   */
  public static final String CLEANUP_PROFILE = "cleanup_profile"; // $NON-NLS-1$$

  /**
   * Stores the id of the clean up profile used when executing clean up on save.<br>
   * <br>
   * Possible values: String value<br>
   * Default value: {@link #DEFAULT_SAVE_PARTICIPANT_PROFILE} <br>
   *
   * @since 3.3
   */
  public static final String CLEANUP_ON_SAVE_PROFILE = "cleanup.on_save_profile_id"; // $NON-NLS-1$

  /**
   * A key to the version of the profile stored in the preferences.<br>
   * <br>
   * Possible values: Integer value<br>
   * Default value: {@link CleanUpProfileVersioner#CURRENT_VERSION} <br>
   *
   * @since 3.3
   */
  public static final String CLEANUP_SETTINGS_VERSION_KEY =
      "cleanup_settings_version"; // $NON-NLS-1$

  /**
   * Id of the 'Eclipse [built-in]' profile.<br>
   * <br>
   *
   * @since 3.3
   */
  public static final String ECLIPSE_PROFILE =
      "org.eclipse.jdt.ui.default.eclipse_clean_up_profile"; // $NON-NLS-1$

  /**
   * Id of the 'Save Participant [built-in]' profile.<br>
   * <br>
   *
   * @since 3.3
   */
  public static final String SAVE_PARTICIPANT_PROFILE =
      "org.eclipse.jdt.ui.default.save_participant_clean_up_profile"; // $NON-NLS-1$

  public static final String CLEANUP_ON_SAVE_ADDITIONAL_OPTIONS =
      "cleanup.on_save_use_additional_actions"; // $NON-NLS-1$

  /**
   * The id of the profile used as a default profile when executing clean up.<br>
   * <br>
   * Possible values: String value<br>
   * Default value: {@link #ECLIPSE_PROFILE} <br>
   *
   * @since 3.3
   */
  public static final String DEFAULT_PROFILE = ECLIPSE_PROFILE;

  /**
   * The id of the profile used as a default profile when executing clean up on save.<br>
   * <br>
   * Possible values: String value<br>
   * Default value: {@link #SAVE_PARTICIPANT_PROFILE} <br>
   *
   * @since 3.3
   */
  public static final String DEFAULT_SAVE_PARTICIPANT_PROFILE = SAVE_PARTICIPANT_PROFILE;

  private static void setEclipseDefaultSettings(CleanUpOptions options) {

    // Member Accesses
    options.setOption(MEMBER_ACCESSES_NON_STATIC_FIELD_USE_THIS, CleanUpOptions.FALSE);
    options.setOption(MEMBER_ACCESSES_NON_STATIC_FIELD_USE_THIS_ALWAYS, CleanUpOptions.FALSE);
    options.setOption(MEMBER_ACCESSES_NON_STATIC_FIELD_USE_THIS_IF_NECESSARY, CleanUpOptions.TRUE);

    options.setOption(MEMBER_ACCESSES_NON_STATIC_METHOD_USE_THIS, CleanUpOptions.FALSE);
    options.setOption(MEMBER_ACCESSES_NON_STATIC_METHOD_USE_THIS_ALWAYS, CleanUpOptions.FALSE);
    options.setOption(MEMBER_ACCESSES_NON_STATIC_METHOD_USE_THIS_IF_NECESSARY, CleanUpOptions.TRUE);

    options.setOption(MEMBER_ACCESSES_STATIC_QUALIFY_WITH_DECLARING_CLASS, CleanUpOptions.TRUE);
    options.setOption(
        MEMBER_ACCESSES_STATIC_QUALIFY_WITH_DECLARING_CLASS_FIELD, CleanUpOptions.FALSE);
    options.setOption(
        MEMBER_ACCESSES_STATIC_QUALIFY_WITH_DECLARING_CLASS_METHOD, CleanUpOptions.FALSE);
    options.setOption(
        MEMBER_ACCESSES_STATIC_QUALIFY_WITH_DECLARING_CLASS_SUBTYPE_ACCESS, CleanUpOptions.TRUE);
    options.setOption(
        MEMBER_ACCESSES_STATIC_QUALIFY_WITH_DECLARING_CLASS_INSTANCE_ACCESS, CleanUpOptions.TRUE);

    // Control Statements
    options.setOption(CONTROL_STATEMENTS_USE_BLOCKS, CleanUpOptions.FALSE);
    options.setOption(CONTROL_STATMENTS_USE_BLOCKS_ALWAYS, CleanUpOptions.TRUE);
    options.setOption(CONTROL_STATMENTS_USE_BLOCKS_NO_FOR_RETURN_AND_THROW, CleanUpOptions.FALSE);
    options.setOption(CONTROL_STATMENTS_USE_BLOCKS_NEVER, CleanUpOptions.FALSE);

    options.setOption(CONTROL_STATMENTS_CONVERT_FOR_LOOP_TO_ENHANCED, CleanUpOptions.FALSE);

    // Expressions
    options.setOption(EXPRESSIONS_USE_PARENTHESES, CleanUpOptions.FALSE);
    options.setOption(EXPRESSIONS_USE_PARENTHESES_NEVER, CleanUpOptions.TRUE);
    options.setOption(EXPRESSIONS_USE_PARENTHESES_ALWAYS, CleanUpOptions.FALSE);

    // Variable Declarations
    options.setOption(VARIABLE_DECLARATIONS_USE_FINAL, CleanUpOptions.FALSE);
    options.setOption(VARIABLE_DECLARATIONS_USE_FINAL_LOCAL_VARIABLES, CleanUpOptions.TRUE);
    options.setOption(VARIABLE_DECLARATIONS_USE_FINAL_PARAMETERS, CleanUpOptions.FALSE);
    options.setOption(VARIABLE_DECLARATIONS_USE_FINAL_PRIVATE_FIELDS, CleanUpOptions.TRUE);

    // Functional Interfaces
    options.setOption(CONVERT_FUNCTIONAL_INTERFACES, CleanUpOptions.FALSE);
    options.setOption(USE_LAMBDA, CleanUpOptions.TRUE);
    options.setOption(USE_ANONYMOUS_CLASS_CREATION, CleanUpOptions.FALSE);

    // Unused Code
    options.setOption(REMOVE_UNUSED_CODE_IMPORTS, CleanUpOptions.TRUE);
    options.setOption(REMOVE_UNUSED_CODE_PRIVATE_MEMBERS, CleanUpOptions.FALSE);
    options.setOption(REMOVE_UNUSED_CODE_PRIVATE_CONSTRUCTORS, CleanUpOptions.TRUE);
    options.setOption(REMOVE_UNUSED_CODE_PRIVATE_FELDS, CleanUpOptions.TRUE);
    options.setOption(REMOVE_UNUSED_CODE_PRIVATE_METHODS, CleanUpOptions.TRUE);
    options.setOption(REMOVE_UNUSED_CODE_PRIVATE_TYPES, CleanUpOptions.TRUE);
    options.setOption(REMOVE_UNUSED_CODE_LOCAL_VARIABLES, CleanUpOptions.FALSE);

    // Unnecessary Code
    options.setOption(REMOVE_UNNECESSARY_CASTS, CleanUpOptions.TRUE);
    options.setOption(REMOVE_UNNECESSARY_NLS_TAGS, CleanUpOptions.TRUE);
    options.setOption(USE_TYPE_ARGUMENTS, CleanUpOptions.FALSE);
    options.setOption(INSERT_INFERRED_TYPE_ARGUMENTS, CleanUpOptions.FALSE);
    options.setOption(REMOVE_REDUNDANT_TYPE_ARGUMENTS, CleanUpOptions.TRUE);

    // Missing Code
    options.setOption(ADD_MISSING_ANNOTATIONS, CleanUpOptions.TRUE);
    options.setOption(ADD_MISSING_ANNOTATIONS_OVERRIDE, CleanUpOptions.TRUE);
    options.setOption(
        ADD_MISSING_ANNOTATIONS_OVERRIDE_FOR_INTERFACE_METHOD_IMPLEMENTATION, CleanUpOptions.TRUE);
    options.setOption(ADD_MISSING_ANNOTATIONS_DEPRECATED, CleanUpOptions.TRUE);

    options.setOption(ADD_MISSING_SERIAL_VERSION_ID, CleanUpOptions.FALSE);
    options.setOption(ADD_MISSING_SERIAL_VERSION_ID_GENERATED, CleanUpOptions.FALSE);
    options.setOption(ADD_MISSING_SERIAL_VERSION_ID_DEFAULT, CleanUpOptions.TRUE);

    options.setOption(ADD_MISSING_NLS_TAGS, CleanUpOptions.FALSE);

    options.setOption(ADD_MISSING_METHODES, CleanUpOptions.FALSE);
    options.setOption(UnimplementedCodeCleanUp.MAKE_TYPE_ABSTRACT, CleanUpOptions.FALSE);

    // Code Organizing
    options.setOption(FORMAT_SOURCE_CODE, CleanUpOptions.FALSE);
    options.setOption(FORMAT_SOURCE_CODE_CHANGES_ONLY, CleanUpOptions.FALSE);

    options.setOption(FORMAT_REMOVE_TRAILING_WHITESPACES, CleanUpOptions.FALSE);
    options.setOption(FORMAT_REMOVE_TRAILING_WHITESPACES_ALL, CleanUpOptions.TRUE);
    options.setOption(FORMAT_REMOVE_TRAILING_WHITESPACES_IGNORE_EMPTY, CleanUpOptions.FALSE);

    options.setOption(FORMAT_CORRECT_INDENTATION, CleanUpOptions.FALSE);

    options.setOption(ORGANIZE_IMPORTS, CleanUpOptions.FALSE);

    options.setOption(SORT_MEMBERS, CleanUpOptions.FALSE);
    options.setOption(SORT_MEMBERS_ALL, CleanUpOptions.FALSE);
  }

  private static void setSaveParticipantSettings(CleanUpOptions options) {

    // Member Accesses
    options.setOption(MEMBER_ACCESSES_NON_STATIC_FIELD_USE_THIS, CleanUpOptions.FALSE);
    options.setOption(MEMBER_ACCESSES_NON_STATIC_FIELD_USE_THIS_ALWAYS, CleanUpOptions.FALSE);
    options.setOption(MEMBER_ACCESSES_NON_STATIC_FIELD_USE_THIS_IF_NECESSARY, CleanUpOptions.TRUE);

    options.setOption(MEMBER_ACCESSES_NON_STATIC_METHOD_USE_THIS, CleanUpOptions.FALSE);
    options.setOption(MEMBER_ACCESSES_NON_STATIC_METHOD_USE_THIS_ALWAYS, CleanUpOptions.FALSE);
    options.setOption(MEMBER_ACCESSES_NON_STATIC_METHOD_USE_THIS_IF_NECESSARY, CleanUpOptions.TRUE);

    options.setOption(MEMBER_ACCESSES_STATIC_QUALIFY_WITH_DECLARING_CLASS, CleanUpOptions.FALSE);
    options.setOption(
        MEMBER_ACCESSES_STATIC_QUALIFY_WITH_DECLARING_CLASS_FIELD, CleanUpOptions.FALSE);
    options.setOption(
        MEMBER_ACCESSES_STATIC_QUALIFY_WITH_DECLARING_CLASS_METHOD, CleanUpOptions.FALSE);
    options.setOption(
        MEMBER_ACCESSES_STATIC_QUALIFY_WITH_DECLARING_CLASS_SUBTYPE_ACCESS, CleanUpOptions.TRUE);
    options.setOption(
        MEMBER_ACCESSES_STATIC_QUALIFY_WITH_DECLARING_CLASS_INSTANCE_ACCESS, CleanUpOptions.TRUE);

    // Control Statements
    options.setOption(CONTROL_STATEMENTS_USE_BLOCKS, CleanUpOptions.FALSE);
    options.setOption(CONTROL_STATMENTS_USE_BLOCKS_ALWAYS, CleanUpOptions.TRUE);
    options.setOption(CONTROL_STATMENTS_USE_BLOCKS_NO_FOR_RETURN_AND_THROW, CleanUpOptions.FALSE);
    options.setOption(CONTROL_STATMENTS_USE_BLOCKS_NEVER, CleanUpOptions.FALSE);

    options.setOption(CONTROL_STATMENTS_CONVERT_FOR_LOOP_TO_ENHANCED, CleanUpOptions.FALSE);

    // Expressions
    options.setOption(EXPRESSIONS_USE_PARENTHESES, CleanUpOptions.FALSE);
    options.setOption(EXPRESSIONS_USE_PARENTHESES_NEVER, CleanUpOptions.TRUE);
    options.setOption(EXPRESSIONS_USE_PARENTHESES_ALWAYS, CleanUpOptions.FALSE);

    // Variable Declarations
    options.setOption(VARIABLE_DECLARATIONS_USE_FINAL, CleanUpOptions.FALSE);
    options.setOption(VARIABLE_DECLARATIONS_USE_FINAL_LOCAL_VARIABLES, CleanUpOptions.TRUE);
    options.setOption(VARIABLE_DECLARATIONS_USE_FINAL_PARAMETERS, CleanUpOptions.FALSE);
    options.setOption(VARIABLE_DECLARATIONS_USE_FINAL_PRIVATE_FIELDS, CleanUpOptions.TRUE);

    // Functional Interfaces
    options.setOption(CONVERT_FUNCTIONAL_INTERFACES, CleanUpOptions.FALSE);
    options.setOption(USE_LAMBDA, CleanUpOptions.TRUE);
    options.setOption(USE_ANONYMOUS_CLASS_CREATION, CleanUpOptions.FALSE);

    // Unused Code
    options.setOption(REMOVE_UNUSED_CODE_IMPORTS, CleanUpOptions.FALSE);
    options.setOption(REMOVE_UNUSED_CODE_PRIVATE_MEMBERS, CleanUpOptions.FALSE);
    options.setOption(REMOVE_UNUSED_CODE_PRIVATE_CONSTRUCTORS, CleanUpOptions.TRUE);
    options.setOption(REMOVE_UNUSED_CODE_PRIVATE_FELDS, CleanUpOptions.TRUE);
    options.setOption(REMOVE_UNUSED_CODE_PRIVATE_METHODS, CleanUpOptions.TRUE);
    options.setOption(REMOVE_UNUSED_CODE_PRIVATE_TYPES, CleanUpOptions.TRUE);
    options.setOption(REMOVE_UNUSED_CODE_LOCAL_VARIABLES, CleanUpOptions.FALSE);

    // Unnecessary Code
    options.setOption(REMOVE_UNNECESSARY_CASTS, CleanUpOptions.TRUE);
    options.setOption(REMOVE_UNNECESSARY_NLS_TAGS, CleanUpOptions.FALSE);
    options.setOption(USE_TYPE_ARGUMENTS, CleanUpOptions.FALSE);
    options.setOption(INSERT_INFERRED_TYPE_ARGUMENTS, CleanUpOptions.FALSE);
    options.setOption(REMOVE_REDUNDANT_TYPE_ARGUMENTS, CleanUpOptions.TRUE);

    // Missing Code
    options.setOption(ADD_MISSING_ANNOTATIONS, CleanUpOptions.TRUE);
    options.setOption(ADD_MISSING_ANNOTATIONS_OVERRIDE, CleanUpOptions.TRUE);
    options.setOption(
        ADD_MISSING_ANNOTATIONS_OVERRIDE_FOR_INTERFACE_METHOD_IMPLEMENTATION, CleanUpOptions.TRUE);
    options.setOption(ADD_MISSING_ANNOTATIONS_DEPRECATED, CleanUpOptions.TRUE);

    options.setOption(ADD_MISSING_SERIAL_VERSION_ID, CleanUpOptions.FALSE);
    options.setOption(ADD_MISSING_SERIAL_VERSION_ID_GENERATED, CleanUpOptions.FALSE);
    options.setOption(ADD_MISSING_SERIAL_VERSION_ID_DEFAULT, CleanUpOptions.TRUE);

    options.setOption(ADD_MISSING_NLS_TAGS, CleanUpOptions.FALSE);

    options.setOption(ADD_MISSING_METHODES, CleanUpOptions.FALSE);
    options.setOption(UnimplementedCodeCleanUp.MAKE_TYPE_ABSTRACT, CleanUpOptions.FALSE);

    // Code Organizing
    options.setOption(FORMAT_SOURCE_CODE, CleanUpOptions.FALSE);
    options.setOption(FORMAT_SOURCE_CODE_CHANGES_ONLY, CleanUpOptions.FALSE);

    options.setOption(FORMAT_REMOVE_TRAILING_WHITESPACES, CleanUpOptions.FALSE);
    options.setOption(FORMAT_REMOVE_TRAILING_WHITESPACES_ALL, CleanUpOptions.TRUE);
    options.setOption(FORMAT_REMOVE_TRAILING_WHITESPACES_IGNORE_EMPTY, CleanUpOptions.FALSE);

    options.setOption(FORMAT_CORRECT_INDENTATION, CleanUpOptions.FALSE);

    options.setOption(ORGANIZE_IMPORTS, CleanUpOptions.TRUE);

    options.setOption(SORT_MEMBERS, CleanUpOptions.FALSE);
    options.setOption(SORT_MEMBERS_ALL, CleanUpOptions.FALSE);

    options.setOption(CLEANUP_ON_SAVE_ADDITIONAL_OPTIONS, CleanUpOptions.FALSE);
  }

  public static void initDefaults(IPreferenceStore store) {
    //		CleanUpOptions settings=
    // JavaPlugin.getDefault().getCleanUpRegistry().getDefaultOptions(CleanUpConstants.DEFAULT_CLEAN_UP_OPTIONS);
    //		for (Iterator<String> iterator= settings.getKeys().iterator(); iterator.hasNext();) {
    //			String key= iterator.next();
    //			store.setDefault(key, settings.getValue(key));
    //		}
    // TODO
    store.setDefault(SHOW_CLEAN_UP_WIZARD, true);
    store.setDefault(CLEANUP_PROFILE, DEFAULT_PROFILE);
    store.setDefault(CLEANUP_ON_SAVE_PROFILE, DEFAULT_SAVE_PARTICIPANT_PROFILE);
  }

  public static void setDefaultOptions(int kind, CleanUpOptions options) {
    switch (kind) {
      case CleanUpConstants.DEFAULT_CLEAN_UP_OPTIONS:
        CleanUpConstants.setEclipseDefaultSettings(options);
        break;
      case CleanUpConstants.DEFAULT_SAVE_ACTION_OPTIONS:
        CleanUpConstants.setSaveParticipantSettings(options);
        break;
      default:
        Assert.isTrue(false, "Unknown Clean Up option kind: " + kind); // $NON-NLS-1$
        break;
    }
  }
}
