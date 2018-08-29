/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation Benjamin Muskalla
 * <b.muskalla@gmx.net> - [quick fix] Quick fix for missing synchronized modifier -
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=245250 Billy Huang <billyhuang31@gmail.com> -
 * [quick assist] concatenate/merge string literals - https://bugs.eclipse.org/77632 Lukas Hanke
 * <hanke@yatta.de> - Bug 241696 [quick fix] quickfix to iterate over a collection -
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=241696
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.correction;

import org.eclipse.osgi.util.NLS;

/** Helper class to get NLSed messages. */
public final class CorrectionMessages extends NLS {

  private static final String BUNDLE_NAME = CorrectionMessages.class.getName();

  private CorrectionMessages() {
    // Do not instantiate
  }

  public static String FixCorrectionProposal_WarningAdditionalProposalInfo;
  public static String JavadocTagsSubProcessor_document_exception_description;
  public static String JavadocTagsSubProcessor_document_parameter_description;
  public static String JavadocTagsSubProcessor_document_type_parameter_description;
  public static String LocalCorrectionsSubProcessor_renaming_duplicate_method;
  public static String LocalCorrectionsSubProcessor_replacefieldaccesswithmethod_description;
  public static String ModifierCorrectionSubProcessor_addstatic_description;
  public static String ModifierCorrectionSubProcessor_addstatictoparenttype_description;
  public static String ModifierCorrectionSubProcessor_addsynchronized_description;
  public static String ModifierCorrectionSubProcessor_changefieldmodifiertononstatic_description;
  public static String ModifierCorrectionSubProcessor_changemodifiertostaticfinal_description;
  public static String ModifierCorrectionSubProcessor_changemodifiertodefault_description;
  public static String ModifierCorrectionSubProcessor_overrides_deprecated_description;
  public static String ModifierCorrectionSubProcessor_remove_override;
  public static String ModifierCorrectionSubProcessor_removefinal_description;
  public static String ModifierCorrectionSubProcessor_removevolatile_description;
  public static String QuickAssistProcessor_convert_anonym_to_nested;
  public static String QuickAssistProcessor_convert_local_to_field_description;
  public static String QuickAssistProcessor_convert_to_indexed_for_loop;
  public static String QuickAssistProcessor_convert_to_iterator_for_loop;
  public static String QuickAssistProcessor_generate_enhanced_for_loop;
  public static String QuickAssistProcessor_generate_iterator_for_loop;
  public static String QuickAssistProcessor_generate_for_loop;
  public static String QuickAssistProcessor_generate_index_for_loop;
  public static String QuickAssistProcessor_convert_to_message_format;
  public static String QuickAssistProcessor_convert_to_multiple_singletype_catch_blocks;
  public static String QuickAssistProcessor_convert_to_single_multicatch_block;
  public static String QuickAssistProcessor_convert_to_string_buffer_description;
  public static String QuickAssistProcessor_exceptiontothrows_description;
  public static String QuickAssistProcessor_extract_to_constant_description;
  public static String QuickAssistProcessor_inline_local_description;
  public static String QuickAssistProcessor_name_extension_from_class;
  public static String QuickAssistProcessor_name_extension_from_interface;
  public static String SerialVersionHashOperation_computing_id;
  public static String SerialVersionHashOperation_error_classnotfound;
  public static String SerialVersionHashOperation_save_caption;
  public static String SerialVersionHashOperation_save_message;
  public static String SerialVersionDefaultProposal_message_default_info;
  public static String SerialVersionHashProposal_message_generated_info;
  public static String SerialVersionHashOperation_dialog_error_caption;
  public static String SerialVersionHashOperation_dialog_error_message;
  public static String CorrectPackageDeclarationProposal_name;
  public static String CorrectPackageDeclarationProposal_remove_description;
  public static String CorrectPackageDeclarationProposal_add_description;
  public static String CorrectPackageDeclarationProposal_change_description;

