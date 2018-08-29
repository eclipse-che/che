/**
 * ***************************************************************************** Copyright (c) 2012,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation Billy Huang
 * <billyhuang31@gmail.com> - [quick assist] concatenate/merge string literals -
 * https://bugs.eclipse.org/77632 Lukas Hanke <hanke@yatta.de> - Bug 241696 [quick fix] quickfix to
 * iterate over a collection - https://bugs.eclipse.org/bugs/show_bug.cgi?id=241696
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.correction;

/**
 * Interface defining relevance values for quick fixes/assists.
 *
 * @see org.eclipse.jdt.ui.text.java.IJavaCompletionProposal#getRelevance()
 * @since 3.9
 */
public interface IProposalRelevance {
  public static final int OVERRIDES_DEPRECATED = 15;
  public static final int ADD_OVERRIDE_ANNOTATION = 15;
  public static final int ADD_DEPRECATED_ANNOTATION = 15;

  public static final int REMOVE_UNUSED_CAST = 10;
  public static final int ADD_UNIMPLEMENTED_METHODS = 10;
  public static final int UNUSED_MEMBER = 10;
  public static final int REMOVE_ELSE = 10;
  public static final int ADD_MISSING_DEFAULT_CASE = 10;
  public static final int ADD_MISSING_CASE_STATEMENTS = 10;
  public static final int UNNECESSARY_INSTANCEOF = 10;
  public static final int REPLACE_FIELD_ACCESS_WITH_METHOD = 10;
  public static final int REMOVE_UNREACHABLE_CODE_INCLUDING_CONDITION = 10;
  public static final int CHANGE_VISIBILITY = 10;
  public static final int MARKER_RESOLUTION = 10;
  public static final int CREATE_LOCAL_PREFIX_OR_SUFFIX_MATCH = 10;
  public static final int CHANGE_NULLNESS_ANNOTATION = 10;

  public static final int CHANGE_NULLNESS_ANNOTATION_IN_OVERRIDDEN_METHOD = 9;
  public static final int REMOVE_FINAL_MODIFIER = 9;
  public static final int GETTER_SETTER_NOT_VISIBLE_FIELD = 9;
  public static final int ADD_MISSING_BODY = 9;
  public static final int MISSING_SERIAL_VERSION = 9;
  public static final int MISSING_SERIAL_VERSION_DEFAULT = 9;
  public static final int CREATE_CONSTANT_PREFIX_OR_SUFFIX_MATCH = 9;
  public static final int CREATE_FIELD_PREFIX_OR_SUFFIX_MATCH = 9;

  public static final int ADD_ABSTRACT_MODIFIER = 8;
  public static final int ADD_STATIC_MODIFIER = 8;
  public static final int ADD_DEFAULT_MODIFIER = 8;
  public static final int ADD_PARENTHESES_AROUND_CAST = 8;
  public static final int REMOVE_ARGUMENTS = 8;
  public static final int QUALIFY_WITH_ENCLOSING_TYPE = 8;
  public static final int THROW_ALLOCATED_OBJECT = 8;
  public static final int CHANGE_TO_METHOD = 8;
  public static final int ADD_FIELD_QUALIFIER = 8;
  public static final int ADD_THROWS_DECLARATION = 8;
  public static final int CHANGE_OVERRIDDEN_MODIFIER_1 = 8;
  public static final int REMOVE_EXCEPTIONS = 8;
  public static final int SWAP_ARGUMENTS = 8;
  public static final int GETTER_SETTER_UNUSED_PRIVATE_FIELD = 8;
  public static final int RENAME_REFACTORING = 8;
  public static final int LINKED_NAMES_ASSIST = 8;
  public static final int ADD_ARGUMENTS = 8;
  public static final int ADD_PROJECT_TO_BUILDPATH = 8;
  public static final int CHANGE_VARIABLE = 8;
  public static final int CHANGE_RETURN_TYPE = 8;
  public static final int CREATE_PARAMETER_PREFIX_OR_SUFFIX_MATCH = 8;

  public static final int CHANGE_OVERRIDDEN_MODIFIER_2 = 7;
  public static final int ADD_EXCEPTIONS = 7;
  public static final int CHANGE_METHOD_SIGNATURE = 7;
  public static final int SURROUND_WITH_TRY_MULTICATCH = 7;
  public static final int CAST_AND_ASSIGN = 7;
  public static final int ADD_ADDITIONAL_MULTI_CATCH = 7;
  public static final int ADD_EXCEPTIONS_TO_EXISTING_CATCH = 7;
  public static final int ADD_ADDITIONAL_CATCH = 7;
  public static final int ADD_NEW_KEYWORD_UPPERCASE = 7;
  public static final int GETTER_SETTER_QUICK_ASSIST = 7;
  public static final int RENAME_REFACTORING_QUICK_FIX = 7;
  public static final int ADD_TO_BUILDPATH = 7;
  public static final int CHANGE_RETURN_TYPE_OF_OVERRIDDEN = 7;
  public static final int CREATE_CAST = 7;
  public static final int ARRAY_CHANGE_TO_LENGTH = 7;
  public static final int MISSING_PACKAGE_DECLARATION = 7;
  public static final int INSERT_INFERRED_TYPE_ARGUMENTS = 7;
  public static final int RETURN_ALLOCATED_OBJECT_MATCH = 7;
  public static final int CREATE_LOCAL = 7;

