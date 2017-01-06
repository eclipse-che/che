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
package org.eclipse.che.ide.api.resources;

import com.google.common.annotations.Beta;

import org.eclipse.che.ide.resource.Path;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Delta which describes changes which has made outside system resource management.
 * For example some refactoring mechanisms may includes from many preparing steps and after applying refactoring resource
 * management should know what exactly happened with resources in the third-party components.
 * <p/>
 * When third-party component performs any changes in the resources, resource management should be informed about external
 * changes by creating instances of current delta and calling {@link Container#synchronize(ResourceDelta...)}.
 * <p/>
 * Given resource paths should be absolute.
 * <p/>
 * Example of usage:
 * <pre>
 *     ResourceDelta deltaUpdate = new ExternalResourceDelta(Path.valueOf("/some/path_1"), ResourceDelta.UPDATED);
 *     ResourceDelta deltaRemove = new ExternalResourceDelta(Path.valueOf("/some/path_2"), ResourceDelta.REMOVED);
 *     ResourceDelta deltaAdd = new ExternalResourceDelta(Path.valueOf("/some/path_3"), ResourceDelta.ADDED);
 *     ResourceDelta deltaMove = new ExternalResourceDelta(Path.valueOf("/some/path_3"), Path.valueOf("/some/path_2"),
 *                                                         ResourceDelta.ADDED | ResourceDelta.MOVED_FROM | ResourceDelta.MOVED_FROM);
 *
 *     Workspace workspace = ... ;
 *     workspace.synchronize(deltaUpdate, deltaRemove, deltaAdd, deltaMove).then(new Operation<ResourceDelta[]>() {
 *         public void apply(ResourceDelta[] appliedDeltas) throws OperationException {
 *              //do something after deltas have been resolved
 *         }
 *     });
 * </pre>
 *
 * @author Vlad Zhukovskiy
 * @see ResourceDelta
 * @see Container#synchronize(ResourceDelta...)
 * @since 4.4.0
 */
@Beta
public class ExternalResourceDelta implements ResourceDelta {

    private Path newPath;
    private Path oldPath;

    protected static int KIND_MASK = 0xF;
    protected int status;

    public ExternalResourceDelta(Path path, int status) {
        this(path, null, status);
    }

    public ExternalResourceDelta(Path newPath, Path oldPath, int status) {
        this.newPath = checkNotNull(newPath);
        this.oldPath = oldPath;
        this.status = status;
    }

    /** {@inheritDoc} */
    @Override
    public int getKind() {
        return status & KIND_MASK;
    }

    /** {@inheritDoc} */
    @Override
    public int getFlags() {
        return status & ~KIND_MASK;
    }

    /** {@inheritDoc} */
    @Override
    public Path getFromPath() {
        return oldPath;
    }

    /** {@inheritDoc} */
    @Override
    public Path getToPath() {
        return newPath;
    }

    /** {@inheritDoc} */
    @Override
    public Resource getResource() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "ExternalResourceDelta{" +
               "newPath=" + newPath +
               ", oldPath=" + oldPath +
               ", status=" + status +
               '}';
    }
}
