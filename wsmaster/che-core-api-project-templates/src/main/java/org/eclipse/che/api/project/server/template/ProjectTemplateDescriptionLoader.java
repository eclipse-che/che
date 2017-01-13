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
package org.eclipse.che.api.project.server.template;

import com.google.inject.Inject;

import org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
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

    private final String                  templateDescriptionLocationDir;
    private final ProjectTemplateRegistry templateRegistry;

    /**
     * @param templateDescriptionLocationDir
     *         Describes value which is a path the directory with template descriptors (not sources).
     * @param templateRegistry
     *         registry which contains templates associated with tags
     */
    @Inject
    public ProjectTemplateDescriptionLoader(@Named("che.template.storage") String templateDescriptionLocationDir,
                                            ProjectTemplateRegistry templateRegistry) {
        this.templateDescriptionLocationDir = templateDescriptionLocationDir;
        this.templateRegistry = templateRegistry;
    }

    @PostConstruct
    public void start() {
        if (templateDescriptionLocationDir == null || !Files.exists(Paths.get(templateDescriptionLocationDir)) ||
            !Files.isDirectory(Paths.get(templateDescriptionLocationDir))) {
            LOG.error(getClass() +
                      " The configuration of project templates descriptors wasn't found or some problem with configuration was found.");
        } else {
            load(Paths.get(templateDescriptionLocationDir));
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
            templateRegistry.register(template.getTags(), template);
        }
    }
}
