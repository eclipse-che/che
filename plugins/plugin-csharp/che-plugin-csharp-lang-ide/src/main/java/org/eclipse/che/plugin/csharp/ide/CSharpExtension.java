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
package org.eclipse.che.plugin.csharp.ide;

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
import org.eclipse.che.plugin.csharp.ide.action.CreateCSharpSourceFileAction;

/** @author Anatolii Bazko */
@Extension(title = "C#")
public class CSharpExtension {

  public static String CSHARP_CATEGORY = "C#";

  @Inject
  public CSharpExtension(
      FileTypeRegistry fileTypeRegistry, @Named("CSharpFileType") FileType csharpFile) {
    fileTypeRegistry.registerFileType(csharpFile);
  }

  @Inject
  private void prepareActions(
      CreateCSharpSourceFileAction csharpSourceFileAction,
      ActionManager actionManager,
      CSharpResources resources,
      IconRegistry iconRegistry) {

    DefaultActionGroup newGroup = (DefaultActionGroup) actionManager.getAction(GROUP_FILE_NEW);

    actionManager.registerAction("newCSharpFile", csharpSourceFileAction);
    newGroup.add(csharpSourceFileAction);
    iconRegistry.registerIcon(
        new Icon(CSHARP_CATEGORY + ".samples.category.icon", resources.category()));
  }
}
