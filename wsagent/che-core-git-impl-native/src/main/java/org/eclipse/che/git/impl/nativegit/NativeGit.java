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
package org.eclipse.che.git.impl.nativegit;

import org.eclipse.che.api.core.util.LineConsumerFactory;
import org.eclipse.che.api.git.Config;
import org.eclipse.che.api.git.CredentialsLoader;
import org.eclipse.che.api.git.GitException;
import org.eclipse.che.git.impl.nativegit.commands.AddCommand;
import org.eclipse.che.git.impl.nativegit.commands.BranchCreateCommand;
import org.eclipse.che.git.impl.nativegit.commands.BranchDeleteCommand;
import org.eclipse.che.git.impl.nativegit.commands.BranchListCommand;
import org.eclipse.che.git.impl.nativegit.commands.BranchRenameCommand;
import org.eclipse.che.git.impl.nativegit.commands.CheckoutCommand;
import org.eclipse.che.git.impl.nativegit.commands.CloneCommand;
import org.eclipse.che.git.impl.nativegit.commands.CommitCommand;
import org.eclipse.che.git.impl.nativegit.commands.DiffCommand;
import org.eclipse.che.git.impl.nativegit.commands.EmptyGitCommand;
import org.eclipse.che.git.impl.nativegit.commands.FetchCommand;
import org.eclipse.che.git.impl.nativegit.commands.InitCommand;
import org.eclipse.che.git.impl.nativegit.commands.ListFilesCommand;
import org.eclipse.che.git.impl.nativegit.commands.LogCommand;
import org.eclipse.che.git.impl.nativegit.commands.LsRemoteCommand;
import org.eclipse.che.git.impl.nativegit.commands.MergeCommand;
import org.eclipse.che.git.impl.nativegit.commands.MoveCommand;
import org.eclipse.che.git.impl.nativegit.commands.PullCommand;
import org.eclipse.che.git.impl.nativegit.commands.PushCommand;
import org.eclipse.che.git.impl.nativegit.commands.RemoteAddCommand;
import org.eclipse.che.git.impl.nativegit.commands.RemoteDeleteCommand;
import org.eclipse.che.git.impl.nativegit.commands.RemoteListCommand;
import org.eclipse.che.git.impl.nativegit.commands.RemoteUpdateCommand;
import org.eclipse.che.git.impl.nativegit.commands.RemoveCommand;
import org.eclipse.che.git.impl.nativegit.commands.ResetCommand;
import org.eclipse.che.git.impl.nativegit.commands.ShowFileContentCommand;
import org.eclipse.che.git.impl.nativegit.commands.StatusCommand;
import org.eclipse.che.git.impl.nativegit.commands.TagCreateCommand;
import org.eclipse.che.git.impl.nativegit.commands.TagDeleteCommand;
import org.eclipse.che.git.impl.nativegit.commands.TagListCommand;
import org.eclipse.che.plugin.ssh.key.script.SshScriptProvider;

import java.io.File;

/**
 * Git commands factory.
 *
 * @author Eugene Voevodin
 */
public class NativeGit {

    private final File              repository;
    private final SshScriptProvider sshScriptProvider;
    private final CredentialsLoader credentialsLoader;
    private final GitAskPassScript  gitAskPassScript;

    protected LineConsumerFactory gitOutputPublisherFactory;

    /**
     * @param repository
     *         directory where will be executed all commands created with
     *         this NativeGit object
     */
    public NativeGit(File repository, SshScriptProvider sshScriptProvider,
                     CredentialsLoader credentialsLoader, GitAskPassScript gitAskPassScript) {
        this.repository = repository;
        this.sshScriptProvider = sshScriptProvider;
        this.credentialsLoader = credentialsLoader;
        this.gitAskPassScript = gitAskPassScript;
    }

    /**
     * @return empty git command
     */
    public EmptyGitCommand createEmptyGitCommand() {
        return new EmptyGitCommand(repository);
    }

