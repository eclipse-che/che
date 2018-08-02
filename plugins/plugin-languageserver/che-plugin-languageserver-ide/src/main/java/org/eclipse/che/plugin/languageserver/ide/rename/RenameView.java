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
