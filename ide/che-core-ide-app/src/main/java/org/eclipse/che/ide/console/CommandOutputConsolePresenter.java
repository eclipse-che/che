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
package org.eclipse.che.ide.console;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.api.workspace.shared.Constants.COMMAND_PREVIEW_URL_ATTRIBUTE_NAME;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.event.shared.EventBus;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.eclipse.che.agent.exec.shared.dto.ProcessSubscribeResponseDto;
import org.eclipse.che.agent.exec.shared.dto.event.ProcessDiedEventDto;
import org.eclipse.che.agent.exec.shared.dto.event.ProcessStartedEventDto;
import org.eclipse.che.agent.exec.shared.dto.event.ProcessStdErrEventDto;
import org.eclipse.che.agent.exec.shared.dto.event.ProcessStdOutEventDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandExecutor;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.exec.ExecAgentCommandManager;
import org.eclipse.che.ide.api.command.exec.ProcessFinishedEvent;
import org.eclipse.che.ide.api.command.exec.ProcessStartedEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.macro.MacroProcessor;
import org.eclipse.che.ide.machine.MachineResources;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Console for command output.
 *
 * @author Artem Zatsarynnyi
 */
public class CommandOutputConsolePresenter
    implements CommandOutputConsole, OutputConsoleView.ActionDelegate {

  private final OutputConsoleView view;
  private final MachineResources resources;
  private final CommandImpl command;
  private final EventBus eventBus;
  private final String machineName;
  private final CommandExecutor commandExecutor;
  private final ExecAgentCommandManager execAgentCommandManager;

  private int pid;
  private boolean finished;

  /** Wrap text or not */
  private boolean wrapText = false;

  /** Follow output when printing text */
  private boolean followOutput = true;

  private final List<ActionDelegate> actionDelegates = new ArrayList<>();

  private OutputCustomizer outputCustomizer = null;

  @Inject
  public CommandOutputConsolePresenter(
      final OutputConsoleView view,
      MachineResources resources,
      CommandExecutor commandExecutor,
      MacroProcessor macroProcessor,
      EventBus eventBus,
      ExecAgentCommandManager execAgentCommandManager,
      @Assisted CommandImpl command,
      @Assisted String machineName,
      AppContext appContext,
      EditorAgent editorAgent) {
    this.view = view;
    this.resources = resources;
    this.execAgentCommandManager = execAgentCommandManager;
    this.command = command;
    this.machineName = machineName;
    this.eventBus = eventBus;
    this.commandExecutor = commandExecutor;

    setCustomizer(
        new CompoundOutputCustomizer(
            new JavaOutputCustomizer(appContext, editorAgent),
            new CSharpOutputCustomizer(appContext, editorAgent),
            new CPPOutputCustomizer(appContext, editorAgent)));

    view.setDelegate(this);

    final String previewUrl = command.getAttributes().get(COMMAND_PREVIEW_URL_ATTRIBUTE_NAME);
    if (!isNullOrEmpty(previewUrl)) {
      macroProcessor
          .expandMacros(previewUrl)
          .then(
              new Operation<String>() {
                @Override
                public void apply(String arg) throws OperationException {
                  view.showPreviewUrl(arg);
                }
              });
    } else {
      view.hidePreview();
    }

    view.showCommandLine(command.getCommandLine());
  }

  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(view);
  }

  @Override
  public CommandImpl getCommand() {
    return command;
  }

  @Nullable
  @Override
  public int getPid() {
    return pid;
  }

  @Override
  public String getTitle() {
    return command.getName();
  }

  @Override
  public SVGResource getTitleIcon() {
    return resources.output();
  }

  @Override
  public void listenToOutput(String wsChannel) {}

  @Override
  public Consumer<ProcessStdErrEventDto> getStdErrConsumer() {
    return event -> {
      String text = event.getText();
      boolean carriageReturn = text.endsWith("\r");
      String color = "red";
      view.print(text, carriageReturn, color);

      for (ActionDelegate actionDelegate : actionDelegates) {
        actionDelegate.onConsoleOutput(CommandOutputConsolePresenter.this);
      }
    };
  }

  @Override
  public Consumer<ProcessStdOutEventDto> getStdOutConsumer() {
    return event -> {
      String stdOutMessage = event.getText();
      boolean carriageReturn = stdOutMessage.endsWith("\r");
      view.print(stdOutMessage, carriageReturn);

      for (ActionDelegate actionDelegate : actionDelegates) {
        actionDelegate.onConsoleOutput(CommandOutputConsolePresenter.this);
      }
    };
  }

  @Override
  public Consumer<ProcessStartedEventDto> getProcessStartedConsumer() {
    return event -> {
      finished = false;
      view.enableStopButton(true);
      view.toggleScrollToEndButton(true);

      pid = event.getPid();

      eventBus.fireEvent(new ProcessStartedEvent(pid, machineName));
    };
  }

  @Override
  public Consumer<ProcessDiedEventDto> getProcessDiedConsumer() {
    return event -> {
      finished = true;
      view.enableStopButton(false);
      view.toggleScrollToEndButton(false);

      eventBus.fireEvent(new ProcessFinishedEvent(pid, machineName));
    };
  }

  @Override
  public Consumer<ProcessSubscribeResponseDto> getProcessSubscribeConsumer() {
    return process -> pid = process.getPid();
  }

  @Override
  public void printOutput(String output) {
    view.print(output.replaceAll("\\[STDOUT\\] ", ""), false);
  }

  @Override
  public boolean isFinished() {
    return finished;
  }

  @Override
  public void stop() {
    execAgentCommandManager.killProcess(machineName, pid);
  }

  @Override
  public void close() {
    actionDelegates.clear();
  }

  @Override
  public void addActionDelegate(ActionDelegate actionDelegate) {
    actionDelegates.add(actionDelegate);
  }

  @Override
  public void reRunProcessButtonClicked() {
    if (isFinished()) {
      commandExecutor.executeCommand(command, machineName);
    } else {
      execAgentCommandManager
          .killProcess(machineName, pid)
          .onSuccess(() -> commandExecutor.executeCommand(command, machineName));
    }
  }

  @Override
  public void stopProcessButtonClicked() {
    stop();
  }

  @Override
  public void clearOutputsButtonClicked() {
    view.clearConsole();
  }

  @Override
  public void downloadOutputsButtonClicked() {
    for (ActionDelegate actionDelegate : actionDelegates) {
      actionDelegate.onDownloadOutput(this);
    }
  }

  @Override
  public void wrapTextButtonClicked() {
    wrapText = !wrapText;
    view.wrapText(wrapText);
    view.toggleWrapTextButton(wrapText);
  }

  @Override
  public void scrollToBottomButtonClicked() {
    followOutput = !followOutput;

    view.toggleScrollToEndButton(followOutput);
    view.enableAutoScroll(followOutput);
  }

  @Override
  public void onOutputScrolled(boolean bottomReached) {
    followOutput = bottomReached;
    view.toggleScrollToEndButton(bottomReached);
  }

  /**
   * Returns the console text.
   *
   * @return console text
   */
  public String getText() {
    return view.getText();
  }

  @Override
  public OutputCustomizer getCustomizer() {
    return outputCustomizer;
  }

  /** Sets up the text output customizer */
  public void setCustomizer(OutputCustomizer customizer) {
    this.outputCustomizer = customizer;
  }
}
