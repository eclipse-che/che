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
package org.eclipse.che.ide.ext.java.client.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.che.ide.api.command.CommandPage;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.command.valueproviders.ClasspathMacro;
import org.eclipse.che.ide.ext.java.client.command.valueproviders.OutputDirMacro;
import org.eclipse.che.ide.ext.java.client.command.valueproviders.SourcepathMacro;
import org.eclipse.che.ide.macro.CurrentProjectPathMacro;

/**
 * Java command type.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class JavaCommandType implements CommandType {

  private static final String ID = "java";

  private final CurrentProjectPathMacro currentProjectPathMacro;
  private final SourcepathMacro sourcepathMacro;
  private final OutputDirMacro outputDirMacro;
  private final ClasspathMacro classpathMacro;
  private final JavaLocalizationConstant localizationConstants;
  private final List<CommandPage> pages;

  @Inject
  public JavaCommandType(
      JavaResources resources,
      JavaCommandPagePresenter page,
      CurrentProjectPathMacro currentProjectPathMacro,
      SourcepathMacro sourcepathMacro,
      OutputDirMacro outputDirMacro,
      ClasspathMacro classpathMacro,
      IconRegistry iconRegistry,
      JavaLocalizationConstant localizationConstants) {
    this.currentProjectPathMacro = currentProjectPathMacro;
    this.sourcepathMacro = sourcepathMacro;
    this.outputDirMacro = outputDirMacro;
    this.classpathMacro = classpathMacro;
    this.localizationConstants = localizationConstants;
    pages = new LinkedList<>();
    pages.add(page);

    iconRegistry.registerIcon(new Icon("command.type." + ID, resources.javaCategoryIcon()));
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public String getDisplayName() {
    return "Java";
  }

  @Override
  public String getDescription() {
    return localizationConstants.commandLineDescription();
  }

  @Override
  public List<CommandPage> getPages() {
    return pages;
  }

  @Override
  public String getCommandLineTemplate() {

    return "cd "
        + currentProjectPathMacro.getName()
        + "\njavac -classpath "
        + classpathMacro.getName()
        + " -sourcepath "
        + sourcepathMacro.getName()
        + " -d "
        + outputDirMacro.getName()
        + " src/Main.java"
        + "\njava -classpath "
        + classpathMacro.getName()
        + outputDirMacro.getName()
        + " Main";
  }

  @Override
  public String getPreviewUrlTemplate() {
    return "";
  }
}
