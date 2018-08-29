/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation Benjamin Muskalla
 * <bmuskalla@eclipsesource.com> - [extract method] Extract method and continue
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=48056 Samrat Dhillon <samrat.dhillon@gmail.com> -
 * [introduce factory] Introduce Factory on an abstract class adds a statement to create an instance
 * of that class - https://bugs.eclipse.org/bugs/show_bug.cgi?id=395016
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring;

import org.eclipse.osgi.util.NLS;

public final class RefactoringCoreMessages extends NLS {

  public static String AbstractRenameChange_Renaming;

  public static String ASTData_update_imports;

  private static final String BUNDLE_NAME =
      "org.eclipse.jdt.internal.corext.refactoring.refactoring"; // $NON-NLS-1$

  public static String CallInliner_cast_analysis_error;

  public static String CallInliner_constructors;

  public static String CallInliner_execution_flow;

  public static String CallInliner_field_initialize_new_local;

  public static String CallInliner_field_initialize_self_reference;

  public static String CallInliner_field_initialize_write_parameter;

  public static String CallInliner_field_initializer_simple;

  public static String CallInliner_multiDeclaration;

  public static String CallInliner_receiver_type;

  public static String CallInliner_simple_functions;

  public static String CallInliner_super_into_this_expression;

  public static String Change_does_not_exist;

  public static String ChangeSignatureRefactoring_add_constructor;

  public static String ChangeSignatureRefactoring_add_super_call;

  public static String ChangeSignatureRefactoring_added_exceptions;

  public static String ChangeSignatureRefactoring_added_parameter_pattern;

  public static String ChangeSignatureRefactoring_added_parameters;

  public static String ChangeSignatureRefactoring_already_has;

  public static String ChangeSignatureRefactoring_anonymous_subclass;

  public static String ChangeSignatureRefactoring_cannot_convert_vararg;

  public static String ChangeSignatureRefactoring_change_signature;

  public static String ChangeSignatureRefactoring_changed_parameter_pattern;

  public static String ChangeSignatureRefactoring_changed_parameters;

  public static String ChangeSignatureRefactoring_checking_preconditions;

  public static String ChangeSignatureRefactoring_constructor_name;

  public static String ChangeSignatureRefactoring_default_value;

  public static String ChangeSignatureRefactoring_default_visibility;

  public static String ChangeSignatureRefactoring_deleted_parameter_pattern;

  public static String ChangeSignatureRefactoring_descriptor_description;

  public static String ChangeSignatureRefactoring_descriptor_description_short;

  public static String ChangeSignatureRefactoring_duplicate_name;

  public static String ChangeSignatureRefactoring_invalid_expression;

  public static String ChangeSignatureRefactoring_method_deleted;

  public static String ChangeSignatureRefactoring_method_name_not_empty;

  public static String ChangeSignatureRefactoring_modify_Parameters;

  public static String ChangeSignatureRefactoring_native;

  public static String ChangeSignatureRefactoring_new_name_pattern;

  public static String ChangeSignatureRefactoring_new_return_type_pattern;

  public static String ChangeSignatureRefactoring_new_visibility_pattern;

  public static String ChangeSignatureRefactoring_no_exception_binding;

  public static String ChangeSignatureRefactoring_non_virtual;

  public static String ChangeSignatureRefactoring_old_and_new_signatures_not_sufficiently_different;

  public static String ChangeSignatureRefactoring_param_name_not_empty;

  public static String ChangeSignatureRefactoring_parameter_type_contains_type_variable;

  public static String ChangeSignatureRefactoring_parameter_used;

  public static String ChangeSignatureRefactoring_preview;

  public static String ChangeSignatureRefactoring_removed_exceptions;

  public static String ChangeSignatureRefactoring_removed_parameters;

  public static String ChangeSignatureRefactoring_restructure_parameters;

  public static String ChangeSignatureRefactoring_return_type_contains_type_variable;

  public static String ChangeSignatureRefactoring_ripple_cannot_convert_vararg;

  public static String ChangeSignatureRefactoring_unchanged;

  public static String ChangeSignatureRefactoring_update_javadoc_reference;

  public static String ChangeSignatureRefactoring_update_parameter_references;

  public static String ChangeSignatureRefactoring_update_reference;

  public static String ChangeSignatureRefactoring_vararg_must_be_last;

  public static String ChangeTypeMessages_CreateChangesForChangeType;

  public static String ChangeTypeRefactoring_allChanges;

  public static String ChangeTypeRefactoring_analyzingMessage;

  public static String ChangeTypeRefactoring_arraysNotSupported;

  public static String ChangeTypeRefactoring_checking_preconditions;

  public static String ChangeTypeRefactoring_descriptor_description;

  public static String ChangeTypeRefactoring_descriptor_description_short;

  public static String ChangeTypeRefactoring_enumsNotSupported;

  public static String ChangeTypeRefactoring_uniontypeNotSupported;

  public static String ChangeTypeRefactoring_insideLocalTypesNotSupported;

  public static String ChangeTypeRefactoring_invalidSelection;

  public static String ChangeTypeRefactoring_localTypesNotSupported;

  public static String ChangeTypeRefactoring_multiDeclarationsNotSupported;

  public static String ChangeTypeRefactoring_name;

  public static String ChangeTypeRefactoring_no_filed;

  public static String ChangeTypeRefactoring_no_method;

  public static String ChangeTypeRefactoring_noMatchingConstraintVariable;

  public static String ChangeTypeRefactoring_notSupportedOnBinary;

  public static String ChangeTypeRefactoring_notSupportedOnNodeType;

  public static String ChangeTypeRefactoring_original_element_pattern;

  public static String ChangeTypeRefactoring_original_type_pattern;

  public static String ChangeTypeRefactoring_primitivesNotSupported;

  public static String ChangeTypeRefactoring_refactored_type_pattern;

  public static String ChangeTypeRefactoring_typeChange;

  public static String ChangeTypeRefactoring_typeParametersNotSupported;

  public static String Checks_all_excluded;

  public static String Checks_cannot_be_parsed;

  public static String Checks_Choose_name;

  public static String Checks_constructor_name;

  public static String Checks_cu_has_compile_errors;

  public static String Checks_cu_name_used;

  public static String Checks_cu_not_created;

  public static String Checks_cu_not_parsed;

  public static String Checks_has_main;

  public static String Checks_method_names_lowercase;

  public static String Checks_method_names_lowercase2;

  public static String Checks_method_native;

  public static String Checks_methodName_constructor;

  public static String Checks_methodName_exists;

  public static String Checks_methodName_overrides;

  public static String Checks_methodName_returnTypeClash;

  public static String Checks_no_dot;

  public static String Checks_validateEdit;

  public static String ClasspathChange_change_name;

  public static String ClasspathChange_progress_message;

  public static String CodeAnalyzer_array_initializer;

  public static String CodeRefactoringUtil_error_message;

  public static String CommentAnalyzer_ends_inside_comment;

  public static String CommentAnalyzer_internal_error;

  public static String CommentAnalyzer_starts_inside_comment;

  public static String CompilationUnitChange_label;

  public static String ConvertAnonymousToNestedRefactoring_anonymous_field_access;

  public static String ConvertAnonymousToNestedRefactoring_another_name;

  public static String ConvertAnonymousToNestedRefactoring_class_name_pattern;

  public static String ConvertAnonymousToNestedRefactoring_compile_errors;

  public static String ConvertAnonymousToNestedRefactoring_declare_final;

  public static String ConvertAnonymousToNestedRefactoring_declare_final_static;

  public static String ConvertAnonymousToNestedRefactoring_declare_static;

  public static String ConvertAnonymousToNestedRefactoring_default_visibility;

  public static String ConvertAnonymousToNestedRefactoring_descriptor_description;

  public static String ConvertAnonymousToNestedRefactoring_descriptor_description_short;

  public static String ConvertAnonymousToNestedRefactoring_extends_local_class;

  public static String ConvertAnonymousToNestedRefactoring_name;

  public static String ConvertAnonymousToNestedRefactoring_name_hides;

  public static String ConvertAnonymousToNestedRefactoring_original_pattern;

  public static String ConvertAnonymousToNestedRefactoring_place_caret;

  public static String ConvertAnonymousToNestedRefactoring_type_exists;

  public static String ConvertAnonymousToNestedRefactoring_visibility_pattern;

  public static String CopyCompilationUnitChange_copy;

  public static String CopyPackageChange_copy;

  public static String CopyPackageFragmentRootChange_copy;

  public static String CopyRefactoring_cu_copyOf1;

  public static String CopyRefactoring_cu_copyOfMore;

  public static String CopyRefactoring_package_copyOf1;

  public static String CopyRefactoring_package_copyOfMore;

  public static String CopyRefactoring_resource_copyOf1;

  public static String CopyRefactoring_resource_copyOfMore;

  public static String CopyRefactoring_update_ref;

  public static String CopyResourceString_copy;

  public static String CreateCopyOfCompilationUnitChange_create_copy;

  public static String CreatePackageChange_Create_package;

  public static String CreatePackageChange_Creating_package;

  public static String DelegateCreator_cannot_create_delegate_for_type;

  public static String DelegateCreator_cannot_create_field_delegate_more_than_one_fragment;

  public static String DelegateCreator_cannot_create_field_delegate_no_initializer;

