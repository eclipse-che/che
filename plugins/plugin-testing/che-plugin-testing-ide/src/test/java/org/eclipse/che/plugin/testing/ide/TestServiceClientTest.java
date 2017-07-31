/*******************************************************************************
 * Copyright (c) 2017 RedHat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   RedHat, Inc. - initial commit
 *******************************************************************************/
package org.eclipse.che.plugin.testing.ide;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.model.workspace.config.Command;
import org.eclipse.che.ide.api.command.exec.dto.ProcessStartResponseDto;
import org.eclipse.che.ide.api.command.exec.dto.event.DtoWithPid;
import org.eclipse.che.ide.api.command.exec.dto.event.ProcessDiedEventDto;
import org.eclipse.che.ide.api.command.exec.dto.event.ProcessStartedEventDto;
import org.eclipse.che.ide.api.command.exec.dto.event.ProcessStdErrEventDto;
import org.eclipse.che.ide.api.command.exec.dto.event.ProcessStdOutEventDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.api.promises.client.js.Executor;
import org.eclipse.che.api.testing.shared.TestResult;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.exec.ExecAgentCommandManager;
import org.eclipse.che.ide.api.command.exec.ExecAgentConsumer;
import org.eclipse.che.ide.api.macro.MacroProcessor;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.workspace.model.MachineImpl;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;
import org.eclipse.che.ide.command.goal.TestGoal;
import org.eclipse.che.ide.console.CommandConsoleFactory;
import org.eclipse.che.ide.console.CommandOutputConsole;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for the TestServiceClient class.
 *
 * @author David Festal
 */
@RunWith(GwtMockitoTestRunner.class)
public class TestServiceClientTest implements MockitoPrinter {

    // Context

    @Mock
    private AppContext                 appContext;
    @Mock
    private AsyncRequestFactory        asyncRequestFactory;
    @Mock
    private DtoUnmarshallerFactory     dtoUnmarshallerFactory;
    @Mock
    private CommandManager             commandManager;
    @Mock
    private ExecAgentCommandManager    execAgentCommandManager;
    @Mock
    private PromiseProvider            promiseProvider;
    @Mock
    private MacroProcessor             macroProcessor;
    @Mock
    private CommandConsoleFactory      commandConsoleFactory;
    @Mock
    private ProcessesPanelPresenter    processesPanelPresenter;
    @Mock
    private DtoFactory                 dtoFactory;
    @Mock
    private TestGoal                testGoal;

    @Mock
    private StatusNotification   statusNotification;
    @Mock
    private MachineImpl          devMachine;
    @Mock
    private MachineImpl          machine;
    @Mock
    private WorkspaceImpl        workspace;
    @Mock
    private CommandOutputConsole commandOutputConsole;

    private TestServiceClient          testServiceClient         = null;

    @Mock
    private RequestTransmitter requestTransmitter;

    @Spy
    private final List<DtoWithPid>     consoleEvents             = new ArrayList<>();

    private static final String        rootOfProjects            = "/projects";
    private Map<String, String>        parameters                = new HashMap<>();
    private String                     testFramework             = "junit";
    private String                     projectPath               = "sampleProject";

