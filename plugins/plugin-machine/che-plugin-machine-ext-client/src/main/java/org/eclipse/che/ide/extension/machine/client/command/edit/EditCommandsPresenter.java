/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.extension.machine.client.command.edit;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.gwt.client.WorkspaceServiceClient;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.actions.SelectCommandComboBox;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfiguration;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfigurationPage;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfigurationPage.DirtyStateListener;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfigurationPage.FieldStateActionDelegate;
import org.eclipse.che.ide.extension.machine.client.command.CommandManager;
import org.eclipse.che.ide.extension.machine.client.command.CommandType;
import org.eclipse.che.ide.extension.machine.client.command.CommandTypeRegistry;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.dialogs.ChoiceDialog;
import org.eclipse.che.ide.api.dialogs.ConfirmDialog;
import org.eclipse.che.ide.util.loging.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Presenter for managing commands.
 *
 * @author Artem Zatsarynnyi
 * @author Oleksii Orel
 * @author Valeriy Svydenko
 */
@Singleton
public class EditCommandsPresenter implements EditCommandsView.ActionDelegate, FieldStateActionDelegate {

    public static final String PREVIEW_URL_ATTR = "previewUrl";

    private final EditCommandsView                               view;
    private final WorkspaceServiceClient                         workspaceServiceClient;
    private final CommandManager                                 commandManager;
    private final DtoFactory                                     dtoFactory;
    private final CommandTypeRegistry                            commandTypeRegistry;
    private final DialogFactory                                  dialogFactory;
    private final MachineLocalizationConstant                    machineLocale;
    private final CoreLocalizationConstant                       coreLocale;
    private final Provider<SelectCommandComboBox>                selectCommandActionProvider;
    private final Set<ConfigurationChangedListener>              configurationChangedListeners;
    private final AppContext                                     appContext;
    /** Set of the existing command names. */
    private final Set<String>                                    commandNames;
    private       CommandConfigurationPage<CommandConfiguration> editedPage;
    /** Command that being edited. */
    private       CommandConfiguration                           editedCommand;
    /** Name of the edited command before editing. */
    String                    editedCommandOriginName;
    String                    editedCommandOriginPreviewUrl;
    String                    workspaceId;
    CommandProcessingCallback commandProcessingCallback;

    @Inject
    protected EditCommandsPresenter(EditCommandsView view,
                                    WorkspaceServiceClient workspaceServiceClient,
                                    CommandTypeRegistry commandTypeRegistry,
                                    DialogFactory dialogFactory,
                                    MachineLocalizationConstant machineLocale,
                                    CoreLocalizationConstant coreLocale,
                                    Provider<SelectCommandComboBox> selectCommandActionProvider,
                                    CommandManager commandManager,
                                    AppContext appContext,
                                    DtoFactory dtoFactory) {
        this.view = view;
        this.workspaceServiceClient = workspaceServiceClient;
        this.commandManager = commandManager;
        this.dtoFactory = dtoFactory;
        this.commandTypeRegistry = commandTypeRegistry;
        this.dialogFactory = dialogFactory;
        this.machineLocale = machineLocale;
        this.coreLocale = coreLocale;
        this.selectCommandActionProvider = selectCommandActionProvider;
        this.view.setDelegate(this);
        this.appContext = appContext;
        configurationChangedListeners = new HashSet<>();
        commandNames = new HashSet<>();
    }

    @Override
    public void onCloseClicked() {
        final CommandConfiguration selectedConfiguration = view.getSelectedConfiguration();
        onNameChanged();
        if (selectedConfiguration != null && isViewModified()) {
            onConfigurationSelected(selectedConfiguration);
        }
        view.close();
    }

    private void selectCommandOnToolbar(CommandConfiguration commandToSelect) {
        selectCommandActionProvider.get().setSelectedCommand(commandToSelect);
    }

