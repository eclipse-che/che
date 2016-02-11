/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Mateusz Matela <mateusz.matela@gmail.com> - [code manipulation] [dcr] toString() builder wizard - https://bugs.eclipse.org/bugs/show_bug.cgi?id=26070
 *     Mateusz Matela <mateusz.matela@gmail.com> - [toString] Template edit dialog has usability issues - https://bugs.eclipse.org/bugs/show_bug.cgi?id=267916
 *     Mateusz Matela <mateusz.matela@gmail.com> - [toString] finish toString() builder wizard - https://bugs.eclipse.org/bugs/show_bug.cgi?id=267710
 *     Mateusz Matela <mateusz.matela@gmail.com> - [toString] toString() generator: Fields in declaration order - https://bugs.eclipse.org/bugs/show_bug.cgi?id=279924
 *******************************************************************************/
package org.eclipse.jdt.internal.ui;

import org.eclipse.osgi.util.NLS;

public final class JavaUIMessages extends NLS {

	private static final String BUNDLE_NAME= "org.eclipse.jdt.internal.ui.JavaUIMessages";//$NON-NLS-1$

	private JavaUIMessages() {
		// Do not instantiate
	}

	public static String FilteredTypesSelectionDialog_TypeFiltersPreferencesAction_label;
	public static String GenerateHashCodeEqualsDialog_blocks_button;
	public static String GenerateHashCodeEqualsDialog_instanceof_button;
	public static String JavaPlugin_additionalInfo_affordance;
	public static String JavaPlugin_internal_error;
	public static String JavaPlugin_initializing_ui;

	public static String JavaElementProperties_name;

	public static String OpenTypeAction_createProjectFirst;
	public static String OpenTypeAction_description;
	public static String OpenTypeAction_tooltip;

	public static String OpenTypeAction_no_active_WorkbenchPage;
	public static String OpenTypeAction_multiStatusMessage;
	public static String OpenTypeAction_errorMessage;
	public static String OpenTypeAction_errorTitle;
	public static String OpenTypeAction_label;
	public static String OpenTypeAction_dialogTitle;
	public static String OpenTypeAction_dialogMessage;

	public static String JavaUI_defaultDialogMessage;

	public static String MultiElementListSelectionDialog_pageInfoMessage;
	public static String PackageSelectionDialog_progress_findEmpty;
	public static String PackageSelectionDialog_progress_search;
	

	public static String TypeInfoViewer_job_label;
	public static String TypeInfoViewer_job_error;
	public static String TypeInfoViewer_job_cancel;
	public static String TypeInfoViewer_default_package;
	public static String TypeInfoViewer_progress_label;
	public static String TypeInfoViewer_searchJob_taskName;
	public static String TypeInfoViewer_syncJob_label;
	public static String TypeInfoViewer_syncJob_taskName;
	public static String TypeInfoViewer_progressJob_label;
	public static String TypeInfoViewer_remove_from_history;
	public static String TypeInfoViewer_separator_message;
	public static String TypeInfoViewer_library_name_format;
	public static String TypeSelectionComponent_label;
	public static String TypeSelectionComponent_menu;
	public static String TypeSelectionComponent_show_status_line_label;
	public static String TypeSelectionComponent_fully_qualify_duplicates_label;
	public static String TypeSelectionDialog2_title_format;
	public static String TypeSelectionDialog_dialogMessage;
	public static String TypeSelectionDialog_errorTitle;
	public static String TypeSelectionDialog_error_type_doesnot_exist;

	public static String TypeSelectionDialog_error3Message;
	public static String TypeSelectionDialog_error3Title;
	public static String TypeSelectionDialog_progress_consistency;
	
	/**
	 * DO NOT REMOVE, used in a product.
	 * @deprecated As of 3.6
	 */
	public static String TypeSelectionDialog_lowerLabel;
	/**
	 * DO NOT REMOVE, used in a product.
	 * @deprecated As of 3.6
	 */
	public static String TypeSelectionDialog_upperLabel;

	public static String ExceptionDialog_seeErrorLogMessage;

	public static String MainTypeSelectionDialog_errorTitle;
	public static String MultiMainTypeSelectionDialog_errorTitle;

