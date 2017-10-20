/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation Stephan Herrmann
 * <stephan@cs.tu-berlin.de> - [quick fix] Add quick fixes for null annotations -
 * https://bugs.eclipse.org/337977
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.fix;

import org.eclipse.osgi.util.NLS;

public class MultiFixMessages extends NLS {
  private static final String BUNDLE_NAME =
      "org.eclipse.jdt.internal.ui.fix.MultiFixMessages"; // $NON-NLS-1$

  private MultiFixMessages() {}

  public static String CleanUpRefactoringWizard_CleaningUp11_Title;
  public static String CleanUpRefactoringWizard_CleaningUpN1_Title;
  public static String CleanUpRefactoringWizard_CleaningUpNN_Title;
  public static String CleanUpRefactoringWizard_CleanUpConfigurationPage_title;
  public static String CleanUpRefactoringWizard_Configure_Button;
  public static String CleanUpRefactoringWizard_ConfigureCustomProfile_button;
  public static String CleanUpRefactoringWizard_CustomCleanUpsDialog_title;
  public static String CleanUpRefactoringWizard_EmptySelection_message;
  public static String CleanUpRefactoringWizard_HideWizard_Link;
  public static String CleanUpRefactoringWizard_Profile_TableHeader;
  public static String CleanUpRefactoringWizard_Project_TableHeader;
  public static String CleanUpRefactoringWizard_unknownProfile_Name;
  public static String CleanUpRefactoringWizard_UnmanagedProfileWithName_Name;
  public static String CleanUpRefactoringWizard_use_configured_radio;
  public static String CleanUpRefactoringWizard_use_custom_radio;
  public static String CleanUpRefactoringWizard_XofYCleanUpsSelected_message;
  public static String CodeFormatCleanUp_correctIndentation_description;
  public static String CodeFormatCleanUp_RemoveTrailingAll_description;
  public static String CodeFormatCleanUp_RemoveTrailingNoEmpty_description;
  public static String CodeFormatFix_correctIndentation_changeGroupLabel;
  public static String CodeFormatFix_RemoveTrailingWhitespace_changeDescription;
  public static String ImportsCleanUp_OrganizeImports_Description;
  public static String SortMembersCleanUp_AllMembers_description;
  public static String SortMembersCleanUp_Excluding_description;
  public static String SortMembersCleanUp_RemoveMarkersWarning0;
  public static String StringMultiFix_AddMissingNonNls_description;
  public static String StringMultiFix_RemoveUnnecessaryNonNls_description;

  public static String UnusedCodeMultiFix_RemoveUnusedVariable_description;
  public static String UnusedCodeMultiFix_RemoveUnusedField_description;
  public static String UnusedCodeMultiFix_RemoveUnusedType_description;
  public static String UnusedCodeMultiFix_RemoveUnusedConstructor_description;
  public static String UnusedCodeMultiFix_RemoveUnusedMethod_description;
  public static String UnusedCodeMultiFix_RemoveUnusedImport_description;
  public static String UnusedCodeCleanUp_RemoveUnusedCasts_description;

  public static String CodeStyleMultiFix_ChangeNonStaticAccess_description;
  public static String CodeStyleMultiFix_AddThisQualifier_description;
  public static String CodeStyleMultiFix_QualifyAccessToStaticField;
  public static String CodeStyleMultiFix_ChangeIndirectAccessToStaticToDirect;
  public static String CodeStyleMultiFix_ConvertSingleStatementInControlBodeyToBlock_description;
  public static String CodeStyleCleanUp_addDefaultSerialVersionId_description;
  public static String CodeStyleCleanUp_QualifyNonStaticMethod_description;
  public static String CodeStyleCleanUp_QualifyStaticMethod_description;
  public static String CodeStyleCleanUp_removeFieldThis_description;
  public static String CodeStyleCleanUp_removeMethodThis_description;

  public static String Java50MultiFix_AddMissingDeprecated_description;
  public static String Java50MultiFix_AddMissingOverride_description;
  public static String Java50MultiFix_AddMissingOverride_description2;
  public static String Java50CleanUp_ConvertToEnhancedForLoop_description;
  public static String Java50CleanUp_AddTypeParameters_description;

  public static String SerialVersionCleanUp_Generated_description;

  public static String CleanUpRefactoringWizard_WindowTitle;
  public static String CleanUpRefactoringWizard_PageTitle;
  public static String CleanUpRefactoringWizard_formatterException_errorMessage;

  public static String ControlStatementsCleanUp_RemoveUnnecessaryBlocks_description;
  public static String
      ControlStatementsCleanUp_RemoveUnnecessaryBlocksWithReturnOrThrow_description;

  public static String UnimplementedCodeCleanUp_AddUnimplementedMethods_description;
  public static String UnimplementedCodeCleanUp_MakeAbstract_description;

  public static String ExpressionsCleanUp_addParanoiac_description;
  public static String ExpressionsCleanUp_removeUnnecessary_description;

  public static String VariableDeclarationCleanUp_AddFinalField_description;
  public static String VariableDeclarationCleanUp_AddFinalParameters_description;
  public static String VariableDeclarationCleanUp_AddFinalLocals_description;

  public static String CodeFormatCleanUp_description;
  public static String CodeFormatFix_description;
  public static String LambdaExpressionsCleanUp_use_lambda_where_possible;
  public static String LambdaExpressionsCleanUp_use_anonymous;

  public static String NullAnnotationsCleanUp_add_nullable_annotation;
  public static String NullAnnotationsCleanUp_add_nonnull_annotation;
  public static String NullAnnotationsCleanUp_remove_redundant_nullness_annotation;

  public static String TypeParametersCleanUp_InsertInferredTypeArguments_description;
  public static String TypeParametersCleanUp_RemoveRedundantTypeArguments_description;

  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, MultiFixMessages.class);
  }
}