    @Override
    public void onSaveClicked() {
        final CommandConfiguration selectedConfiguration;
        if (view.getSelectedConfiguration() == null) {
            return;
        }
        onNameChanged();
        selectedConfiguration = view.getSelectedConfiguration();

        updateCommand(selectedConfiguration).then(new Operation<WorkspaceDto>() {
            @Override
            public void apply(WorkspaceDto arg) throws OperationException {
                commandProcessingCallback = getCommandProcessingCallback();
                fetchCommands();
                fireConfigurationUpdated(selectedConfiguration);
            }
        });
    }

    private Promise<WorkspaceDto> updateCommand(final CommandConfiguration selectedConfiguration) {
        final CommandDto commandDto = dtoFactory.createDto(CommandDto.class)
                                                .withName(selectedConfiguration.getName())
                                                .withCommandLine(selectedConfiguration.toCommandLine())
                                                .withType(selectedConfiguration.getType().getId())
                                                .withAttributes(selectedConfiguration.getAttributes());

        if (editedCommandOriginName.trim().equals(selectedConfiguration.getName())) {
            return workspaceServiceClient.updateCommand(workspaceId, selectedConfiguration.getName(), commandDto);
        } else {
            onNameChanged();
            //generate a new unique name if input one already exists
            final String newName = getUniqueCommandName(selectedConfiguration.getType(), selectedConfiguration.getName());

            if (selectedConfiguration.equals(view.getSelectedConfiguration())) {
                //update selected configuration name
                view.getSelectedConfiguration().setName(newName);
            }

            return workspaceServiceClient.deleteCommand(workspaceId, editedCommandOriginName)
                                         .thenPromise(new Function<WorkspaceDto, Promise<WorkspaceDto>>() {
                                             @Override
                                             public Promise<WorkspaceDto> apply(WorkspaceDto arg) throws FunctionException {
                                                 commandDto.setName(newName);
                                                 return workspaceServiceClient.addCommand(workspaceId, commandDto);
                                             }
                                         });
        }
    }

    @Override
    public void onCancelClicked() {
        commandProcessingCallback = getCommandProcessingCallback();
        fetchCommands();
    }

    @Override
    public void onDuplicateClicked() {
        final CommandConfiguration selectedConfiguration = view.getSelectedConfiguration();
        if (selectedConfiguration != null) {
            createNewCommand(selectedConfiguration.getType(), selectedConfiguration.toCommandLine(), selectedConfiguration.getName(),
                             selectedConfiguration.getAttributes());
        }
    }

    @Override
    public void onAddClicked() {
        final CommandType selectedType = view.getSelectedCommandType();
        if (selectedType != null) {
            createNewCommand(selectedType, null, null, null);
        }
    }

    private void createNewCommand(final CommandType type, final String customCommand, final String customName, final Map<String, String> attributes) {
        if (!isViewModified()) {
            reset();
            createCommand(type, customCommand, customName, attributes);
            return;
        }

        final ConfirmCallback saveCallback = new ConfirmCallback() {
            @Override
            public void accepted() {
                updateCommand(editedCommand).then(new Operation<WorkspaceDto>() {
                    @Override
                    public void apply(WorkspaceDto arg) throws OperationException {
                        reset();
                        createCommand(type, customCommand, customName, attributes);
                    }
                });
            }
        };

        final ConfirmCallback discardCallback = new ConfirmCallback() {
            @Override
            public void accepted() {
                fetchCommands();
                reset();
                createCommand(type, customCommand, customName, attributes);
            }
        };

        final ChoiceDialog dialog = dialogFactory.createChoiceDialog(
                machineLocale.editCommandsSaveChangesTitle(),
                machineLocale.editCommandsSaveChangesConfirmation(editedCommand.getName()),
                coreLocale.save(),
                machineLocale.editCommandsSaveChangesDiscard(),
                saveCallback,
                discardCallback);
        dialog.show();
    }