	public static String PackageSelectionDialog_error_title;
	public static String PackageSelectionDialog_error3Message;
	public static String PackageSelectionDialog_nopackages_title;
	public static String PackageSelectionDialog_nopackages_message;
	public static String ProblemMarkerManager_problem_marker_update_job_description;

	public static String OverrideMethodDialog_groupMethodsByTypes;
	public static String OverrideMethodDialog_dialog_title;
	public static String OverrideMethodDialog_dialog_description;
	public static String OverrideMethodDialog_selectioninfo_more;
	public static String OverrideMethodDialog_link_tooltip;
	public static String OverrideMethodDialog_link_message;

	public static String GenerateHashCodeEqualsDialog_dialog_title;
	public static String GenerateHashCodeEqualsDialog_selectioninfo_more;
	public static String GenerateHashCodeEqualsDialog_no_entries;
	public static String GenerateHashCodeEqualsDialog_select_at_least_one_field;
	public static String GenerateHashCodeEqualsDialog_select_fields_to_include;

	public static String GenerateToStringDialog_defaultTemplateName;
	public static String GenerateToStringDialog_dialog_title;
	public static String GenerateToStringDialog_select_fields_to_include;
	public static String GenerateToStringDialog_selectioninfo_customBuilderConfigError;
	public static String GenerateToStringDialog_selectioninfo_more;
	public static String GenerateToStringDialog_methods_node;
	public static String GenerateToStringDialog_fields_node;
	public static String GenerateToStringDialog_inherited_fields_node;
	public static String GenerateToStringDialog_inherited_methods_node;
	public static String GenerateToStringDialog_string_format_combo;
	public static String GenerateToStringDialog_manage_templates_button;
	public static String GenerateToStringDialog_code_style_combo;
	public static String GenerateToStringDialog_codeStyleConfigureButton;
	public static String GenerateToStringDialog_customBuilderConfig_appendMethodField;
	public static String GenerateToStringDialog_customBuilderConfig_applyButton;
	public static String GenerateToStringDialog_customBuilderConfig_browseButton;
	public static String GenerateToStringDialog_customBuilderConfig_builderClassField;
	public static String GenerateToStringDialog_customBuilderConfig_chainedCallsCheckbox;
	public static String GenerateToStringDialog_customBuilderConfig_classSelection_message;
	public static String GenerateToStringDialog_customBuilderConfig_classSelection_windowTitle;
	public static String GenerateToStringDialog_customBuilderConfig_dataValidationError;
	public static String GenerateToStringDialog_customBuilderConfig_invalidAppendMethodError;
	public static String GenerateToStringDialog_customBuilderConfig_invalidClassError;
	public static String GenerateToStringDialog_customBuilderConfig_invalidVariableNameError;
	public static String GenerateToStringDialog_customBuilderConfig_invalidResultMethodError;
	public static String GenerateToStringDialog_customBuilderConfig_varNameField;
	public static String GenerateToStringDialog_customBuilderConfig_noAppendMethodError;
	public static String GenerateToStringDialog_customBuilderConfig_noBuilderClassError;
	public static String GenerateToStringDialog_customBuilderConfig_noConstructorError;
	public static String GenerateToStringDialog_customBuilderConfig_noResultMethodError;
	public static String GenerateToStringDialog_customBuilderConfig_resultMethodField;
	public static String GenerateToStringDialog_customBuilderConfig_typeValidationError;
	public static String GenerateToStringDialog_customBuilderConfig_windowTitle;
	public static String GenerateToStringDialog_ignore_default_button;
	public static String GenerateToStringDialog_limit_elements_button;
	public static String GenerateToStringDialog_newTemplateName;
	public static String GenerateToStringDialog_newTemplateNameArg;
	public static String GenerateToStringDialog_skip_null_button;
	public static String GenerateToStringDialog_sort_button;
	public static String GenerateToStringDialog_template_content;
	public static String GenerateToStringDialog_template_name;
	public static String GenerateToStringDialog_templateEdition_NewWindowTitle;
	public static String GenerateToStringDialog_templateEdition_TemplateNameDuplicateErrorMessage;
	public static String GenerateToStringDialog_templateEdition_TemplateNameEmptyErrorMessage;
	public static String GenerateToStringDialog_templateEdition_WindowTitle;
	public static String GenerateToStringDialog_templateManagerApplyButton;
	public static String GenerateToStringDialog_templateManagerNoTemplateErrorMessage;
	public static String GenerateToStringDialog_down_button;
	public static String GenerateToStringDialog_generated_code_group;
	public static String GenerateToStringDialog_templatesManagerNewButton;
	public static String GenerateToStringDialog_templatesManagerPreview;
	public static String GenerateToStringDialog_templatesManagerRemoveButton;
	public static String GenerateToStringDialog_templatesManagerTemplatesList;
	public static String GenerateToStringDialog_templatesManagerTitle;
	public static String GenerateToStringDialog_teplatesManagerEditButton;
	public static String GenerateToStringDialog_up_button;

