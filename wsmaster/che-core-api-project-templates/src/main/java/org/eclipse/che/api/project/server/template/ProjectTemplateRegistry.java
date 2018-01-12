/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.project.server.template;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.inject.Singleton;
import org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor;

/** @author Vitaly Parfonov */
@Singleton
public class ProjectTemplateRegistry {

  private final Map<List<String>, List<ProjectTemplateDescriptor>> templates =
      new ConcurrentHashMap<>();

  public void register(List<String> tags, ProjectTemplateDescriptor template) {
    List<ProjectTemplateDescriptor> templateList = templates.get(tags);
    if (templateList == null) {
      templates.put(tags, templateList = new CopyOnWriteArrayList<>());
    }
    templateList.add(template);
  }

  public void register(List<String> tags, List<ProjectTemplateDescriptor> templates) {
    List<ProjectTemplateDescriptor> templateList = this.templates.get(tags);
    if (templateList == null) {
      this.templates.put(tags, new CopyOnWriteArrayList<>(templates));
    } else {
      templateList.addAll(templates);
    }
  }

  public List<ProjectTemplateDescriptor> getTemplates(List<String> tags) {
    List<ProjectTemplateDescriptor> templateDescriptors = new ArrayList<>();

    templates
        .entrySet()
        .stream()
        .filter(entry -> entry.getKey().containsAll(tags))
        .forEach(entry -> templateDescriptors.addAll(entry.getValue()));

    return templateDescriptors;
  }

  public List<ProjectTemplateDescriptor> getAllTemplates() {
    List<ProjectTemplateDescriptor> allTemplates = new ArrayList<>();
    for (Map.Entry<List<String>, List<ProjectTemplateDescriptor>> entry : templates.entrySet()) {
      allTemplates.addAll(entry.getValue());
    }
    return allTemplates;
  }
}
