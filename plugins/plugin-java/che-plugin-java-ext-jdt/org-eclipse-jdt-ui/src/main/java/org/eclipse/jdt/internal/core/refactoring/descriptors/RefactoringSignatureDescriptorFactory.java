/**
 * ***************************************************************************** Copyright (c) 2008
 * IBM Corporation and others. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.core.refactoring.descriptors;

import java.util.Map;
import org.eclipse.jdt.core.refactoring.descriptors.ChangeMethodSignatureDescriptor;
import org.eclipse.jdt.core.refactoring.descriptors.ConvertAnonymousDescriptor;
import org.eclipse.jdt.core.refactoring.descriptors.ConvertLocalVariableDescriptor;
import org.eclipse.jdt.core.refactoring.descriptors.ConvertMemberTypeDescriptor;
import org.eclipse.jdt.core.refactoring.descriptors.CopyDescriptor;
import org.eclipse.jdt.core.refactoring.descriptors.DeleteDescriptor;
import org.eclipse.jdt.core.refactoring.descriptors.EncapsulateFieldDescriptor;
import org.eclipse.jdt.core.refactoring.descriptors.ExtractClassDescriptor;
import org.eclipse.jdt.core.refactoring.descriptors.ExtractConstantDescriptor;
import org.eclipse.jdt.core.refactoring.descriptors.ExtractInterfaceDescriptor;
import org.eclipse.jdt.core.refactoring.descriptors.ExtractLocalDescriptor;
import org.eclipse.jdt.core.refactoring.descriptors.ExtractMethodDescriptor;
import org.eclipse.jdt.core.refactoring.descriptors.ExtractSuperclassDescriptor;
import org.eclipse.jdt.core.refactoring.descriptors.GeneralizeTypeDescriptor;
import org.eclipse.jdt.core.refactoring.descriptors.InferTypeArgumentsDescriptor;
import org.eclipse.jdt.core.refactoring.descriptors.InlineConstantDescriptor;
import org.eclipse.jdt.core.refactoring.descriptors.InlineLocalVariableDescriptor;
import org.eclipse.jdt.core.refactoring.descriptors.InlineMethodDescriptor;
import org.eclipse.jdt.core.refactoring.descriptors.IntroduceFactoryDescriptor;
import org.eclipse.jdt.core.refactoring.descriptors.IntroduceIndirectionDescriptor;
import org.eclipse.jdt.core.refactoring.descriptors.IntroduceParameterDescriptor;
import org.eclipse.jdt.core.refactoring.descriptors.IntroduceParameterObjectDescriptor;
import org.eclipse.jdt.core.refactoring.descriptors.MoveDescriptor;
import org.eclipse.jdt.core.refactoring.descriptors.MoveMethodDescriptor;
import org.eclipse.jdt.core.refactoring.descriptors.MoveStaticMembersDescriptor;
import org.eclipse.jdt.core.refactoring.descriptors.PullUpDescriptor;
import org.eclipse.jdt.core.refactoring.descriptors.PushDownDescriptor;
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor;
import org.eclipse.jdt.core.refactoring.descriptors.UseSupertypeDescriptor;

/**
 * Internal factory for Java refactoring signature descriptors.
 *
 * @since 3.5
 */
public class RefactoringSignatureDescriptorFactory {

  public static ChangeMethodSignatureDescriptor createChangeMethodSignatureDescriptor() {
    return new ChangeMethodSignatureDescriptor();
  }

  public static ChangeMethodSignatureDescriptor createChangeMethodSignatureDescriptor(
      String project, String description, String comment, Map arguments, int flags) {
    return new ChangeMethodSignatureDescriptor(project, description, comment, arguments, flags);
  }

  public static ConvertAnonymousDescriptor createConvertAnonymousDescriptor() {
    return new ConvertAnonymousDescriptor();
  }

  public static ConvertAnonymousDescriptor createConvertAnonymousDescriptor(
      String project, String description, String comment, Map arguments, int flags) {
    return new ConvertAnonymousDescriptor(project, description, comment, arguments, flags);
  }

  public static GeneralizeTypeDescriptor createGeneralizeTypeDescriptor() {
    return new GeneralizeTypeDescriptor();
  }

  public static GeneralizeTypeDescriptor createGeneralizeTypeDescriptor(
      String project, String description, String comment, Map arguments, int flags) {
    return new GeneralizeTypeDescriptor(project, description, comment, arguments, flags);
  }

  public static CopyDescriptor createCopyDescriptor() {
    return new CopyDescriptor();
  }