  public static String ChangeCorrectionProposal_error_title;
  public static String ChangeCorrectionProposal_error_message;
  public static String ChangeCorrectionProposal_name_with_shortcut;
  public static String CUCorrectionProposal_error_title;
  public static String CUCorrectionProposal_error_message;
  public static String ReorgCorrectionsSubProcessor_renametype_description;
  public static String ReorgCorrectionsSubProcessor_renamecu_description;
  public static String ReorgCorrectionsSubProcessor_movecu_default_description;
  public static String ReorgCorrectionsSubProcessor_movecu_description;
  public static String ReorgCorrectionsSubProcessor_organizeimports_description;
  public static String ReorgCorrectionsSubProcessor_addcp_project_description;
  public static String ReorgCorrectionsSubProcessor_addcp_archive_description;
  public static String ReorgCorrectionsSubProcessor_addcp_classfolder_description;
  public static String ReorgCorrectionsSubProcessor_change_project_compliance_description;
  public static String ReorgCorrectionsSubProcessor_change_workspace_compliance_description;
  public static String ReorgCorrectionsSubProcessor_addcp_variable_description;
  public static String ReorgCorrectionsSubProcessor_addcp_library_description;
  public static String LocalCorrectionsSubProcessor_surroundwith_trycatch_description;
  public static String LocalCorrectionsSubProcessor_surroundwith_trymulticatch_description;
  public static String LocalCorrectionsSubProcessor_add_default_case_description;
  public static String LocalCorrectionsSubProcessor_add_missing_cases_description;
  public static String LocalCorrectionsSubProcessor_addthrows_description;
  public static String ClasspathFixProcessorDescriptor_error_processing_processors;
  public static String LocalCorrectionsSubProcessor_addadditionalcatch_description;
  public static String LocalCorrectionsSubProcessor_addadditionalmulticatch_description;
  public static String LocalCorrectionsSubProcessor_addexceptiontoexistingcatch_description;
  public static String LocalCorrectionsSubProcessor_addexceptionstoexistingcatch_description;
  public static String LocalCorrectionsSubProcessor_unnecessaryinstanceof_description;
  public static String LocalCorrectionsSubProcessor_unnecessarythrow_description;
  public static String LocalCorrectionsSubProcessor_classtointerface_description;
  public static String LocalCorrectionsSubProcessor_externalizestrings_description;
  public static String LocalCorrectionsSubProcessor_extendstoimplements_description;
  public static String LocalCorrectionsSubProcessor_setparenteses_bitop_description;
  public static String LocalCorrectionsSubProcessor_uninitializedvariable_description;
  public static String LocalCorrectionsSubProcessor_removesemicolon_description;
  public static String LocalCorrectionsSubProcessor_removeunreachablecode_description;
  public static String
      LocalCorrectionsSubProcessor_removeunreachablecode_including_condition_description;
  public static String LocalCorrectionsSubProcessor_removeelse_description;
  public static String LocalCorrectionsSubProcessor_hiding_local_label;
  public static String LocalCorrectionsSubProcessor_hiding_field_label;
  public static String LocalCorrectionsSubProcessor_rename_var_label;
  public static String LocalCorrectionsSubProcessor_hiding_argument_label;
  public static String LocalCorrectionsSubProcessor_setparenteses_description;
  public static String LocalCorrectionsSubProcessor_setparenteses_instanceof_description;
  public static String LocalCorrectionsSubProcessor_InferGenericTypeArguments;
  public static String LocalCorrectionsSubProcessor_InferGenericTypeArguments_description;
  public static String TypeMismatchSubProcessor_addcast_description;
  public static String TypeMismatchSubProcessor_changecast_description;
  public static String TypeMismatchSubProcessor_changereturntype_description;
  public static String TypeMismatchSubProcessor_changereturnofoverridden_description;
  public static String TypeMismatchSubProcessor_changereturnofimplemented_description;
  public static String TypeMismatchSubProcessor_create_loop_variable_description;
  public static String TypeMismatchSubProcessor_removeexceptions_description;
  public static String TypeMismatchSubProcessor_addexceptions_description;
  public static String TypeMismatchSubProcessor_incompatible_for_each_type_description;
  public static String TypeMismatchSubProcessor_insertnullcheck_description;
  public static String RemoveDeclarationCorrectionProposal_removeunusedfield_description;
  public static String RemoveDeclarationCorrectionProposal_removeunusedmethod_description;
  public static String RemoveDeclarationCorrectionProposal_removeunusedconstructor_description;
  public static String RemoveDeclarationCorrectionProposal_removeunusedtype_description;
  public static String RemoveDeclarationCorrectionProposal_removeunusedvar_description;
  public static String RenameRefactoringProposal_additionalInfo;
  public static String RenameRefactoringProposal_name;