  public static final int REMOVE_SEMICOLON = 6;
  public static final int CREATE_METHOD_IN_SUPER = 6;
  public static final int QUALIFY_LHS = 6;
  public static final int CHANGE_WORKSPACE_COMPLIANCE = 6;
  public static final int ARRAY_CHANGE_TO_METHOD = 6;
  public static final int CAST_ARGUMENT_1 = 6;
  public static final int CHANGE_METHOD = 6;
  public static final int VOID_METHOD_RETURNS = 6;
  public static final int RENAME_CU = 6;
  public static final int MOVE_CU_TO_PACKAGE = 6;
  public static final int INITIALIZE_VARIABLE = 6;
  public static final int MISSING_RETURN_TYPE = 6;
  public static final int CHANGE_METHOD_RETURN_TYPE = 6;
  public static final int UNNECESSARY_NLS_TAG = 6;
  public static final int RAW_TYPE_REFERENCE = 6;
  public static final int CREATE_NON_STATIC_ACCESS_USING_DECLARING_TYPE = 6;
  public static final int CREATE_INDIRECT_ACCESS_TO_STATIC = 6;
  public static final int SURROUND_WITH_TRY_CATCH = 6;
  public static final int REMOVE_REDUNDANT_TYPE_ARGUMENTS = 6;
  public static final int REMOVE_REDUNDANT_SUPER_INTERFACE = 6;
  public static final int CHANGE_EXTENDS_TO_IMPLEMENTS = 6;
  public static final int REMOVE_OVERRIDE = 6;
  public static final int MOVE_EXCEPTION_TO_SEPERATE_CATCH_BLOCK = 6;
  public static final int REMOVE_ABSTRACT_MODIFIER = 6;
  public static final int REMOVE_EXCEPTION = 6;
  public static final int REPLACE_EXCEPTION_WITH_THROWS = 6;
  public static final int REMOVE_TYPE_ARGUMENTS = 6;
  public static final int CHANGE_IF_ELSE_TO_BLOCK = 6;
  public static final int REMOVE_NATIVE = 6;
  public static final int REMOVE_UNUSED_IMPORT = 6;
  public static final int CHANGE_TYPE_OF_RECEIVER_NODE = 6;
  public static final int EXTRACT_LOCAL_ALL = 6;
  public static final int CHANGE_TO_ATTRIBUTE_SIMILAR_NAME = 6;
  public static final int CREATE_FIELD = 6;
  public static final int CONVERT_TO_LAMBDA_EXPRESSION = 6;

