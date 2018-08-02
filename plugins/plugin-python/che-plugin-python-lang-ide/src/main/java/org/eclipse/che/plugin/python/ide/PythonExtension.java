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
package org.eclipse.che.plugin.python.ide;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_FILE_NEW;
import static org.eclipse.che.plugin.python.shared.ProjectAttributes.PYTHON_CATEGORY;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.plugin.python.ide.action.CreatePythonFileAction;

/**
 * Python extension entry point.
 *
 * @author Valeriy Svydenko
 */
@Extension(title = "Python")
public class PythonExtension {
  @Inject
  public PythonExtension(
      FileTypeRegistry fileTypeRegistry,
      CreatePythonFileAction createPythonFileAction,
      ActionManager actionManager,
      PythonResources pythonResources,
      IconRegistry iconRegistry,
      @Named("PythonFileType") FileType pythonFile) {
    fileTypeRegistry.registerFileType(pythonFile);

    DefaultActionGroup newGroup = (DefaultActionGroup) actionManager.getAction(GROUP_FILE_NEW);
    actionManager.registerAction("pythonFile", createPythonFileAction);
    newGroup.add(createPythonFileAction);

    iconRegistry.registerIcon(
        new Icon(PYTHON_CATEGORY + ".samples.category.icon", pythonResources.category()));
  }
}
