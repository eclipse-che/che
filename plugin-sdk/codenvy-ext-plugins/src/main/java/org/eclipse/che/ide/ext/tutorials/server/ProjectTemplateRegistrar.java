/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.tutorials.server;


import org.eclipse.che.api.project.server.ProjectTemplateDescription;
import org.eclipse.che.api.project.server.ProjectTemplateRegistry;
import org.eclipse.che.api.project.shared.dto.ProjectTemplateDescriptor;
import org.eclipse.che.ide.ext.tutorials.shared.Constants;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

import static org.eclipse.che.api.project.server.DtoConverter.toTemplateDescriptor;

/**
 * @author Vitaly Parfonov
 */
@Singleton
public class ProjectTemplateRegistrar {

    private final String baseUrl;
    private final ProjectTemplateRegistry projectTemplateRegistry;

    @Inject
    public ProjectTemplateRegistrar(@Named("extension-url") String baseUrl,
                                    ProjectTemplateRegistry projectTemplateRegistry) {
        this.baseUrl = baseUrl;
        this.projectTemplateRegistry = projectTemplateRegistry;

    }

    @PostConstruct
    private void register() {
        projectTemplateRegistry.register(Constants.TUTORIAL_ID, getTemplates());
    }


    private List<ProjectTemplateDescriptor> getTemplates() {
        final List<ProjectTemplateDescriptor> list = new ArrayList<>();


        list.add(toTemplateDescriptor(new ProjectTemplateDescription("Samples",
                                                                     "zip",
                                                                     "GIST EXAMPLE",
                                                                     "Simple Codenvy extension project is demonstrating basic usage " +
                                                                     "Codenvy API.",
                                                                     baseUrl + "/gist-extension.zip"), Constants.TUTORIAL_ID));

        list.add(toTemplateDescriptor(new ProjectTemplateDescription("Samples",
                                                "zip",
                                                "EMPTY EXTENSION PROJECT",
                                                "This is a ready to use structure of a Codenvy extension with a minimal set of files and dependencies.",
                                                baseUrl + "/empty-extension.zip"), Constants.TUTORIAL_ID));

        list.add(toTemplateDescriptor(new ProjectTemplateDescription("Samples",
                                                "zip",
                                                "HELLO WORLD EXTENSION",
                                                "This is a simple Codenvy Extension that prints Hello World in Output console and adds Hello World item to a content menu.",
                                                baseUrl + "/helloworld-extension.zip"), Constants.TUTORIAL_ID));

        list.add(toTemplateDescriptor(new ProjectTemplateDescription("Samples",
                                                "zip",
                                                "ANALYTICS EVENT LOGGER EXTENSION",
                                                "This is a simple Codenvy Extension that logs an event for the Analytics.",
                                                baseUrl + "/analytics-event-logger-extension.zip"), Constants.TUTORIAL_ID));

        list.add(toTemplateDescriptor(new ProjectTemplateDescription("Samples",
                                                "zip",
                                                "NOTIFICATION API TUTORIAL",
                                                "Tutorial that is demonstrating how to use Codenvy Notification API.",
                                                baseUrl + "/notification-api-tutorial.zip"), Constants.TUTORIAL_ID));

        list.add(toTemplateDescriptor(new ProjectTemplateDescription("Samples",
                                                "zip",
                                                "ACTION API TUTORIAL",
                                                "Tutorial that is demonstrating how to use Codenvy Action API.",
                                                baseUrl + "/action-api-tutorial.zip"), Constants.TUTORIAL_ID));

        list.add(toTemplateDescriptor(new ProjectTemplateDescription("Samples",
                                                "zip",
                                                "PART API TUTORIAL",
                                                "Tutorial that is demonstrating how to use Codenvy Part API.",
                                                baseUrl + "/parts-api-tutorial.zip"), Constants.TUTORIAL_ID));

        list.add(toTemplateDescriptor(new ProjectTemplateDescription("Samples",
                                                "zip",
                                                "EDITOR API TUTORIAL",
                                                "Tutorial that is demonstrating how to use Codenvy Editor API.",
                                                baseUrl + "/editor-api-tutorial.zip"), Constants.TUTORIAL_ID));

        list.add(toTemplateDescriptor(new ProjectTemplateDescription("Samples",
                                                "zip",
                                                "WYSIWYG EDITOR TUTORIAL",
                                                "The following tutorial will take you through simple example to learn how to implement " +
                                                "WYSIWYG editor.",
                                                baseUrl + "/wysiwyg-editor-tutorial.zip"), Constants.TUTORIAL_ID));

        list.add(toTemplateDescriptor(new ProjectTemplateDescription("Samples",
                                                "zip",
                                                "GIN TUTORIAL",
                                                "The following tutorial will take you through simple example to learn how to use GIN with" +
                                                " Codenvy API.",
                                                baseUrl + "/gin-tutorial.zip"), Constants.TUTORIAL_ID));

        list.add(toTemplateDescriptor(new ProjectTemplateDescription("Samples",
                                                "zip",
                                                "THEME API TUTORIAL",
                                                "The following tutorial will take you through simple example to learn how to use Theme " +
                                                "with Codenvy API.",
                                                baseUrl + "/theme-api-tutorial.zip"), Constants.TUTORIAL_ID));

        list.add(toTemplateDescriptor(new ProjectTemplateDescription("Samples",
                                                "zip",
                                                "ICON REGISTRY API TUTORIAL",
                                                "The following tutorial will take you through simple example to learn how to use " +
                                                "IconRegistry.",
                                                baseUrl + "/icons-registry-api-tutorial.zip"), Constants.TUTORIAL_ID));

        list.add(toTemplateDescriptor(new ProjectTemplateDescription("Samples",
                                                "zip",
                                                "SERVER SIDE API TUTORIAL",
                                                "The following tutorial will take you through a simple example demonstrating registration" +
                                                "of a server side component and server-client side communication.",
                                                baseUrl + "/server-side-tutorial.zip"), Constants.TUTORIAL_ID));
        return list;
    }

}