	public static String JavaElementLabels_default_package;
	public static String JavaElementLabels_anonym_type;
	public static String JavaElementLabels_anonym;
	public static String JavaElementLabels_import_container;
	public static String JavaElementLabels_initializer;
	public static String JavaElementLabels_category;
	public static String JavaElementLabels_concat_string;
	public static String JavaElementLabels_comma_string;
	public static String JavaElementLabels_declseparator_string;
	public static String JavaElementLabels_category_separator_string;
	public static String JavaElementLabels_onClassPathOf;
	public static String JavaElementLinks_title;

	public static String StatusBarUpdater_num_elements_selected;

	public static String OpenTypeHierarchyUtil_error_open_view;
	public static String OpenTypeHierarchyUtil_error_open_perspective;
	public static String OpenTypeHierarchyUtil_error_open_editor;

	public static String TypeInfoLabelProvider_default_package;

	public static String JavaUIHelp_link_label;
	public static String JavaUIHelpContext_javaHelpCategory_label;

	public static String ResourceTransferDragAdapter_cannot_delete_resource;
	public static String ResourceTransferDragAdapter_moving_resource;
	public static String ResourceTransferDragAdapter_cannot_delete_files_singular;
	public static String ResourceTransferDragAdapter_cannot_delete_files_plural;

	public static String Spelling_error_label;
	public static String Spelling_correct_label;
	public static String Spelling_add_info;
	public static String Spelling_add_label;
	public static String Spelling_add_askToConfigure_title;
	public static String Spelling_add_askToConfigure_question;
	public static String Spelling_add_askToConfigure_ignoreMessage;
	public static String Spelling_ignore_info;
	public static String Spelling_ignore_label;
	public static String Spelling_disable_label;
	public static String Spelling_disable_info;
	public static String Spelling_case_label;
	public static String Spelling_error_case_label;
	public static String AbstractSpellingDictionary_encodingError;

	public static String JavaAnnotationHover_multipleMarkersAtThisLine;
	public static String JavaEditor_codeassist_noCompletions;

	public static String OptionalMessageDialog_dontShowAgain;
	public static String ElementValidator_cannotPerform;
	public static String SelectionListenerWithASTManager_job_title;

	public static String JavaOutlineControl_statusFieldText_hideInheritedMembers;
	public static String JavaOutlineControl_statusFieldText_showInheritedMembers;

	public static String RenameSupport_not_available;
	public static String RenameSupport_dialog_title;

	public static String CoreUtility_job_title;
	public static String CoreUtility_buildall_taskname;
	public static String CoreUtility_buildproject_taskname;

	public static String FilteredTypesSelectionDialog_default_package;
	public static String FilteredTypesSelectionDialog_dialogMessage;
	public static String FilteredTypesSelectionDialog_error_type_doesnot_exist;
	public static String FilteredTypesSelectionDialog_library_name_format;
	public static String FilteredTypesSelectionDialog_searchJob_taskName;
	public static String FilteredTypeSelectionDialog_showContainerForDuplicatesAction;
	public static String FilteredTypeSelectionDialog_titleFormat;

	public static String InitializeAfterLoadJob_starter_job_name;

	static {
		NLS.initializeMessages(BUNDLE_NAME, JavaUIMessages.class);
	}

	public static String HistoryListAction_remove;
	public static String HistoryListAction_max_entries_constraint;
	public static String HistoryListAction_remove_all;
}
