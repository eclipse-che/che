/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.ui.util;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 *
 */
public class PatternConstructor {


	private PatternConstructor() {
		// don't instantiate
	}

	/**
	 * Creates a pattern element from the pattern string which is either a reg-ex expression or in our old
	 * 'StringMatcher' format.
	 * @param pattern The search pattern
	 * @param isCaseSensitive Set to <code>true</code> to create a case insensitive pattern
	 * @param isRegexSearch <code>true</code> if the passed string already is a reg-ex pattern
	 * @return The created pattern
	 * @throws PatternSyntaxException
	 */
	public static Pattern createPattern(String pattern, boolean isCaseSensitive, boolean isRegexSearch) throws PatternSyntaxException {
		if (!isRegexSearch) {
			pattern=  asRegEx(pattern, new StringBuffer()).toString();
		}

		if (!isCaseSensitive)
			return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);

		return Pattern.compile(pattern, Pattern.MULTILINE);
	}

	/**
	 * Creates a pattern element from the pattern string which is either a reg-ex expression or in our old
	 * 'StringMatcher' format.
	 * @param patterns The search patterns
	 * @param isCaseSensitive Set to <code>true</code> to create a case insensitive pattern
	 * @param isRegexSearch <code>true</code> if the passed string already is a reg-ex pattern
	 * @return The created pattern
	 * @throws PatternSyntaxException
	 */
	public static Pattern createPattern(String[] patterns, boolean isCaseSensitive, boolean isRegexSearch) throws PatternSyntaxException {
		StringBuffer pattern= new StringBuffer();
		for (int i= 0; i < patterns.length; i++) {
			if (i > 0) {
				pattern.append('|');
			}
			if (isRegexSearch) {
				pattern.append(patterns[i]);
			} else {
				asRegEx(patterns[i], pattern);
			}
		}
		return createPattern(pattern.toString(), isCaseSensitive, true);
	}


	/**
	 * Translates a StringMatcher pattern (using '*' and '?') to a regex pattern string
	 * @param stringMatcherPattern a pattern using '*' and '?'
	 * @param out string buffer
	 * @return string buffer
	 */
	private static StringBuffer asRegEx(String stringMatcherPattern, StringBuffer out) {
		boolean escaped= false;
		boolean quoting= false;

		int i= 0;
		while (i < stringMatcherPattern.length()) {
			char ch= stringMatcherPattern.charAt(i++);

			if (ch == '*' && !escaped) {
				if (quoting) {
					out.append("\\E"); //$NON-NLS-1$
					quoting= false;
				}
				out.append(".*"); //$NON-NLS-1$
				escaped= false;
				continue;
			} else if (ch == '?' && !escaped) {
				if (quoting) {
					out.append("\\E"); //$NON-NLS-1$
					quoting= false;
				}
				out.append("."); //$NON-NLS-1$
				escaped= false;
				continue;
			} else if (ch == '\\' && !escaped) {
				escaped= true;
				continue;

			} else if (ch == '\\' && escaped) {
				escaped= false;
				if (quoting) {
					out.append("\\E"); //$NON-NLS-1$
					quoting= false;
				}
				out.append("\\\\"); //$NON-NLS-1$
				continue;
			}

			if (!quoting) {
				out.append("\\Q"); //$NON-NLS-1$
				quoting= true;
			}
			if (escaped && ch != '*' && ch != '?' && ch != '\\')
				out.append('\\');
			out.append(ch);
			escaped= ch == '\\';

		}
		if (quoting)
			out.append("\\E"); //$NON-NLS-1$

		return out;
	}

}
