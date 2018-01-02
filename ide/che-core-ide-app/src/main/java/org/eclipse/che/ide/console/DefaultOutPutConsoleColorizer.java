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
package org.eclipse.che.ide.console;

import static com.google.gwt.regexp.shared.RegExp.compile;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import javax.inject.Inject;

/**
 * Default implementation {@link OutputConsoleColorizer} for {@link OutputConsoleView}.
 *
 * @author Vitaliy Guliy
 * @author Alexander Andriienko
 */
public class DefaultOutPutConsoleColorizer implements OutputConsoleColorizer {

  @Inject
  public DefaultOutPutConsoleColorizer() {}

  private static final String DOCKER = "DOCKER";
  private static final String ERROR = "ERROR";
  private static final String WARN = "WARN";
  private static final String STDOUT = "STDOUT";
  private static final String STDERR = "STDERR";

  // regexp
  private static String REGEXP =
      "\\[\\s*("
          + DOCKER
          + ")\\s*\\]|"
          + "\\[\\s*("
          + ERROR
          + ")\\s*\\]|"
          + "\\[\\s*("
          + WARN
          + ")\\s*\\]|"
          + "\\[\\s*("
          + STDOUT
          + ")\\s*\\]|"
          + "\\[\\s*("
          + STDERR
          + ")\\s*\\]";

  // color sequences
  private static String DOCKER_COLOR_SEQ = "\u001b[38;5;75m";
  private static String ERROR_COLOR_SEQ = "\u001b[38;5;9m";
  private static String WARN_COLOR_SEQ = "\u001b[38;5;214m";
  private static String STDOUT_COLOR_SEQ = "\u001b[38;5;112m";
  private static String STDERR_COLOR_SEQ = "\u001b[38;5;9m";
  private static String RESET_COLOR_SEQ = "\u001b[0m";

  // patterns
  private RegExp COLOR_REGEXP = compile(REGEXP, "g");

  @Override
  public String colorize(String outPutText) {
    StringBuilder result = new StringBuilder();
    MatchResult matchResult;
    int offSet = 0;

    while ((matchResult = COLOR_REGEXP.exec(outPutText)) != null) {
      for (int i = 1; i <= matchResult.getGroupCount(); i++) {
        String group = matchResult.getGroup(i);
        if (group != null) {
          result.append(outPutText.substring(offSet, matchResult.getIndex()));
          offSet = COLOR_REGEXP.getLastIndex();

          String textToColorize =
              outPutText.substring(matchResult.getIndex(), offSet);
          switch (group) {
            case DOCKER:
              textToColorize =
                  textToColorize.replace(DOCKER, DOCKER_COLOR_SEQ + DOCKER + RESET_COLOR_SEQ);
              break;
            case ERROR:
              textToColorize =
                  textToColorize.replace(ERROR, ERROR_COLOR_SEQ + ERROR + RESET_COLOR_SEQ);
              break;
            case WARN:
              textToColorize =
                  textToColorize.replace(WARN, WARN_COLOR_SEQ + WARN + RESET_COLOR_SEQ);
              break;
            case STDOUT:
              textToColorize =
                  textToColorize.replace(STDOUT, STDOUT_COLOR_SEQ + STDOUT + RESET_COLOR_SEQ);
              break;
            case STDERR:
              textToColorize =
                  textToColorize.replace(STDERR, STDERR_COLOR_SEQ + STDERR + RESET_COLOR_SEQ);
              break;
            default:
              // do nothing
          }
          result.append(textToColorize);
        }
      }
    }
    result.append(outPutText.substring(offSet));

    return result.toString();
  }
}