    private String getUniqueCommandName(CommandType customType, String customName) {
        final String newCommandName;

        if (customName == null || customName.isEmpty()) {
            newCommandName = "new" + customType.getDisplayName();
        } else {
            if (!commandNames.contains(customName)) {
                return customName;
            }
            newCommandName = customName + " copy";
        }
        if (!commandNames.contains(newCommandName)) {
            return newCommandName;
        }
        for (int count = 1; count < 1000; count++) {
            if (!commandNames.contains(newCommandName + "-" + count)) {
                return newCommandName + "-" + count;
            }
        }
        return newCommandName;
    }

    private void createCommand(CommandType type) {
        createCommand(type, null, null, null);
    }

    private void createCommand(CommandType type, String customCommand, String customName, Map<String, String> attributes) {
        Map<String, String> attributesToUpdate = (attributes != null) ? attributes : new HashMap<String, String>();

        attributesToUpdate.put(PREVIEW_URL_ATTR, type.getPreviewUrlTemplate());

        final CommandDto commandDto = dtoFactory.createDto(CommandDto.class)
                                                .withName(getUniqueCommandName(type, customName))
                                                .withCommandLine(customCommand != null ? customCommand : type.getCommandTemplate())
                                                .withAttributes(attributesToUpdate)
                                                .withType(type.getId());
        workspaceServiceClient.addCommand(workspaceId, commandDto).then(new Operation<WorkspaceDto>() {
            @Override
            public void apply(WorkspaceDto arg) throws OperationException {
                fetchCommands();

                final CommandType type = commandTypeRegistry.getCommandTypeById(commandDto.getType());
                final CommandConfiguration command = type.getConfigurationFactory().createFromDto(commandDto);
                fireConfigurationAdded(command);
                view.setSelectedConfiguration(command);
            }
        });
    }

    @Override
    public void onRemoveClicked(final CommandConfiguration selectedConfiguration) {
        if (selectedConfiguration == null) {
            return;
        }

        final ConfirmCallback confirmCallback = new ConfirmCallback() {
            @Override
            public void accepted() {
                workspaceServiceClient.deleteCommand(workspaceId, selectedConfiguration.getName()).then(new Operation<WorkspaceDto>() {
                    @Override
                    public void apply(WorkspaceDto arg) throws OperationException {
                        view.selectNextItem();
                        commandProcessingCallback = getCommandProcessingCallback();
                        fetchCommands();
                        fireConfigurationRemoved(selectedConfiguration);
                    }
                });
            }
        };

        final ConfirmDialog confirmDialog = dialogFactory.createConfirmDialog(
                machineLocale.editCommandsViewRemoveTitle(),
                machineLocale.editCommandsRemoveConfirmation(selectedConfiguration.getName()),
                confirmCallback,
                null);
        confirmDialog.show();
    }

    @Override
    public void onExecuteClicked() {
        final CommandConfiguration selectedConfiguration = view.getSelectedConfiguration();
        if (selectedConfiguration == null) {
            return;
        }

        if (isViewModified()) {
            dialogFactory.createMessageDialog("", machineLocale.editCommandsExecuteMessage(), null).show();
            return;
        }

        commandManager.execute(selectedConfiguration);
        view.close();
    }

    @Override
    public void onEnterClicked() {
        if (view.isCancelButtonInFocus()) {
            onCancelClicked();
            return;
        }

        if (view.isCloseButtonInFocus()) {
            onCloseClicked();
            return;
        }
        onSaveClicked();
    }

    private void reset() {
        editedCommand = null;
        editedCommandOriginName = null;
        editedCommandOriginPreviewUrl = null;
        editedPage = null;

        view.setConfigurationName("");
        view.setConfigurationPreviewUrl("");
        view.clearCommandConfigurationsContainer();
    }

