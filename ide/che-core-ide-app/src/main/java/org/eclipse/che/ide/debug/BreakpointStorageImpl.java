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
package org.eclipse.che.ide.debug;

import elemental.js.util.JsArrayOf;
import elemental.util.ArrayOf;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.debug.shared.dto.BreakpointDto;
import org.eclipse.che.api.debug.shared.dto.LocationDto;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Executor;
import org.eclipse.che.api.promises.client.js.JsPromiseProvider;
import org.eclipse.che.api.promises.client.js.ResolveFunction;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.debug.Breakpoint;
import org.eclipse.che.ide.api.debug.BreakpointStorage;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.workspace.WorkspaceReadyEvent;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.util.storage.LocalStorage;
import org.eclipse.che.ide.util.storage.LocalStorageProvider;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import static org.eclipse.che.ide.api.debug.Breakpoint.Type.BREAKPOINT;

/**
 * Breakpoints storage based on local storage.
 *
 * @author Anatolii Bazko
 */
@Singleton
public class BreakpointStorageImpl implements BreakpointStorage {

    private static final Logger LOG                                  = Logger.getLogger(BreakpointStorageImpl.class.getName());
    private static final String LOCAL_STORAGE_BREAKPOINTS_KEY_PREFIX = "che-breakpoints-";

    private final AppContext             appContext;
    private final DtoFactory             dtoFactory;
    private final LocalStorage           storage;
    private final JsPromiseProvider      promiseProvider;
    private final Promise<Void>          readAllBreakpointMarker;
    private final List<Breakpoint>       breakpoints;
    private final EventBus               eventBus;
    private final String                 storageKey;

    @Inject
    public BreakpointStorageImpl(AppContext appContext,
                                 DtoFactory dtoFactory,
                                 JsPromiseProvider promiseProvider,
                                 LocalStorageProvider localStorageProvider,
                                 EventBus eventBus) {
        this.appContext = appContext;
        this.dtoFactory = dtoFactory;
        this.promiseProvider = promiseProvider;
        this.eventBus = eventBus;
        this.storage = localStorageProvider.get();
        this.breakpoints = new LinkedList<>();
        this.storageKey = LOCAL_STORAGE_BREAKPOINTS_KEY_PREFIX + appContext.getWorkspaceId();

        if (storage == null) {
            LOG.warning("Local storage is not supported. Breakpoints won't be preserved.");
            this.readAllBreakpointMarker = promiseProvider.resolve(null);
        } else {
            this.readAllBreakpointMarker = getReadAllBreakpointsMarker();
            this.readAllBreakpointMarker.then(onFulfilled -> {
                preserve();
            });

            clearOutdatedRecords();
        }
    }

    @Override
    public void addAll(final List<Breakpoint> breakpoints) {
        readAllBreakpointMarker.then(onFulfilled -> {
            BreakpointStorageImpl.this.breakpoints.addAll(breakpoints);

            preserve();
        });
    }

    @Override
    public void add(final Breakpoint breakpoint) {
        readAllBreakpointMarker.then(onFulfilled -> {
            BreakpointStorageImpl.this.breakpoints.add(breakpoint);

            preserve();
        });
    }

    @Override
    public void delete(final Breakpoint breakpoint) {
        readAllBreakpointMarker.then(onFulfilled -> {
            BreakpointStorageImpl.this.breakpoints.removeIf(
                    b -> b.getLineNumber() == breakpoint.getLineNumber() && b.getPath().equals(breakpoint.getPath())
            );

            preserve();
        });
    }

    @Override
    public void deleteAll(final List<Breakpoint> breakpoints) {
        readAllBreakpointMarker.then(onFulfilled -> {
            for (Breakpoint breakpoint : breakpoints) {
                BreakpointStorageImpl.this.breakpoints.removeIf(
                        b -> b.getLineNumber() == breakpoint.getLineNumber() && b.getPath().equals(breakpoint.getPath())
                );
            }

            preserve();
        });
    }

    @Override
    public void clear() {
        readAllBreakpointMarker.then(onFulfilled -> {
            BreakpointStorageImpl.this.breakpoints.clear();
            preserve();
        });
    }

