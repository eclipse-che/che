/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.project.server.impl;

import static org.eclipse.che.api.fs.server.WsPathUtils.absolutize;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.VcsStatusProvider;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.project.shared.dto.TreeElement;

/**
 * Injects VCS status to attributes of {@link ItemReference} dto.
 *
 * @author Igor Vinokur
 */
@Singleton
public class ProjectServiceVcsStatusInjector {

  private final ProjectManager projectManager;
  private final Set<VcsStatusProvider> vcsStatusProviders;

  @Inject
  public ProjectServiceVcsStatusInjector(
      ProjectManager projectManager, Set<VcsStatusProvider> vcsStatusProviders) {
    this.projectManager = projectManager;
    this.vcsStatusProviders = vcsStatusProviders;
  }

  /**
   * Find related VCS provider and set VCS status of {@link ItemReference} file to it's attributes
   * if VCS provider is present.
   *
   * @param itemReference file to update
   */
  public ItemReference injectVcsStatus(ItemReference itemReference)
      throws ServerException, NotFoundException {
    Optional<VcsStatusProvider> optional = getVcsStatusProvider(itemReference.getProject());
    if (optional.isPresent()) {
      Map<String, String> attributes = new HashMap<>(itemReference.getAttributes());
      attributes.put("vcs.status", optional.get().getStatus(itemReference.getPath()).toString());
      itemReference.setAttributes(attributes);
    }
    return itemReference;
  }

  /**
   * Find related VCS provider and set VCS status of {@link ItemReference} file to it's attributes
   * to each item of the given list, if VCS provider is present.
   *
   * @param itemReferences list of {@link ItemReference} files to update
   */
  public List<ItemReference> injectVcsStatus(List<ItemReference> itemReferences)
      throws ServerException, NotFoundException {
    Optional<ItemReference> itemReferenceOptional =
        itemReferences
            .stream()
            .filter(itemReference -> "file".equals(itemReference.getType()))
            .findAny();
    if (itemReferenceOptional.isPresent()) {
      String project = normalizeProjectPath(itemReferenceOptional.get().getProject());
      Optional<VcsStatusProvider> vcsStatusProviderOptional = getVcsStatusProvider(project);
      if (vcsStatusProviderOptional.isPresent()) {
        List<String> itemReferenceFiles =
            itemReferences
                .stream()
                .filter(itemReference -> "file".equals(itemReference.getType()))
                .map(itemReference -> normalizeFilePath(itemReference.getPath()))
                .collect(Collectors.toList());
        Map<String, VcsStatusProvider.VcsStatus> status =
            vcsStatusProviderOptional.get().getStatus(project, itemReferenceFiles);

        itemReferences
            .stream()
            .filter(itemReference -> "file".equals(itemReference.getType()))
            .forEach(
                itemReference -> {
                  Map<String, String> attributes = new HashMap<>(itemReference.getAttributes());
                  attributes.put("vcs.status", status.get(itemReference.getPath()).toString());
                  itemReference.setAttributes(attributes);
                });
      }
    }
    return itemReferences;
  }

  /**
   * Find related VCS provider and set VCS status of {@link TreeElement} file to it's attributes to
   * each item of the given list, if VCS provider is present.
   *
   * @param treeElements list of {@link TreeElement} files to update
   */
  public List<TreeElement> injectVcsStatusTreeElements(List<TreeElement> treeElements)
      throws ServerException, NotFoundException {
    Optional<TreeElement> treeElementOptional =
        treeElements
            .stream()
            .filter(treeElement -> "file".equals(treeElement.getNode().getType()))
            .findAny();
    if (treeElementOptional.isPresent()) {
      String project = normalizeProjectPath(treeElementOptional.get().getNode().getProject());
      String projectWsPath = absolutize(project);
      Optional<VcsStatusProvider> vcsStatusProviderOptional = getVcsStatusProvider(projectWsPath);
      if (vcsStatusProviderOptional.isPresent()) {
        List<String> treeElementFiles =
            treeElements
                .stream()
                .filter(treeElement -> "file".equals(treeElement.getNode().getType()))
                .map(treeElement -> normalizeFilePath(treeElement.getNode().getPath()))
                .collect(Collectors.toList());
        Map<String, VcsStatusProvider.VcsStatus> status =
            vcsStatusProviderOptional.get().getStatus(projectWsPath, treeElementFiles);

        treeElements
            .stream()
            .filter(itemReference -> "file".equals(itemReference.getNode().getType()))
            .forEach(
                itemReference -> {
                  Map<String, String> attributes =
                      new HashMap<>(itemReference.getNode().getAttributes());
                  attributes.put(
                      "vcs.status", status.get(itemReference.getNode().getPath()).toString());
                  itemReference.getNode().setAttributes(attributes);
                });
      }
    }

    return treeElements;
  }

  private String normalizeFilePath(String filePath) {
    String normalizedPath = filePath.startsWith("/") ? filePath.substring(1) : filePath;
    return normalizedPath.substring(normalizedPath.indexOf("/") + 1);
  }

  private String normalizeProjectPath(String projectPath) {
    if (projectPath.startsWith("/")) {
      projectPath = projectPath.substring(1);
    }
    return projectPath;
  }

  private Optional<VcsStatusProvider> getVcsStatusProvider(String projectWsPath)
      throws ServerException, NotFoundException {
    List<String> vcsAttributes =
        projectManager
            .get(projectWsPath)
            .orElseThrow(() -> new NotFoundException("Can't find project"))
            .getAttributes()
            .get("vcs.provider.name");
    return vcsStatusProviders
        .stream()
        .filter(
            vcsStatusProvider ->
                vcsStatusProvider
                    .getVcsName()
                    .equals(vcsAttributes != null ? vcsAttributes.get(0) : null))
        .findAny();
  }
}
