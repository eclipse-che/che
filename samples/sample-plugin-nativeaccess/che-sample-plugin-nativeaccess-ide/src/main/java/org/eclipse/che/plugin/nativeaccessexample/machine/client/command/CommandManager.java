package org.eclipse.che.plugin.nativeaccessexample.machine.client.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.MachineServiceClient;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.util.UUID;

import javax.validation.constraints.NotNull;

/**
 * Simple command manager which allows to run native commands within the workspace Docker container.
 * Please note that the actual call is delegated to the MachineServiceClient service.
 *
 * @author Mathias Schaefer <mathias.schaefer@eclipsesource.com>
 */
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

    /**
     * Execute the the given command command within the workspace Docker container.
     */
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
