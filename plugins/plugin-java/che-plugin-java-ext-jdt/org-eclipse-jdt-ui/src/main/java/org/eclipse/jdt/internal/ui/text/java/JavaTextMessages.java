/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2009 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.java;

import org.eclipse.osgi.util.NLS;

/** Helper class to get NLSed messages. */
final class JavaTextMessages extends NLS {

  private static final String BUNDLE_NAME = JavaTextMessages.class.getName();

  private JavaTextMessages() {
    // Do not instantiate
  }

  public static String CompletionProcessor_error_accessing_title;
  public static String CompletionProcessor_error_accessing_message;
  public static String CompletionProcessor_error_notOnBuildPath_title;
  public static String CompletionProcessor_error_notOnBuildPath_message;
  public static String CompletionProcessor_error_javaCompletion_took_too_long_message;
  public static String CompletionProposalComputerRegistry_messageAvoidanceHint;
  public static String CompletionProposalComputerRegistry_messageAvoidanceHintWithWarning;
  public static String ContentAssistProcessor_all_disabled_message;
  public static String ContentAssistProcessor_all_disabled_preference_link;
  public static String ContentAssistProcessor_all_disabled_title;
  public static String FilledArgumentNamesMethodProposal_error_msg;
  public static String ParameterGuessingProposal_error_msg;
  public static String GetterSetterCompletionProposal_getter_label;
  public static String GetterSetterCompletionProposal_setter_label;
  public static String MethodCompletionProposal_constructor_label;
  public static String MethodCompletionProposal_method_label;

  static {
    NLS.initializeMessages(BUNDLE_NAME, JavaTextMessages.class);
  }

  public static String ContentAssistProcessor_computing_proposals;
  public static String ContentAssistProcessor_collecting_proposals;
  public static String ContentAssistProcessor_sorting_proposals;
  public static String ContentAssistProcessor_computing_contexts;
  public static String ContentAssistProcessor_collecting_contexts;
  public static String ContentAssistProcessor_sorting_contexts;
  public static String CompletionProposalComputerDescriptor_illegal_attribute_message;
  public static String CompletionProposalComputerDescriptor_reason_invalid;
  public static String CompletionProposalComputerDescriptor_reason_instantiation;
  public static String CompletionProposalComputerDescriptor_reason_runtime_ex;
  public static String CompletionProposalComputerDescriptor_reason_API;
  public static String CompletionProposalComputerDescriptor_reason_performance;
  public static String CompletionProposalComputerDescriptor_blame_message;
  public static String CompletionProposalComputerRegistry_invalid_message;
  public static String CompletionProposalComputerRegistry_error_dialog_title;
  public static String ContentAssistProcessor_defaultProposalCategory;
  public static String ContentAssistProcessor_toggle_affordance_press_gesture;
  public static String ContentAssistProcessor_toggle_affordance_click_gesture;
  public static String ContentAssistProcessor_toggle_affordance_update_message;
  public static String ContentAssistProcessor_empty_message;
  public static String ContentAssistHistory_serialize_error;
  public static String ContentAssistHistory_deserialize_error;
  public static String ProposalSorterHandle_blame;
}