  public static String DelegateCreator_cannot_create_field_delegate_not_final;

  public static String DelegateCreator_change_category_description;

  public static String DelegateCreator_change_category_title;

  public static String DelegateCreator_keep_original_changed_plural;

  public static String DelegateCreator_keep_original_changed_singular;

  public static String DelegateCreator_use_member_instead;

  public static String DelegateFieldCreator_keep_original_renamed_plural;

  public static String DelegateFieldCreator_keep_original_renamed_singular;

  public static String DelegateFieldCreator_text_edit_group_label;

  public static String DelegateMethodCreator_keep_original_moved_plural;

  public static String DelegateMethodCreator_keep_original_moved_singular;

  public static String DelegateMethodCreator_keep_original_moved_plural_member;

  public static String DelegateMethodCreator_keep_original_moved_singular_member;

  public static String DelegateMethodCreator_keep_original_renamed_plural;

  public static String DelegateMethodCreator_keep_original_renamed_singular;

  public static String DelegateMethodCreator_text_edit_group_field;

  public static String DeleteChangeCreator_1;

  public static String DeletePackageFragmentRootChange_delete;

  public static String DeletePackageFragmentRootChange_restore_file;

  public static String DeleteRefactoring_1;

  public static String DeleteRefactoring_2;

  public static String DeleteRefactoring_3_singular;

  public static String DeleteRefactoring_3_plural;

  public static String DeleteRefactoring_4;

  public static String DeleteRefactoring_5;

  public static String DeleteRefactoring_7;

  public static String DeleteRefactoring_8;

  public static String DeleteRefactoring_9;

  public static String DeleteRefactoring_delete_package_fragment_root;

  public static String DeleteSourceManipulationChange_0;

  public static String DynamicValidationRefactoringChange_fatal_error;

  public static String DynamicValidationStateChange_workspace_changed;

  public static String ExtractClassContribution_error_unknown_descriptor;

  public static String ExtractClassRefactoring_change_comment_header;

  public static String ExtractClassRefactoring_change_name;

  public static String ExtractClassRefactoring_comment_extracted_class;

  public static String ExtractClassRefactoring_comment_field_renamed;

  public static String ExtractClassRefactoring_comment_fieldname;

  public static String ExtractClassRefactoring_comment_getters;

  public static String ExtractClassRefactoring_comment_move_field;

  public static String ExtractClassRefactoring_comment_package;

  public static String ExtractClassRefactoring_error_duplicate_field_name;

  public static String ExtractClassRefactoring_error_field_already_exists;

  public static String ExtractClassRefactoring_error_field_is_static;

  public static String ExtractClassRefactoring_error_msg_one_field;

  public static String ExtractClassRefactoring_error_no_usable_fields;

  public static String ExtractClassRefactoring_error_referencing_private_class;

  public static String ExtractClassRefactoring_error_referencing_protected_class;

  public static String ExtractClassRefactoring_error_switch;

  public static String ExtractClassRefactoring_error_toplevel_name_clash;

  public static String ExtractClassRefactoring_error_unable_to_convert_node;

  public static String ExtractClassRefactoring_errror_nested_name_clash;

  public static String ExtractClassRefactoring_fatal_error_cannot_resolve_binding;

  public static String ExtractClassRefactoring_group_insert_parameter;

  public static String ExtractClassRefactoring_group_remove_field;

  public static String ExtractClassRefactoring_group_replace_read;

  public static String ExtractClassRefactoring_group_replace_write;

  public static String ExtractClassRefactoring_progress_create_change;

  public static String ExtractClassRefactoring_progress_final_conditions;

  public static String ExtractClassRefactoring_progress_msg_check_initial_condition;

  public static String ExtractClassRefactoring_progress_updating_references;

  public static String ExtractClassRefactoring_refactoring_name;

  public static String ExtractClassRefactoring_warning_field_is_transient;

  public static String ExtractClassRefactoring_warning_field_is_volatile;

  public static String ExtractClassRefactoring_warning_no_fields_moved;

  public static String ExtractClassRefactoring_warning_semantic_change;

  public static String ExtractConstantRefactoring_another_variable;

  public static String ExtractConstantRefactoring_change_name;

  public static String ExtractConstantRefactoring_checking_preconditions;

  public static String ExtractConstantRefactoring_constant_expression_pattern;

  public static String ExtractConstantRefactoring_constant_name_pattern;

  public static String ExtractConstantRefactoring_convention;

  public static String ExtractConstantRefactoring_declare_constant;

  public static String ExtractConstantRefactoring_default_visibility;

  public static String ExtractConstantRefactoring_descriptor_description;

  public static String ExtractConstantRefactoring_descriptor_description_short;

  public static String ExtractConstantRefactoring_name;

  public static String ExtractConstantRefactoring_no_void;

  public static String ExtractConstantRefactoring_not_load_time_constant;

  public static String ExtractConstantRefactoring_null_literals;

  public static String ExtractConstantRefactoring_qualify_references;

  public static String ExtractConstantRefactoring_replace;

  public static String ExtractConstantRefactoring_replace_occurrences;

  public static String ExtractConstantRefactoring_select_expression;

  public static String ExtractConstantRefactoring_visibility_pattern;

  public static String ExtractInterfaceProcessor_add_comment;

  public static String ExtractInterfaceProcessor_add_super_interface;

  public static String ExtractInterfaceProcessor_category_description;

  public static String ExtractInterfaceProcessor_category_name;

  public static String ExtractInterfaceProcessor_checking;

  public static String ExtractInterfaceProcessor_creating;

  public static String ExtractInterfaceProcessor_description_descriptor_short;

  public static String ExtractInterfaceProcessor_descriptor_description;

  public static String ExtractInterfaceProcessor_existing_compilation_unit;

  public static String ExtractInterfaceProcessor_existing_default_type;

  public static String ExtractInterfaceProcessor_existing_type;

  public static String ExtractInterfaceProcessor_extracted_members_pattern;

  public static String ExtractInterfaceProcessor_internal_error;

  public static String ExtractInterfaceProcessor_name;

  public static String ExtractInterfaceProcessor_no_annotation;

  public static String ExtractInterfaceProcessor_no_anonymous;

  public static String ExtractInterfaceProcessor_no_binary;

  public static String ExtractInterfaceProcessor_refactored_element_pattern;

  public static String ExtractInterfaceProcessor_remove_field_label;

  public static String ExtractInterfaceProcessor_remove_method_label;

  public static String ExtractInterfaceProcessor_rewrite_comment;

  public static String ExtractInterfaceRefactoring_name;

  public static String ExtractMethodAnalyzer_after_do_keyword;

  public static String ExtractMethodAnalyzer_ambiguous_return_value;

  public static String ExtractMethodAnalyzer_assignments_to_local;

  public static String ExtractMethodAnalyzer_branch_break_mismatch;

  public static String ExtractMethodAnalyzer_branch_continue_mismatch;

  public static String ExtractMethodAnalyzer_branch_mismatch;

  public static String ExtractMethodAnalyzer_cannot_determine_return_type;

  public static String ExtractMethodAnalyzer_cannot_extract_anonymous_type;

  public static String ExtractMethodAnalyzer_cannot_extract_for_initializer;

  public static String ExtractMethodAnalyzer_cannot_extract_for_updater;

  public static String ExtractMethodAnalyzer_cannot_extract_from_annotation;

  public static String ExtractMethodAnalyzer_cannot_extract_method_name_reference;

  public static String ExtractMethodAnalyzer_cannot_extract_part_of_qualified_name;

  public static String ExtractMethodAnalyzer_cannot_extract_name_in_declaration;

  public static String ExtractMethodAnalyzer_cannot_extract_null_type;

  public static String ExtractMethodAnalyzer_cannot_extract_switch_case;

  public static String ExtractMethodAnalyzer_cannot_extract_type_reference;

  public static String ExtractMethodAnalyzer_cannot_extract_variable_declaration;

  public static String ExtractMethodAnalyzer_cannot_extract_variable_declaration_fragment;

  public static String
      ExtractMethodAnalyzer_cannot_extract_variable_declaration_fragment_from_field;

  public static String ExtractMethodAnalyzer_compile_errors;

  public static String ExtractMethodAnalyzer_compile_errors_no_parent_binding;

  public static String ExtractMethodAnalyzer_leftHandSideOfAssignment;

  public static String ExtractMethodAnalyzer_no_valid_destination_type;

  public static String ExtractMethodAnalyzer_invalid_selection;

  public static String ExtractMethodAnalyzer_parent_mismatch;

  public static String ExtractMethodAnalyzer_resource_in_try_with_resources;

  public static String ExtractMethodAnalyzer_single_expression_or_set;

  public static String ExtractMethodAnalyzer_super_or_this;

  public static String ExtractMethodRefactoring_add_method;

  public static String ExtractMethodRefactoring_change_name;

  public static String ExtractMethodRefactoring_checking_new_name;

  public static String ExtractMethodRefactoring_declare_thrown_exceptions;

  public static String ExtractMethodRefactoring_default_visibility;

  public static String ExtractMethodRefactoring_descriptor_description;

  public static String ExtractMethodRefactoring_descriptor_description_short;

  public static String ExtractMethodRefactoring_destination_pattern;