  public static final int ADD_ALL_MISSING_TAGS = 5;
  public static final int QUALIFY_INNER_TYPE_NAME = 5;
  public static final int REMOVE_TAG = 5;
  public static final int INVALID_OPERATOR = 5;
  public static final int REMOVE_METHOD_BODY = 5;
  public static final int INSERT_BREAK_STATEMENT = 5;
  public static final int REMOVE_CATCH_CLAUSE = 5;
  public static final int CHANGE_TO_RETURN = 5;
  public static final int INCOMPATIBLE_FOREACH_TYPE = 5;
  public static final int CHANGE_RETURN_TYPE_TO_VOID = 5;
  public static final int CHANGE_TO_CONSTRUCTOR = 5;
  public static final int FIX_SUPPRESS_TOKEN = 5;
  public static final int REMOVE_ANNOTATION = 5;
  public static final int OVERRIDE_HASHCODE = 5;
  public static final int ADD_BLOCK = 5;
  public static final int MAKE_TYPE_ABSTRACT = 5;
  public static final int ADD_MISSING_NLS_TAGS = 5;
  public static final int CREATE_NON_STATIC_ACCESS_USING_INSTANCE_TYPE = 5;
  public static final int MAKE_TYPE_ABSTRACT_FIX = 5;
  public static final int CONVERT_LOCAL_TO_FIELD = 5;
  public static final int CONVERT_ANONYMOUS_TO_NESTED = 5;
  public static final int IMPORT_EXPLICIT = 5;
  public static final int ADD_STATIC_IMPORT = 5;
  public static final int REMOVE_SAFEVARARGS = 5;
  public static final int INFER_GENERIC_TYPE_ARGUMENTS = 5;
  public static final int ORGANIZE_IMPORTS = 5;
  public static final int INLINE_LOCAL = 5;
  public static final int CREATE_PARAMETER = 5;
  public static final int UNNECESSARY_THROW = 5;
  public static final int ADD_METHOD_MODIFIER = 5;
  public static final int CHANGE_MODIFIER_TO_FINAL = 5;
  public static final int CHANGE_MODIFIER_OF_VARIABLE_TO_FINAL = 5;
  public static final int CONFIGURE_ACCESS_RULES = 5;
  public static final int CONFIGURE_BUILD_PATH = 5;
  public static final int CHANGE_METHOD_ADD_PARAMETER = 5;
  public static final int CHANGE_METHOD_REMOVE_PARAMETER = 5;
  public static final int CREATE_METHOD = 5;
  public static final int CHANGE_METHOD_SWAP_PARAMETERS = 5;
  public static final int CREATE_ATTRIBUTE = 5;
  public static final int CREATE_CONSTRUCTOR = 5;
  public static final int CAST_ARGUMENT_2 = 5;
  public static final int CHANGE_TYPE_OF_NODE_TO_CAST = 5;
  public static final int IMPORT_NOT_FOUND_NEW_TYPE = 5;
  public static final int REMOVE_INVALID_MODIFIERS = 5;
  public static final int CHANGE_VISIBILITY_TO_NON_PRIVATE = 5;
  public static final int CHANGE_MODIFIER_TO_STATIC = 5;
  public static final int VARIABLE_TYPE_PROPOSAL_1 = 5;
  public static final int EXTRACT_LOCAL = 5;
  public static final int QUALIFY_RHS = 5;
  public static final int ADD_CONSTRUCTOR_FROM_SUPER_CLASS = 5;
  public static final int GETTER_SETTER_UNQUALIFIED_FIELD_ACCESS = 5;
  public static final int RENAME_TYPE = 5;
  public static final int CHANGE_PROJECT_COMPLIANCE = 5;
  public static final int CORRECT_PACKAGE_DECLARATION = 5;
  public static final int TYPE_ARGUMENTS_FROM_CONTEXT = 5;
  public static final int REMOVE_REDUNDANT_NULLNESS_ANNOTATION = 5;

  public static final int ADD_MISSING_TAG = 4;
  public static final int INSERT_FALL_THROUGH = 4;
  public static final int REPLACE_CATCH_CLAUSE_WITH_THROWS = 4;
  public static final int INSERT_CASES_OMITTED = 4;
  public static final int REMOVE_ASSIGNMENT = 4;
  public static final int EXTERNALIZE_STRINGS = 4;
  public static final int ADD_NEW_KEYWORD = 4;
  public static final int REMOVE_STATIC_MODIFIER = 4;
  public static final int EXTRACT_METHOD = 4;
  public static final int METHOD_RETURNS_VOID = 4;
  public static final int EXTRACT_CONSTANT = 4;
  public static final int CREATE_CONSTANT = 4;

  public static final int CHANGE_CLASS_TO_INTERFACE = 3;
  public static final int GENERATE_HASHCODE_AND_EQUALS = 3;
  public static final int SIMILAR_TYPE = 3;
  public static final int EXTRACT_LOCAL_ALL_ERROR = 3;
  public static final int ASSIGN_PARAM_TO_NEW_FIELD = 3;
  public static final int ASSIGN_TO_LOCAL = 3;
  public static final int CHANGE_CAST = 3;
  public static final int CHANGE_TO_ATTRIBUTE = 3;
  public static final int CHANGE_LAMBDA_BODY_TO_BLOCK = 3;
  public static final int CHANGE_LAMBDA_BODY_TO_EXPRESSION = 3;

  public static final int CONVERT_TO_INDEXED_FOR_LOOP = 2;
  public static final int GENERATE_ENHANCED_FOR_LOOP = 2;
  public static final int USE_SEPARATE_CATCH_BLOCKS = 2;
  public static final int INSERT_NULL_CHECK = 2;
  public static final int COMBINE_CATCH_BLOCKS = 2;
  public static final int EXTRACT_LOCAL_ERROR = 2;
  public static final int ASSIGN_TO_FIELD = 2;
  public static final int RETURN_ALLOCATED_OBJECT = 2;
  public static final int REMOVE_BLOCK_FIX = 2;
  public static final int CONVERT_TO_ANONYMOUS_CLASS_CREATION = 2;

