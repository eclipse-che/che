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
package org.eclipse.che.ide.ext.java.client.refactoring;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.ExternalResourceDelta;
import org.eclipse.che.ide.api.resources.ResourceDelta;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangeInfo;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.resources.reveal.RevealResourceEvent;

import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.ide.api.resources.ResourceDelta.ADDED;
import static org.eclipse.che.ide.api.resources.ResourceDelta.MOVED_FROM;
import static org.eclipse.che.ide.api.resources.ResourceDelta.MOVED_TO;
import static org.eclipse.che.ide.api.resources.ResourceDelta.UPDATED;

/**
 * Utility class for the refactoring operations.
 * It is needed for refreshing the project tree, updating content of the opening editors.
 *
 * @author Valeriy Svydenko
 * @author Vlad Zhukovskyi
 */
@Singleton
public class RefactoringUpdater {

    private final AppContext appContext;
    private       EventBus   eventBus;

    @Inject
    public RefactoringUpdater(AppContext appContext,
                              EventBus eventBus) {
        this.appContext = appContext;
        this.eventBus = eventBus;
    }

    /**
     * Iterates over each refactoring change and according to change type performs specific update operation.
     * i.e. for {@code ChangeName#UPDATE} updates only opened editors, for {@code ChangeName#MOVE or ChangeName#RENAME_COMPILATION_UNIT}
     * updates only new paths and opened editors, for {@code ChangeName#RENAME_PACKAGE} reloads package structure and restore expansion.
     *
     * @param changes
     *         applied changes
     */
    public void updateAfterRefactoring(List<ChangeInfo> changes) {
        if (changes == null || changes.isEmpty()) {
            return;
        }

        ExternalResourceDelta[] deltas = new ExternalResourceDelta[0];

        for (ChangeInfo change : changes) {

            final ExternalResourceDelta delta;

            final Path newPath = Path.valueOf(change.getPath());
            final Path oldPath = !isNullOrEmpty(change.getOldPath()) ? Path.valueOf(change.getOldPath()) : Path.EMPTY;

            switch (change.getName()) {
                case MOVE:
                case RENAME_COMPILATION_UNIT:
                    delta = new ExternalResourceDelta(newPath, oldPath, ADDED | MOVED_FROM | MOVED_TO);
                    break;
                case RENAME_PACKAGE:
                    delta = new ExternalResourceDelta(newPath, oldPath, ADDED | MOVED_FROM | MOVED_TO);
                    break;
                case UPDATE:
                    delta = new ExternalResourceDelta(newPath, UPDATED);
                    break;
                default:
                    continue;
            }

            final int index = deltas.length;
            deltas = Arrays.copyOf(deltas, index + 1);
            deltas[index] = delta;
        }

        if (deltas.length > 0) {
            appContext.getWorkspaceRoot().synchronize(deltas).then(new Operation<ResourceDelta[]>() {
                @Override
                public void apply(final ResourceDelta[] appliedDeltas) throws OperationException {
                    for (ResourceDelta delta : appliedDeltas) {
                        if ((delta.getFlags() & (MOVED_FROM | MOVED_TO)) != 0) {
                            eventBus.fireEvent(new RevealResourceEvent(delta.getToPath()));
                        }
                    }
                }
            });
        }
    }
}