  public static String ModifierCorrectionSubProcessor_changemodifiertoabstract_description;
  public static String ModifierCorrectionSubProcessor_changemodifiertostatic_description;
  public static String ModifierCorrectionSubProcessor_changemodifiertononstatic_description;
  public static String ModifierCorrectionSubProcessor_changemodifiertofinal_description;
  public static String ModifierCorrectionSubProcessor_changemodifiertononfinal_description;
  public static String ModifierCorrectionSubProcessor_changevisibility_description;
  public static String ModifierCorrectionSubProcessor_removeabstract_description;
  public static String ModifierCorrectionSubProcessor_removebody_description;
  public static String ModifierCorrectionSubProcessor_default;
  public static String ModifierCorrectionSubProcessor_addabstract_description;
  public static String ModifierCorrectionSubProcessor_removenative_description;
  public static String ModifierCorrectionSubProcessor_addmissingbody_description;
  public static String ModifierCorrectionSubProcessor_changemethodtononfinal_description;
  public static String ModifierCorrectionSubProcessor_changeoverriddenvisibility_description;
  public static String ModifierCorrectionSubProcessor_changemethodvisibility_description;
  public static String ModifierCorrectionSubProcessor_changemethodtononstatic_description;
  public static String ModifierCorrectionSubProcessor_removeinvalidmodifiers_description;
  public static String ReturnTypeSubProcessor_constrnamemethod_description;
  public static String ReturnTypeSubProcessor_voidmethodreturns_description;
  public static String ReturnTypeSubProcessor_removereturn_description;
  public static String ReturnTypeSubProcessor_missingreturntype_description;
  public static String ReturnTypeSubProcessor_wrongconstructorname_description;
  public static String ReturnTypeSubProcessor_changetovoid_description;
  public static String MissingReturnTypeCorrectionProposal_addreturnstatement_description;
  public static String MissingReturnTypeCorrectionProposal_changereturnstatement_description;
  public static String TypeArgumentMismatchSubProcessor_removeTypeArguments;
  public static String UnimplementedMethodsCorrectionProposal_description;
  public static String UnimplementedMethodsCorrectionProposal_enum_info;
  public static String UnimplementedMethodsCorrectionProposal_info_singular;
  public static String UnimplementedMethodsCorrectionProposal_info_plural;

  public static String UnimplementedCodeFix_DependenciesErrorMessage;
  public static String UnimplementedCodeFix_DependenciesStatusMessage;
  public static String UnimplementedCodeFix_MakeAbstractFix_label;
  public static String UnimplementedCodeFix_TextEditGroup_label;

