/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.debug;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.eclipse.che.api.debug.shared.dto.BreakpointConfigurationDto;
import org.eclipse.che.api.debug.shared.dto.BreakpointDto;
import org.eclipse.che.api.debug.shared.dto.LocationDto;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.BreakpointConfiguration;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.impl.BreakpointImpl;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.debug.BreakpointStorage;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.util.storage.LocalStorage;
import org.eclipse.che.ide.util.storage.LocalStorageProvider;
import org.eclipse.che.ide.workspace.WorkspaceServiceClient;

/**
 * Breakpoints storage based on local storage.
 *
 * @author Anatolii Bazko
 */
@Singleton
public class BreakpointStorageImpl implements BreakpointStorage {

  private static final Logger LOG = Logger.getLogger(BreakpointStorageImpl.class.getName());
  private static final String LOCAL_STORAGE_BREAKPOINTS_KEY_PREFIX = "che-breakpoints-";

  private final DtoFactory dtoFactory;
  private final LocalStorage storage;
  private final WorkspaceServiceClient workspaceServiceClient;
  private final List<Breakpoint> breakpoints;
  private final String storageKey;

  @Inject
  public BreakpointStorageImpl(
      AppContext appContext,
      DtoFactory dtoFactory,
      LocalStorageProvider localStorageProvider,
      WorkspaceServiceClient workspaceServiceClient) {

    this.storageKey = LOCAL_STORAGE_BREAKPOINTS_KEY_PREFIX + appContext.getWorkspaceId();
    this.dtoFactory = dtoFactory;
    this.storage = localStorageProvider.get();
    this.workspaceServiceClient = workspaceServiceClient;
    this.breakpoints = new LinkedList<>(readAll());

    if (storage == null) {
      LOG.warning("Local storage is not supported. Breakpoints won't be preserved.");
    } else {
      clearOutdatedRecords();
    }
  }

  @Override
  public void addAll(final List<Breakpoint> breakpoints) {
    this.breakpoints.addAll(breakpoints);
    preserve();
  }

  @Override
  public void add(final Breakpoint breakpoint) {
    breakpoints.add(breakpoint);
    preserve();
  }

  @Override
  public void delete(final Breakpoint breakpoint) {
    breakpoints.removeIf(b -> isSameBreakpointLocation(breakpoint, b));
    preserve();
  }

  @Override
  public void deleteAll(final List<Breakpoint> breakpoints) {
    for (Breakpoint breakpoint : breakpoints) {
      breakpoints.removeIf(b -> isSameBreakpointLocation(breakpoint, b));
    }

    preserve();
  }

  @Override
  public void update(Breakpoint breakpoint) {
    breakpoints.removeIf(b -> isSameBreakpointLocation(breakpoint, b));
    breakpoints.add(breakpoint);
    preserve();
  }

  private boolean isSameBreakpointLocation(Breakpoint b1, Breakpoint b2) {
    return b2.getLocation().getLineNumber() == b1.getLocation().getLineNumber()
        && b2.getLocation().getTarget().equals(b1.getLocation().getTarget());
  }

  @Override
  public void clear() {
    breakpoints.clear();
    preserve();
  }

  @Override
  public List<Breakpoint> getAll() {
    return unmodifiableList(breakpoints);
  }

  @Override
  public List<Breakpoint> getByPath(String filePath) {
    return breakpoints
        .stream()
        .filter(b -> b.getLocation().getTarget().equals(filePath))
        .collect(Collectors.toList());
  }

  @Override
  public Optional<Breakpoint> get(String filePath, int lineNumber) {
    return breakpoints
        .stream()
        .filter(
            b ->
                b.getLocation().getLineNumber() == lineNumber
                    && b.getLocation().getTarget().equals(filePath))
        .findAny();
  }

  private void preserve() {
    if (storage == null) {
      return;
    }

    List<BreakpointDto> breakpoints2save = new LinkedList<>();
    for (Breakpoint breakpoint : breakpoints) {
      breakpoints2save.add(toDto(breakpoint));
    }

    storage.setItem(storageKey, dtoFactory.toJson(breakpoints2save));
  }

  private List<? extends Breakpoint> readAll() {
    if (storage == null) {
      return emptyList();
    }

    String json = storage.getItem(storageKey);
    if (json == null) {
      return emptyList();
    }

    return dtoFactory
        .createListDtoFromJson(json, BreakpointDto.class)
        .stream()
        .map(BreakpointImpl::new)
        .collect(Collectors.toList());
  }

  /**
   * Remove all keys from the local storage that contain breakpoints for unexisted workspaces.
   *
   * <p>Implementation doesn't handle workspace removal, so it is necessary to check if workspaces
   * doesn't exist and remove local storage records if so.
   */
  private void clearOutdatedRecords() {
    for (int i = 0; i < storage.getLength(); i++) {
      String key = storage.key(i);
      if (key != null && key.startsWith(LOCAL_STORAGE_BREAKPOINTS_KEY_PREFIX)) {
        String wsId = key.substring(LOCAL_STORAGE_BREAKPOINTS_KEY_PREFIX.length());

        Promise<WorkspaceImpl> workspace = workspaceServiceClient.getWorkspace(wsId);
        workspace.catchError(
            arg -> {
              storage.removeItem(key);
            });
      }
    }
  }

  private BreakpointDto toDto(Breakpoint breakpoint) {
    BreakpointConfiguration breakpointConfiguration = breakpoint.getBreakpointConfiguration();
    BreakpointConfigurationDto breakpointConfigurationDto =
        breakpointConfiguration == null
            ? null
            : dtoFactory
                .createDto(BreakpointConfigurationDto.class)
                .withSuspendPolicy(breakpointConfiguration.getSuspendPolicy())
                .withHitCount(breakpointConfiguration.getHitCount())
                .withCondition(breakpointConfiguration.getCondition())
                .withConditionEnabled(breakpointConfiguration.isConditionEnabled())
                .withHitCountEnabled(breakpointConfiguration.isHitCountEnabled());

    Location location = breakpoint.getLocation();
    LocationDto locationDto =
        dtoFactory
            .createDto(LocationDto.class)
            .withTarget(location.getTarget())
            .withLineNumber(location.getLineNumber())
            .withExternalResourceId(location.getExternalResourceId())
            .withExternalResource(location.isExternalResource())
            .withResourceProjectPath(location.getResourceProjectPath());

    return dtoFactory
        .createDto(BreakpointDto.class)
        .withLocation(locationDto)
        .withEnabled(breakpoint.isEnabled())
        .withBreakpointConfiguration(breakpointConfigurationDto);
  }
}
