/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.testing.ide;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.machine.shared.dto.execagent.ProcessStartResponseDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.api.promises.client.js.Executor;
import org.eclipse.che.api.promises.client.js.Executor.ExecutorBody;
import org.eclipse.che.api.promises.client.js.JsPromiseError;
import org.eclipse.che.api.promises.client.js.RejectFunction;
import org.eclipse.che.api.promises.client.js.ResolveFunction;
import org.eclipse.che.api.testing.shared.TestResult;
import org.eclipse.che.ide.MimeType;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.machine.ExecAgentCommandManager;
import org.eclipse.che.ide.api.machine.execagent.ExecAgentPromise;
import org.eclipse.che.ide.api.macro.MacroProcessor;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.command.goal.TestGoal;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.console.CommandConsoleFactory;
import org.eclipse.che.ide.console.CommandOutputConsole;
import org.eclipse.che.ide.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.HTTPHeader;

import com.google.gwt.http.client.URL;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import static org.eclipse.che.api.workspace.shared.Constants.COMMAND_PREVIEW_URL_ATTRIBUTE_NAME;

/**
 * Client for calling test services
 *
 * @author Mirage Abeysekara
 * @author David Festal
 */
@Singleton
public class TestServiceClient {

    private final static RegExp           mavenCleanBuildPattern            =
                                                                 RegExp.compile("(.*)mvn +clean +install +(\\-f +\\$\\{current\\.project\\.path\\}.*)");

    public static final String            PROJECT_BUILD_NOT_STARTED_MESSAGE = "The project build could not be started (see Build output). "
                                                                              + "Test run is cancelled.\n"
                                                                              + "You should probably check the settings of the 'test-compile' command.";

    public static final String            PROJECT_BUILD_FAILED_MESSAGE      = "The project build failed (see Build output). "
                                                                              + "Test run is cancelled.\n"
                                                                              + "You might want to check the settings of the 'test-compile' command.";

    public static final String            EXECUTING_TESTS_MESSAGE           = "Executing test session.";


    private final AppContext              appContext;
    private final AsyncRequestFactory     asyncRequestFactory;
    private final DtoUnmarshallerFactory  dtoUnmarshallerFactory;
    private final CommandManager          commandManager;
    private final ExecAgentCommandManager execAgentCommandManager;
    private final PromiseProvider         promiseProvider;
    private final MacroProcessor          macroProcessor;
    private final CommandConsoleFactory   commandConsoleFactory;
    private final ProcessesPanelPresenter processesPanelPresenter;
    private final TestGoal                testGoal;


    @Inject
    public TestServiceClient(AppContext appContext,
                             AsyncRequestFactory asyncRequestFactory,
                             DtoUnmarshallerFactory dtoUnmarshallerFactory,
                             DtoFactory dtoFactory,
                             CommandManager commandManager,
                             ExecAgentCommandManager execAgentCommandManager,
                             PromiseProvider promiseProvider,
                             MacroProcessor macroProcessor,
                             CommandConsoleFactory commandConsoleFactory,
                             ProcessesPanelPresenter processesPanelPresenter,
                             TestGoal testGoal) {
        this.appContext = appContext;
        this.asyncRequestFactory = asyncRequestFactory;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.commandManager = commandManager;
        this.execAgentCommandManager = execAgentCommandManager;
        this.promiseProvider = promiseProvider;
        this.macroProcessor = macroProcessor;
        this.commandConsoleFactory = commandConsoleFactory;
        this.processesPanelPresenter = processesPanelPresenter;
        this.testGoal = testGoal;
    }

    public Promise<CommandImpl> getOrCreateTestCompileCommand() {
        List<CommandImpl> commands = commandManager.getCommands();

        for (CommandImpl command : commands) {
            if (command.getName() != null && command.getName().startsWith("test-compile") && "mvn".equals(command.getType())) {
                return promiseProvider.resolve(command);
            }
        }
        for (CommandImpl command : commands) {
            if ("build".equals(command.getName()) && "mvn".equals(command.getType())) {
                String commandLine = command.getCommandLine();
                MatchResult result = mavenCleanBuildPattern.exec(commandLine);
                if (result != null) {
                    String testCompileCommandLine = mavenCleanBuildPattern.replace(commandLine, "$1mvn test-compile $2");
                    return commandManager.createCommand(testGoal.getId(), "mvn", "test-compile", testCompileCommandLine, new HashMap<String, String>());
                }
            }
        }
        return promiseProvider.resolve(null);
    }

    public Promise<TestResult> getTestResult(String projectPath, String testFramework, Map<String, String> parameters) {
        return getTestResult(projectPath, testFramework, parameters, null);
    }

