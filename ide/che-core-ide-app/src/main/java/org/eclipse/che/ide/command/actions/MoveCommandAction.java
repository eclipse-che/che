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
package org.eclipse.che.ide.command.actions;

import static org.eclipse.che.ide.FontAwesome.SHARE_SQUARE;

import com.google.gwt.core.client.Scheduler;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.DelayedTask;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.command.editor.CommandEditor;
import org.eclipse.che.ide.command.editor.page.goal.GoalPage;
import org.eclipse.che.ide.command.explorer.CommandsExplorerPresenter;
import org.eclipse.che.ide.command.explorer.CommandsExplorerView;
import org.eclipse.che.ide.command.node.NodeFactory;

@Singleton
public class MoveCommandAction extends BaseAction {

  private CommandsExplorerPresenter commandsExplorer;
  private NodeFactory nodeFactory;
  private EditorAgent editorAgent;

  @Inject
  public MoveCommandAction(
      CommandsExplorerPresenter commandsExplorer,
      NodeFactory nodeFactory,
      EditorAgent editorAgent,
      ActionMessages messages) {
    super(messages.moveCommandActionTitle(), messages.moveCommandActionDescription(), SHARE_SQUARE);
    this.commandsExplorer = commandsExplorer;
    this.nodeFactory = nodeFactory;
    this.editorAgent = editorAgent;
  }

  @Override
  public void update(ActionEvent e) {
    CommandImpl command = ((CommandsExplorerView) commandsExplorer.getView()).getSelectedCommand();

    e.getPresentation().setEnabledAndVisible(command != null);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    CommandImpl command = ((CommandsExplorerView) commandsExplorer.getView()).getSelectedCommand();

    editorAgent.openEditor(
        nodeFactory.newCommandFileNode(command),
        new EditorAgent.OpenEditorCallback() {
          @Override
          public void onEditorOpened(EditorPartPresenter editor) {}

          @Override
          public void onEditorActivated(EditorPartPresenter editor) {
            if (editor instanceof CommandEditor) {
              new DelayedTask() {
                @Override
                public void onExecute() {
                  ((CommandEditor) editor)
                      .getPages()
                      .stream()
                      .filter(page -> page instanceof GoalPage)
                      .findFirst()
                      .ifPresent(page -> Scheduler.get().scheduleFinally(page::focus));
                }
              }.delay(500);
            }
          }

          @Override
          public void onInitializationFailed() {}
        });
  }
}
