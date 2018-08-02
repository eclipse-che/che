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
package org.eclipse.che.plugin.java.server.che;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.jdt.javadoc.JavaElementLinks;
import org.eclipse.core.internal.filebuffers.FileBuffersPlugin;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.codeassist.impl.AssistOptions;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.junit.After;
import org.junit.Before;

/** @author Evgen Vidolob */
// TODO: rework after new Project API
public abstract class BaseTest {

  protected static final String wsPath = BaseTest.class.getResource("/projects").getFile();
  private static final String workspacePath = BaseTest.class.getResource("/projects").getFile();
  protected static Map<String, String> options = new HashMap<>();
  protected static JavaProject project;
  protected static EventService eventService = new EventService();
  protected static ResourcesPlugin plugin /*= new ResourcesPlugin("target/index", workspacePath,
                                                                             new DummyProjectManager(workspacePath, eventService))*/;
  protected static JavaPlugin javaPlugin = new JavaPlugin(wsPath + "/set", null, null);
  protected static FileBuffersPlugin fileBuffersPlugin = new FileBuffersPlugin();

  static {
    plugin.start();
    javaPlugin.start();
  }

  public BaseTest() {
    options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
    options.put(JavaCore.CORE_ENCODING, "UTF-8");
    options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
    options.put(CompilerOptions.OPTION_TargetPlatform, JavaCore.VERSION_1_8);
    options.put(AssistOptions.OPTION_PerformVisibilityCheck, AssistOptions.ENABLED);
    options.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.WARNING);
    options.put(CompilerOptions.OPTION_TaskTags, CompilerOptions.WARNING);
    options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.WARNING);
    options.put(CompilerOptions.OPTION_SuppressWarnings, CompilerOptions.DISABLED);
    options.put(JavaCore.COMPILER_TASK_TAGS, "TODO,FIXME,XXX");
    options.put(
        JavaCore.COMPILER_PB_UNUSED_PARAMETER_INCLUDE_DOC_COMMENT_REFERENCE, JavaCore.ENABLED);
    options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
    options.put(CompilerOptions.OPTION_Process_Annotations, JavaCore.DISABLED);
  }

  protected static String getHandldeForRtJarStart() {
    String javaHome = System.getProperty("java.home") + "/lib/rt.jar";
    javaHome = javaHome.replaceAll("/", "\\\\/");
    return String.valueOf(JavaElementLinks.LINK_SEPARATOR) + "=test/" + javaHome;
  }

  @Before
  public void setUp() throws Exception {
    project =
        (JavaProject) JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject("/test");
  }

  @After
  public void closeProject() throws Exception {
    if (project != null) {
      project.close();
    }
    File pref = new File(wsPath + "/test/.che/project.preferences");
    if (pref.exists()) {
      pref.delete();
    }
  }
}
