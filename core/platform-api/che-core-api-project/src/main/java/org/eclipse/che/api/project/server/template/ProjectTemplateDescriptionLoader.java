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
package org.eclipse.che.api.project.server.template;

import com.google.inject.Inject;

import org.eclipse.che.api.project.shared.dto.ProjectTemplateDescriptor;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Reads project template descriptions that may be described in separate json-files for every project type. This file should be named as
 * &lt;project_type_id&gt;.json.
 *
 * @author Artem Zatsarynnyi
 * @author Dmitry Shnurenko
 */
@Singleton
public class ProjectTemplateDescriptionLoader {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectTemplateRegistry.class);

    private final String                  templateDescriptionsDir;
    private final String                  templateLocationDir;
    private final ProjectTemplateRegistry templateRegistry;

    /**
     * @param templateDescriptionsDir
     *         Describe path to the dir where to locate json file that describes templates for project types.
     *         Json file must have name like: "projectTypeId".json (e.g, maven.json, python.json and so on)
     * @param templateLocationDir
     *         Describe value to the dir where templates sources are located.
     *         If in ImportSourceDescriptor.location is set in the path ${project.template_location_dir}
     *         it will replaced with value that is set in configuration
     * @param templateRegistry
     *         registry which contains templates associated with tags
     */
    @Inject
    public ProjectTemplateDescriptionLoader(@Named("project.template_descriptions_dir") String templateDescriptionsDir,
                                            @Named("project.template_location_dir") String templateLocationDir,
                                            ProjectTemplateRegistry templateRegistry) {
        this.templateDescriptionsDir = templateDescriptionsDir;
        this.templateLocationDir = templateLocationDir;
        this.templateRegistry = templateRegistry;

        start();
    }

    public void start() {
        if (templateDescriptionsDir == null || !Files.exists(Paths.get(templateDescriptionsDir)) ||
            !Files.isDirectory(Paths.get(templateDescriptionsDir))) {
            LOG.error(getClass() +
                      " The configuration of project templates descriptors wasn't found or some problem with configuration was found.");
        } else {
            Path dirPath = Paths.get(templateDescriptionsDir);

            load(dirPath);
        }
    }

    private void load(@NotNull Path dirPath) {
        File templatesFolder = new File(dirPath.toString());

        File[] files = templatesFolder.listFiles();

        if (files == null) {
            LOG.error(getClass() + " The folder " + dirPath + "doesn't contain any template files.");
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                continue;
            }

            try (InputStream inputStream = new FileInputStream(file)) {
                resolveTemplate(inputStream);
            } catch (IOException exception) {
                LOG.error(getClass() + " Can't read file " + file.getPath());
            }
        }
    }

    private void resolveTemplate(InputStream stream) throws IOException {
        final List<ProjectTemplateDescriptor> templates = DtoFactory.getInstance().createListDtoFromJson(stream, 
                                                                                                         ProjectTemplateDescriptor.class);
        for (ProjectTemplateDescriptor template : templates) {
            SourceStorageDto source = template.getSource();
            String location = source.getLocation();
            
            if (location.contains("${project.template_location_dir}") && templateLocationDir != null) {
                source.setLocation(location.replace("${project.template_location_dir}", templateLocationDir));
            }

            templateRegistry.register(template.getTags(), template);
        }
    }
}
