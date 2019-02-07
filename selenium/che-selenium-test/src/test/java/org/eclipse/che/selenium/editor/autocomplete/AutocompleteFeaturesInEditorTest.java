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
  private static final String PROJECT_NAME = NameGenerator.generate("TestProject", 4);
  private static final String[] autocompleteContentAfterFirst = {
    "request : HttpServletRequest",
    "response : HttpServletResponse",
    "numGuessByUser : String",
    "result : String",
    "view : ModelAndView",
    "secretNum : String AppController",
    "handleRequest(HttpServletRequest request, HttpServletResponse response) : ModelAndView AppController",
    "wait(long timeout, int nanos) : void Object",
    "wait(long timeout) : void Object",
    "wait() : void Object",
    "toString() : String Object",
    "notifyAll() : void Object",
    "notify() : void Object",
    "hashCode() : int Object",
    "getClass() : Class<?> Object",
    "finalize() : void Object",
    "equals(Object obj) : boolean Object",
    "clone() : Object Object",
    "AppController - org.eclipse.qa.examples"
  };

  private static final String[] autocompleteContentAfterSecondBeforeScroll = {
    "CASE_INSENSITIVE_ORDER : Comparator<java.lang.String> String",
    "valueOf(char[] data, int offset, int count) : String String",
    "valueOf(double d) : String String",
    "valueOf(float f) : String String",
    "valueOf(long l) : String String",
    "valueOf(int i) : String String",
    "valueOf(char c) : String String",
    "valueOf(boolean b) : String String",
    "valueOf(char[] data) : String String",
    "valueOf(Object obj) : String String",
    "trim() : String String",
    "toUpperCase(Locale locale) : String String",
    "toUpperCase() : String String",
    "toString() : String String",
    "toLowerCase(Locale locale) : String String",
    "toLowerCase() : String String",
    "toCharArray() : char[] String",
    "substring(int beginIndex, int endIndex) : String String",
    "substring(int beginIndex) : String String",
    "subSequence(int beginIndex, int endIndex) : CharSequence String",
    "startsWith(String prefix, int toffset) : boolean String",
    "startsWith(String prefix) : boolean String",
    "split(String regex, int limit) : String[] String",
    "split(String regex) : String[] String",
    "replaceFirst(String regex, String replacement) : String String",
    "replaceAll(String regex, String replacement) : String String",
    "replace(CharSequence target, CharSequence replacement) : String String",
    "replace(char oldChar, char newChar) : String String",
    "regionMatches(boolean ignoreCase, int toffset, String other, int ooffset, int len) : boolean String",
    "regionMatches(int toffset, String other, int ooffset, int len) : boolean String",
    "offsetByCodePoints(int index, int codePointOffset) : int String",
    "matches(String regex) : boolean String",
    "length() : int String",
    "lastIndexOf(String str, int fromIndex) : int String",
    "lastIndexOf(int ch, int fromIndex) : int String",
    "lastIndexOf(String str) : int String",
    "lastIndexOf(int ch) : int String",
    "join(CharSequence delimiter, Iterable<? extends CharSequence> elements) : String String",
    "join(CharSequence delimiter, CharSequence... elements) : String String",
    "isEmpty() : boolean String",
    "intern() : String String",
    "indexOf(String str, int fromIndex) : int String",
    "indexOf(int ch, int fromIndex) : int String",
    "indexOf(String str) : int String",
    "indexOf(int ch) : int String",
    "hashCode() : int String",
    "getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) : void String",
    "getBytes(int srcBegin, int srcEnd, byte[] dst, int dstBegin) : void String",
    "getBytes(Charset charset) : byte[] String",
    "getBytes(String charsetName) : byte[] String"
  };

  private static final String[] autocompleteContentAfterSecondAfterScroll = {
    "replace(CharSequence target, CharSequence replacement) : String String",
    "replace(char oldChar, char newChar) : String String",
    "regionMatches(boolean ignoreCase, int toffset, String other, int ooffset, int len) : boolean String",
    "regionMatches(int toffset, String other, int ooffset, int len) : boolean String",
    "offsetByCodePoints(int index, int codePointOffset) : int String",
    "matches(String regex) : boolean String",
    "length() : int String",
    "lastIndexOf(String str, int fromIndex) : int String",
    "lastIndexOf(int ch, int fromIndex) : int String",
    "lastIndexOf(String str) : int String",
    "lastIndexOf(int ch) : int String",
    "join(CharSequence delimiter, Iterable<? extends CharSequence> elements) : String String",
    "join(CharSequence delimiter, CharSequence... elements) : String String",
    "isEmpty() : boolean String",
    "intern() : String String",
    "indexOf(String str, int fromIndex) : int String",
    "indexOf(int ch, int fromIndex) : int String",
    "indexOf(String str) : int String",
    "indexOf(int ch) : int String",
    "hashCode() : int String",
    "getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) : void String",
    "getBytes(int srcBegin, int srcEnd, byte[] dst, int dstBegin) : void String",
    "getBytes(Charset charset) : byte[] String",
    "getBytes(String charsetName) : byte[] String",
    "getBytes() : byte[] String",
    "format(Locale l, String format, Object... args) : String String",
    "format(String format, Object... args) : String String",
    "equalsIgnoreCase(String anotherString) : boolean String",
    "equals(Object anObject) : boolean String",
    "endsWith(String suffix) : boolean String",
    "copyValueOf(char[] data, int offset, int count) : String String",
    "copyValueOf(char[] data) : String String",
    "contentEquals(CharSequence cs) : boolean String",
    "contentEquals(StringBuffer sb) : boolean String",
    "contains(CharSequence s) : boolean String",
    "concat(String str) : String String",
    "compareToIgnoreCase(String str) : int String",
    "compareTo(String anotherString) : int String",
    "codePointCount(int beginIndex, int endIndex) : int String",
    "codePointBefore(int index) : int String",
    "codePointAt(int index) : int String",
    "charAt(int index) : char String",
    "wait(long timeout, int nanos) : void Object",
    "wait(long timeout) : void Object",
    "wait() : void Object",
    "notifyAll() : void Object",
    "notify() : void Object",
    "getClass() : Class<?> Object"
  };

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
    consoles.waitJDTLSProjectResolveFinishedMessage(PROJECT_NAME);
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
    editor.setCursorToLine(38);
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
    for (String content : autocompleteContentAfterSecondBeforeScroll) {
      assertTrue(textFromEditorAfterSecondCall.contains(content));
    }

    editor.scrollAutocompleteFormToBottom();

    textFromEditorAfterSecondCall = editor.getAllVisibleTextFromAutocomplete();
    for (String content : autocompleteContentAfterSecondAfterScroll) {
      assertTrue(textFromEditorAfterSecondCall.contains(content));
    }

    editor.enterAutocompleteProposal("getBytes() : byte[] ");
    editor.waitTextIntoEditor("result.getBytes()");
    editor.typeTextIntoEditor(".");
    editor.launchAutocompleteAndWaitContainer();
    editor.enterAutocompleteProposal("toString() : String ");
    editor.typeTextIntoEditor(";");
    editor.waitTextIntoEditor("result.getBytes().toString();");
    editor.waitTabFileWithSavedStatus("AppController");
  }

  // need for check ready - state the ide editor
  private void reparseEditorCode() {
    editor.setCursorToLine(37);
    editor.typeTextIntoEditor("a;");
    editor.waitMarkerInPosition(ERROR, 37);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.BACK_SPACE.toString());
    editor.typeTextIntoEditor(Keys.BACK_SPACE.toString());
    editor.waitMarkerInvisibility(ERROR, 37);
  }
}
