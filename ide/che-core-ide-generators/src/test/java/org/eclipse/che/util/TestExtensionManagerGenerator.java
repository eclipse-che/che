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
package org.eclipse.che.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import static org.junit.Assert.assertEquals;

/** @author <a href="mailto:nzamosenchuk@exoplatform.com">Nikolay Zamosenchuk</a> */
public class TestExtensionManagerGenerator {

    @Test
    public void shouldMatchExtensions() {
        List<String> correct = new ArrayList<String>();
        correct.add("@Singleton @Extension  (title = \"Demo extension1\", id = \"ide.ext.demo\", version = \"2.0.0\")"
                    + "public class DemoExtension");
        // no whitespace after Extension keyword
        correct.add("@Singleton @Extension(title = \"Demo extension2\", id = \"ide.ext.demo\", version = \"2.0.0\")"
                    + "public class DemoExtension");
        // linebrake after Extension()
        correct.add("@Singleton "
                    + "@Extension  (title = \"Demo extension3\", id = \"ide.ext.demo\", version = \"2.0.0\") \n"
                    + "public class DemoExtension");
        matchExtensions(correct, true);
    }

    @Test
    public void shouldNotMatchExtensions() {
        List<String> incorrect = new ArrayList<String>();
        // no "@" char before extension
        incorrect.add("@Singleton Extension  (title = \"Demo extension1\", id = \"ide.ext.demo\", version = \"2.0.0\")"
                      + "public class DemoExtension");
        matchExtensions(incorrect, false);
    }

    /**
     * Checks that Extension Pattern matches or not @Extension annotations
     *
     * @param strings
     *         the collection of strings to test
     * @param expected
     *         expected result
     */
    protected void matchExtensions(List<String> strings, boolean expected) {
        for (String matchingString : strings) {
            Matcher matcher = ExtensionManagerGenerator.EXT_PATTERN.matcher(matchingString);
            assertEquals(String.format("Line '%s' should" + (expected ? "" : " not") + " match", matchingString),
                         expected, matcher.matches());
        }
    }

}
