/*
 * Copyright (c) 2017 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.console;

import static com.google.gwt.regexp.shared.RegExp.compile;

import com.google.gwt.regexp.shared.RegExp;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.resource.Path;

/**
 * Output customizer adds an anchor link to the lines that match a .NET C# compilation error or
 * warning message and a stack trace line pattern and installs the handler functions for the links.
 * The handler parses the stack trace line, searches for a candidate C# file to navigate to, opens
 * the found file in editor and reveals it to the required line and column (if available) according
 * to the line information
 *
 * @author Victor Rubezhny
 */
public class CSharpOutputCustomizer extends AbstractOutputCustomizer {

  private static final RegExp COMPILATION_ERROR_OR_WARNING =
      compile("(.+\\.(cs|CS)\\(\\d+,\\d+\\): (error|warning) .+: .+ \\[.+\\])");
  private static final RegExp LINE_AT = compile("(\\s+at .+ in .+\\.(cs|CS):line \\d+)");

  /**
   * Constructs Compound Output Customizer Object
   *
   * @param appContext
   * @param editorAgent
   */
  public CSharpOutputCustomizer(AppContext appContext, EditorAgent editorAgent) {
    super(appContext, editorAgent);

    exportCompilationMessageAnchorClickHandlerFunction();
    exportStacktraceLineAnchorClickHandlerFunction();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.eclipse.che.ide.extension.machine.client.outputspanel.console.
   * OutputCustomizer#canCustomize(java.lang.String)
   */
  @Override
  public boolean canCustomize(String text) {
    return (COMPILATION_ERROR_OR_WARNING.exec(text) != null || LINE_AT.exec(text) != null);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.eclipse.che.ide.extension.machine.client.outputspanel.console.
   * OutputCustomizer#customize(java.lang.String)
   */
  @Override
  public String customize(String text) {
    if (COMPILATION_ERROR_OR_WARNING.exec(text) != null)
      return customizeCompilationErrorOrWarning(text);

    if (LINE_AT.exec(text) != null) return customizeStacktraceLine(text);

    return text;
  }

  /*
   * Customizes a Compilation Error/Warning line
   */
  private String customizeCompilationErrorOrWarning(String text) {
    try {
      int end = text.indexOf("):"),
          openBracket = text.lastIndexOf("(", end),
          comma = text.lastIndexOf(",", end),
          closeSBracket = text.lastIndexOf("]"),
          openSBracket = text.lastIndexOf("[", closeSBracket);
      String fileName = text.substring(0, openBracket).trim();
      String projectFileName = text.substring(openSBracket + "[".length(), closeSBracket).trim();
      int lineNumber = Integer.valueOf(text.substring(openBracket + "(".length(), comma).trim());
      int columnNumber = Integer.valueOf(text.substring(comma + ",".length(), end).trim());

      String customText =
          "<a href='javascript:openCSCM(\""
              + fileName
              + "\",\""
              + projectFileName
              + "\","
              + lineNumber
              + ","
              + columnNumber
              + ");'>";
      customText += text.substring(0, end + ")".length());
      customText += "</a>";
      customText += text.substring(end + ")".length());
      text = customText;
    } catch (IndexOutOfBoundsException ex) {
      // ignore
    } catch (NumberFormatException ex) {
      // ignore
    }

    return text;
  }

  /*
   * Customizes a Stacktrace line
   */
  private String customizeStacktraceLine(String text) {
    try {
      int start = text.lastIndexOf(" in ") + " in ".length(), end = text.indexOf(":line ", start);

      String fileName = text.substring(start, end).trim();
      int lineNumber = Integer.valueOf(text.substring(end + ":line ".length()).trim());

      String customText = text.substring(0, start);
      customText += "<a href='javascript:openCSSTL(\"" + fileName + "\"," + lineNumber + ");'>";
      customText += text.substring(start);
      customText += "</a>";
      text = customText;
    } catch (IndexOutOfBoundsException ex) {
      // ignore
    } catch (NumberFormatException ex) {
      // ignore
    }

    return text;
  }

  /**
   * A callback that is to be called for an anchor for C# Compilation Error/Warning Message
   *
   * @param fileName
   * @param projectFile
   * @param lineNumber
   * @param columnNumber
   */
  public void handleCompilationMessageAnchorClick(
      String fileName, String projectFile, final int lineNumber, final int columnNumber) {
    if (fileName == null || projectFile == null) {
      return;
    }

    openFileInEditorAndReveal(
        appContext,
        editorAgent,
        Path.valueOf(projectFile).removeFirstSegments(1).parent().append(fileName),
        lineNumber,
        columnNumber);
  }

  /**
   * A callback that is to be called for an anchor for C# Runtime Exception Stacktrace line
   *
   * @param fileName
   * @param lineNumber
   */
  public void handleStacktraceLineAnchorClick(String fileName, int lineNumber) {
    if (fileName == null) {
      return;
    }

    openFileInEditorAndReveal(
        appContext, editorAgent, Path.valueOf(fileName).removeFirstSegments(1), lineNumber, 0);
  }

  /** Sets up a java callback to be called for an anchor for C# Compilation Error/Warning Message */
  public native void exportCompilationMessageAnchorClickHandlerFunction() /*-{
        var that = this;
        $wnd.openCSCM = $entry(function(fileName,projectFile,lineNumber,columnNumber) {
            that.@org.eclipse.che.ide.console.CSharpOutputCustomizer::handleCompilationMessageAnchorClick(*)(fileName,projectFile,lineNumber,columnNumber);
        });
    }-*/;

  /** Sets up a java callback to be called for an anchor for C# Runtime Exception Stacktrace line */
  public native void exportStacktraceLineAnchorClickHandlerFunction() /*-{
        var that = this;
        $wnd.openCSSTL = $entry(function(fileName,lineNumber) {
            that.@org.eclipse.che.ide.console.CSharpOutputCustomizer::handleStacktraceLineAnchorClick(*)(fileName,lineNumber);
        });
    }-*/;
}
