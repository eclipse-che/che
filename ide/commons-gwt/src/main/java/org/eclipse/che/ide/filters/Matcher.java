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
package org.eclipse.che.ide.filters;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Matcher the wey to match some string. S
 *
 * @author Evgen Vidolob
 */
public interface Matcher {

  @Nullable
  List<Match> match(String word, String wordToMatch);

  class MatcherUtil {
    public static Matcher or(final Matcher... matchers) {
      return new Matcher() {
        @Override
        public List<Match> match(String word, String wordToMatch) {
          for (Matcher matcher : matchers) {
            List<Match> matches = matcher.match(word, wordToMatch);
            if (matches != null) {
              return matches;
            }
          }

          return null;
        }
      };
    }

    public static Matcher and(final Matcher... matchers) {
      return new Matcher() {
        @Override
        public List<Match> match(String word, String wordToMatch) {
          List<Match> result = new ArrayList<>();
          for (Matcher matcher : matchers) {
            List<Match> matchList = matcher.match(word, wordToMatch);
            if (matchList != null) {
              result.addAll(matchList);
            }
          }
          return result;
        }
      };
    }
  }
}