    /**
     * Creates clone command that will be used without ssh key
     *
     * @return clone command
     */
    public CloneCommand createCloneCommand() {
        CloneCommand cloneCommand = new CloneCommand(repository, sshScriptProvider, credentialsLoader, gitAskPassScript);
        cloneCommand.setLineConsumerFactory(gitOutputPublisherFactory);
        return cloneCommand;
    }

    /**
     * @return commit command
     */
    public CommitCommand createCommitCommand() {
        CommitCommand command = new CommitCommand(repository);
        command.setLineConsumerFactory(gitOutputPublisherFactory);
        return command;
    }

    /**
     * @return branch create command
     */
    public BranchRenameCommand createBranchRenameCommand() {
        BranchRenameCommand command = new BranchRenameCommand(repository, sshScriptProvider, credentialsLoader, gitAskPassScript);
        command.setLineConsumerFactory(gitOutputPublisherFactory);
        return command;
    }

    /**
     * @return remote add command
     */
    public RemoteAddCommand createRemoteAddCommand() {
        RemoteAddCommand command = new RemoteAddCommand(repository);
        command.setLineConsumerFactory(gitOutputPublisherFactory);
        return command;
    }

    /**
     * @return remote list command
     */
    public RemoteListCommand createRemoteListCommand() {
        RemoteListCommand command = new RemoteListCommand(repository);
        command.setLineConsumerFactory(gitOutputPublisherFactory);
        return command;
    }

    /**
     * @return remote delete command
     */
    public RemoteDeleteCommand createRemoteDeleteCommand() {
        RemoteDeleteCommand command = new RemoteDeleteCommand(repository);
        command.setLineConsumerFactory(gitOutputPublisherFactory);
        return command;
    }

    /**
     * @return log command
     */
    public LogCommand createLogCommand() {
        LogCommand command = new LogCommand(repository);
        command.setLineConsumerFactory(gitOutputPublisherFactory);
        return command;
    }

    /**
     * @return ls command
     */
    public LsRemoteCommand createLsRemoteCommand() {
        LsRemoteCommand command = new LsRemoteCommand(repository, sshScriptProvider, credentialsLoader, gitAskPassScript);
        command.setLineConsumerFactory(gitOutputPublisherFactory);
        return command;
    }

    /**
     * @return add command
     */
    public AddCommand createAddCommand() {
        AddCommand command = new AddCommand(repository);
        command.setLineConsumerFactory(gitOutputPublisherFactory);
        return command;
    }

    /**
     * @return init command
     */
    public InitCommand createInitCommand() {
        InitCommand command = new InitCommand(repository);
        command.setLineConsumerFactory(gitOutputPublisherFactory);
        return command;
    }

    /**
     * @return diff command
     */
    public DiffCommand createDiffCommand() {
        DiffCommand command = new DiffCommand(repository);
        command.setLineConsumerFactory(gitOutputPublisherFactory);
        return command;
    }

    /**
     * @return show command
     */
    public ShowFileContentCommand createShowFileContentCommand() {
        ShowFileContentCommand command = new ShowFileContentCommand(repository);
        command.setLineConsumerFactory(gitOutputPublisherFactory);
        return command;
    }

    /**
     * @return reset command
     */
    public ResetCommand createResetCommand() {
        ResetCommand command = new ResetCommand(repository);
        command.setLineConsumerFactory(gitOutputPublisherFactory);
        return command;
    }

    /**
     * @return tag create command
     */
    public TagCreateCommand createTagCreateCommand() {
        TagCreateCommand command = new TagCreateCommand(repository);
        command.setLineConsumerFactory(gitOutputPublisherFactory);
        return command;
    }

    /**
     * @return tag delete command
     */
    public TagDeleteCommand createTagDeleteCommand() {
        TagDeleteCommand command = new TagDeleteCommand(repository);
        command.setLineConsumerFactory(gitOutputPublisherFactory);
        return command;
    }

    /**
     * @return tah list command
     */
    public TagListCommand createTagListCommand() {
        TagListCommand command = new TagListCommand(repository);
        command.setLineConsumerFactory(gitOutputPublisherFactory);
        return command;
    }

