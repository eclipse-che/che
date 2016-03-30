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
package org.eclipse.che.plugin.svn.ide;

import com.google.gwt.user.client.rpc.AsyncCallback;

import org.eclipse.che.plugin.svn.shared.Depth;
import org.eclipse.che.plugin.svn.shared.InfoResponse;
import org.eclipse.che.plugin.svn.shared.ListResponse;
import org.eclipse.che.ide.rest.AsyncRequestCallback;

import javax.validation.constraints.NotNull;

import org.eclipse.che.plugin.svn.shared.CLIOutputResponse;
import org.eclipse.che.plugin.svn.shared.CLIOutputResponseList;
import org.eclipse.che.plugin.svn.shared.CLIOutputWithRevisionResponse;

import java.util.List;
import java.util.Map;

/**
 * Service for interacting with Subversion from the client.
 *
 * @author Jeremy Whitlock
 */
public interface SubversionClientService {

    /**
     * Adds the provided paths to version control.
     *
     * @param projectPath
     *         the project path
     * @param paths
     *         the paths to update
     * @param depth
     *         the update depth (--depth)
     * @param addIgnored
     *         whether or not to add ignored files (--no-ignore)
     * @param addParents
     *         whether or not to add parent paths (--parents)
     * @param autoProps
     *         whether to explicitly use automatic properties (--auto-props)
     * @param noAutoProps
     *         whether to explicitly not use automatic properties (--no-auto-props)
     * @param callback
     *         the callback
     */
    void add(final @NotNull String projectPath, final List<String> paths, final String depth, final boolean addIgnored,
             final boolean addParents, final boolean autoProps, final boolean noAutoProps,
             final AsyncRequestCallback<CLIOutputResponse> callback);

    /**
     * Removes the provided paths from version control.
     *
     * @param projectPath
     *         the project path
     * @param paths
     *         the paths to remove
     * @param callback
     *         the callback
     */
    void remove(final @NotNull String projectPath, final List<String> paths,
                final AsyncRequestCallback<CLIOutputResponse> callback);

    /**
     * Reverts any local changes to provided paths and resolves any conflicted states.
     *
     * @param projectPath
     *         the project path
     * @param paths
     *         the paths to remove
     * @param callback
     *         the callback
     */
    void revert(final @NotNull String projectPath, final List<String> paths, final String depth,
                final AsyncRequestCallback<CLIOutputResponse> callback);

    /**
     * Copy provided path.
     *
     * @param projectPath
     *         the project path
     * @param source
     *         source item path
     * @param destination
     *         destination path
     * @param callback
     *         the callback
     */
    void copy(final @NotNull String projectPath, final String source, final String destination, final String comment,
              final AsyncRequestCallback<CLIOutputResponse> callback);

    /**
     * Merge specified URL with target.
     *
     * @param projectPath
     *          project path
     * @param target
     *          target directory
     * @param sourceURL
     *          source URL to merge
     * @param callback
     *          callback
     */
    void merge(final @NotNull String projectPath, final String target, final String sourceURL,
               final AsyncRequestCallback<CLIOutputResponse> callback);

    /**
     * Retrieves the information about repository item.
     *
     * @param projectPath
     *          relative path to the project in local workspace
     * @param target
     *          target to operate
     * @param revision
     *          revision, use HEAD to specify latest revision
     * @param children
     *          whether list children or not
     * @param callback
     *          callback
     */
    void info(final @NotNull String projectPath, final String target, final String revision, final boolean children,
              final AsyncRequestCallback<InfoResponse> callback);


    /**
     * List server directory.
     *
     * @param projectPath
     *          project path
     * @param target
     *          target directory
     * @param callback
     *          callback
     */
    void list(final @NotNull String projectPath, final String target,
                     final AsyncRequestCallback<ListResponse> callback);

    /**
     * Retrieves the status for the provided paths, or the working copy as a whole.
     *
     * @param projectPath
     *         the project path
     * @param paths
     *         the paths to update
     * @param depth
     *         the update depth (--depth)
     * @param ignoreExternals
     *         whether or not to ignore externals (--ignore-externals)
     * @param showIgnored
     *         whether or not to show ignored paths (--no-ignored)
     * @param showUpdates
     *         whether or not to show repository updates (--show-updates)
     * @param showUnversioned
     *         whether or not to show unversioned paths (--quiet)
     * @param verbose
     *         whether or not to be verbose (--verbose)
     * @param changeLists
     *         which changelists to operation on (--changelist)
     * @param callback
     *         the callback
     */
    void status(final @NotNull String projectPath, final List<String> paths, final String depth,
                final boolean ignoreExternals, final boolean showIgnored, final boolean showUpdates,
                final boolean showUnversioned, final boolean verbose, final List<String> changeLists,
                final AsyncRequestCallback<CLIOutputResponse> callback);