  public static String ExtractMethodRefactoring_duplicates_multi;

  public static String ExtractMethodRefactoring_duplicates_single;

  public static String ExtractMethodRefactoring_error_nameInUse;

  public static String ExtractMethodRefactoring_error_sameParameter;

  public static String ExtractMethodRefactoring_error_vararg_ordering;

  public static String ExtractMethodRefactoring_generate_comment;

  public static String ExtractMethodRefactoring_name;

  public static String ExtractMethodRefactoring_name_pattern;

  public static String ExtractMethodRefactoring_no_set_of_statements;

  public static String ExtractMethodRefactoring_organize_imports;

  public static String ExtractMethodRefactoring_replace_continue;

  public static String ExtractMethodRefactoring_replace_occurrences;

  public static String ExtractMethodRefactoring_substitute_with_call;

  public static String ExtractMethodRefactoring_visibility_pattern;

  public static String ExtractSupertypeProcessor_add_supertype;

  public static String ExtractSupertypeProcessor_category_description;

  public static String ExtractSupertypeProcessor_category_name;

  public static String ExtractSupertypeProcessor_checking;

  public static String ExtractSupertypeProcessor_computing_possible_types;

  public static String ExtractSupertypeProcessor_descriptor_description;

  public static String ExtractSupertypeProcessor_descriptor_description_short;

  public static String ExtractSupertypeProcessor_extract_supertype;

  public static String ExtractSupertypeProcessor_preparing;

  public static String ExtractSupertypeProcessor_refactored_element_pattern;

  public static String ExtractSupertypeProcessor_subtypes_pattern;

  public static String ExtractSupertypeProcessor_unexpected_exception_on_layer;

  public static String ExtractTempRefactoring_another_variable;

  public static String ExtractTempRefactoring_array_initializer;

  public static String ExtractTempRefactoring_assigned_to;

  public static String ExtractTempRefactoring_assignment;

  public static String ExtractTempRefactoring_change_name;

  public static String ExtractTempRefactoring_checking_preconditions;

  public static String ExtractTempRefactoring_convention;

  public static String ExtractTempRefactoring_declare_final;

  public static String ExtractTempRefactoring_declare_local_variable;

  public static String ExtractTempRefactoring_descriptor_description;

  public static String ExtractTempRefactoring_descriptor_description_short;

  public static String ExtractTempRefactoring_destination_pattern;

  public static String ExtractTempRefactoring_explicit_constructor;

  public static String ExtractTempRefactoring_expr_in_method_or_initializer;

  public static String ExtractTempRefactoring_expression_pattern;

  public static String ExtractTempRefactoring_for_initializer_updater;

  public static String ExtractTempRefactoring_name;

  public static String ExtractTempRefactoring_name_in_new;

  public static String ExtractTempRefactoring_name_pattern;

  public static String ExtractTempRefactoring_names_in_declarations;

  public static String ExtractTempRefactoring_no_void;

  public static String ExtractTempRefactoring_null_literals;

  public static String ExtractTempRefactoring_refers_to_for_variable;

  public static String ExtractTempRefactoring_replace;

  public static String ExtractTempRefactoring_replace_occurrences;

  public static String ExtractTempRefactoring_resource_in_try_with_resources;

  public static String ExtractTempRefactoring_select_expression;

  public static String FlowAnalyzer_execution_flow;

  public static String HierarchyRefactoring_add_member;

  public static String HierarchyRefactoring_annotation_members;

  public static String HierarchyRefactoring_does_not_exist;

  public static String HierarchyRefactoring_enum_members;

  public static String HierarchyRefactoring_gets_instantiated;

  public static String HierarchyRefactoring_initializer;

  public static String HierarchyRefactoring_interface_members;

  public static String HierarchyRefactoring_members_of_binary;

  public static String HierarchyRefactoring_members_of_read_only;

  public static String HierarchyRefactoring_remove_member;

  public static String InferTypeArgumentsRefactoring_addTypeArguments;

  public static String InferTypeArgumentsRefactoring_assume_clone;

  public static String InferTypeArgumentsRefactoring_building;

  public static String InferTypeArgumentsRefactoring_calculating_dependencies;

  public static String InferTypeArgumentsRefactoring_creatingChanges;

  public static String InferTypeArgumentsRefactoring_descriptor_description;

  public static String InferTypeArgumentsRefactoring_descriptor_description_project;

  public static String InferTypeArgumentsRefactoring_error_in_cu_skipped;

  public static String InferTypeArgumentsRefactoring_error_skipped;

  public static String InferTypeArgumentsRefactoring_internal_error;

  public static String InferTypeArgumentsRefactoring_leave_unconstrained;

  public static String InferTypeArgumentsRefactoring_name;

  public static String InferTypeArgumentsRefactoring_not50;

  public static String InferTypeArgumentsRefactoring_not50Library;

  public static String InferTypeArgumentsRefactoring_original_elements;

  public static String InferTypeArgumentsRefactoring_removeCast;

  public static String InferTypeArgumentsRefactoring_solving;

  public static String InitializableRefactoring_argument_not_exist;

  public static String InitializableRefactoring_illegal_argument;

  public static String InitializableRefactoring_inacceptable_arguments;

  public static String InitializableRefactoring_input_not_exists;

  public static String InitializableRefactoring_inputs_do_not_exist;

  public static String InlineConstantRefactoring_binary_file;

  public static String InlineConstantRefactoring_blank_finals;

  public static String InlineConstantRefactoring_descriptor_description;

  public static String InlineConstantRefactoring_descriptor_description_short;

  public static String InlineConstantRefactoring_inline;

  public static String InlineConstantRefactoring_Inline;

  public static String InlineConstantRefactoring_local_anonymous_unsupported;

  public static String InlineConstantRefactoring_name;

  public static String InlineConstantRefactoring_original_pattern;

  public static String InlineConstantRefactoring_preview;

  public static String InlineConstantRefactoring_remove_declaration;

  public static String InlineConstantRefactoring_replace_references;

  public static String InlineConstantRefactoring_static_final_field;

  public static String InlineConstantRefactoring_syntax_errors;

  public static String InlineMethodRefactoring_checking_implements_error;

  public static String InlineMethodRefactoring_checking_overridden;

  public static String InlineMethodRefactoring_checking_overridden_error;

  public static String InlineMethodRefactoring_checking_overrides_error;

  public static String InlineMethodRefactoring_descriptor_description;

  public static String InlineMethodRefactoring_descriptor_description_short;

  public static String InlineMethodRefactoring_edit_delete;

  public static String InlineMethodRefactoring_edit_import;

  public static String InlineMethodRefactoring_edit_inline;

  public static String InlineMethodRefactoring_edit_inlineCall;

  public static String InlineMethodRefactoring_error_classFile;

  public static String InlineMethodRefactoring_error_noMethodDeclaration;

  public static String InlineMethodRefactoring_name;

  public static String InlineMethodRefactoring_nestedInvocation;

  public static String InlineMethodRefactoring_original_pattern;

  public static String InlineMethodRefactoring_processing;

  public static String InlineMethodRefactoring_remove_method;

  public static String InlineMethodRefactoring_replace_references;

  public static String InlineMethodRefactoring_searching;

  public static String InlineMethodRefactoring_SourceAnalyzer_abstract_methods;

  public static String InlineMethodRefactoring_SourceAnalyzer_declaration_has_errors;

  public static String InlineMethodRefactoring_SourceAnalyzer_methoddeclaration_has_errors;

  public static String InlineMethodRefactoring_SourceAnalyzer_native_methods;

  public static String InlineMethodRefactoring_SourceAnalyzer_qualified_this_expressions;

  public static String InlineMethodRefactoring_SourceAnalyzer_recursive_call;

  public static String InlineMethodRefactoring_SourceAnalyzer_syntax_errors;

  public static String InlineMethodRefactoring_SourceAnalyzer_typedeclaration_has_errors;

  public static String InlineTempRefactoring_assigned_more_once;

  public static String InlineTempRefactoring_descriptor_description;

  public static String InlineTempRefactoring_descriptor_description_short;

  public static String InlineTempRefactoring_exceptions_declared;

  public static String InlineTempRefactoring_for_initializers;

  public static String InlineTempRefactoring_inline;

  public static String InlineTempRefactoring_inline_edit_name;

  public static String InlineTempRefactoring_method_parameter;

  public static String InlineTempRefactoring_name;

  public static String InlineTempRefactoring_not_initialized;

  public static String InlineTempRefactoring_original_pattern;

  public static String InlineTempRefactoring_preview;

  public static String InlineTempRefactoring_remove_edit_name;

  public static String InlineTempRefactoring_resource_in_try_with_resources;

  public static String InlineTempRefactoring_select_temp;

  public static String InlineTemRefactoring_error_message_fieldsCannotBeInlined;

  public static String InlineTemRefactoring_error_message_nulLiteralsCannotBeInlined;

  public static String IntroduceFactory_addFactoryMethod;

  public static String IntroduceFactory_callSitesInBinaryClass;

  public static String IntroduceFactory_cantCheckForInterface;

  public static String IntroduceFactory_cantPutFactoryInBinaryClass;

  public static String IntroduceFactory_cantPutFactoryMethodOnAnnotation;

  public static String IntroduceFactory_cantPutFactoryMethodOnInterface;

