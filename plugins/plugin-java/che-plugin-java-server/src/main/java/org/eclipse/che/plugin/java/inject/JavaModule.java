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
package org.eclipse.che.plugin.java.inject;

import static java.util.Arrays.asList;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncher;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.java.languageserver.JavaLanguageServerLauncher;
import org.eclipse.che.plugin.java.languageserver.ProjectProxyLauncher;

/** @author Anatolii Bazko */
@DynaModule
public class JavaModule extends AbstractModule {
  public static final String JAVA_SOURCE = "javaSource";
  public static final String JAVA_CLASS = "javaClass";

  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), LanguageServerLauncher.class)
        .addBinding()
        .toInstance(new ProjectProxyLauncher(new JavaLanguageServerLauncher()));
    LanguageDescription javaSource = new LanguageDescription();
    javaSource.setFileExtensions(asList("java"));
    javaSource.setLanguageId(JAVA_SOURCE);
    javaSource.setMimeType("text/x-java-source");
    Multibinder.newSetBinder(binder(), LanguageDescription.class)
        .addBinding()
        .toInstance(javaSource);

    LanguageDescription javaClass = new LanguageDescription();
    javaClass.setFileExtensions(asList("class"));
    javaClass.setLanguageId(JAVA_CLASS);
    javaClass.setMimeType("text/x-java-source");
    Multibinder.newSetBinder(binder(), LanguageDescription.class)
        .addBinding()
        .toInstance(javaClass);
  }
}
