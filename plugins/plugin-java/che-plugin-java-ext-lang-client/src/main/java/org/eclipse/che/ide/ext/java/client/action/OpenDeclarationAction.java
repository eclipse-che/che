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
package org.eclipse.che.ide.ext.java.client.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.editor.OpenDeclarationFinder;

/**
 * Invoke open declaration action ofr java element
 *
 * @author Evgen Vidolob
 */
@Singleton
public class OpenDeclarationAction extends JavaEditorAction {

  private OpenDeclarationFinder declarationFinder;

  @Inject
  public OpenDeclarationAction(
      JavaLocalizationConstant constant,
      EditorAgent editorAgent,
      OpenDeclarationFinder declarationFinder,
      JavaResources resources,
      FileTypeRegistry fileTypeRegistry) {
    super(
        constant.actionOpenDeclarationTitle(),
        constant.actionOpenDeclarationDescription(),
        resources.openDeclaration(),
        editorAgent,
        fileTypeRegistry);
    this.declarationFinder = declarationFinder;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    declarationFinder.openDeclaration();
  }
}