    @Override
    public Promise<List<Breakpoint>> readAll() {
        return readAllBreakpointMarker.then(onFulfilled -> breakpoints, onRejected -> Collections.emptyList());
    }

    private void preserve() {
        if (storage == null) {
            return;
        }

        List<BreakpointDto> breakpoints2save = new LinkedList<>();

        for (Breakpoint breakpoint : breakpoints) {
            breakpoints2save.add(dtoFactory.createDto(BreakpointDto.class)
                                           .withLocation(dtoFactory.createDto(LocationDto.class)
                                                                   .withTarget(breakpoint.getPath())
                                                                   .withLineNumber(breakpoint.getLineNumber())));
        }

        storage.setItem(storageKey, dtoFactory.toJson(breakpoints2save));
    }

    private Promise<Void> getReadAllBreakpointsMarker() {
        return promiseProvider.create(Executor.create((resolve, reject) -> addWorkspaceHandler(resolve)));
    }

    private void addWorkspaceHandler(final ResolveFunction<Void> readAllMarkerResolveFunc) {
        eventBus.addHandler(WorkspaceReadyEvent.getType(), event -> BreakpointStorageImpl.this.onWorkspaceReady(readAllMarkerResolveFunc));
    }

    private void onWorkspaceReady(ResolveFunction<Void> readAllMarkerResolveFunc) {
        ArrayOf<Promise<?>> breakpointPromises = prepareBreakpointPromises();

        promiseProvider.all2(breakpointPromises).then(breakpoints -> {
            for (int i = 0; i < breakpoints.length(); i++) {
                Breakpoint breakpoint = (Breakpoint)breakpoints.get(i);
                if (breakpoint != null) {
                    BreakpointStorageImpl.this.breakpoints.add(breakpoint);
                }
            }
            readAllMarkerResolveFunc.apply(null);
        }).catchError(arg -> {
            readAllMarkerResolveFunc.apply(null);
        });
    }

    private ArrayOf<Promise<?>> prepareBreakpointPromises() {
        ArrayOf<Promise<?>> breakpointPromises = JsArrayOf.create();

        String json = storage.getItem(storageKey);
        if (json == null) {
            return breakpointPromises;
        }

        for (BreakpointDto dto : dtoFactory.createListDtoFromJson(json, BreakpointDto.class)) {
            Promise<Optional<File>> filePromise = appContext.getWorkspaceRoot().getFile(dto.getLocation().getTarget());
            breakpointPromises.push(toBreakpointPromise(filePromise, dto));
        }

        return breakpointPromises;
    }

    private Promise<Breakpoint> toBreakpointPromise(Promise<Optional<File>> filePromise, BreakpointDto dto) {
        return filePromise.then((Function<Optional<File>, Breakpoint>)file -> createBreakpoint(file.get(), dto)).catchError(error -> null);
    }

    @Nullable
    private Breakpoint createBreakpoint(@Nullable File file, BreakpointDto dto) {
        return file == null ? null : new Breakpoint(BREAKPOINT,
                                                    dto.getLocation().getLineNumber(),
                                                    dto.getLocation().getTarget(),
                                                    file,
                                                    false);
    }

    /**
     * Remove all keys from the local storage that contain breakpoints for unexisted workspaces.
     *
     * Implementation doesn't handle workspace removal, so it is necessary to check if workspaces doesn't exists and remove
     * local storage records if so.
     */
    private void clearOutdatedRecords() {
        for (int i = 0; i < storage.getLength(); i++) {
            String key = storage.key(i);
            if (key != null && key.startsWith(LOCAL_STORAGE_BREAKPOINTS_KEY_PREFIX)) {
                String wsId = key.substring(LOCAL_STORAGE_BREAKPOINTS_KEY_PREFIX.length());

                // FIXME: spi
//                Promise<WorkspaceDto> workspace = workspaceServiceClient.getWorkspace(wsId);
//                workspace.catchError(arg -> {
//                    storage.removeItem(key);
//                });
            }
        }
    }
}
