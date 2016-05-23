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
package org.eclipse.che.ide.ext.java.client.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.command.valueproviders.ClasspathProvider;
import org.eclipse.che.ide.ext.java.client.command.valueproviders.OutputDirProvider;
import org.eclipse.che.ide.ext.java.client.command.valueproviders.SourcepathProvider;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfiguration;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfigurationFactory;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfigurationPage;
import org.eclipse.che.ide.extension.machine.client.command.CommandType;
import org.eclipse.che.ide.extension.machine.client.command.valueproviders.CurrentProjectPathProvider;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Java command type.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class JavaCommandType implements CommandType {

    private static final String ID               = "java";
    private static final String DISPLAY_NAME     = "Java";

    private final JavaResources                                                        resources;
    private final CurrentProjectPathProvider                                           currentProjectPathProvider;
    private final SourcepathProvider                                                   sourcepathProvider;
    private final OutputDirProvider                                                    outputDirProvider;
    private final ClasspathProvider classpathProvider;
    private final JavaCommandConfigurationFactory                                      configurationFactory;
    private final Collection<CommandConfigurationPage<? extends CommandConfiguration>> pages;

    @Inject
    public JavaCommandType(JavaResources resources,
                           JavaCommandPagePresenter page,
                           CurrentProjectPathProvider currentProjectPathProvider,
                           SourcepathProvider sourcepathProvider,
                           OutputDirProvider outputDirProvider,
                           ClasspathProvider classpathProvider,
                           IconRegistry iconRegistry) {
        this.resources = resources;
        this.currentProjectPathProvider = currentProjectPathProvider;
        this.sourcepathProvider = sourcepathProvider;
        this.outputDirProvider = outputDirProvider;
        this.classpathProvider = classpathProvider;
        configurationFactory = new JavaCommandConfigurationFactory(this);
        pages = new LinkedList<>();
        pages.add(page);

        iconRegistry.registerIcon(new Icon(ID + ".commands.category.icon", resources.javaCategoryIcon()));
    }

    @NotNull
    @Override
    public String getId() {
        return ID;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @NotNull
    @Override
    public SVGResource getIcon() {
        return resources.javaCategoryIcon();
    }

    @NotNull
    @Override
    public Collection<CommandConfigurationPage<? extends CommandConfiguration>> getConfigurationPages() {
        return pages;
    }

    @NotNull
    @Override
    public CommandConfigurationFactory<JavaCommandConfiguration> getConfigurationFactory() {
        return configurationFactory;
    }

    @NotNull
    @Override
    public String getCommandTemplate() {
        return "cd " + currentProjectPathProvider.getKey() +
               " && javac -classpath " + classpathProvider.getKey() +
               " -sourcepath " + sourcepathProvider.getKey() +
               " -d " + outputDirProvider.getKey() +
               " src/Main.java" +
               " && java -classpath " + classpathProvider.getKey() + outputDirProvider.getKey() +
               " com.company.Main";
    }

    @Override
    public String getPreviewUrlTemplate() {
        return "";
    }
}