    @Override
    public void onConfigurationSelected(final CommandConfiguration configuration) {
        if (!isViewModified()) {
            handleCommandSelection(configuration);
            return;
        }

        final ConfirmCallback saveCallback = new ConfirmCallback() {
            @Override
            public void accepted() {
                updateCommand(editedCommand).then(new Operation<WorkspaceDto>() {
                    @Override
                    public void apply(WorkspaceDto arg) throws OperationException {
                        fetchCommands();
                        fireConfigurationUpdated(editedCommand);
                        handleCommandSelection(configuration);
                    }
                });
            }
        };

        final ConfirmCallback discardCallback = new ConfirmCallback() {
            @Override
            public void accepted() {
                reset();
                fetchCommands();
                handleCommandSelection(configuration);
            }
        };

        final ChoiceDialog dialog = dialogFactory.createChoiceDialog(
                machineLocale.editCommandsSaveChangesTitle(),
                machineLocale.editCommandsSaveChangesConfirmation(editedCommand.getName()),
                coreLocale.save(),
                machineLocale.editCommandsSaveChangesDiscard(),
                saveCallback,
                discardCallback);
        dialog.show();
    }

    private String getPreviewUrlOrNull(CommandConfiguration configuration) {
        if (configuration.getAttributes() != null) {
            return configuration.getAttributes().get(PREVIEW_URL_ATTR);
        }

        return null;
    }

    private void handleCommandSelection(CommandConfiguration configuration) {
        editedCommand = configuration;
        editedCommandOriginName = configuration.getName();
        editedCommandOriginPreviewUrl = getPreviewUrlOrNull(configuration);

        view.setConfigurationName(configuration.getName());
        view.setConfigurationPreviewUrl(getPreviewUrlOrNull(configuration));

        final Collection<CommandConfigurationPage<? extends CommandConfiguration>> pages = configuration.getType().getConfigurationPages();
        for (CommandConfigurationPage<? extends CommandConfiguration> page : pages) {
            final CommandConfigurationPage<CommandConfiguration> p = ((CommandConfigurationPage<CommandConfiguration>)page);

            editedPage = p;

            p.setFieldStateActionDelegate(this);

            p.setDirtyStateListener(new DirtyStateListener() {
                @Override
                public void onDirtyStateChanged() {
                    view.setCancelButtonState(isViewModified());
                    view.setSaveButtonState(isViewModified());
                }
            });
            p.resetFrom(configuration);
            p.go(view.getCommandConfigurationsContainer());

            // TODO: for now only the 1'st page is showing but need to show all the pages
            break;
        }
    }

    @Override
    public void onNameChanged() {
        CommandConfiguration selectedConfiguration = view.getSelectedConfiguration();
        if (selectedConfiguration == null || !selectedConfiguration.equals(editedCommand)) {
            return;
        }
        selectedConfiguration.setName(view.getConfigurationName());
        view.setCancelButtonState(isViewModified());
        view.setSaveButtonState(isViewModified());
    }

    @Override
    public void onPreviewUrlChanged() {
        CommandConfiguration selectedConfiguration = view.getSelectedConfiguration();
        if (selectedConfiguration == null || !selectedConfiguration.equals(editedCommand)) {
            return;
        }
        selectedConfiguration.getAttributes().put(PREVIEW_URL_ATTR, view.getConfigurationPreviewUrl());
        view.setCancelButtonState(isViewModified());
        view.setSaveButtonState(isViewModified());
    }

    /** Show dialog. */
    public void show() {
        workspaceId = appContext.getWorkspaceId();
        fetchCommands();
        view.show();
    }

