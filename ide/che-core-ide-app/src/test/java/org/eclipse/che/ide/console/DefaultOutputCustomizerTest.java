/*******************************************************************************
 * Copyright (c) 2017 RedHat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   RedHat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.console;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.google.gwtmockito.GwtMockitoTestRunner;

/**
 * JUnit test for stacktrace line detection in DefaultOutputCustomazer.
 * 
 * See: CHE-15 - Java stacktrace support (From Platform to Che Workspace)
 */
@RunWith(GwtMockitoTestRunner.class)
public class DefaultOutputCustomizerTest {
    @Mock
    AppContext appContext;
    @Mock
    EditorAgent editorAgent;

    private OutputCustomizer outputCustomizer;

    @Before
    public void setUp() throws Exception {
        outputCustomizer = new DefaultOutputCustomizer(appContext, editorAgent);
    }

    /*
     * Tests the specified customizer on customizable and non-customizable lines
     */
    static void testStackTraceLine(OutputCustomizer customizer, boolean shouldBeCustomizable, String line,
            String expectedCustomization) throws Exception {
        Assert.assertEquals(
                "Line [" + line + "] is " + (shouldBeCustomizable ? "" : "not ") + "customizable while it should"
                        + (shouldBeCustomizable ? "n\'t " : " ") + "be: ",
                Boolean.valueOf(customizer.canCustomize(line)), Boolean.valueOf(shouldBeCustomizable));
        if (shouldBeCustomizable) {
            Assert.assertEquals("Wrong customization result:", customizer.customize(line), expectedCustomization);
        }
    }

    /**
     * Test for the detection of initial stacktrace lines in
     * DefaultOutputCustomazer. These lines are not to be customized, however these
     * lines show an examples of beginning the StackTrace and might be used in
     * future to set up the customizer properly.
     * 
     * @throws Exception
     */
    @Test
    public void testInitialStackTraceLines() throws Exception {
        testStackTraceLine(outputCustomizer, false,
                "org.test.HighLevelException: org.test.MidLevelException: org.test.LowLevelException", null);
        testStackTraceLine(outputCustomizer, false, 
                "Caused by: org.test.MidLevelException: org.test.LowLevelException", null);
        testStackTraceLine(outputCustomizer, false, 
                "Caused by: org.test.LowLevelException", null);
        testStackTraceLine(outputCustomizer, false,
                "Exception in thread \"main\" java.lang.ArithmeticException: / by zero", null);
    }

    /**
     * Test for the detection of informative stacktrace lines in
     * DefaultOutputCustomazer. These lines have an information on qualified path,
     * file name and line number for an exception
     * 
     * @throws Exception
     */
    @Test
    public void testValuableStackTraceLines() throws Exception {
        testStackTraceLine(outputCustomizer, true, 
                "   at org.test.Junk.main(Junk.java:6)",
                "   at org.test.Junk.main(<a href='javascript:open(\"org.test.Junk.main\", \"Junk.java\", 6);'>Junk.java:6</a>)");
        testStackTraceLine(outputCustomizer, true, 
                "   at org.test.TrashClass.throwItThere(Junk.java:51)",
                "   at org.test.TrashClass.throwItThere(<a href='javascript:open(\"org.test.TrashClass.throwItThere\", \"Junk.java\", 51);'>Junk.java:51</a>)");
        testStackTraceLine(outputCustomizer, true, 
                "   at MyClass$ThrowInConstructor.<init>(MyClass.java:16)",
                "   at MyClass$ThrowInConstructor.<init>(<a href='javascript:open(\"MyClass$ThrowInConstructor.<init>\", \"MyClass.java\", 16);'>MyClass.java:16</a>)");
    }

    /**
     * Test for the detection of other stacktrace lines in DefaultOutputCustomazer.
     * Other lines that can be a part of Stack Trace, however do not contain any
     * useful information
     * 
     * @throws Exception
     */
    @Test
    public void testOtherStackTraceLines() throws Exception {
        testStackTraceLine(outputCustomizer, false, "   ... 1 more", null);
    }

    /**
     * Test for the detection of non-stacktrace lines and parts of other kinds of
     * stacktraces (not the Java ones) that must not be customized in
     * DefaultOutputCustomazer. Other lines that might occur in output console
     * 
     * @throws Exception
     */
    @Test
    public void testNonStackTraceLines() throws Exception {
        testStackTraceLine(outputCustomizer, false, 
                "[STDOUT] Listening for transport dt_socket at address: 4403", 
                null);
        testStackTraceLine(outputCustomizer, false,
                "Unhandled Exception: System.NullReferenceException: Object reference not set to an instance of an object.",
                null);
        testStackTraceLine(outputCustomizer, false,
                "   at hwapp.ppp.PPPProgram.Main1(String[] args) in /home/jeremy/projects/csharp/hwapp/ppp/PPPProgram.cs:line 18",
                null);
        testStackTraceLine(outputCustomizer, false,
                "   at hwapp.Program.Main(String[] args) in /home/jeremy/projects/csharp/hwapp/Program.cs:line 10",
                null);
        testStackTraceLine(outputCustomizer, false,
                "Program.cs(2,13): error CS0234: The type or namespace name 'ppp' does not exist in the namespace 'hwapp' (are you missing an assembly reference?) [/home/jeremy/projects/csharp/hwapp/hwapp.csproj]",
                null);
        testStackTraceLine(outputCustomizer, false,
                "ppp/PPPProgram.cs(9,17): warning CS0219: The variable 'testIntValue' is assigned but its value is never used [/home/jeremy/projects/csharp/hwapp/hwapp.csproj]",
                null);
    }
}
