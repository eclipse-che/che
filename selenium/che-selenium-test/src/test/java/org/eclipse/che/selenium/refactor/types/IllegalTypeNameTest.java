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
package org.eclipse.che.selenium.refactor.types;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * //
 *
 * @author Musienko Maxim
 */
public class IllegalTypeNameTest {
  private static final String nameOfProject =
      NameGenerator.generate(IllegalTypeNameTest.class.getName(), 2);
  private static final String pathToPackageInChePrefix =
      nameOfProject + "/src" + "/main" + "/java" + "/renametype";

  private String pathToCurrentPackage;
  private String contentFromInA;

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setup() throws Exception {
    URL resource = IllegalTypeNameTest.this.getClass().getResource("/projects/RenameType");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        nameOfProject,
        ProjectTemplates.MAVEN_SIMPLE);
    ide.open(workspace);
  }

  @Test
  public void illegalTypeName4() throws Exception {
    projectExplorer.waitVisibleItem(nameOfProject);
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.scrollToItemByPath(nameOfProject + "/pom.xml");
    loader.waitOnClosed();
    setFieldsForTest("testIllegalTypeName4");
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActive();
    Assert.assertEquals(editor.getVisibleTextFromEditor(), contentFromInA);
    editor.waitTextIntoEditor(contentFromInA);
    projectExplorer.selectVisibleItem("A.java");
  }

  private void setFieldsForTest(String nameCurrentTest) throws Exception {
    pathToCurrentPackage = pathToPackageInChePrefix + "/" + nameCurrentTest;

    URL resourcesInA =
        getClass()
            .getResource(
                "/org/eclipse/che/selenium/refactor/types/" + nameCurrentTest + "//A.java");
    contentFromInA = getTextFromFile(resourcesInA);
  }

  private String getTextFromFile(URL url) throws Exception {
    String result = "";
    List<String> listWithAllLines =
        Files.readAllLines(Paths.get(url.toURI()), Charset.forName("UTF-8"));
    for (String buffer : listWithAllLines) {
      result += buffer + '\n';
    }

    return result;
  }
}