  public static String IntroduceFactory_checking_preconditions;

  public static String IntroduceFactory_checkingActivation;

  public static String IntroduceFactory_constructorInBinaryClass;

  public static String IntroduceFactory_constructorInEnum;

  public static String IntroduceFactory_createChanges;

  public static String IntroduceFactory_descriptor_description;

  public static String IntroduceFactory_duplicateMethodName;

  public static String IntroduceFactory_examiningSelection;

  public static String IntroduceFactory_name;

  public static String IntroduceFactory_noASTNodeForConstructorSearchHit;

  public static String IntroduceFactory_noBindingForSelectedConstructor;

  public static String IntroduceFactory_noConstructorCallNodeInsideFoundVarbleDecl;

  public static String IntroduceFactory_noSuchClass;

  public static String IntroduceFactory_notAConstructorInvocation;

  public static String IntroduceFactory_protectConstructor;

  public static String IntroduceFactory_replaceCalls;

  public static String IntroduceFactory_syntaxError;

  public static String IntroduceFactory_unableToResolveConstructorBinding;

  public static String IntroduceFactory_unexpectedASTNodeTypeForConstructorSearchHit;

  public static String IntroduceFactory_unexpectedInitializerNodeType;

  public static String IntroduceFactory_unsupportedNestedTypes;

  public static String IntroduceFactory_abstractClass;

  public static String IntroduceFactoryRefactoring_declare_private;

  public static String IntroduceFactoryRefactoring_descriptor_description_short;

  public static String IntroduceFactoryRefactoring_factory_pattern;

  public static String IntroduceFactoryRefactoring_original_pattern;

  public static String IntroduceFactoryRefactoring_owner_pattern;

  public static String IntroduceFactoryRefactoring_replaceJavadocReference;

  public static String IntroduceIndirectionRefactoring_adjusting_visibility;

  public static String IntroduceIndirectionRefactoring_call_warning_anonymous_cannot_qualify;

  public static String IntroduceIndirectionRefactoring_call_warning_declaring_type_not_found;

  public static String IntroduceIndirectionRefactoring_call_warning_static_expression_access;

  public static String IntroduceIndirectionRefactoring_call_warning_super_keyword;

  public static String IntroduceIndirectionRefactoring_call_warning_type_arguments;

  public static String IntroduceIndirectionRefactoring_cannot_create_in_annotation;

  public static String IntroduceIndirectionRefactoring_cannot_create_in_binary;

  public static String IntroduceIndirectionRefactoring_cannot_create_in_nested_nonstatic;

  public static String IntroduceIndirectionRefactoring_cannot_create_in_readonly;

  public static String IntroduceIndirectionRefactoring_cannot_create_on_interface;

  public static String IntroduceIndirectionRefactoring_cannot_run_without_intermediary_type;

  public static String IntroduceIndirectionRefactoring_cannot_update_binary_target_visibility;

  public static String IntroduceIndirectionRefactoring_checking_activation;

  public static String IntroduceIndirectionRefactoring_checking_conditions;

  public static String IntroduceIndirectionRefactoring_type_does_not_exist_error;

  public static String IntroduceIndirectionRefactoring_type_not_selected_error;

  public static String IntroduceIndirectionRefactoring_could_not_parse_declaring_type_error;

  public static String IntroduceIndirectionRefactoring_declaring_pattern;

  public static String IntroduceIndirectionRefactoring_descriptor_description;

  public static String IntroduceIndirectionRefactoring_descriptor_description_short;

  public static String
      IntroduceIndirectionRefactoring_duplicate_method_name_in_declaring_type_error;

  public static String IntroduceIndirectionRefactoring_group_description_create_new_method;

  public static String IntroduceIndirectionRefactoring_group_description_replace_call;

  public static String IntroduceIndirectionRefactoring_introduce_indirection;

  public static String IntroduceIndirectionRefactoring_introduce_indirection_name;

  public static String IntroduceIndirectionRefactoring_looking_for_references;

  public static String IntroduceIndirectionRefactoring_method_pattern;

  public static String IntroduceIndirectionRefactoring_not_available_for_constructors;

  public static String IntroduceIndirectionRefactoring_not_available_for_local_or_anonymous_types;

  public static String IntroduceIndirectionRefactoring_not_available_on_annotation;

  public static String IntroduceIndirectionRefactoring_not_available_on_this_selection;

  public static String IntroduceIndirectionRefactoring_open_hierarchy_error;

  public static String IntroduceIndirectionRefactoring_original_pattern;

  public static String IntroduceIndirectionRefactoring_unable_determine_declaring_type;

  public static String IntroduceParameterObjectRefactoring_cannotalanyzemethod_mappingerror;

  public static String IntroduceParameterObjectRefactoring_cannotanalysemethod_compilererror;

  public static String IntroduceParameterObjectRefactoring_descriptor_create_getter;

  public static String IntroduceParameterObjectRefactoring_descriptor_create_setter;

  public static String IntroduceParameterObjectRefactoring_descriptor_description;

  public static String IntroduceParameterObjectRefactoring_descriptor_enclosing_type;

  public static String IntroduceParameterObjectRefactoring_descriptor_fields;

  public static String IntroduceParameterObjectRefactoring_descriptor_keep_parameter;

  public static String IntroduceParameterObjectRefactoring_descriptor_object_class;

  public static String IntroduceParameterObjectRefactoring_descriptor_package;

  public static String IntroduceParameterObjectRefactoring_error_cannot_resolve_type;

  public static String IntroduceParameterObjectRefactoring_parameter_object_creation_error;

  public static String IntroduceParameterObjectRefactoring_refactoring_name;

  public static String IntroduceParameterRefactoring_cannot_introduce;

  public static String IntroduceParameterRefactoring_descriptor_description;

  public static String IntroduceParameterRefactoring_descriptor_description_short;

  public static String IntroduceParameterRefactoring_expression_in_method;

  public static String IntroduceParameterRefactoring_expression_pattern;

  public static String IntroduceParameterRefactoring_name;

  public static String IntroduceParameterRefactoring_no_binding;

  public static String IntroduceParameterRefactoring_no_void;

  public static String IntroduceParameterRefactoring_original_pattern;

  public static String IntroduceParameterRefactoring_parameter_pattern;

  public static String IntroduceParameterRefactoring_replace;

  public static String IntroduceParameterRefactoring_select;

  public static String IntroduceParameterRefactoring_syntax_error;

  public static String JavaCopyProcessor_changeName;

  public static String JavaCopyProcessor_processorName;

  public static String JavaDeleteProcessor_confirm_linked_folder_delete;

  public static String JavaDeleteProcessor_creating_change;

  public static String JavaDeleteProcessor_delete_accessors;

  public static String JavaDeleteProcessor_delete_linked_folder_question;

  public static String JavaDeleteProcessor_delete_subpackages;

  public static String JavaDeleteProcessor_description_plural;

  public static String JavaDeleteProcessor_description_singular;

  public static String JavaDeleteProcessor_header_singular;

  public static String JavaDeleteProcessor_header_plural;

  public static String JavaDeleteProcessor_project_pattern;

  public static String JavaDeleteProcessor_unsaved_changes;

  public static String JavaDeleteProcessor_workspace;

  public static String JavaElementUtil_initializer;

  public static String JavaMoveProcessor_change_name;

  public static String JavaRefactoringDescriptor_inferred_setting_pattern;

  public static String JavaRefactoringDescriptor_keep_original;

  public static String JavaRefactoringDescriptor_keep_original_deprecated;

  public static String JavaRefactoringDescriptor_not_available;

  public static String JavaRefactoringDescriptor_original_element_pattern;

  public static String JavaRefactoringDescriptor_original_elements;

  public static String JavaRefactoringDescriptor_qualified_names;

  public static String JavaRefactoringDescriptor_qualified_names_pattern;

  public static String JavaRefactoringDescriptor_rename_similar;

  public static String JavaRefactoringDescriptor_rename_similar_embedded;

  public static String JavaRefactoringDescriptor_rename_similar_suffix;

  public static String JavaRefactoringDescriptor_renamed_element_pattern;

  public static String JavaRefactoringDescriptor_textual_occurrences;

  public static String JavaRefactoringDescriptor_update_references;

  public static String JavaRefactoringDescriptorComment_destination_pattern;

  public static String JavaRefactoringDescriptorComment_element_delimiter;

  public static String JavaRefactoringDescriptorComment_original_project;

  public static String JavaRefactoringDescriptorComment_textual_move_only;

  public static String LocalTypeAnalyzer_local_type_from_outside;

  public static String LocalTypeAnalyzer_local_type_referenced_outside;

  public static String LoggedCreateTargetChange_change_name;

  public static String MemberCheckUtil_field_exists;

  public static String MemberCheckUtil_same_param_count;

  public static String MemberCheckUtil_signature_exists;

  public static String MemberCheckUtil_type_name_conflict0;

  public static String MemberCheckUtil_type_name_conflict1;

  public static String MemberCheckUtil_type_name_conflict2;

  public static String MemberCheckUtil_type_name_conflict3;

  public static String MemberCheckUtil_type_name_conflict4;

  public static String MemberVisibilityAdjustor_adjusting;

  public static String MemberVisibilityAdjustor_adjustments_description;

  public static String MemberVisibilityAdjustor_adjustments_name;