  public static String UnresolvedElementsSubProcessor_swaparguments_description;
  public static String UnresolvedElementsSubProcessor_add_static_import_description;
  public static String UnresolvedElementsSubProcessor_addargumentcast_description;
  public static String UnresolvedElementsSubProcessor_changemethod_description;
  public static String UnresolvedElementsSubProcessor_changetoouter_description;
  public static String UnresolvedElementsSubProcessor_changetomethod_description;
  public static String UnresolvedElementsSubProcessor_create_loop_variable_description;
  public static String UnresolvedElementsSubProcessor_createmethod_description;
  public static String UnresolvedElementsSubProcessor_createmethod_other_description;
  public static String UnresolvedElementsSubProcessor_createconstructor_description;
  public static String UnresolvedElementsSubProcessor_changetype_description;
  public static String UnresolvedElementsSubProcessor_changetype_nopack_description;
  public static String UnresolvedElementsSubProcessor_importtype_description;
  public static String UnresolvedElementsSubProcessor_changevariable_description;
  public static String UnresolvedElementsSubProcessor_createfield_description;
  public static String UnresolvedElementsSubProcessor_createfield_other_description;
  public static String UnresolvedElementsSubProcessor_createlocal_description;
  public static String UnresolvedElementsSubProcessor_createparameter_description;
  public static String UnresolvedElementsSubProcessor_createconst_description;
  public static String UnresolvedElementsSubProcessor_createenum_description;
  public static String UnresolvedElementsSubProcessor_createconst_other_description;
  public static String UnresolvedElementsSubProcessor_removestatement_description;
  public static String UnresolvedElementsSubProcessor_changeparamsignature_description;
  public static String UnresolvedElementsSubProcessor_changemethodtargetcast_description;
  public static String UnresolvedElementsSubProcessor_changeparamsignature_constr_description;
  public static String UnresolvedElementsSubProcessor_swapparams_description;
  public static String UnresolvedElementsSubProcessor_swapparams_constr_description;
  public static String UnresolvedElementsSubProcessor_removeparam_description;
  public static String UnresolvedElementsSubProcessor_removeparams_description;
  public static String UnresolvedElementsSubProcessor_removeparam_constr_description;
  public static String UnresolvedElementsSubProcessor_removeparams_constr_description;
  public static String UnresolvedElementsSubProcessor_addargument_description;
  public static String UnresolvedElementsSubProcessor_addarguments_description;
  public static String UnresolvedElementsSubProcessor_removeargument_description;
  public static String UnresolvedElementsSubProcessor_removearguments_description;
  public static String UnresolvedElementsSubProcessor_addparam_description;
  public static String UnresolvedElementsSubProcessor_addparams_description;
  public static String UnresolvedElementsSubProcessor_addparam_constr_description;
  public static String UnresolvedElementsSubProcessor_addparams_constr_description;
  public static String UnresolvedElementsSubProcessor_importexplicit_description;
  public static String UnresolvedElementsSubProcessor_missingcastbrackets_description;
  public static String UnresolvedElementsSubProcessor_methodtargetcast2_description;
  public static String UnresolvedElementsSubProcessor_changemethodtargetcast2_description;
  public static String UnresolvedElementsSubProcessor_methodtargetcast_description;
  public static String UnresolvedElementsSubProcessor_arraychangetomethod_description;
  public static String UnresolvedElementsSubProcessor_arraychangetolength_description;
  public static String UnresolvedElementsSubProcessor_addnewkeyword_description;
  public static String JavadocTagsSubProcessor_addjavadoc_method_description;
  public static String JavadocTagsSubProcessor_addjavadoc_type_description;
  public static String JavadocTagsSubProcessor_addjavadoc_field_description;
  public static String JavadocTagsSubProcessor_addjavadoc_paramtag_description;
  public static String JavadocTagsSubProcessor_addjavadoc_throwstag_description;
  public static String JavadocTagsSubProcessor_addjavadoc_returntag_description;
  public static String JavadocTagsSubProcessor_addjavadoc_enumconst_description;
  public static String JavadocTagsSubProcessor_addjavadoc_allmissing_description;
  public static String JavadocTagsSubProcessor_qualifylinktoinner_description;
  public static String JavadocTagsSubProcessor_removetag_description;
  public static String NoCorrectionProposal_description;
  public static String MarkerResolutionProposal_additionaldesc;
  public static String NewCUCompletionUsingWizardProposal_createclass_description;
  public static String NewCUCompletionUsingWizardProposal_createenum_description;
  public static String NewCUCompletionUsingWizardProposal_createclass_inpackage_description;
  public static String NewCUCompletionUsingWizardProposal_createinnerclass_description;
  public static String NewCUCompletionUsingWizardProposal_createinnerenum_description;
  public static String NewCUCompletionUsingWizardProposal_createannotation_description;
  public static String NewCUCompletionUsingWizardProposal_createinnerclass_intype_description;
  public static String NewCUCompletionUsingWizardProposal_createinnerenum_intype_description;
  public static String NewCUCompletionUsingWizardProposal_createinterface_description;
  public static String NewCUCompletionUsingWizardProposal_createinterface_inpackage_description;
  public static String NewCUCompletionUsingWizardProposal_createinnerinterface_description;
  public static String NewCUCompletionUsingWizardProposal_createenum_inpackage_description;
  public static String NewCUCompletionUsingWizardProposal_createinnerannotation_description;
  public static String NewCUCompletionUsingWizardProposal_createinnerinterface_intype_description;
  public static String NewCUCompletionUsingWizardProposal_createinnerannotation_intype_description;
  public static String NewCUCompletionUsingWizardProposal_createannotation_inpackage_description;
  public static String NewCUCompletionUsingWizardProposal_createclass_info;
  public static String NewCUCompletionUsingWizardProposal_createenum_info;
  public static String NewCUCompletionUsingWizardProposal_createinterface_info;
  public static String NewCUCompletionUsingWizardProposal_createannotation_info;
  public static String ConstructorFromSuperclassProposal_description;
  public static String AssignToVariableAssistProposal_assigntolocal_description;
  public static String AssignToVariableAssistProposal_assigntofield_description;
  public static String AssignToVariableAssistProposal_assignparamtofield_description;
  public static String QuickAssistProcessor_catchclausetothrows_description;
  public static String QuickAssistProcessor_change_lambda_body_to_block;
  public static String QuickAssistProcessor_change_lambda_body_to_expression;
  public static String QuickAssistProcessor_removecatchclause_description;
  public static String QuickAssistProcessor_removeexception_description;
  public static String QuickAssistProcessor_unwrap_ifstatement;
  public static String QuickAssistProcessor_unwrap_whilestatement;
  public static String QuickAssistProcessor_unwrap_forstatement;
  public static String QuickAssistProcessor_unwrap_dostatement;
  public static String QuickAssistProcessor_unwrap_trystatement;
  public static String QuickAssistProcessor_unwrap_anonymous;
  public static String QuickAssistProcessor_unwrap_block;
  public static String QuickAssistProcessor_unwrap_labeledstatement;
  public static String QuickAssistProcessor_unwrap_methodinvocation;
  public static String QuickAssistProcessor_unwrap_synchronizedstatement;
  public static String QuickAssistProcessor_splitdeclaration_description;
  public static String QuickAssistProcessor_joindeclaration_description;
  public static String QuickAssistProcessor_addfinallyblock_description;
  public static String QuickAssistProcessor_addelseblock_description;
  public static String QuickAssistProcessor_replacethenwithblock_description;
  public static String QuickAssistProcessor_replaceelsewithblock_description;
  public static String QuickAssistProcessor_replacethenelsewithblock_description;
  public static String QuickAssistProcessor_replacebodywithblock_description;
  public static String QuickAssistProcessor_invertequals_description;
  public static String QuickAssistProcessor_typetoarrayInitializer_description;
  public static String QuickAssistProcessor_createmethodinsuper_description;
  public static String LinkedNamesAssistProposal_proposalinfo;
  public static String LinkedNamesAssistProposal_description;
  public static String QuickTemplateProcessor_surround_label;
  public static String NewCUCompletionUsingWizardProposal_dialogtitle;
  public static String NewCUCompletionUsingWizardProposal_tooltip_enclosingtype;
  public static String NewCUCompletionUsingWizardProposal_tooltip_package;

