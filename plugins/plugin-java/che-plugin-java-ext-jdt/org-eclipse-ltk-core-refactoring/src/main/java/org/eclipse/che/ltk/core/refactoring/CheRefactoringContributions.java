/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ltk.core.refactoring;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.internal.registry.IRegistryConstants;
import org.eclipse.core.internal.registry.RegistryMessages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/** @author Evgen Vidolob */
public class CheRefactoringContributions {

  private static final Map<String, String> refactoringContributions = new HashMap<>();

  static {
    refactoringContributions.put(
        "org.eclipse.jdt.ui.rename.compilationunit",
        "org.eclipse.jdt.internal.corext.refactoring.scripting.RenameCompilationUnitRefactoringContribution");
    refactoringContributions.put(
        "org.eclipse.jdt.ui.rename.enum.constant",
        "org.eclipse.jdt.internal.corext.refactoring.scripting.RenameEnumConstRefactoringContribution");
    refactoringContributions.put(
        "org.eclipse.jdt.ui.rename.field",
        "org.eclipse.jdt.internal.corext.refactoring.scripting.RenameFieldRefactoringContribution");
    refactoringContributions.put(
        "org.eclipse.jdt.ui.rename.local.variable",
        "org.eclipse.jdt.internal.corext.refactoring.scripting.RenameLocalVariableRefactoringContribution");
    refactoringContributions.put(
        "org.eclipse.jdt.ui.rename.method",
        "org.eclipse.jdt.internal.corext.refactoring.scripting.RenameMethodRefactoringContribution");
    refactoringContributions.put(
        "org.eclipse.jdt.ui.rename.package",
        "org.eclipse.jdt.internal.corext.refactoring.scripting.RenamePackageRefactoringContribution");
    refactoringContributions.put(
        "org.eclipse.jdt.ui.rename.type.parameter",
        "org.eclipse.jdt.internal.corext.refactoring.scripting.RenameTypeParameterRefactoringContribution");
    refactoringContributions.put(
        "org.eclipse.jdt.ui.rename.type",
        "org.eclipse.jdt.internal.corext.refactoring.scripting.RenameTypeRefactoringContribution");
    // TODO :
    /*
      <contribution
          class="org.eclipse.jdt.internal.corext.refactoring.scripting.ChangeMethodSignatureRefactoringContribution"
          id="org.eclipse.jdt.ui.change.method.signature"/>
    <contribution
          class="org.eclipse.jdt.internal.corext.refactoring.scripting.MoveMethodRefactoringContribution"
          id="org.eclipse.jdt.ui.move.method"/>
    <contribution
          class="org.eclipse.jdt.internal.corext.refactoring.scripting.MoveStaticMembersRefactoringContribution"
          id="org.eclipse.jdt.ui.move.static"/>
    <contribution
          class="org.eclipse.jdt.internal.corext.refactoring.scripting.ExtractInterfaceRefactoringContribution"
          id="org.eclipse.jdt.ui.extract.interface"/>
    <contribution
          class="org.eclipse.jdt.internal.corext.refactoring.scripting.UseSupertypeRefactoringContribution"
          id="org.eclipse.jdt.ui.use.supertype"/>
    <contribution
          class="org.eclipse.jdt.internal.corext.refactoring.scripting.PullUpRefactoringContribution"
          id="org.eclipse.jdt.ui.pull.up"/>
    <contribution
          class="org.eclipse.jdt.internal.corext.refactoring.scripting.PushDownRefactoringContribution"
          id="org.eclipse.jdt.ui.push.down"/>
    <contribution
          class="org.eclipse.jdt.internal.corext.refactoring.scripting.ConvertAnonymousRefactoringContribution"
          id="org.eclipse.jdt.ui.convert.anonymous"/>
    <contribution
          class="org.eclipse.jdt.internal.corext.refactoring.scripting.MoveMemberTypeRefactoringContribution"
          id="org.eclipse.jdt.ui.move.inner"/>
    <contribution
          class="org.eclipse.jdt.internal.corext.refactoring.scripting.InlineMethodRefactoringContribution"
          id="org.eclipse.jdt.ui.inline.method"/>
    <contribution
          class="org.eclipse.jdt.internal.corext.refactoring.scripting.InlineTempRefactoringContribution"
          id="org.eclipse.jdt.ui.inline.temp"/>
    <contribution
          class="org.eclipse.jdt.internal.corext.refactoring.scripting.InlineConstantRefactoringContribution"
          id="org.eclipse.jdt.ui.inline.constant"/>
    <contribution
          class="org.eclipse.jdt.internal.corext.refactoring.scripting.ExtractMethodRefactoringContribution"
          id="org.eclipse.jdt.ui.extract.method"/>
    <contribution
          class="org.eclipse.jdt.internal.corext.refactoring.scripting.ExtractTempRefactoringContribution"
          id="org.eclipse.jdt.ui.extract.temp"/>
    <contribution
          class="org.eclipse.jdt.internal.corext.refactoring.scripting.ExtractConstantRefactoringContribution"
          id="org.eclipse.jdt.ui.extract.constant"/>
    <contribution
          class="org.eclipse.jdt.internal.corext.refactoring.scripting.ExtractClassContribution"
          id="org.eclipse.jdt.ui.extract.class">
    </contribution>
    <contribution
          class="org.eclipse.jdt.internal.corext.refactoring.scripting.IntroduceParameterRefactoringContribution"
          id="org.eclipse.jdt.ui.introduce.parameter"/>
    <contribution
          class="org.eclipse.jdt.internal.corext.refactoring.scripting.IntroduceParameterObjectContribution"
          id="org.eclipse.jdt.ui.introduce.parameter.object"/>
    <contribution
          class="org.eclipse.jdt.internal.corext.refactoring.scripting.IntroduceFactoryRefactoringContribution"
          id="org.eclipse.jdt.ui.introduce.factory"/>
    <contribution
          class="org.eclipse.jdt.internal.corext.refactoring.scripting.IntroduceIndirectionRefactoringContribution"
          id="org.eclipse.jdt.ui.introduce.indirection"/>
    <contribution
          class="org.eclipse.jdt.internal.corext.refactoring.scripting.PromoteTempToFieldRefactoringContribution"
          id="org.eclipse.jdt.ui.promote.temp"/>
    <contribution
          class="org.eclipse.jdt.internal.corext.refactoring.scripting.SelfEncapsulateRefactoringContribution"
          id="org.eclipse.jdt.ui.self.encapsulate"/>
    <contribution
          class="org.eclipse.jdt.internal.corext.refactoring.scripting.InferTypeArgumentsRefactoringContribution"
          id="org.eclipse.jdt.ui.infer.typearguments"/>
    <contribution
          class="org.eclipse.jdt.internal.corext.refactoring.scripting.ChangeTypeRefactoringContribution"
          id="org.eclipse.jdt.ui.change.type"/>
    <contribution
          class="org.eclipse.jdt.internal.corext.refactoring.scripting.ExtractSupertypeRefactoringContribution"
          id="org.eclipse.jdt.ui.extract.superclass"/>
    <contribution
          class="org.eclipse.jdt.internal.corext.refactoring.scripting.DeleteRefactoringContribution"
          id="org.eclipse.jdt.ui.delete"/>
    <contribution
          class="org.eclipse.jdt.internal.corext.refactoring.scripting.MoveRefactoringContribution"
          id="org.eclipse.jdt.ui.move"/>
    <contribution
          class="org.eclipse.jdt.internal.corext.refactoring.scripting.CopyRefactoringContribution"
          id="org.eclipse.jdt.ui.copy"/>
       */
  }

  public static Map<String, String> getRefactoringContributions() {
    return new HashMap<>(refactoringContributions);
  }

  public static Object createExecutableExtension(String clazz) throws CoreException {

    Class<?> exClass = null;
    try {
      exClass = Class.forName(clazz);
    } catch (ClassNotFoundException e) {
      throw new CoreException(
          new Status(
              IStatus.ERROR,
              RegistryMessages.OWNER_NAME,
              IRegistryConstants.PLUGIN_ERROR,
              "Can't find class: " + clazz,
              e));
    }
    try {
      return exClass.newInstance();

    } catch (Exception e) {
      throw new CoreException(
          new Status(
              IStatus.ERROR,
              RegistryMessages.OWNER_NAME,
              IRegistryConstants.PLUGIN_ERROR,
              "Can't create new instance of: " + clazz,
              e));
    }
  }
}
