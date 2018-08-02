/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.cpp.ide;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_FILE_NEW;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.plugin.cpp.ide.action.CreateCSourceFileAction;
import org.eclipse.che.plugin.cpp.ide.action.CreateCppSourceFileAction;
import org.eclipse.che.plugin.cpp.ide.action.CreateHeaderSourceFileAction;

/** @author Vitalii Parfonov */
@Extension(title = "Cpp")
public class CppExtension {

  public static String C_CATEGORY = "C/C++";

  @Inject
  public CppExtension(
      FileTypeRegistry fileTypeRegistry,
      @Named("CFileType") FileType cFile,
      @Named("CppFileType") FileType cppFile,
      @Named("HFileType") FileType hFile) {
    fileTypeRegistry.registerFileType(cFile);
    fileTypeRegistry.registerFileType(cppFile);
    fileTypeRegistry.registerFileType(hFile);
  }

  @Inject
  private void prepareActions(
      CreateCSourceFileAction newCSourceFileAction,
      CreateCppSourceFileAction newCppSourceFileAction,
      CreateHeaderSourceFileAction newHeadSourceFileAction,
      ActionManager actionManager,
      CppResources resources,
      IconRegistry iconRegistry) {

    DefaultActionGroup newGroup = (DefaultActionGroup) actionManager.getAction(GROUP_FILE_NEW);

    actionManager.registerAction("newCFile", newCSourceFileAction);
    actionManager.registerAction("newCppFile", newCppSourceFileAction);
    actionManager.registerAction("newHFile", newHeadSourceFileAction);
    newGroup.add(newCSourceFileAction);
    newGroup.add(newHeadSourceFileAction);
    newGroup.add(newCppSourceFileAction);
    iconRegistry.registerIcon(
        new Icon(C_CATEGORY + ".samples.category.icon", resources.category()));
  }
}