    /**
     * Fetch commands from server and update view.
     */
    private void fetchCommands() {
        final String originName = editedCommandOriginName;

        reset();
        view.setCancelButtonState(false);
        view.setSaveButtonState(false);

        workspaceServiceClient.getCommands(workspaceId).then(new Function<List<CommandDto>, List<CommandConfiguration>>() {
            @Override
            public List<CommandConfiguration> apply(List<CommandDto> arg) throws FunctionException {
                final List<CommandConfiguration> configurationList = new ArrayList<>();

                for (CommandDto descriptor : arg) {
                    final CommandType type = commandTypeRegistry.getCommandTypeById(descriptor.getType());
                    // skip command if it's type isn't registered
                    if (type != null) {
                        try {
                            configurationList.add(type.getConfigurationFactory().createFromDto(descriptor));
                        } catch (IllegalArgumentException e) {
                            Log.warn(EditCommandsPresenter.class, e.getMessage());
                        }
                    }
                }

                return configurationList;
            }
        }).then(new Operation<List<CommandConfiguration>>() {
            @Override
            public void apply(List<CommandConfiguration> commandConfigurations) throws OperationException {
                commandNames.clear();

                final Map<CommandType, List<CommandConfiguration>> categories = new HashMap<>();

                for (CommandType type : commandTypeRegistry.getCommandTypes()) {
                    final List<CommandConfiguration> settingsCategory = new ArrayList<>();
                    for (CommandConfiguration configuration : commandConfigurations) {
                        if (type.getId().equals(configuration.getType().getId())) {
                            settingsCategory.add(configuration);
                            commandNames.add(configuration.getName());
                            if (configuration.getName().equals(originName)) {
                                view.setSelectedConfiguration(configuration);
                            }
                        }
                    }
                    Collections.sort(settingsCategory, new Comparator<CommandConfiguration>() {
                        @Override
                        public int compare(CommandConfiguration o1, CommandConfiguration o2) {
                            return o1.getName().compareTo(o2.getName());
                        }
                    });
                    categories.put(type, settingsCategory);
                }
                view.setData(categories);
                view.setFilterState(!commandConfigurations.isEmpty());

                if (commandProcessingCallback != null) {
                    commandProcessingCallback.onCompleted();
                    commandProcessingCallback = null;
                }
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                dialogFactory.createMessageDialog("Error", arg.toString(), null).show();
            }
        });
    }

    private boolean isViewModified() {
        if (editedCommand == null || editedPage == null) {
            return false;
        }
        return editedPage.isDirty()
               || !editedCommandOriginName.equals(view.getConfigurationName())
               || !editedCommandOriginPreviewUrl.equals(view.getConfigurationPreviewUrl());
    }

    private void fireConfigurationAdded(CommandConfiguration command) {
        for (ConfigurationChangedListener listener : configurationChangedListeners) {
            listener.onConfigurationAdded(command);
        }
    }

    private void fireConfigurationRemoved(CommandConfiguration command) {
        for (ConfigurationChangedListener listener : configurationChangedListeners) {
            listener.onConfigurationRemoved(command);
        }
    }

    private void fireConfigurationUpdated(CommandConfiguration command) {
        for (ConfigurationChangedListener listener : configurationChangedListeners) {
            listener.onConfigurationsUpdated(command);
        }
    }

    private CommandProcessingCallback getCommandProcessingCallback() {
        return new CommandProcessingCallback() {
            @Override
            public void onCompleted() {
                view.setCloseButtonInFocus();
            }
        };
    }

    public void addConfigurationsChangedListener(ConfigurationChangedListener listener) {
        configurationChangedListeners.add(listener);
    }

    public void removeConfigurationsChangedListener(ConfigurationChangedListener listener) {
        configurationChangedListeners.remove(listener);
    }

    @Override
    public void updatePreviewURLState(boolean isVisible) {
        view.setPreviewUrlState(isVisible);
    }

    /** Listener that will be called when command configuration changed. */
    public interface ConfigurationChangedListener {
        void onConfigurationAdded(CommandConfiguration command);

        void onConfigurationRemoved(CommandConfiguration command);

        void onConfigurationsUpdated(CommandConfiguration command);
    }

    interface CommandProcessingCallback {
        /** Called when handling of command is completed successfully. */
        void onCompleted();
    }

}
