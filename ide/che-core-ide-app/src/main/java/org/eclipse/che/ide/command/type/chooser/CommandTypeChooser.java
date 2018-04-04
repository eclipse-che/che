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
package org.eclipse.che.ide.command.type.chooser;

import static java.util.Comparator.comparing;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.api.promises.client.js.Executor;
import org.eclipse.che.api.promises.client.js.Executor.ExecutorBody;
import org.eclipse.che.api.promises.client.js.JsPromiseError;
import org.eclipse.che.api.promises.client.js.RejectFunction;
import org.eclipse.che.api.promises.client.js.ResolveFunction;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.command.CommandTypeRegistry;
import org.eclipse.che.ide.command.type.CommandTypeMessages;

/**
 * Provides a simple mechanism for the user to choose a {@link CommandType}.
 *
 * @author Artem Zatsarynnyi
 */
public class CommandTypeChooser implements CommandTypeChooserView.ActionDelegate {

  private final CommandTypeChooserView view;
  private final CommandTypeRegistry commandTypeRegistry;
  private final PromiseProvider promiseProvider;
  private final CommandTypeMessages messages;

  private ResolveFunction<CommandType> resolveFunction;
  private RejectFunction rejectFunction;

  @Inject
  public CommandTypeChooser(
      CommandTypeChooserView view,
      CommandTypeRegistry commandTypeRegistry,
      PromiseProvider promiseProvider,
      CommandTypeMessages messages) {
    this.view = view;
    this.commandTypeRegistry = commandTypeRegistry;
    this.promiseProvider = promiseProvider;
    this.messages = messages;

    view.setDelegate(this);
  }

  /**
   * Pops up a command type chooser dialog at the position relative to the browser's client area.
   *
   * @param left the left position, in pixels
   * @param top the top position, in pixels
   * @return promise that will be resolved with a chosen {@link CommandType} or rejected in case
   *     command type selection has been cancelled
   */
  public Promise<CommandType> show(int left, int top) {
    final List<CommandType> commandTypes = new ArrayList<>(commandTypeRegistry.getCommandTypes());

    if (commandTypes.size() == 1) {
      return promiseProvider.resolve(commandTypes.get(0));
    }

    commandTypes.sort(comparing(CommandType::getDisplayName));

    view.setCommandTypes(commandTypes);

    view.show(left, top);

    return promiseProvider.create(
        Executor.create(
            (ExecutorBody<CommandType>)
                (resolve, reject) -> {
                  resolveFunction = resolve;
                  rejectFunction = reject;
                }));
  }

  @Override
  public void onSelected(CommandType commandType) {
    view.close();

    resolveFunction.apply(commandType);
  }

  @Override
  public void onCanceled() {
    rejectFunction.apply(JsPromiseError.create(messages.typeChooserMessageCanceled()));
  }
}