    Promise<TestResult> promiseFromExecutorBody(ExecutorBody<TestResult> executorBody) {
        return promiseProvider.create(Executor.create(executorBody));
    }

    PromiseError promiseFromThrowable(Throwable t) {
        return JsPromiseError.create(t);
    }

    Promise<TestResult> runTestsAfterCompilation(String projectPath,
                                                 String testFramework,
                                                 Map<String, String> parameters,
                                                 StatusNotification statusNotification,
                                                 Promise<CommandImpl> compileCommand) {
        return compileCommand.thenPromise(command -> {
            final Machine machine;
            if (command == null) {
                machine = null;
            } else {
                machine = appContext.getDevMachine().getDescriptor();
            }
            if (machine == null) {
                if (statusNotification != null) {
                    statusNotification.setContent("Executing the tests without preliminary compilation.");
                }
                return sendTests(projectPath, testFramework, parameters);
            }

            if (statusNotification != null) {
                statusNotification.setContent("Compiling the project before starting the test session.");
            }
            return promiseFromExecutorBody(new ExecutorBody<TestResult>() {
                boolean compiled = false;

                @Override
                public void apply(final ResolveFunction<TestResult> resolve, RejectFunction reject) {
                    macroProcessor.expandMacros(command.getCommandLine()).then(new Operation<String>() {
                        @Override
                        public void apply(String expandedCommandLine) throws OperationException {
                            Map<String, String> attributes = new HashMap<>();
                            attributes.putAll(command.getAttributes());
                            attributes.remove(COMMAND_PREVIEW_URL_ATTRIBUTE_NAME);

                            CommandImpl expandedCommand = new CommandImpl(command.getName(), expandedCommandLine,
                                                                          command.getType(), attributes);

                            final CommandOutputConsole console = commandConsoleFactory.create(expandedCommand, machine);
                            final String machineId = machine.getId();

                            processesPanelPresenter.addCommandOutput(machineId, console);
                            ExecAgentPromise<ProcessStartResponseDto> processPromise = execAgentCommandManager.startProcess(machineId,
                                                                                                                            expandedCommand);
                            processPromise.then(startResonse -> {
                                if (!startResonse.getAlive()) {
                                    reject.apply(promiseFromThrowable(new Throwable(PROJECT_BUILD_NOT_STARTED_MESSAGE)));
                                }
                            }).thenIfProcessStartedEvent(console.getProcessStartedOperation()).thenIfProcessStdErrEvent(evt -> {
                                if (evt.getText().contains("BUILD SUCCESS")) {
                                    compiled = true;
                                }
                                console.getStdErrOperation().apply(evt);
                            }).thenIfProcessStdOutEvent(evt -> {
                                if (evt.getText().contains("BUILD SUCCESS")) {
                                    compiled = true;
                                }
                                console.getStdOutOperation().apply(evt);
                            }).thenIfProcessDiedEvent(evt -> {
                                console.getProcessDiedOperation().apply(evt);
                                if (compiled) {
                                    if (statusNotification != null) {
                                        statusNotification.setContent(EXECUTING_TESTS_MESSAGE);
                                    }
                                    sendTests(projectPath,
                                              testFramework,
                                              parameters).then(new Operation<TestResult>() {
                                                  @Override
                                                  public void apply(TestResult result) throws OperationException {
                                                      resolve.apply(result);
                                                  }
                                              }, new Operation<PromiseError>() {
                                                  @Override
                                                  public void apply(PromiseError error) throws OperationException {
                                                      reject.apply(error);
                                                  }
                                              });
                                } else {
                                    reject.apply(promiseFromThrowable(new Throwable(PROJECT_BUILD_FAILED_MESSAGE)));
                                }
                            });
                        }
                    });
                }
            });
        });
    }

    public Promise<TestResult> getTestResult(String projectPath,
                                             String testFramework,
                                             Map<String, String> parameters,
                                             StatusNotification statusNotification) {
        return runTestsAfterCompilation(projectPath, testFramework, parameters, statusNotification,
                                        getOrCreateTestCompileCommand());
    }

    public Promise<TestResult> sendTests(String projectPath, String testFramework, Map<String, String> parameters) {
        StringBuilder sb = new StringBuilder();
        if (parameters != null) {
            for (Map.Entry<String, String> e : parameters.entrySet()) {
                if (sb.length() > 0) {
                    sb.append('&');
                }
                sb.append(URL.encode(e.getKey())).append('=').append(URL.encode(e.getValue()));
            }
        }
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + "/che/testing/run/?projectPath=" + projectPath
                     + "&testFramework=" + testFramework + "&" + sb.toString();
        return asyncRequestFactory.createGetRequest(url).header(HTTPHeader.ACCEPT, MimeType.APPLICATION_JSON)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(TestResult.class));
    }

}