  public static String MemberVisibilityAdjustor_change_visibility;

  public static String MemberVisibilityAdjustor_change_visibility_default;

  public static String MemberVisibilityAdjustor_change_visibility_field_warning;

  public static String MemberVisibilityAdjustor_change_visibility_method_warning;

  public static String MemberVisibilityAdjustor_change_visibility_private;

  public static String MemberVisibilityAdjustor_change_visibility_protected;

  public static String MemberVisibilityAdjustor_change_visibility_public;

  public static String MemberVisibilityAdjustor_change_visibility_type_warning;

  public static String MemberVisibilityAdjustor_checking;

  public static String MethodChecks_implements;

  public static String MethodChecks_overrides;

  public static String MoveCompilationUnitChange_name;

  public static String MoveCuUpdateCreator_searching;

  public static String MoveCuUpdateCreator_update_imports;

  public static String MoveCuUpdateCreator_update_references;

  public static String MoveInnerToTopRefactoring_already_declared;

  public static String MoveInnerToTopRefactoring_change_label;

  public static String MoveInnerToTopRefactoring_change_qualifier;

  public static String MoveInnerToTopRefactoring_change_visibility_type_warning;

  public static String MoveInnerToTopRefactoring_compilation_Unit_exists;

  public static String MoveInnerToTopRefactoring_creating_change;

  public static String MoveInnerToTopRefactoring_creating_preview;

  public static String MoveInnerToTopRefactoring_declare_final;

  public static String MoveInnerToTopRefactoring_descriptor_description;

  public static String MoveInnerToTopRefactoring_descriptor_description_short;

  public static String MoveInnerToTopRefactoring_field_pattern;

  public static String MoveInnerToTopRefactoring_move_to_Top;

  public static String MoveInnerToTopRefactoring_name;

  public static String MoveInnerToTopRefactoring_name_used;

  public static String MoveInnerToTopRefactoring_names_start_lowercase;

  public static String MoveInnerToTopRefactoring_original_pattern;

  public static String MoveInnerToTopRefactoring_parameter_pattern;

  public static String MoveInnerToTopRefactoring_type_exists;

  public static String MoveInnerToTopRefactoring_update_constructor_reference;

  public static String MoveInnerToTopRefactoring_update_type_reference;

  public static String MoveInstanceMethodProcessor_add_moved_method;

  public static String MoveInstanceMethodProcessor_cannot_be_moved;

  public static String MoveInstanceMethodProcessor_checking;

  public static String MoveInstanceMethodProcessor_creating;

  public static String MoveInstanceMethodProcessor_descriptor_description;

  public static String MoveInstanceMethodProcessor_descriptor_description_short;

  public static String MoveInstanceMethodProcessor_inline_inaccurate;

  public static String MoveInstanceMethodProcessor_inline_method_invocation;

  public static String MoveInstanceMethodProcessor_inline_overridden;

  public static String MoveInstanceMethodProcessor_method_already_exists;

  public static String MoveInstanceMethodProcessor_method_name_pattern;

  public static String MoveInstanceMethodProcessor_method_type_clash;

  public static String MoveInstanceMethodProcessor_moved_element_pattern;

  public static String MoveInstanceMethodProcessor_name;

  public static String MoveInstanceMethodProcessor_no_annotation;

  public static String MoveInstanceMethodProcessor_no_binary;

  public static String MoveInstanceMethodProcessor_no_constructors;

  public static String MoveInstanceMethodProcessor_no_generic_targets;

  public static String MoveInstanceMethodProcessor_no_interface;

  public static String MoveInstanceMethodProcessor_no_native_methods;

  public static String MoveInstanceMethodProcessor_no_null_argument;

  public static String MoveInstanceMethodProcessor_no_resolved_target;

  public static String MoveInstanceMethodProcessor_no_static_methods;

  public static String MoveInstanceMethodProcessor_no_synchronized_methods;

  public static String MoveInstanceMethodProcessor_no_type_variables;

  public static String MoveInstanceMethodProcessor_parameter_name_pattern;

  public static String MoveInstanceMethodProcessor_potentially_recursive;

  public static String MoveInstanceMethodProcessor_present_type_parameter_warning;

  public static String MoveInstanceMethodProcessor_refers_enclosing_instances;

  public static String MoveInstanceMethodProcessor_remove_original_method;

  public static String MoveInstanceMethodProcessor_single_implementation;

  public static String MoveInstanceMethodProcessor_target_element_pattern;

  public static String MoveInstanceMethodProcessor_target_name_already_used;

  public static String MoveInstanceMethodProcessor_this_reference;

  public static String MoveInstanceMethodProcessor_uses_super;

  public static String MoveInstanceMethodRefactoring_name;

  public static String MoveMembersRefactoring_accessed_field;

  public static String MoveMembersRefactoring_accessed_method;

  public static String MoveMembersRefactoring_accessed_type;

  public static String MoveMembersRefactoring_addMembers;

  public static String MoveMembersRefactoring_binary;

  public static String MoveMembersRefactoring_checking;

  public static String MoveMembersRefactoring_compile_errors;

  public static String MoveMembersRefactoring_creating;

  public static String MoveMembersRefactoring_deleteMembers;

  public static String MoveMembersRefactoring_dest_binary;

  public static String MoveMembersRefactoring_inside;

  public static String MoveMembersRefactoring_member_will_be_public;

  public static String MoveMembersRefactoring_move_members;

  public static String MoveMembersRefactoring_Move_Members;

  public static String MoveMembersRefactoring_moved_field;

  public static String MoveMembersRefactoring_moved_method;

  public static String MoveMembersRefactoring_moved_type;

  public static String MoveMembersRefactoring_multi_var_fields;

  public static String MoveMembersRefactoring_native;

  public static String MoveMembersRefactoring_not_exist;

  public static String MoveMembersRefactoring_not_found;

  public static String MoveMembersRefactoring_Object;

  public static String MoveMembersRefactoring_only_public_static;

  public static String MoveMembersRefactoring_only_public_static_18;

  public static String MoveMembersRefactoring_read_only;

  public static String MoveMembersRefactoring_referenceUpdate;

  public static String MoveMembersRefactoring_same;

  public static String MoveMembersRefactoring_static_declaration;

  public static String MovePackageChange_move;

  public static String MovePackageFragmentRootChange_move;

  public static String MoveRefactoring_0;

  public static String MoveRefactoring_reorganize_elements;

  public static String MoveRefactoring_scanning_qualified_names;

  public static String MoveStaticMemberAnalyzer_nonStatic;

  public static String MoveStaticMembersProcessor_description_descriptor_short_multi;

  public static String MoveStaticMembersProcessor_descriptor_description_multi;

  public static String MoveStaticMembersProcessor_descriptor_description_single;

  public static String MoveStaticMembersProcessor_target_element_pattern;

  public static String MultiStateCompilationUnitChange_name_pattern;

  public static String OverwriteHelper_0;

  public static String OverwriteHelper_1;

  public static String OverwriteHelper_2;

  public static String OverwriteHelper_3;

  public static String PromoteTempToFieldRefactoring_cannot_promote;

  public static String PromoteTempToFieldRefactoring_declare_final;

  public static String PromoteTempToFieldRefactoring_declare_final_static;

  public static String PromoteTempToFieldRefactoring_declare_static;

  public static String PromoteTempToFieldRefactoring_default_visibility;

  public static String PromoteTempToFieldRefactoring_descriptor_description;

  public static String PromoteTempToFieldRefactoring_descriptor_description_short;

  public static String PromoteTempToFieldRefactoring_editName;

  public static String PromoteTempToFieldRefactoring_exceptions;

  public static String PromoteTempToFieldRefactoring_field_pattern;

  public static String PromoteTempToFieldRefactoring_initialize_constructor;

  public static String PromoteTempToFieldRefactoring_initialize_declaration;

  public static String PromoteTempToFieldRefactoring_initialize_method;

  public static String PromoteTempToFieldRefactoring_interface_methods;

  public static String PromoteTempToFieldRefactoring_method_parameters;

  public static String PromoteTempToFieldRefactoring_name;

  public static String PromoteTempToFieldRefactoring_Name_conflict;

  public static String PromoteTempToFieldRefactoring_Name_conflict_with_field;

  public static String PromoteTempToFieldRefactoring_only_declared_in_methods;

  public static String PromoteTempToFieldRefactoring_original_pattern;

  public static String PromoteTempToFieldRefactoring_select_declaration;

  public static String PromoteTempToFieldRefactoring_uses_type_declared_locally;

  public static String PromoteTempToFieldRefactoring_visibility_pattern;

  public static String PullUpRefactoring_add_abstract_method;

  public static String PullUpRefactoring_add_method_stub;

  public static String PullUpRefactoring_add_override_annotation;

  public static String PullUpRefactoring_calculating_required;

  public static String PullUpRefactoring_category_description;

  public static String PullUpRefactoring_category_name;

  public static String PullUpRefactoring_checking;

  public static String PullUpRefactoring_checking_referenced_elements;

  public static String PullUpRefactoring_descriptor_description;

  public static String PullUpRefactoring_descriptor_description_full;

  public static String PullUpRefactoring_descriptor_description_short;

  public static String PullUpRefactoring_descriptor_description_short_multiple;

