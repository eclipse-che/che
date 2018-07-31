/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.editor.autocomplete;

import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.ERROR;
import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class AutocompleteFeaturesInEditorTest {
  private static final String PROJECT_NAME =
      NameGenerator.generate(AutocompleteFeaturesInEditorTest.class.getSimpleName(), 4);
  private static final String[] autocompleteContentAfterFirst = {
    "request : HttpServletRequest",
    "response : HttpServletResponse",
    "result : String",
    "handleRequest(HttpServletRequest request, HttpServletResponse response) : ModelAndView",
    "wait(long timeout, int nanos) : void - Object",
    "wait(long timeout) : void - Object",
    "wait() : void - Object",
    "toString() : String - Object",
    "notifyAll() : void - Object",
    "notify() : void - Object"
  };

  private static final String[] autocompleteContentAfterSecond = {
    "charAt(int index) : char - String",
    "chars() : IntStream - CharSequence",
    "codePointAt(int index) : int - String",
    "codePointBefore(int index) : int - String",
    "codePointCount(int beginIndex, int endIndex) : int - String",
    "codePoints() : IntStream - CharSequence",
    "compareTo(String anotherString) : int - String",
    "compareToIgnoreCase(String str) : int - String",
    "concat(String str) : String - String",
    "contains(CharSequence s) : boolean - String",
    "contentEquals(CharSequence cs) : boolean - String",
    "contentEquals(StringBuffer sb) : boolean - String",
    "endsWith(String suffix) : boolean - String",
    "equals(Object anObject) : boolean - String",
    "equalsIgnoreCase(String anotherString) : boolean - String",
    "getBytes() : byte[] - String",
    "getBytes(Charset charset) : byte[] - String",
    "getBytes(String charsetName) : byte[] - String",
    "getBytes(int srcBegin, int srcEnd, byte[] dst, int dstBegin) : void - String",
    "getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) : void - String",
    "getClass() : Class<?> - Object",
    "hashCode() : int - String",
    "indexOf(int ch) : int - String",
    "indexOf(String str) : int - String",
    "indexOf(int ch, int fromIndex) : int - String",
    "indexOf(String str, int fromIndex) : int - String",
    "intern() : String - String",
    "isEmpty() : boolean - String",
    "lastIndexOf(int ch) : int - String",
    "lastIndexOf(String str) : int - String",
    "lastIndexOf(int ch, int fromIndex) : int - String",
    "lastIndexOf(String str, int fromIndex) : int - String",
    "length() : int - String",
    "matches(String regex) : boolean - String",
    "notify() : void - Object",
    "notifyAll() : void - Object",
    "offsetByCodePoints(int index, int codePointOffset) : int - String",
    "regionMatches(int toffset, String other, int ooffset, int len) : boolean - String",
    "regionMatches(boolean ignoreCase, int toffset, String other, int ooffset, int len) : boolean - String",
    "replace(char oldChar, char newChar) : String - String",
    "replace(CharSequence target, CharSequence replacement) : String - String",
    "replaceAll(String regex, String replacement) : String - String",
    "replaceFirst(String regex, String replacement) : String - String",
    "split(String regex) : String[] - String",
    "split(String regex, int limit) : String[] - String",
    "startsWith(String prefix) : boolean - String",
    "startsWith(String prefix, int toffset) : boolean - String",
    "subSequence(int beginIndex, int endIndex) : CharSequence - String",
    "substring(int beginIndex) : String - String",
    "substring(int beginIndex, int endIndex) : String - String"
  };

  private static final String contentAfterEditing =
      "public class AppController implements Controller {\n"
          + "    private static final String secretNum = Integer.toString(new Random().nextInt(10));\n"
          + "\n"
          + "    @Override\n"
          + "    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {\n"
          + "        String numGuessByUser = request.getParameter(\"numGuess\");\n"
          + "        String result = \"\";\n"
          + "        \n"
          + "        if (numGuessByUser != null && numGuessByUser.equals(secretNum)) {\n"
          + "            result = \"Congrats! The number is \" + secretNum;\n"
          + "        } \n"
          + "        \n"
          + "        else if (numGuessByUser != null) {\n"
          + "            result = \"Sorry, you failed. Try again later!\";\n"
          + "        }\n"
          + "\n"
          + "        ModelAndView view = new ModelAndView(\"guess_num\");\n"
          + "        result.getBytes().toString();\n"
          + "        view.addObject(\"num\", result);\n"
          + "        return view;\n"
          + "    }\n"
          + "}\n";
  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private Consoles consoles;

  @BeforeClass
  public void prepare() throws Exception {
    URL resource = getClass().getResource("/projects/default-spring-project");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(workspace);
  }

  @Test
  public void createJavaSpringProjectAndTestEditor() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    notificationsPopupPanel.waitProgressPopupPanelClose();
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByVisibleNameInExplorer("AppController.java");
    editor.waitActive();
    loader.waitOnClosed();
    reparseEditorCode();
    editor.setCursorToLine(37);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.launchAutocompleteAndWaitContainer();
    String textFromEditorAfterFirstCall = editor.getAllVisibleTextFromAutocomplete();
    for (String content : autocompleteContentAfterFirst) {
      assertTrue(textFromEditorAfterFirstCall.contains(content));
    }

    editor.enterAutocompleteProposal("result : String");
    projectExplorer.openItemByVisibleNameInExplorer("AppController.java");
    editor.waitTextIntoEditor("result");
    editor.typeTextIntoEditor(".");
    editor.launchAutocompleteAndWaitContainer();
    String textFromEditorAfterSecondCall = editor.getAllVisibleTextFromAutocomplete();
    for (String content : autocompleteContentAfterSecond) {
      assertTrue(textFromEditorAfterSecondCall.contains(content));
    }
    editor.enterAutocompleteProposal("getBytes() : byte[]");
    editor.typeTextIntoEditor(".");
    editor.launchAutocompleteAndWaitContainer();
    editor.enterAutocompleteProposal("toString() : String");
    editor.typeTextIntoEditor(";");
    consoles.closeProcessesArea();
    editor.waitTextIntoEditor(contentAfterEditing);
    editor.waitTabFileWithSavedStatus("AppController");
  }

  // need for check ready - state the ide editor
  private void reparseEditorCode() {
    editor.setCursorToLine(36);
    editor.typeTextIntoEditor("a;");
    editor.waitMarkerInPosition(ERROR, 36);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.BACK_SPACE.toString());
    editor.typeTextIntoEditor(Keys.BACK_SPACE.toString());
    editor.waitMarkerInvisibility(ERROR, 36);
  }
}