  public static String JavaCorrectionProcessor_addquote_description;
  public static String JavaCorrectionProcessor_error_quickfix_message;
  public static String JavaCorrectionProcessor_error_status;
  public static String JavaCorrectionProcessor_error_quickassist_message;
  public static String JavaCorrectionProcessor_go_to_closest_using_menu;
  public static String JavaCorrectionProcessor_go_to_closest_using_key;
  public static String JavaCorrectionProcessor_go_to_original_using_menu;
  public static String JavaCorrectionProcessor_go_to_original_using_key;

  public static String TaskMarkerProposal_description;
  public static String TypeChangeCompletionProposal_field_name;
  public static String TypeChangeCompletionProposal_variable_name;
  public static String TypeChangeCompletionProposal_param_name;
  public static String TypeChangeCompletionProposal_method_name;
  public static String ImplementInterfaceProposal_name;
  public static String AddUnimplementedMethodsOperation_AddMissingMethod_group;
  public static String AdvancedQuickAssistProcessor_convertToIfReturn;
  public static String AdvancedQuickAssistProcessor_combineSelectedStrings;
  public static String AdvancedQuickAssistProcessor_convertToIfElse_description;
  public static String AdvancedQuickAssistProcessor_inverseIf_description;
  public static String AdvancedQuickAssistProcessor_inverseBooleanVariable;
  public static String AdvancedQuickAssistProcessor_castAndAssign;
  public static String AdvancedQuickAssistProcessor_pullNegationUp;
  public static String AdvancedQuickAssistProcessor_joinIfSequence;
  public static String AdvancedQuickAssistProcessor_pickSelectedString;
  public static String AdvancedQuickAssistProcessor_negatedVariableName;
  public static String AdvancedQuickAssistProcessor_pushNegationDown;
  public static String AdvancedQuickAssistProcessor_putConditionalExpressionInParentheses;
  public static String AdvancedQuickAssistProcessor_convertSwitchToIf;
  public static String AdvancedQuickAssistProcessor_convertSwitchToIf_preserveNPE;
  public static String AdvancedQuickAssistProcessor_convertIfElseToSwitch;
  public static String AdvancedQuickAssistProcessor_convertIfElseToSwitch_handleNullArg;
  public static String AdvancedQuickAssistProcessor_inverseIfContinue_description;
  public static String AdvancedQuickAssistProcessor_inverseIfToContinue_description;
  public static String AdvancedQuickAssistProcessor_exchangeInnerAndOuterIfConditions_description;
  public static String AdvancedQuickAssistProcessor_inverseConditions_description;
  public static String AdvancedQuickAssistProcessor_inverseConditionalExpression_description;
  public static String AdvancedQuickAssistProcessor_replaceIfWithConditional;
  public static String AdvancedQuickAssistProcessor_replaceConditionalWithIf;
  public static String AdvancedQuickAssistProcessor_joinWithOuter_description;
  public static String AdvancedQuickAssistProcessor_joinWithInner_description;
  public static String AdvancedQuickAssistProcessor_splitAndCondition_description;
  public static String AdvancedQuickAssistProcessor_joinWithOr_description;
  public static String AdvancedQuickAssistProcessor_splitOrCondition_description;
  public static String AdvancedQuickAssistProcessor_exchangeOperands_description;
  public static String AddTypeParameterProposal_method_label;
  public static String AddTypeParameterProposal_type_label;

