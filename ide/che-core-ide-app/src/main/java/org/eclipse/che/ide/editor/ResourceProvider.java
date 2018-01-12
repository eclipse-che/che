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
package org.eclipse.che.ide.editor;

import static java.util.Optional.ofNullable;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Optional;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.command.node.CommandFileNode;
import org.eclipse.che.ide.command.node.NodeFactory;

/**
 * Provides resource ({@link VirtualFile}) by it's path. Used internally by {@link EditorAgentImpl}
 * for restoring opened editors.
 */
@Singleton
class ResourceProvider {

  private final AppContext appContext;
  private final CommandManager commandManager;
  private final NodeFactory nodeFactory;
  private final PromiseProvider promiseProvider;

  @Inject
  public ResourceProvider(
      AppContext appContext,
      CommandManager commandManager,
      NodeFactory nodeFactory,
      PromiseProvider promiseProvider) {
    this.appContext = appContext;
    this.commandManager = commandManager;
    this.nodeFactory = nodeFactory;
    this.promiseProvider = promiseProvider;
  }

  Promise<Optional<VirtualFile>> getResource(String path) {
    if (path.startsWith("commands/")) {
      final String commandName = path.substring(path.lastIndexOf('/') + 1);
      final Optional<CommandImpl> command = commandManager.getCommand(commandName);

      if (command.isPresent()) {
        CommandFileNode node = nodeFactory.newCommandFileNode(command.get());
        return promiseProvider.resolve(Optional.of(node));
      } else {
        return promiseProvider.reject(new Exception("Command " + commandName + " not found"));
      }
    } else {
      return appContext
          .getWorkspaceRoot()
          .getFile(path)
          .then(
              (Function<com.google.common.base.Optional<File>, Optional<VirtualFile>>)
                  arg -> ofNullable(arg.orNull()));
    }
  }
}