    /**
     * @return branch create command
     */
    public BranchCreateCommand createBranchCreateCommand() {
        BranchCreateCommand command = new BranchCreateCommand(repository);
        command.setLineConsumerFactory(gitOutputPublisherFactory);
        return command;
    }

    /**
     * @return config
     */
    public Config createConfig() throws GitException {
        return new ConfigImpl(repository);
    }

    /**
     * @return branch checkout command
     */
    public CheckoutCommand createCheckoutCommand() {
    	CheckoutCommand command = new CheckoutCommand(repository);
        command.setLineConsumerFactory(gitOutputPublisherFactory);
        return command;
    }

    /**
     * @return list files command
     */
    public ListFilesCommand createListFilesCommand() {
        ListFilesCommand command = new ListFilesCommand(repository);
        command.setLineConsumerFactory(gitOutputPublisherFactory);
        return command;
    }

    /**
     * @return branch list command
     */
    public BranchListCommand createBranchListCommand() {
        BranchListCommand command = new BranchListCommand(repository);
        command.setLineConsumerFactory(gitOutputPublisherFactory);
        return command;
    }

    /**
     * @return branch delete command
     */
    public BranchDeleteCommand createBranchDeleteCommand() {
        BranchDeleteCommand command = new BranchDeleteCommand(repository, sshScriptProvider, credentialsLoader, gitAskPassScript);
        command.setLineConsumerFactory(gitOutputPublisherFactory);
        return command;
    }

    /**
     * @return remote command
     */
    public RemoveCommand createRemoveCommand() {
        RemoveCommand command = new RemoveCommand(repository);
        command.setLineConsumerFactory(gitOutputPublisherFactory);
        return command;
    }

    /**
     * @return move command
     */
    public MoveCommand createMoveCommand() {
        MoveCommand command = new MoveCommand(repository);
        command.setLineConsumerFactory(gitOutputPublisherFactory);
        return command;
    }

    /**
     * @return status command
     */
    public StatusCommand createStatusCommand() {
        StatusCommand command = new StatusCommand(repository);
        command.setLineConsumerFactory(gitOutputPublisherFactory);
        return command;
    }

    /**
     * @return merge command
     */
    public MergeCommand createMergeCommand() {
        MergeCommand command = new MergeCommand(repository);
        command.setLineConsumerFactory(gitOutputPublisherFactory);
        return command;
    }

    /**
     * Creates fetch command that will be used without ssh key
     *
     * @return fetch command
     */
    public FetchCommand createFetchCommand() {
        FetchCommand command = new FetchCommand(repository, sshScriptProvider, credentialsLoader, gitAskPassScript);
        command.setLineConsumerFactory(gitOutputPublisherFactory);
        return command;
    }

    /**
     * Creates pull command that will be used without ssh key
     *
     * @return pull command
     */
    public PullCommand createPullCommand() {
        PullCommand command = new PullCommand(repository, sshScriptProvider, credentialsLoader, gitAskPassScript);
        command.setLineConsumerFactory(gitOutputPublisherFactory);
        return command;
    }

    /**
     * @return remote update command
     */
    public RemoteUpdateCommand createRemoteUpdateCommand() {
        RemoteUpdateCommand command = new RemoteUpdateCommand(repository);
        command.setLineConsumerFactory(gitOutputPublisherFactory);
        return command;
    }

    /**
     * Creates push command that will be used without ssh key
     *
     * @return push command
     */
    public PushCommand createPushCommand() {
        PushCommand command = new PushCommand(repository, sshScriptProvider, credentialsLoader, gitAskPassScript);
        command.setLineConsumerFactory(gitOutputPublisherFactory);
        return command;
    }

    /**
     * @return repository
     */
    public File getRepository() {
        return repository;
    }


    public void setOutputLineConsumerFactory(LineConsumerFactory gitOutputPublisherFactory) {
        this.gitOutputPublisherFactory = gitOutputPublisherFactory;
    }
}