  static {
    NLS.initializeMessages(BUNDLE_NAME, CorrectionMessages.class);
  }

  public static String LocalCorrectionsSubProcessor_externalizestrings_additional_info;
  public static String LocalCorrectionsSubProcessor_generate_hashCode_equals_additional_info;
  public static String LocalCorrectionsSubProcessor_generate_hashCode_equals_description;
  public static String AssignToVariableAssistProposal_assigntoexistingfield_description;
  public static String ReorgCorrectionsSubProcessor_50_compliance_operation;
  public static String ReorgCorrectionsSubProcessor_no_required_jre_title;
  public static String ReorgCorrectionsSubProcessor_no_required_jre_message;
  public static String ReorgCorrectionsSubProcessor_required_compliance_changeworkspace_description;
  public static String ReorgCorrectionsSubProcessor_required_compliance_changeproject_description;
  public static String
      GetterSetterCorrectionSubProcessor_creategetterunsingencapsulatefield_description;
  public static String GetterSetterCorrectionSubProcessor_encapsulate_field_error_message;
  public static String GetterSetterCorrectionSubProcessor_additional_info;
  public static String GetterSetterCorrectionSubProcessor_encapsulate_field_error_title;
  public static String GetterSetterCorrectionSubProcessor_replacewithgetter_description;
  public static String GetterSetterCorrectionSubProcessor_replacewithsetter_description;
  public static String
      ReorgCorrectionsSubProcessor_50_compliance_changeProjectJREToDefault_description;
  public static String ReorgCorrectionsSubProcessor_50_compliance_changeWorkspaceJRE_description;
  public static String ReorgCorrectionsSubProcessor_50_compliance_changeProjectJRE_description;
  public static String ModifierCorrectionSubProcessor_default_visibility_label;
  public static String UnresolvedElementsSubProcessor_change_to_static_import_description;
  public static String ReorgCorrectionsSubProcessor_configure_buildpath_label;
  public static String ReorgCorrectionsSubProcessor_configure_buildpath_description;
  public static String QuickAssistProcessor_extract_to_local_all_description;
  public static String QuickAssistProcessor_extract_to_local_description;
  public static String QuickAssistProcessor_extractmethod_description;
  public static String QuickAssistProcessor_move_exception_to_separate_catch_block;
  public static String QuickAssistProcessor_move_exceptions_to_separate_catch_block;
  public static String SuppressWarningsSubProcessor_suppress_warnings_label;
  public static String ReorgCorrectionsSubProcessor_accessrules_description;
  public static String ReorgCorrectionsSubProcessor_project_seup_fix_description;
  public static String ReorgCorrectionsSubProcessor_project_seup_fix_info;
  public static String UnresolvedElementsSubProcessor_change_full_type_description;
  public static String UnresolvedElementsSubProcessor_copy_annotation_jar_description;
  public static String UnresolvedElementsSubProcessor_copy_annotation_jar_info;
  public static String LocalCorrectionsSubProcessor_remove_allocated_description;
  public static String LocalCorrectionsSubProcessor_remove_redundant_superinterface;
  public static String LocalCorrectionsSubProcessor_return_allocated_description;
  public static String LocalCorrectionsSubProcessor_qualify_left_hand_side_description;
  public static String LocalCorrectionsSubProcessor_qualify_right_hand_side_description;
  public static String
      UnresolvedElementsSubProcessor_UnresolvedElementsSubProcessor_changetoattribute_description;
  public static String
      UnresolvedElementsSubProcessor_UnresolvedElementsSubProcessor_createattribute_description;
  public static String MissingAnnotationAttributesProposal_add_missing_attributes_label;
  public static String FixCorrectionProposal_ErrorAdditionalProposalInfo;
  public static String FixCorrectionProposal_MultiFixChange_label;
  public static String FixCorrectionProposal_HitCtrlEnter_description;
  public static String FixCorrectionProposal_hitCtrlEnter_variable_description;
  public static String LocalCorrectionsSubProcessor_insert_break_statement;
  public static String LocalCorrectionsSubProcessor_insert_cases_omitted;
  public static String LocalCorrectionsSubProcessor_insert_fall_through;
  public static String LocalCorrectionsSubProcessor_override_hashCode_description;
  public static String LocalCorrectionsSubProcessor_throw_allocated_description;
  public static String SuppressWarningsSubProcessor_fix_suppress_token_label;
  public static String SuppressWarningsSubProcessor_remove_annotation_label;
  public static String VarargsWarningsSubProcessor_add_safevarargs_label;
  public static String VarargsWarningsSubProcessor_add_safevarargs_to_method_label;
  public static String VarargsWarningsSubProcessor_remove_safevarargs_label;
}
