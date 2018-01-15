/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.languageserver.ide.rename;

import com.google.inject.ImplementedBy;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.languageserver.shared.model.ExtendedTextDocumentEdit;
import org.eclipse.che.api.languageserver.shared.model.ExtendedWorkspaceEdit;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;
import org.eclipse.che.plugin.languageserver.ide.rename.RenameView.ActionDelegate;
import org.eclipse.che.plugin.languageserver.ide.rename.model.RenameProject;

/** View for rename edits tree */
@ImplementedBy(RenameViewImpl.class)
public interface RenameView extends View<ActionDelegate> {

  void showRenameResult(Map<String, ExtendedWorkspaceEdit> results);

  List<RenameProject> getRenameProjects();

  interface ActionDelegate extends BaseActionDelegate {

    List<RenameProject> convert(List<ExtendedTextDocumentEdit> documentChanges);

    void cancel();

    void applyRename();
  }
}