  public static CopyDescriptor createCopyDescriptor(
      String project, String description, String comment, Map arguments, int flags) {
    return new CopyDescriptor(project, description, comment, arguments, flags);
  }

  public static DeleteDescriptor createDeleteDescriptor() {
    return new DeleteDescriptor();
  }

  public static DeleteDescriptor createDeleteDescriptor(
      String project, String description, String comment, Map arguments, int flags) {
    return new DeleteDescriptor(project, description, comment, arguments, flags);
  }

  public static ExtractClassDescriptor createExtractClassDescriptor(
      String project, String description, String comment, Map arguments, int flags)
      throws IllegalArgumentException {
    return new ExtractClassDescriptor(project, description, comment, arguments, flags);
  }

  public static ExtractClassDescriptor createExtractClassDescriptor() {
    return new ExtractClassDescriptor();
  }

  public static ExtractConstantDescriptor createExtractConstantDescriptor() {
    return new ExtractConstantDescriptor();
  }

  public static ExtractConstantDescriptor createExtractConstantDescriptor(
      String project, String description, String comment, Map arguments, int flags) {
    return new ExtractConstantDescriptor(project, description, comment, arguments, flags);
  }

  public static ExtractInterfaceDescriptor createExtractInterfaceDescriptor() {
    return new ExtractInterfaceDescriptor();
  }

  public static ExtractInterfaceDescriptor createExtractInterfaceDescriptor(
      String project, String description, String comment, Map arguments, int flags) {
    return new ExtractInterfaceDescriptor(project, description, comment, arguments, flags);
  }

  public static ExtractMethodDescriptor createExtractMethodDescriptor() {
    return new ExtractMethodDescriptor();
  }

  public static ExtractMethodDescriptor createExtractMethodDescriptor(
      String project, String description, String comment, Map arguments, int flags) {
    return new ExtractMethodDescriptor(project, description, comment, arguments, flags);
  }

  public static ExtractSuperclassDescriptor createExtractSuperclassDescriptor() {
    return new ExtractSuperclassDescriptor();
  }

  public static ExtractSuperclassDescriptor createExtractSuperclassDescriptor(
      String project, String description, String comment, Map arguments, int flags) {
    return new ExtractSuperclassDescriptor(project, description, comment, arguments, flags);
  }

  public static ExtractLocalDescriptor createExtractLocalDescriptor() {
    return new ExtractLocalDescriptor();
  }

  public static ExtractLocalDescriptor createExtractLocalDescriptor(
      String project, String description, String comment, Map arguments, int flags) {
    return new ExtractLocalDescriptor(project, description, comment, arguments, flags);
  }

  public static InferTypeArgumentsDescriptor createInferTypeArgumentsDescriptor() {
    return new InferTypeArgumentsDescriptor();
  }

  public static InferTypeArgumentsDescriptor createInferTypeArgumentsDescriptor(
      String project, String description, String comment, Map arguments, int flags) {
    return new InferTypeArgumentsDescriptor(project, description, comment, arguments, flags);
  }

  public static InlineConstantDescriptor createInlineConstantDescriptor() {
    return new InlineConstantDescriptor();
  }

  public static InlineConstantDescriptor createInlineConstantDescriptor(
      String project, String description, String comment, Map arguments, int flags) {
    return new InlineConstantDescriptor(project, description, comment, arguments, flags);
  }

  public static InlineMethodDescriptor createInlineMethodDescriptor() {
    return new InlineMethodDescriptor();
  }

  public static InlineMethodDescriptor createInlineMethodDescriptor(
      String project, String description, String comment, Map arguments, int flags) {
    return new InlineMethodDescriptor(project, description, comment, arguments, flags);
  }

  public static InlineLocalVariableDescriptor createInlineLocalVariableDescriptor() {
    return new InlineLocalVariableDescriptor();
  }

  public static InlineLocalVariableDescriptor createInlineLocalVariableDescriptor(
      String project, String description, String comment, Map arguments, int flags) {
    return new InlineLocalVariableDescriptor(project, description, comment, arguments, flags);
  }

  public static IntroduceFactoryDescriptor createIntroduceFactoryDescriptor() {
    return new IntroduceFactoryDescriptor();
  }

  public static IntroduceFactoryDescriptor createIntroduceFactoryDescriptor(
      String project, String description, String comment, Map arguments, int flags) {
    return new IntroduceFactoryDescriptor(project, description, comment, arguments, flags);
  }

  public static IntroduceIndirectionDescriptor createIntroduceIndirectionDescriptor() {
    return new IntroduceIndirectionDescriptor();
  }