    @SuppressWarnings("rawtypes")
    private Map<Class< ? >, Operation> operationsOnProcessEvents = new HashMap<>();

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);

        testServiceClient = spy(new TestServiceClient(appContext, asyncRequestFactory, dtoUnmarshallerFactory, dtoFactory, commandManager,
                                                      execAgentCommandManager, promiseProvider, macroProcessor, commandConsoleFactory,
                processesPanelPresenter, testGoal, requestTransmitter));

        doReturn(new PromiseMocker<TestResult>().getPromise()).when(testServiceClient).sendTests(anyString(), anyString(),
                                                                                                 anyMapOf(String.class, String.class));
        doAnswer(new FunctionAnswer<Executor.ExecutorBody<TestResult>, Promise<TestResult>>(executorBody -> {
            ExecutorPromiseMocker<TestResult> mocker = new ExecutorPromiseMocker<TestResult>(executorBody,
                                                                                             (testResult, thisMocker) -> {
                                                                                                 thisMocker.applyOnThenOperation(testResult);
                                                                                                 return null;
                                                                                             },
                                                                                             (promiseError, thisMocker) -> {
                                                                                                 thisMocker.applyOnCatchErrorOperation(promiseError);
                                                                                                 return null;
                                                                                             });

            executorBody.apply(mocker.getResolveFunction(), mocker.getRejectFunction());

            return mocker.getPromise();
        })).when(testServiceClient).promiseFromExecutorBody(Matchers.<Executor.ExecutorBody<TestResult>> any());

        doAnswer(new FunctionAnswer<Throwable, PromiseError>(throwable -> {
            PromiseError promiseError = mock(PromiseError.class);
            when(promiseError.getCause()).thenReturn(throwable);
            return promiseError;
        })).when(testServiceClient).promiseFromThrowable(any(Throwable.class));

        when(appContext.getWorkspace()).thenReturn(workspace);
        when(workspace.getDevMachine()).thenReturn(Optional.of(devMachine));
        when(machine.getName()).thenReturn("DevMachineId");

        doAnswer(new FunctionAnswer<String, Promise<String>>(commandLine -> {
            String processedCommandLine = commandLine.replace("${current.project.path}", rootOfProjects + "/" + projectPath);
            return new PromiseMocker<String>().applyOnThenOperation(processedCommandLine).getPromise();
        })).when(macroProcessor).expandMacros(anyString());

        when(commandConsoleFactory.create(any(CommandImpl.class), anyString())).then(createCall -> {
            CommandOutputConsole commandOutputConsole = mock(CommandOutputConsole.class);
            when(commandOutputConsole.getProcessStartedConsumer()).thenReturn(processStartedEvent -> {
                consoleEvents.add(processStartedEvent);
            });
            when(commandOutputConsole.getProcessDiedConsumer()).thenReturn(processDiedEvent -> {
                consoleEvents.add(processDiedEvent);
            });
            when(commandOutputConsole.getStdErrConsumer()).thenReturn(processStdErrEvent -> {
                consoleEvents.add(processStdErrEvent);
            });
            when(commandOutputConsole.getStdOutConsumer()).thenReturn(processStdOutEvent -> {
                consoleEvents.add(processStdOutEvent);
            });
            return commandOutputConsole;
        });
        consoleEvents.clear();

        when(execAgentCommandManager.startProcess(anyString(), any(Command.class))).then(startProcessCall -> {
            @SuppressWarnings("unchecked")
            ExecAgentConsumer<ProcessStartResponseDto> execAgentConsumer =
                                                                       (ExecAgentConsumer<ProcessStartResponseDto>)mock(ExecAgentConsumer.class);
            class ProcessEventForward<DtoType> extends FunctionAnswer<Operation<DtoType>, ExecAgentConsumer<ProcessStartResponseDto>> {
                public ProcessEventForward(Class<DtoType> dtoClass) {
                    super(new java.util.function.Function<Operation<DtoType>, ExecAgentConsumer<ProcessStartResponseDto>>() {
                        @Override
                        public ExecAgentConsumer<ProcessStartResponseDto> apply(Operation<DtoType> op) {
                            operationsOnProcessEvents.put(dtoClass, op);
                            return execAgentConsumer;
                        }
                    });
                }
            }

            when(execAgentConsumer.then(any())).then(new ProcessEventForward<>(ProcessStartResponseDto.class));
            when(execAgentConsumer.thenIfProcessStartedEvent(any())).then(new ProcessEventForward<>(ProcessStartedEventDto.class));
            when(execAgentConsumer.thenIfProcessDiedEvent(any())).then(new ProcessEventForward<>(ProcessDiedEventDto.class));
            when(execAgentConsumer.thenIfProcessStdErrEvent(any())).then(new ProcessEventForward<>(ProcessStdErrEventDto.class));
            when(execAgentConsumer.thenIfProcessStdOutEvent(any())).then(new ProcessEventForward<>(ProcessStdOutEventDto.class));

            return execAgentConsumer;
        });
        operationsOnProcessEvents.clear();

        when(testGoal.getId()).thenReturn("Test");
    }

    @SuppressWarnings("unchecked")
    private void triggerProcessEvents(DtoWithPid... processEvents) {
        for (DtoWithPid event : processEvents) {
            operationsOnProcessEvents.entrySet().stream().filter(entry -> {
                return entry.getKey().isAssignableFrom(event.getClass());
            }).map(Map.Entry::getValue).forEach(op -> {
                try {
                    op.apply(event);
                } catch (OperationException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Test
    public void createCompileCommandFromStandardMavenCommands() {
        when(commandManager.getCommands()).thenReturn(asList(new CommandImpl("run",
                                                                             "mvn run -f ${current.project.path}",
                                                                             "mvn"),
                                                             new CommandImpl("build",
                                                                             "mvn clean install -f ${current.project.path}",
                                                                             "mvn")));
        testServiceClient.getOrCreateTestCompileCommand();
        verify(commandManager).createCommand("Test",
                                             "mvn",
                                             "test-compile",
                                             "mvn test-compile -f ${current.project.path}",
                                             Collections.emptyMap());
    }

    @Test
    public void createCompileCommandFromSCLEnabledMavenBuildCommand() {
        when(commandManager.getCommands()).thenReturn(asList(new CommandImpl("build",
                                                                             "scl enable rh-maven33 'mvn clean install -f ${current.project.path}'",
                                                                             "mvn")));
        testServiceClient.getOrCreateTestCompileCommand();
        verify(commandManager).createCommand("Test",
                                             "mvn",
                                             "test-compile",
                                             "scl enable rh-maven33 'mvn test-compile -f ${current.project.path}'",
                                             Collections.emptyMap());
    }

    @Test
    public void reuseExistingCompileCommand() {
        CommandImpl existingCompileCommand = new CommandImpl("test-compile",
                                                             "mvn test-compile -f ${current.project.path}",
                                                             "mvn");
        when(commandManager.getCommands()).thenReturn(asList(new CommandImpl("run",
                                                                             "mvn run -f ${current.project.path}",
                                                                             "mvn"),
                                                             new CommandImpl("build",
                                                                             "mvn clean install -f ${current.project.path}",
                                                                             "mvn"),
                                                             existingCompileCommand));

        testServiceClient.getOrCreateTestCompileCommand();

        verify(promiseProvider).resolve(existingCompileCommand);
    }

    @Test
    public void noBuildCommand() {
        when(commandManager.getCommands()).thenReturn(asList(new CommandImpl("customBuild",
                                                                             "mvn clean install -f ${current.project.path}",
                                                                             "mvn")));

        testServiceClient.getOrCreateTestCompileCommand();

        verify(promiseProvider).resolve(null);
    }

    @Test
    public void buildCommandNotAMavenCommand() {
        when(commandManager.getCommands()).thenReturn(asList(new CommandImpl("build",
                                                                             "mvn clean install -f ${current.project.path}",
                                                                             "someOtherType")));

        testServiceClient.getOrCreateTestCompileCommand();

        verify(promiseProvider).resolve(null);
    }

    @Test
    public void mavenBuildCommandHasNoCleanInstallPart() {
        when(commandManager.getCommands()).thenReturn(asList(new CommandImpl("build",
                                                                             "mvn clean SomeOtherGoalInTeMiddle install -f ${current.project.path}",
                                                                             "mvn")));

        testServiceClient.getOrCreateTestCompileCommand();

        verify(promiseProvider).resolve(null);
    }

    private Promise<CommandImpl> createCommandPromise(CommandImpl command) {
        return new PromiseMocker<CommandImpl>().applyOnThenPromise(command).getPromise();
    }

    @Test
    public void runTestsDirectlyBecauseNoCompilationCommand() {
        Promise<CommandImpl> compileCommandPromise = createCommandPromise(null);
        testServiceClient.runTestsAfterCompilation(projectPath, testFramework, parameters, statusNotification, compileCommandPromise);

        verify(statusNotification).setContent("Executing the tests without preliminary compilation.");
        verify(execAgentCommandManager, never()).startProcess(anyString(), Matchers.<Command> any());
        verify(testServiceClient).sendTests(projectPath, testFramework, parameters);
    }

    @Test
    public void runTestsDirectlyBecauseNoDevMachine() {
        Promise<CommandImpl> compileCommandPromise = createCommandPromise(new CommandImpl("test-compile",
                                                                                          "mvn test-compile -f ${current.project.path}",
                                                                                          "mvn"));

        when(workspace.getDevMachine()).thenReturn(Optional.empty());

        testServiceClient.runTestsAfterCompilation(projectPath, testFramework, parameters, statusNotification, compileCommandPromise);

        verify(statusNotification).setContent("Executing the tests without preliminary compilation.");
        verify(execAgentCommandManager, never()).startProcess(anyString(), Matchers.<Command> any());
        verify(testServiceClient).sendTests(projectPath, testFramework, parameters);
    }

    private ProcessStartResponseDto processStartResponse(boolean alive) {
        ProcessStartResponseDto event = mock(ProcessStartResponseDto.class);
        when(event.getAlive()).thenReturn(alive);
        return event;
    }

    private ProcessStartedEventDto processStarted() {
        ProcessStartedEventDto event = mock(ProcessStartedEventDto.class);
        when(event.toString()).thenReturn("Started");
        return event;
    }

    private ProcessDiedEventDto processDied() {
        ProcessDiedEventDto event = mock(ProcessDiedEventDto.class);
        when(event.toString()).thenReturn("Died");
        return event;
    }

    private ProcessStdErrEventDto processStdErr(final String text) {
        ProcessStdErrEventDto event = mock(ProcessStdErrEventDto.class);
        when(event.getText()).thenReturn(text);
        when(event.toString()).thenReturn("StdErr - " + text);
        return event;
    }

    private ProcessStdOutEventDto processStdOut(String text) {
        ProcessStdOutEventDto event = mock(ProcessStdOutEventDto.class);
        when(event.getText()).thenReturn(text);
        when(event.toString()).thenReturn("StdOut - " + text);
        return event;
    }


    @Test
    public void cancelledTestsBecauseCompilationNotStarted() {
        Promise<CommandImpl> compileCommandPromise = createCommandPromise(new CommandImpl(
                                                                                          "test-compile",
                                                                                          "mvn test-compile -f ${current.project.path}",
                                                                                          "mvn"));

        Promise<TestResult> result = testServiceClient.runTestsAfterCompilation(projectPath, testFramework, parameters, statusNotification,
                                                                                compileCommandPromise);

        triggerProcessEvents(processStartResponse(false));

        verify(testServiceClient, never()).sendTests(anyString(), anyString(), anyMapOf(String.class, String.class));
        verify(statusNotification).setContent("Compiling the project before starting the test session.");
        result.catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError promiseError) throws OperationException {
                Throwable cause = promiseError.getCause();
                Assert.assertNotNull(cause);
                Assert.assertEquals(TestServiceClient.PROJECT_BUILD_NOT_STARTED_MESSAGE, cause.getMessage());
            }
        });
    }

    @Test
    public void cancelledTestsBecauseCompilationFailed() {
        Promise<CommandImpl> compileCommandPromise = createCommandPromise(new CommandImpl(
                                                                                          "test-compile",
                                                                                          "mvn test-compile -f ${current.project.path}",
                                                                                          "mvn"));

        Promise<TestResult> result = testServiceClient.runTestsAfterCompilation(projectPath, testFramework, parameters, statusNotification,
                                                                                compileCommandPromise);

        triggerProcessEvents(processStartResponse(true),
                             processStarted(),
                             processStdErr("A small warning"),
                             processStdOut("BUILD FAILURE"),
                             processDied());

        verify(testServiceClient, never()).sendTests(anyString(), anyString(), anyMapOf(String.class, String.class));
        verify(statusNotification).setContent("Compiling the project before starting the test session.");
        result.catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError promiseError) throws OperationException {
                Throwable cause = promiseError.getCause();
                Assert.assertNotNull(cause);
                Assert.assertEquals(TestServiceClient.PROJECT_BUILD_FAILED_MESSAGE, cause.getMessage());
            }
        });
    }


    @Test
    @Ignore
    public void sucessfulTestsAfterCompilation() {

        Promise<CommandImpl> compileCommandPromise = createCommandPromise(new CommandImpl(
                                                                                          "test-compile",
                                                                                          "mvn test-compile -f ${current.project.path}",
                                                                                          "mvn"));

        Promise<TestResult> resultPromise = testServiceClient.runTestsAfterCompilation(projectPath, testFramework, parameters,
                                                                                       statusNotification,
                                                                                       compileCommandPromise);

        triggerProcessEvents(processStartResponse(true),
                             processStarted(),
                             processStdErr("A small warning"),
                             processStdOut("BUILD SUCCESS"),
                             processDied());

        verify(testServiceClient).sendTests(projectPath, testFramework, parameters);
        verify(statusNotification).setContent("Compiling the project before starting the test session.");
        verify(execAgentCommandManager).startProcess(
                                                     "DevMachineId",
                                                     new CommandImpl(
                                                                     "test-compile",
                                                                     "mvn test-compile -f " + rootOfProjects + "/" + projectPath,
                                                                     "mvn"));
        verify(statusNotification).setContent(TestServiceClient.EXECUTING_TESTS_MESSAGE);
        resultPromise.then(testResult -> {
            Assert.assertNotNull(testResult);
        });

        ArrayList<String> eventStrings = new ArrayList<>();
        for (DtoWithPid event : consoleEvents) {
            eventStrings.add(event.toString());
        }
        Assert.assertEquals(eventStrings,
                            Arrays.asList("Started",
                                          "StdErr - A small warning",
                                          "StdOut - BUILD SUCCESS",
                                          "Died"));
    }
}
