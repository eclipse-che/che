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
package org.eclipse.che.ide.command.node;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.command.CommandUtils;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.smartTree.data.HasAction;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;

/**
 * Extension of {@link AbstractCommandNode} that also acts as a {@link VirtualFile} for using it in
 * editor.
 */
public class CommandFileNode extends AbstractCommandNode implements HasAction, VirtualFile {

  /** Extension for the file type that represents a command. */
  public static final String FILE_TYPE_EXT = "che_command_internal";

  private final EditorAgent editorAgent;

  @Inject
  public CommandFileNode(
      @Assisted CommandImpl data, CommandUtils commandUtils, EditorAgent editorAgent) {
    super(data, null, commandUtils);

    this.editorAgent = editorAgent;
  }

  @Override
  public void updatePresentation(NodePresentation presentation) {
    super.updatePresentation(presentation);

    presentation.setPresentableText(getDisplayName());
    presentation.setPresentableTextCss("overflow: hidden; text-overflow: ellipsis;");
  }

  @Override
  public void actionPerformed() {
    editorAgent.openEditor(this);
  }

  @Override
  public Path getLocation() {
    return Path.valueOf("commands/" + getData().getType() + "/" + getData().getName());
  }

  @Override
  public String getName() {
    return getData().getName() + "." + FILE_TYPE_EXT;
  }

  @Override
  public String getDisplayName() {
    return getData().getName();
  }

  @Override
  public boolean isReadOnly() {
    return false;
  }

  @Override
  public String getContentUrl() {
    return null;
  }

  @Override
  public Promise<String> getContent() {
    return null;
  }

  @Override
  public Promise<Void> updateContent(String content) {
    return null;
  }
}
