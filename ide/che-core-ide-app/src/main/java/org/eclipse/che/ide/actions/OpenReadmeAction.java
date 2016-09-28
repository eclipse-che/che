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
package org.eclipse.che.ide.actions;

import com.google.common.base.Optional;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent;
import org.eclipse.che.ide.api.resources.ResourceDelta;

import javax.inject.Inject;

import static org.eclipse.che.ide.api.event.FileEvent.createOpenFileEvent;

/**
 * @author Dmitry Kuleshov
 */
public class OpenReadmeAction {
    private static final String README_MD         = "README.md";
    private static final String DEFAULT_README_MD = ".che/README.md";

    @Inject
    public OpenReadmeAction(final EventBus eventBus) {
        eventBus.addHandler(ResourceChangedEvent.getType(), new ResourceChangedEvent.ResourceChangedHandler() {
            @Override
            public void onResourceChanged(ResourceChangedEvent event) {
                final Resource resource = event.getDelta().getResource();
                final int kind = event.getDelta().getKind();

                if (resource.isProject() && kind == ResourceDelta.ADDED) {
                    final Project project = (Project)resource;
                    processProjectReadme(project);
                }
            }

            private void processProjectReadme(final Project project) {
                project.getFile(README_MD).then(new Operation<Optional<File>>() {
                    @Override
                    public void apply(Optional<File> arg) {
                        if (arg.isPresent()) {
                            fireOpenFileEvent(arg);
                        } else {
                            project.getFile(DEFAULT_README_MD).then(new Operation<Optional<File>>() {
                                @Override
                                public void apply(Optional<File> arg) {
                                    if (arg.isPresent()) {
                                        fireOpenFileEvent(arg);
                                    }
                                }
                            });
                        }
                    }
                });
            }

            private void fireOpenFileEvent(Optional<File> arg) {
                final File file = arg.get();
                final FileEvent openFileEvent = createOpenFileEvent(file);
                eventBus.fireEvent(openFileEvent);
            }
        });
    }
}