  public static final int JOIN_VARIABLE_DECLARATION = 1;
  public static final int INVERT_EQUALS = 1;
  public static final int CONVERT_TO_ITERATOR_FOR_LOOP = 1;
  public static final int GENERATE_FOR_LOOP = 1;
  public static final int ADD_TYPE_TO_ARRAY_INITIALIZER = 1;
  public static final int REMOVE_EXTRA_PARENTHESES = 1;
  public static final int CONVERT_ITERABLE_LOOP_TO_ENHANCED = 1;
  public static final int CONVERT_FOR_LOOP_TO_ENHANCED = 1;
  public static final int SPLIT_OR_CONDITION = 1;
  public static final int SPLIT_AND_CONDITION = 1;
  public static final int REPLACE_IF_ELSE_WITH_CONDITIONAL = 1;
  public static final int REPLACE_CONDITIONAL_WITH_IF_ELSE = 1;
  public static final int PULL_NEGATION_DOWN = 1;
  public static final int PULL_NEGATION_UP = 1;
  public static final int JOIN_IF_STATEMENTS_WITH_OR = 1;
  public static final int JOIN_IF_SEQUENCE = 1;
  public static final int JOIN_IF_WITH_OUTER_IF = 1;
  public static final int INVERSE_IF_STATEMENT = 1;
  public static final int INVERT_IF_TO_CONTINUE = 1;
  public static final int INVERSE_IF_CONTINUE = 1;
  public static final int INVERSE_CONDITIONS = 1;
  public static final int INVERSE_CONDITIONAL_EXPRESSION = 1;
  public static final int CONVERT_TO_IF_ELSE = 1;
  public static final int EXCHANGE_OPERANDS = 1;
  public static final int EXCHANGE_INNER_AND_OUTER_IF_CONDITIONS = 1;
  public static final int CONVERT_SWITCH_TO_IF_ELSE = 1;
  public static final int CONVERT_IF_ELSE_TO_SWITCH = 1;
  public static final int DOCUMENT_UNUSED_ITEM = 1;
  public static final int PICK_SELECTED_STRING = 1;
  public static final int COMBINE_STRINGS = 1;
  public static final int INVERSE_BOOLEAN_VARIABLE = 1;
  public static final int REMOVE_UNUSED_ALLOCATED_OBJECT = 1;
  public static final int UNWRAP_STATEMENTS = 1;
  public static final int SPLIT_VARIABLE_DECLARATION = 1;
  public static final int ADD_FINALLY_BLOCK = 1;
  public static final int ADD_ELSE_BLOCK = 1;
  public static final int CONVERT_TO_STRING_BUFFER = 1;
  public static final int JOIN_IF_WITH_INNER_IF = 1;
  public static final int ADD_JAVADOC_ENUM = 1;
  public static final int ADD_JAVADOC_FIELD = 1;
  public static final int ADD_JAVADOC_TYPE = 1;
  public static final int ADD_JAVADOC_METHOD = 1;
  public static final int EXTRACT_METHOD_ERROR = 1;
  public static final int EXTRACT_CONSTANT_ERROR = 1;
  public static final int LINKED_NAMES_ASSIST_ERROR = 1;
  public static final int RENAME_REFACTORING_ERROR = 1;
  public static final int ASSIGN_PARAM_TO_EXISTING_FIELD = 1;
  public static final int INSERT_INFERRED_TYPE_ARGUMENTS_ERROR = 1;
  public static final int RETURN_ALLOCATED_OBJECT_VOID = 1;
  public static final int CONVERT_TO_IF_RETURN = 1;

  public static final int CONVERT_TO_MESSAGE_FORMAT = 0;
  public static final int COPY_ANNOTATION_JAR = 0;
  public static final int NO_SUGGESSTIONS_AVAILABLE = 0;
  public static final int ADD_QUOTE = 0;
  public static final int NEW_TYPE = 0;
  public static final int SIMILAR_VARIABLE_PROPOSAL = 0;
  public static final int EXTRACT_LOCAL_ALL_ZERO_SELECTION = 0;

  public static final int EXTRACT_LOCAL_ZERO_SELECTION = -1;
  public static final int MAKE_VARIABLE_DECLARATION_FINAL = -1;

  public static final int ADD_SUPPRESSWARNINGS = -2;
  public static final int VARIABLE_TYPE_PROPOSAL_2 = -2;
  public static final int EXTRACT_CONSTANT_ZERO_SELECTION = -2;
  public static final int ADD_SAFEVARARGS = -2;

  public static final int ADD_PARANOIDAL_PARENTHESES = -9;

  public static final int ADD_PARENTHESES_FOR_EXPRESSION = -10;

  // Be careful while tweaking these values because WordCorrectionProposal uses -distance (between
  // the words) as relevance.
  public static final int DISABLE_SPELL_CHECKING = Integer.MIN_VALUE + 1;
  public static final int WORD_IGNORE = Integer.MIN_VALUE + 1;
  public static final int ADD_WORD = Integer.MIN_VALUE;
}
