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
package org.eclipse.che.plugin.php.ide;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_FILE_NEW;
import static org.eclipse.che.plugin.php.shared.Constants.PHP_CATEGORY;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.plugin.php.ide.action.CreatePhpSourceFileAction;

/** @author Kaloyan Raev */
@Extension(title = "PHP")
public class PhpExtension {

  @Inject
  public PhpExtension(FileTypeRegistry fileTypeRegistry, @Named("PhpFileType") FileType phpFile) {
    fileTypeRegistry.registerFileType(phpFile);
  }

  @Inject
  private void prepareActions(
      CreatePhpSourceFileAction phpSourceFileAction,
      ActionManager actionManager,
      PhpResources resources,
      IconRegistry iconRegistry) {

    DefaultActionGroup newGroup = (DefaultActionGroup) actionManager.getAction(GROUP_FILE_NEW);

    actionManager.registerAction("newPhpFile", phpSourceFileAction);
    newGroup.add(phpSourceFileAction);
    iconRegistry.registerIcon(
        new Icon(PHP_CATEGORY + ".samples.category.icon", resources.category()));
  }
}