  public static IntroduceIndirectionDescriptor createIntroduceIndirectionDescriptor(
      String project, String description, String comment, Map arguments, int flags) {
    return new IntroduceIndirectionDescriptor(project, description, comment, arguments, flags);
  }

  public static IntroduceParameterObjectDescriptor createIntroduceParameterObjectDescriptor() {
    return new IntroduceParameterObjectDescriptor();
  }

  public static IntroduceParameterObjectDescriptor createIntroduceParameterObjectDescriptor(
      String project, String description, String comment, Map arguments, int flags)
      throws IllegalArgumentException {
    return new IntroduceParameterObjectDescriptor(project, description, comment, arguments, flags);
  }

  public static IntroduceParameterDescriptor createIntroduceParameterDescriptor() {
    return new IntroduceParameterDescriptor();
  }

  public static IntroduceParameterDescriptor createIntroduceParameterDescriptor(
      String project, String description, String comment, Map arguments, int flags) {
    return new IntroduceParameterDescriptor(project, description, comment, arguments, flags);
  }

  public static ConvertMemberTypeDescriptor createConvertMemberTypeDescriptor() {
    return new ConvertMemberTypeDescriptor();
  }

  public static ConvertMemberTypeDescriptor createConvertMemberTypeDescriptor(
      String project, String description, String comment, Map arguments, int flags) {
    return new ConvertMemberTypeDescriptor(project, description, comment, arguments, flags);
  }

  public static MoveMethodDescriptor createMoveMethodDescriptor() {
    return new MoveMethodDescriptor();
  }

  public static MoveMethodDescriptor createMoveMethodDescriptor(
      String project, String description, String comment, Map arguments, int flags) {
    return new MoveMethodDescriptor(project, description, comment, arguments, flags);
  }

  public static MoveDescriptor createMoveDescriptor() {
    return new MoveDescriptor();
  }

  public static MoveDescriptor createMoveDescriptor(
      String project, String description, String comment, Map arguments, int flags) {
    return new MoveDescriptor(project, description, comment, arguments, flags);
  }

  public static MoveStaticMembersDescriptor createMoveStaticMembersDescriptor() {
    return new MoveStaticMembersDescriptor();
  }

  public static MoveStaticMembersDescriptor createMoveStaticMembersDescriptor(
      String project, String description, String comment, Map arguments, int flags) {
    return new MoveStaticMembersDescriptor(project, description, comment, arguments, flags);
  }

  public static ConvertLocalVariableDescriptor createConvertLocalVariableDescriptor() {
    return new ConvertLocalVariableDescriptor();
  }

  public static ConvertLocalVariableDescriptor createConvertLocalVariableDescriptor(
      String project, String description, String comment, Map arguments, int flags) {
    return new ConvertLocalVariableDescriptor(project, description, comment, arguments, flags);
  }

  public static PullUpDescriptor createPullUpDescriptor() {
    return new PullUpDescriptor();
  }

  public static PullUpDescriptor createPullUpDescriptor(
      String project, String description, String comment, Map arguments, int flags) {
    return new PullUpDescriptor(project, description, comment, arguments, flags);
  }

  public static PushDownDescriptor createPushDownDescriptor() {
    return new PushDownDescriptor();
  }

  public static PushDownDescriptor createPushDownDescriptor(
      String project, String description, String comment, Map arguments, int flags) {
    return new PushDownDescriptor(project, description, comment, arguments, flags);
  }

  public static RenameJavaElementDescriptor createRenameJavaElementDescriptor(String id) {
    return new RenameJavaElementDescriptor(id);
  }

  public static RenameJavaElementDescriptor createRenameJavaElementDescriptor(
      String id, String project, String description, String comment, Map arguments, int flags) {
    return new RenameJavaElementDescriptor(id, project, description, comment, arguments, flags);
  }

  public static EncapsulateFieldDescriptor createEncapsulateFieldDescriptor() {
    return new EncapsulateFieldDescriptor();
  }

  public static EncapsulateFieldDescriptor createEncapsulateFieldDescriptor(
      String project, String description, String comment, Map arguments, int flags) {
    return new EncapsulateFieldDescriptor(project, description, comment, arguments, flags);
  }

  public static UseSupertypeDescriptor createUseSupertypeDescriptor() {
    return new UseSupertypeDescriptor();
  }

  public static UseSupertypeDescriptor createUseSupertypeDescriptor(
      String project, String description, String comment, Map arguments, int flags) {
    return new UseSupertypeDescriptor(project, description, comment, arguments, flags);
  }
}