  public static String PullUpRefactoring_different_field_type;

  public static String PullUpRefactoring_different_method_return_type;

  public static String PullUpRefactoring_field_cannot_be_accessed;

  public static String PullUpRefactoring_Field_declared_in_class;

  public static String PullUpRefactoring_field_not_accessible;

  public static String PullUpRefactoring_final_fields;

  public static String PullUpRefactoring_incompatible_langauge_constructs;

  public static String PullUpRefactoring_incompatible_language_constructs1;

  public static String PullUpRefactoring_lower_default_visibility;

  public static String PullUpRefactoring_lower_protected_visibility;

  public static String PullUpRefactoring_make_target_abstract;

  public static String PullUpRefactoring_method_cannot_be_accessed;

  public static String PullUpRefactoring_Method_declared_in_class;

  public static String PullUpRefactoring_method_not_accessible;

  public static String PullUpRefactoring_moving_static_method_to_interface;

  public static String PullUPRefactoring_no_all_binary;

  public static String PullUpRefactoring_no_java_lang_Object;

  public static String PullUpRefactoring_non_final_pull_up_to_interface;

  public static String PullUPRefactoring_not_java_lang_object;

  public static String PullUpRefactoring_not_this_type;

  public static String PullUpRefactoring_Pull_Up;

  public static String PullUpRefactoring_Type_declared_in_class;

  public static String PullUpRefactoring_type_not_accessible;

  public static String PullUpRefactoring_Type_variable_not_available;

  public static String PullUpRefactoring_Type_variable2_not_available;

  public static String PullUpRefactoring_Type_variable3_not_available;

  public static String PullUpRefactoring_Type_variables_not_available;

  public static String PushDownRefactoring_calculating_required;

  public static String PushDownRefactoring_category_description;

  public static String PushDownRefactoring_category_name;

  public static String PushDownRefactoring_change_name;

  public static String PushDownRefactoring_check_references;

  public static String PushDownRefactoring_checking;

  public static String PushDownRefactoring_descriptor_description;

  public static String PushDownRefactoring_descriptor_description_full;

  public static String PushDownRefactoring_descriptor_description_short;

  public static String PushDownRefactoring_descriptor_description_short_multi;

  public static String PushDownRefactoring_field_not_accessible;

  public static String PushDownRefactoring_make_abstract;

  public static String PushDownRefactoring_method_not_accessible;

  public static String PushDownRefactoring_name;

  public static String PushDownRefactoring_no_subclasses;

  public static String PushDownRefactoring_pushed_members_pattern;

  public static String PushDownRefactoring_referenced;

  public static String PushDownRefactoring_type_not_accessible;

  public static String QualifiedNameFinder_qualifiedNames_description;

  public static String QualifiedNameFinder_qualifiedNames_name;

  public static String QualifiedNameFinder_update_name;

  public static String QualifiedNameSearchResult_change_name;

  public static String ReadOnlyResourceFinder_0;

  public static String ReadOnlyResourceFinder_1;

  public static String ReadOnlyResourceFinder_2;

  public static String ReadOnlyResourceFinder_3;

  public static String Refactoring_binary;

  public static String Refactoring_not_in_model;

  public static String Refactoring_read_only;

  public static String Refactoring_unknown_structure;

  public static String RefactoringAnalyzeUtil_name_collision;

  public static String RefactoringSearchEngine_binary_match_grouped;

  public static String RefactoringSearchEngine_binary_match_ungrouped;

  public static String RefactoringSearchEngine_inaccurate_match;

  public static String RefactoringSearchEngine_non_cu_matches;

  public static String RefactoringSearchEngine_potential_matches;

  public static String RefactoringSearchEngine_searching_occurrences;

  public static String RefactoringSearchEngine_searching_referenced_fields;

  public static String RefactoringSearchEngine_searching_referenced_methods;

  public static String RefactoringSearchEngine_searching_referenced_types;

  public static String RenameAnalyzeUtil_reference_shadowed;

  public static String RenameAnalyzeUtil_shadows;

  public static String RenameCompilationUnitChange_descriptor_description;

  public static String RenameCompilationUnitChange_descriptor_description_short;

  public static String RenameCompilationUnitChange_name;

  public static String RenameCompilationUnitRefactoring_name;

  public static String RenameCompilationUnitRefactoring_not_parsed;

  public static String RenameCompilationUnitRefactoring_not_parsed_1;

  public static String RenameCompilationUnitRefactoring_same_name;

  public static String RenameEnumConstProcessor_descriptor_description;

  public static String RenameEnumConstProcessor_descriptor_description_short;

  public static String RenameEnumConstRefactoring_another_name;

  public static String RenameEnumConstRefactoring_const_already_defined;

  public static String RenameEnumConstRefactoring_convention;

  public static String RenameEnumConstRefactoring_name;

  public static String RenameFieldProcessor_descriptor_description;

  public static String RenameFieldRefactoring_already_exists;

  public static String RenameFieldRefactoring_another_name;

  public static String RenameFieldRefactoring_another_name2;

  public static String RenameFieldRefactoring_checking;

  public static String RenameFieldRefactoring_declared_in_supertype;

  public static String RenameFieldRefactoring_deleted;

  public static String RenameFieldRefactoring_descriptor_description_short;

  public static String RenameFieldRefactoring_field_already_defined;

  public static String RenameFieldRefactoring_field_already_defined2;

  public static String RenameFieldRefactoring_hiding;

  public static String RenameFieldRefactoring_hiding2;

  public static String RenameFieldRefactoring_name;

  public static String RenameFieldRefactoring_overridden;

  public static String RenameFieldRefactoring_overridden_or_overrides;

  public static String RenameFieldRefactoring_searching;

  public static String RenameFieldRefactoring_setting_rename_getter;

  public static String RenameFieldRefactoring_setting_rename_settter;

  public static String RenameFieldRefactoring_should_start_lowercase;

  public static String RenameFieldRefactoring_should_start_lowercase2;

  public static String RenameFieldRefactoring_Update_field_declaration;

  public static String RenameFieldRefactoring_Update_field_reference;

  public static String RenameFieldRefactoring_Update_getter_occurrence;

  public static String RenameFieldRefactoring_Update_setter_occurrence;

  public static String RenameJavaProjectChange_descriptor_description;

  public static String RenameJavaProjectChange_rename;

  public static String RenameJavaProjectChange_update;

  public static String RenameJavaProjectProcessor_descriptor_description_short;

  public static String RenameJavaProjectProcessor_folder_already_exists;

  public static String RenameJavaProjectRefactoring_already_exists;

  public static String RenameJavaProjectRefactoring_read_only;

  public static String RenameJavaProjectRefactoring_rename;

  public static String RenameLocalVariableProcessor_descriptor_description;

  public static String RenameLocalVariableProcessor_descriptor_description_short;

  public static String RenameMethodInInterfaceRefactoring_already_defined;

  public static String RenameMethodInInterfaceRefactoring_special_case;

  public static String RenameMethodProcessor_change_name;

  public static String RenameMethodProcessor_descriptor_description;

  public static String RenameMethodProcessor_descriptor_description_short;

  public static String RenameMethodProcessor_is_binary;

  public static String RenameMethodRefactoring_deleted;

  public static String RenameMethodRefactoring_name;

  public static String RenameMethodRefactoring_no_binary;

  public static String RenameMethodRefactoring_no_native;

  public static String RenameMethodRefactoring_no_native_1;

  public static String RenameMethodRefactoring_no_read_only;

  public static String RenameMethodRefactoring_not_in_model;

  public static String RenameMethodRefactoring_same_name;

  public static String RenameMethodRefactoring_same_name2;

  public static String RenameMethodRefactoring_taskName_checkingPreconditions;

  public static String RenameMethodRefactoring_taskName_searchingForReferences;

  public static String RenameMethodRefactoring_update_declaration;

  public static String RenameMethodRefactoring_update_occurrence;

  public static String RenameMethodRefactoringContribution_could_not_create;

  public static String RenamePackageChange_checking_change;

  public static String RenamePackageChange_name;

  public static String RenamePackageChange_name_with_subpackages;

  public static String RenamePackageProcessor_descriptor_description;

  public static String RenamePackageProcessor_descriptor_description_short;

  public static String RenamePackageProcessor_rename_subpackages;

  public static String RenamePackageProcessor_subpackage_collides;

  public static String RenamePackageRefactoring_aleady_exists;

  public static String RenamePackageRefactoring_another_name;

  public static String RenamePackageRefactoring_change_name;

  public static String RenamePackageRefactoring_checking;

  public static String RenamePackageRefactoring_contains_type;

  public static String RenamePackageRefactoring_creating_change;

  public static String RenamePackageRefactoring_name;

  public static String RenamePackageRefactoring_package_exists;

  public static String RenamePackageRefactoring_Packagered_only;

  public static String RenamePackageRefactoring_resource_read_only;

  public static String RenamePackageRefactoring_searching;

  public static String RenamePackageRefactoring_searching_text;

  public static String RenamePackageRefactoring_update_imports;

  public static String RenamePackageRefactoring_update_reference;

  public static String RenamePrivateMethodRefactoring_hierarchy_defines;

  public static String RenamePrivateMethodRefactoring_hierarchy_defines2;

  public static String RenamePrivateMethodRefactoring_update;