    /**
     * Updates the provided paths, or the working copy as a whole, to the latest, or requested, repository version.
     *
     * @param projectPath
     *         the project path
     * @param paths
     *         the paths to update
     * @param revision
     *         the revision (0 indicates HEAD) (--revision)
     * @param depth
     *         the update depth (--depth)
     * @param ignoreExternals
     *         whether or not to ignore externals (--ignore-externals)
     * @param accept
     *         the accept argument (--accept)
     * @param callback
     *         the callback
     */
    void update(final @NotNull String projectPath, final List<String> paths, final String revision, final String depth,
                final boolean ignoreExternals, final String accept,
                final AsyncRequestCallback<CLIOutputWithRevisionResponse> callback);

    void showLog(final @NotNull String projectPath, final List<String> paths, final String revision,
                 final AsyncRequestCallback<CLIOutputResponse> callback);

    void showDiff(final @NotNull String projectPath, final List<String> paths, final String revision,
                  final AsyncRequestCallback<CLIOutputResponse> callback);

    /**
     * Locks the given paths.
     *
     * @param projectPath
     *         the path of the project
     * @param paths
     *         the paths to lock
     * @param force
     *         if false, will warn if another user already has a lock on a target, leave this target unchanged, and continue.<br>
     *         if true, will steal the lock from the previous owner instead
     * @param callback
     *         follow-up action
     */
    void lock(final @NotNull String projectPath, final List<String> paths, final boolean force,
              final AsyncRequestCallback<CLIOutputResponse> callback);

    /**
     * Unocks the given paths.
     *
     * @param projectPath
     *         the path of the project
     * @param paths
     *         the paths to lock
     * @param force
     *         if false, will warn if another user already has a lock on a target, leave this target unchanged, and continue.<br>
     *         if true, will unlock anyway
     * @param callback
     *         follow-up action
     */
    void unlock(final @NotNull String projectPath, final List<String> paths, final boolean force,
                final AsyncRequestCallback<CLIOutputResponse> callback);

    /**
     * Commits the changes in the repository.
     *
     * @param projectPath
     *         the project path
     * @param paths
     *         the paths to include in the commit
     * @param message
     *         the commit message
     * @param keepChangeLists
     *         if true, doesn't remove the changelist assigments from working copy items after committing
     * @param keepLocks
     *         if true, doesn't unlock files after commiting
     * @param callback
     *         the callback
     */
    void commit(String projectPath, List<String> paths, String message,
                boolean keepChangeLists, boolean keepLocks,
                AsyncRequestCallback<CLIOutputWithRevisionResponse> callback);

    /**
     * Cleans up recursively the working copy.
     *
     * @param projectPath
     *         the project path
     * @param paths
     *         the paths to clean up
     * @param callback
     *         the callback
     */
    void cleanup(String projectPath, List<String> paths, AsyncRequestCallback<CLIOutputResponse> callback);

    void showConflicts(final @NotNull String projectPath, final List<String> paths, final AsyncCallback<List<String>> callback);

    void resolve(final @NotNull String projectPath,
                 final Map<String, String> resolution,
                 final String depth,
                 final AsyncCallback<CLIOutputResponseList> callback);

    void saveCredentials(String repositoryUrl, String username, String password, AsyncRequestCallback<Void> callback);

    /**
     * Move provided path.
     *
     * @param projectPath
     *         the project path
     * @param source
     *         source item path
     * @param destination
     *         destination path
     * @param callback
     *         the callback
     */
    void move(final @NotNull String projectPath, final List<String> source, final String destination, final String comment,
              final AsyncRequestCallback<CLIOutputResponse> callback);

    /**
     * Set specified property to a path or a target.
     *
     * @param projectPath
     *         the project path
     * @param propertyName
     *         the property name
     * @param propertyValues
     *         the property value
     * @param depth
     *         the depth
     * @param force
     *         forcing or not
     * @param path
     *         path to which property sets
     * @param callback
     *         the callback
     */
    void propertySet(final String projectPath, final String propertyName, final String propertyValues,
                     final Depth depth, final boolean force, final String path,
                     final AsyncRequestCallback<CLIOutputResponse> callback);

    /**
     * Get specified property for a path or a target.
     *
     * @param projectPath the project path
     * @param propertyName the property name
     * @param path path to which property get
     * @param callback the callback
     */
    void propertyGet(final String projectPath,
                     final String propertyName,
                     final String path,
                     final AsyncRequestCallback<CLIOutputResponse> callback);

    /**
     * Get properties set for a path or a target.
     *
     * @param projectPath the project path
     * @param propertyName the property name
     * @param path path to which property get
     * @param callback the callback
     */
    void propertyList(final String projectPath,
                      final String path,
                      final AsyncRequestCallback<CLIOutputResponse> callback);

    /**
     * Delete specified property from a path or a target.
     *
     * @param projectPath
     *         the project path
     * @param propertyName
     *         the property name
     * @param depth
     *         the depth
     * @param force
     *         forcing or not
     * @param path
     *         path from which property should be deleted
     * @param callback
     *         the callback
     */
    void propertyDelete(final String projectPath, final String propertyName, final Depth depth,
                        final boolean force, final String path, final AsyncRequestCallback<CLIOutputResponse> callback);
}
