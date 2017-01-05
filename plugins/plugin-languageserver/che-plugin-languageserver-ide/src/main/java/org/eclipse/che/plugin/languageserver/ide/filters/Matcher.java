/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.languageserver.ide.filters;

import org.eclipse.che.commons.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Evgen Vidolob
 */
public interface Matcher {

    @Nullable
    List<Match> match(String word, String wordToMatch);

    class MatcherUtil{
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