  public static String RenameResourceChange_does_not_exist;

  public static String RenameSourceFolderChange_descriptor_description;

  public static String RenameSourceFolderChange_descriptor_description_short;

  public static String RenameSourceFolderChange_error_underlying_resource_not_existing;

  public static String RenameSourceFolderChange_rename;

  public static String RenameSourceFolderChange_rename_archive;

  public static String RenameSourceFolderChange_rename_external;

  public static String RenameSourceFolderChange_rename_linked;

  public static String RenameSourceFolderRefactoring_alread_exists;

  public static String RenameSourceFolderRefactoring_already_exists;

  public static String RenameSourceFolderRefactoring_blank;

  public static String RenameSourceFolderRefactoring_invalid_name;

  public static String RenameSourceFolderRefactoring_rename;

  public static String RenameTempRefactoring_changeName;

  public static String RenameTempRefactoring_lowercase;

  public static String RenameTempRefactoring_lowercase2;

  public static String RenameTempRefactoring_must_select_local;

  public static String RenameTempRefactoring_only_in_methods_and_initializers;

  public static String RenameTempRefactoring_only_in_methods_initializers_and_lambda;

  public static String RenameTempRefactoring_rename;

  public static String RenameTypeParameterProcessor_change_name;

  public static String RenameTypeParameterProcessor_descriptor_description;

  public static String RenameTypeParameterProcessor_descriptor_description_short;

  public static String RenameTypeParameterProcessor_name;

  public static String RenameTypeParameterRefactoring_another_name;

  public static String RenameTypeParameterRefactoring_checking;

  public static String RenameTypeParameterRefactoring_class_type_parameter_already_defined;

  public static String RenameTypeParameterRefactoring_deleted;

  public static String RenameTypeParameterRefactoring_method_type_parameter_already_defined;

  public static String RenameTypeParameterRefactoring_searching;

  public static String RenameTypeParameterRefactoring_should_start_lowercase;

  public static String RenameTypeParameterRefactoring_type_parameter_inner_class_clash;

  public static String RenameTypeParameterRefactoring_update_type_parameter_declaration;

  public static String RenameTypeParameterRefactoring_update_type_parameter_reference;

  public static String ReferencesInBinaryContext_binaryRefsNotUpdated;

  public static String RenameTypeProcessor_cannot_rename_fields_same_new_name;

  public static String RenameTypeProcessor_cannot_rename_methods_same_new_name;

  public static String RenameTypeProcessor_change_name;

  public static String RenameTypeProcessor_changeCategory_fields;

  public static String RenameTypeProcessor_changeCategory_fields_description;

  public static String RenameTypeProcessor_changeCategory_local_variables;

  public static String RenameTypeProcessor_changeCategory_local_variables_description;

  public static String RenameTypeProcessor_changeCategory_method;

  public static String RenameTypeProcessor_changeCategory_method_description;

  public static String RenameTypeProcessor_changeCategory_type;

  public static String RenameTypeProcessor_changeCategory_type_description;

  public static String
      RenameTypeProcessor_checking_similarly_named_declarations_refactoring_conditions;

  public static String RenameTypeProcessor_creating_changes;

  public static String RenameTypeProcessor_descriptor_description;

  public static String RenameTypeProcessor_descriptor_description_short;

  public static String RenameTypeProcessor_deselected_method_is_overridden;

  public static String RenameTypeProcessor_progress_current_total;

  public static String ReferencesInBinaryContext_ref_in_binaries_description;

  public static String ReferencesInBinaryContext_ref_in_binaries_description_plural;

  public static String RenameTypeProcessor_renamed_method_is_overridden;

  public static String RenameTypeRefactoring_another_type;

  public static String RenameTypeRefactoring_checking;

  public static String RenameTypeRefactoring_choose_another_name;

  public static String RenameTypeRefactoring_creating_change;

  public static String RenameTypeRefactoring_does_not_exist;

  public static String RenameTypeRefactoring_enclosed;

  public static String RenameTypeRefactoring_enclosed_type_native;

  public static String RenameTypeRefactoring_encloses;

  public static String RenameTypeRefactoring_exists;

  public static String RenameTypeRefactoring_imported;

  public static String RenameTypeRefactoring_local_type;

  public static String RenameTypeRefactoring_member_type;

  public static String RenameTypeRefactoring_member_type_exists;

  public static String RenameTypeRefactoring_name;

  public static String RenameTypeRefactoring_name_conflict1;

  public static String RenameTypeRefactoring_rename_constructor;

  public static String RenameTypeRefactoring_searching;

  public static String RenameTypeRefactoring_searching_text;

  public static String RenameTypeRefactoring_update;

  public static String RenameTypeRefactoring_update_reference;

  public static String RenameTypeRefactoring_will_not_rename;

  public static String RenameVirtualMethodRefactoring_hierarchy_declares1;

  public static String RenameVirtualMethodRefactoring_hierarchy_declares2;

  public static String RenameVirtualMethodRefactoring_requieres_renaming_native;

  public static String ReorgPolicy_copy;

  public static String ReorgPolicy_copy_package;

  public static String ReorgPolicy_copy_source_folder;

  public static String ReorgPolicy_move;

  public static String ReorgPolicy_move_members;

  public static String ReorgPolicy_move_package;

  public static String ReorgPolicy_move_source_folder;

  public static String ReorgPolicyFactory_archive;

  public static String ReorgPolicyFactory_cannot;

  public static String ReorgPolicyFactory_cannot_modify;

  public static String ReorgPolicyFactory_cannot_move_package_to_parent;

  public static String ReorgPolicyFactory_cannot_move_source_to_parent;

  public static String ReorgPolicyFactory_cannot1;

  public static String ReorgPolicyFactory_copy_compilation_unit;

  public static String ReorgPolicyFactory_copy_compilation_units;

  public static String ReorgPolicyFactory_copy_description_plural;

  public static String ReorgPolicyFactory_copy_description_singular;

  public static String ReorgPolicyFactory_copy_elements_header_singular;

  public static String ReorgPolicyFactory_copy_elements_header_plural;

  public static String ReorgPolicyFactory_copy_elements_plural;

  public static String ReorgPolicyFactory_copy_elements_singular;

  public static String ReorgPolicyFactory_copy_field;

  public static String ReorgPolicyFactory_copy_fields;

  public static String ReorgPolicyFactory_copy_file;

  public static String ReorgPolicyFactory_copy_files;

  public static String ReorgPolicyFactory_copy_folder;

  public static String ReorgPolicyFactory_copy_folders;

  public static String ReorgPolicyFactory_copy_header_singular;

  public static String ReorgPolicyFactory_copy_header_plural;

  public static String ReorgPolicyFactory_copy_import;

  public static String ReorgPolicyFactory_copy_import_containers;

  public static String ReorgPolicyFactory_copy_import_section;

  public static String ReorgPolicyFactory_copy_imports;

  public static String ReorgPolicyFactory_copy_initializer;

  public static String ReorgPolicyFactory_copy_initializers;

  public static String ReorgPolicyFactory_copy_method;

  public static String ReorgPolicyFactory_copy_methods;

  public static String ReorgPolicyFactory_copy_package;

  public static String ReorgPolicyFactory_copy_package_declarations;

  public static String ReorgPolicyFactory_copy_package_singular;

  public static String ReorgPolicyFactory_copy_packages_header_singular;

  public static String ReorgPolicyFactory_copy_packages_header_plural;

  public static String ReorgPolicyFactory_copy_packages_plural;

  public static String ReorgPolicyFactory_copy_roots_header_singular;

  public static String ReorgPolicyFactory_copy_roots_header_plural;

  public static String ReorgPolicyFactory_copy_roots_plural;

  public static String ReorgPolicyFactory_copy_roots_singular;

  public static String ReorgPolicyFactory_copy_type;

  public static String ReorgPolicyFactory_copy_types;

  public static String ReorgPolicyFactory_doesnotexist0;

  public static String ReorgPolicyFactory_doesnotexist1;

  public static String ReorgPolicyFactory_element2parent;

  public static String ReorgPolicyFactory_external;

  public static String ReorgPolicyFactory_inaccessible;

  public static String ReorgPolicyFactory_inconsistent;

  public static String ReorgPolicyFactory_invalidDestinationKind;

  public static String ReorgPolicyFactory_jmodel;

  public static String ReorgPolicyFactory_linked;

  public static String ReorgPolicyFactory_move_compilation_unit;

  public static String ReorgPolicyFactory_move_compilation_units;

  public static String ReorgPolicyFactory_move_description_plural;

  public static String ReorgPolicyFactory_move_description_singular;

  public static String ReorgPolicyFactory_move_elements_header_singular;

  public static String ReorgPolicyFactory_move_elements_header_plural;

  public static String ReorgPolicyFactory_move_elements_plural;

  public static String ReorgPolicyFactory_move_elements_singular;

  public static String ReorgPolicyFactory_move_field;

  public static String ReorgPolicyFactory_move_fields;

  public static String ReorgPolicyFactory_move_file;

  public static String ReorgPolicyFactory_move_files;

  public static String ReorgPolicyFactory_move_folder;

  public static String ReorgPolicyFactory_move_folders;

  public static String ReorgPolicyFactory_move_header_singular;

  public static String ReorgPolicyFactory_move_header_plural;

