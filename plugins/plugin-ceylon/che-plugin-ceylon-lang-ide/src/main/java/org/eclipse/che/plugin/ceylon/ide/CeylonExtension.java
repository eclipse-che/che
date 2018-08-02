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
package org.eclipse.che.plugin.ceylon.ide;

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
import org.eclipse.che.plugin.ceylon.ide.action.CreateCeylonFileAction;

/**
 * Python extension entry point.
 *
 * @author David Festal
 */
@Extension(title = "Ceylon")
public class CeylonExtension {

  public static String CEYLON_CATEGORY = "Ceylon";

  @Inject
  public CeylonExtension(
      FileTypeRegistry fileTypeRegistry,
      CreateCeylonFileAction createCeylonFileAction,
      ActionManager actionManager,
      CeylonResources ceylonResources,
      IconRegistry iconRegistry,
      @Named("CeylonFileType") FileType ceylonFile) {
    fileTypeRegistry.registerFileType(ceylonFile);
  }

  @Inject
  private void prepareActions(
      CreateCeylonFileAction createCeylonFileAction,
      ActionManager actionManager,
      CeylonResources resources,
      IconRegistry iconRegistry) {

    DefaultActionGroup newGroup = (DefaultActionGroup) actionManager.getAction(GROUP_FILE_NEW);

    actionManager.registerAction("newCeylonFile", createCeylonFileAction);
    newGroup.add(createCeylonFileAction);
    iconRegistry.registerIcon(
        new Icon(CEYLON_CATEGORY + ".samples.category.icon", resources.category()));
  }
}
