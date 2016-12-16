---
tags: [ "eclipse" , "che" ]
title: Workspace Access
excerpt: ""
layout: docs
permalink: /:categories/native-access-to-the-workspace/
---
One of the core strengths of Che is the workspace concept. Additionally to project files, such as source code, Che also allows to share workspaces, which provide the necessary runtime to compile and run the system under development. Workspaces can also provide any other kind of tooling to work on a project, this tooling can then be triggered in the Che IDE.

The concept of workspaces makes it very convenient to reuse any existing tool in Che, as those tools can be executed on the workspace runtime. Che already provides several of those tool integrations, such as Maven or Git.

There are two ways to integrate tools running on the workspace: First, you can provide a custom workspace service, which triggers the native workspace call and provides an API to be used by the client IDE. Second, you can use the existing command support of Che, which already allows to execute native commands on the workspace without implementing a custom service. In this tutorial, we will demonstrate the second option, please refer to this tutorial [this tutorial](doc:serverworkspace-access) to learn how to implement custom services.

In the following, we implement a simple command, which creates a new file called "che-was-here" using the native "touch" command. The command is finally called by a sample action, however, we encapsulate the execution of the command into a dedicated class called `CommandManager` which can execute arbitrary commands on the workspace (see following listing).

To execute a command, Che provides the service `MachineServiceClient`. To execute a command, it requires the ID of the machine the command should be executed on, the command to be executed, an output channel and a callback.

The ID can be retrieved from the `AppContext`. The `Command` is created using the Che `DtoFactory`. The parameter `CommandLine` specifies the native command to be executed. Finally, we create a sample output channel and execute the command with an empty call back.
```java  
@Singleton
public class CommandManager {

    private final DtoFactory dtoFactory;
    private final MachineServiceClient machineServiceClient;
    private final NotificationManager notificationManager;
    private final AppContext appContext;

    @Inject
    public CommandManager(DtoFactory dtoFactory, MachineServiceClient machineServiceClient, NotificationManager notificationManager, AppContext appContext) {
        this.dtoFactory = dtoFactory;
        this.machineServiceClient = machineServiceClient;
        this.notificationManager = notificationManager;
        this.appContext = appContext;
    }

    /** Execute the the given command configuration on the developer machine. */
    public void execute(String commandLine) {
        final Machine machine = appContext.getDevMachine().getDescriptor();
        if (machine == null) {
            return;
        }
        String machineID = machine.getId();
        final CommandDto command = dtoFactory.createDto(CommandDto.class)
                .withName("some-command")
                .withCommandLine(commandLine)
                .withType("arbitrary-type");
        final String outputChannel = "process:output:" + UUID.uuid();
        executeCommand(command, machineID, outputChannel);
    }

    public void executeCommand(final CommandDto command, @NotNull final String machineID, String outputChannel) {
        final Promise<MachineProcessDto> processPromise = machineServiceClient.executeCommand(machineID, command, outputChannel);
        processPromise.then(new Operation<MachineProcessDto>() {
            @Override
            public void apply(MachineProcessDto process) throws OperationException
            {
                //Do nothing in this example
            }

        });

    }
}
```
Now, the `CommandManager` can be used to trigger any kind of command line operation. The following example action uses the native "touch" command to create a new file.
```java  
@Singleton
public class RunNativeCommandAction extends Action {

    public final static String ACTION_ID = "runNativeCommandSAction";

    private CommandManager commandManager;

    @Inject
    public RunNativeCommandAction(CommandManager commandManager) {
        super("Run native command demo");
        this.commandManager = commandManager;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Log.warn(getClass(), "Executing native command...");
        commandManager.execute("cd && touch che-was-here");
    }

}
```