  public static String ReorgPolicyFactory_move_import_containers;

  public static String ReorgPolicyFactory_move_import_declaration;

  public static String ReorgPolicyFactory_move_import_declarations;

  public static String ReorgPolicyFactory_move_import_section;

  public static String ReorgPolicyFactory_move_initializer;

  public static String ReorgPolicyFactory_move_initializers;

  public static String ReorgPolicyFactory_move_method;

  public static String ReorgPolicyFactory_move_methods;

  public static String ReorgPolicyFactory_move_package_declaration;

  public static String ReorgPolicyFactory_move_package_declarations;

  public static String ReorgPolicyFactory_move_packages_header_singular;

  public static String ReorgPolicyFactory_move_packages_header_plural;

  public static String ReorgPolicyFactory_move_packages_plural;

  public static String ReorgPolicyFactory_move_packages_singular;

  public static String ReorgPolicyFactory_move_roots_header_singular;

  public static String ReorgPolicyFactory_move_roots_header_plural;

  public static String ReorgPolicyFactory_move_roots_plural;

  public static String ReorgPolicyFactory_move_roots_singular;

  public static String ReorgPolicyFactory_move_type;

  public static String ReorgPolicyFactory_move_types;

  public static String ReorgPolicyFactory_no_java_element;

  public static String ReorgPolicyFactory_no_resource;

  public static String ReorgPolicyFactory_noCopying;

  public static String ReorgPolicyFactory_noJavaUpdates;

  public static String ReorgPolicyFactory_noMoving;

  public static String ReorgPolicyFactory_not_this_resource;

  public static String ReorgPolicyFactory_package_decl;

  public static String ReorgPolicyFactory_package2parent;

  public static String ReorgPolicyFactory_packages;

  public static String ReorgPolicyFactory_parent;

  public static String ReorgPolicyFactory_phantom;

  public static String ReorgPolicyFactory_readonly;

  public static String ReorgPolicyFactory_src2nosrc;

  public static String ReorgPolicyFactory_src2proj;

  public static String ReorgPolicyFactory_src2writable;

  public static String ReorgPolicyFactory_structure;

  public static String ReorgUtils_0;

  public static String ReorgUtils_1;

  public static String ReorgUtils_10;

  public static String ReorgUtils_11;

  public static String ReorgUtils_12;

  public static String ReorgUtils_13;

  public static String ReorgUtils_14;

  public static String ReorgUtils_15;

  public static String ReorgUtils_16;

  public static String ReorgUtils_17;

  public static String ReorgUtils_18;

  public static String ReorgUtils_2;

  public static String ReorgUtils_20;

  public static String ReorgUtils_21;

  public static String ReorgUtils_3;

  public static String ReorgUtils_4;

  public static String ReorgUtils_5;

  public static String ReorgUtils_6;

  public static String ReorgUtils_7;

  public static String ReorgUtils_8;

  public static String ReorgUtils_9;

  public static String ReplaceInvocationsRefactoring_cannot_find_method_declaration;

  public static String ReplaceInvocationsRefactoring_cannot_replace_in_binary;

  public static String ReplaceInvocationsRefactoring_change_name;

  public static String ReplaceInvocationsRefactoring_descriptor_description;

  public static String ReplaceInvocationsRefactoring_descriptor_description_short;

  public static String ReplaceInvocationsRefactoring_name;

  public static String ReplaceInvocationsRefactoring_original_pattern;

  public static String ReplaceInvocationsRefactoring_replace_references;

  public static String ReplaceInvocationsRefactoring_select_method_to_apply;

  public static String SelfEncapsulateField_AccessAnalyzer_cannot_convert_postfix_expression;

  public static String SelfEncapsulateField_AccessAnalyzer_encapsulate_postfix_access;

  public static String SelfEncapsulateField_AccessAnalyzer_encapsulate_prefix_access;

  public static String SelfEncapsulateField_AccessAnalyzer_encapsulate_read_access;

  public static String SelfEncapsulateField_AccessAnalyzer_encapsulate_write_access;

  public static String SelfEncapsulateField_add_getter;

  public static String SelfEncapsulateField_add_setter;

  public static String SelfEncapsulateField_analyzing;

  public static String SelfEncapsulateField_cannot_analyze_selected_field;

  public static String SelfEncapsulateField_change_visibility;

  public static String SelfEncapsulateField_checking_preconditions;

  public static String SelfEncapsulateField_compiler_errors_field;

  public static String SelfEncapsulateField_compiler_errors_update;

  public static String SelfEncapsulateField_create_changes;

  public static String SelfEncapsulateField_default_visibility;

  public static String SelfEncapsulateField_descriptor_description_short;

  public static String SelfEncapsulateField_do_not_use_accessors;

  public static String SelfEncapsulateField_generate_comments;

  public static String SelfEncapsulateField_getter_pattern;

  public static String SelfEncapsulateField_method_exists;

  public static String SelfEncapsulateField_name;

  public static String SelfEncapsulateField_original_pattern;

  public static String SelfEncapsulateField_searching_for_cunits;

  public static String SelfEncapsulateField_setter_pattern;

  public static String SelfEncapsulateField_type_not_resolveable;

  public static String SelfEncapsulateField_use_accessors;

  public static String SelfEncapsulateField_visibility_pattern;

  public static String SelfEncapsulateFieldRefactoring_descriptor_description;

  public static String SelfEncapsulateFieldRefactoring_methoddoesnotexist_status_fatalError;

  public static String SelfEncapsulateFieldRefactoring_nonstatic_method_but_static_field;

  public static String SelfEncapsulateFieldRefactoring_nosuchmethod_status_fatalError;

  public static String SelfEncapsulateFieldRefactoring_static_method_but_nonstatic_field;

  public static String SourceCreationOperation_creating_source_folder;

  public static String StatementAnalyzer_beginning_of_selection;

  public static String StatementAnalyzer_catch_argument;

  public static String StatementAnalyzer_do_body_expression;

  public static String StatementAnalyzer_doesNotCover;

  public static String StatementAnalyzer_end_of_selection;

  public static String StatementAnalyzer_for_expression_updater;

  public static String StatementAnalyzer_for_initializer_expression;

  public static String StatementAnalyzer_for_updater_body;

  public static String StatementAnalyzer_switch_statement;

  public static String StatementAnalyzer_synchronized_statement;

  public static String StatementAnalyzer_try_statement;

  public static String StatementAnalyzer_while_expression_body;

  public static String StubCreationOperation_creating_type_stubs;

  public static String SuperTypeRefactoringProcessor_category_description;

  public static String SuperTypeRefactoringProcessor_category_name;

  public static String SuperTypeRefactoringProcessor_creating;

  public static String SuperTypeRefactoringProcessor_update_type_occurrence;

  public static String SuperTypeRefactoringProcessor_use_in_instanceof_setting;

  public static String SuperTypeRefactoringProcessor_user_supertype_setting;

  public static String SurroundWithTryCatchAnalyzer_cannotHandleSuper;

  public static String SurroundWithTryCatchAnalyzer_cannotHandleThis;

  public static String SurroundWithTryCatchAnalyzer_compile_errors;

  public static String SurroundWithTryCatchAnalyzer_doesNotContain;

  public static String SurroundWithTryCatchAnalyzer_doesNotCover;

  public static String SurroundWithTryCatchAnalyzer_onlyStatements;

  public static String SurroundWithTryCatchRefactoring_name;

  public static String SurroundWithTryCatchRefactoring_notMultipleexceptions;

  public static String TargetProvider_cannot_local_method_in_binary;

  public static String TargetProvider_inaccurate_match;

  public static String TargetProvider_method_declaration_not_unique;

  public static String TextMatchUpdater_searching;

  public static String TextMatchUpdater_textualMatches_description;

  public static String TextMatchUpdater_textualMatches_name;

  public static String TextMatchUpdater_update;

  public static String TypeContextChecker_ambiguous;

  public static String TypeContextChecker_couldNotResolveType;

  public static String TypeContextChecker_invalid_return_type;

  public static String TypeContextChecker_invalid_return_type_syntax;

  public static String TypeContextChecker_invalid_type_name;

  public static String TypeContextChecker_invalid_type_syntax;

  public static String TypeContextChecker_no_vararg_below_50;

  public static String TypeContextChecker_not_unique;

  public static String TypeContextChecker_parameter_type;

  public static String TypeContextChecker_return_type_not_empty;

  public static String UndoDeleteResourceChange_already_exists;

  public static String UndoDeleteResourceChange_cannot_restore;

  public static String UndoDeleteResourceChange_change_name;

  public static String UseSuperTypeProcessor_checking;

  public static String UseSuperTypeProcessor_creating;

  public static String UseSuperTypeProcessor_descriptor_description;

  public static String UseSuperTypeProcessor_descriptor_description_short;

  public static String UseSuperTypeProcessor_internal_error;

  public static String UseSuperTypeProcessor_name;

  public static String UseSuperTypeProcessor_refactored_element_pattern;

  public static String UseSupertypeWherePossibleRefactoring_name;

  public static String ChangeSignatureRefactoring_lambda_expression;

  static {
    NLS.initializeMessages(BUNDLE_NAME, RefactoringCoreMessages.class);
  }

  private RefactoringCoreMessages() {
    // Do not instantiate
  }
}
