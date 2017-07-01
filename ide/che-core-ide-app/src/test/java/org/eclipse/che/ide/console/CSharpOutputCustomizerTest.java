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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.google.gwtmockito.GwtMockitoTestRunner;

/**
 * JUnit test for C# Compilation Error/Warning and Stacktrace line detection in
 * CSharpOutputCustomazer.
 * 
 * See: Issue #5489 - .NET C# stacktrace support #5489
 */
@RunWith(GwtMockitoTestRunner.class)
public class CSharpOutputCustomizerTest {
    @Mock
    AppContext appContext;
    @Mock
    EditorAgent editorAgent;

    private OutputCustomizer outputCustomizer;

    @Before
    public void setUp() throws Exception {
        outputCustomizer = new CSharpOutputCustomizer(appContext, editorAgent);
    }

    /**
     * Test for the detection of initial stacktrace lines in CSharpOutputCustomazer.
     * These lines are not to be customized, however these lines show an examples of
     * beginning the StackTrace and might be used in future to set up the customizer
     * properly.
     * 
     * @throws Exception
     */
    @Test
    public void testInitialStackTraceLines() throws Exception {
        DefaultOutputCustomizerTest.testStackTraceLine(outputCustomizer, false,
                "Unhandled Exception: System.NullReferenceException: Object reference not set to an instance of an object.",
                null);
    }

    /**
     * Test for the detection of informative stacktrace lines in
     * CSharpOutputCustomizer. These lines have an information on file path and line
     * number for an exception
     * 
     * @throws Exception
     */
    @Test
    public void testValuableStackTraceLines() throws Exception {
        DefaultOutputCustomizerTest.testStackTraceLine(outputCustomizer, true,
                "   at hwapp.ppp.PPPProgram.Main1(String[] args) in /home/jeremy/projects/csharp/hwapp/ppp/PPPProgram.cs:line 18",
                "   at hwapp.ppp.PPPProgram.Main1(String[] args) in <a href='javascript:openCSSTL(\"/home/jeremy/projects/csharp/hwapp/ppp/PPPProgram.cs\",18);'>/home/jeremy/projects/csharp/hwapp/ppp/PPPProgram.cs:line 18</a>");
        DefaultOutputCustomizerTest.testStackTraceLine(outputCustomizer, true,
                "   at hwapp.Program.Main(String[] args) in /home/jeremy/projects/csharp/hwapp/Program.cs:line 10",
                "   at hwapp.Program.Main(String[] args) in <a href='javascript:openCSSTL(\"/home/jeremy/projects/csharp/hwapp/Program.cs\",10);'>/home/jeremy/projects/csharp/hwapp/Program.cs:line 10</a>");
    }

    /**
     * Test for the detection of compilation message lines in
     * CSharpOutputCustomizer. These lines have an information on file relative
     * path, project file path and line and column numbers for a Compilation
     * Error/Warning Message
     * 
     * @throws Exception
     */
    @Test
    public void testValuableCompilationMessagesLines() throws Exception {
        DefaultOutputCustomizerTest.testStackTraceLine(outputCustomizer, true,
                "Program.cs(2,13): error CS0234: The type or namespace name 'ppp' does not exist in the namespace 'hwapp' (are you missing an assembly reference?) [/home/jeremy/projects/csharp/hwapp/hwapp.csproj]",
                "<a href='javascript:openCSCM(\"Program.cs\",\"/home/jeremy/projects/csharp/hwapp/hwapp.csproj\",2,13);'>Program.cs(2,13)</a>: error CS0234: The type or namespace name 'ppp' does not exist in the namespace 'hwapp' (are you missing an assembly reference?) [/home/jeremy/projects/csharp/hwapp/hwapp.csproj]");
        DefaultOutputCustomizerTest.testStackTraceLine(outputCustomizer, true,
                "ppp/PPPProgram.cs(9,17): warning CS0219: The variable 'testIntValue' is assigned but its value is never used [/home/jeremy/projects/csharp/hwapp/hwapp.csproj]",
                "<a href='javascript:openCSCM(\"ppp/PPPProgram.cs\",\"/home/jeremy/projects/csharp/hwapp/hwapp.csproj\",9,17);'>ppp/PPPProgram.cs(9,17)</a>: warning CS0219: The variable 'testIntValue' is assigned but its value is never used [/home/jeremy/projects/csharp/hwapp/hwapp.csproj]");
    }

    /**
     * Test for the detection of non-stacktrace lines and parts of other kinds of
     * stacktraces (not the Java ones) that must not be customized in
     * CSharpOutputCustomizer. Other lines that might occur in output console
     * 
     * @throws Exception
     */
    @Test
    public void testNonCSharpStackTraceLines() throws Exception {
        DefaultOutputCustomizerTest.testStackTraceLine(outputCustomizer, false,
                "[STDOUT] Listening for transport dt_socket at address: 4403", null);
        DefaultOutputCustomizerTest.testStackTraceLine(outputCustomizer, false,
                "org.test.HighLevelException: org.test.MidLevelException: org.test.LowLevelException", null);
        DefaultOutputCustomizerTest.testStackTraceLine(outputCustomizer, false,
                "Caused by: org.test.MidLevelException: org.test.LowLevelException", null);
        DefaultOutputCustomizerTest.testStackTraceLine(outputCustomizer, false, 
                "Caused by: org.test.LowLevelException", null);
        DefaultOutputCustomizerTest.testStackTraceLine(outputCustomizer, false,
                "Exception in thread \"main\" java.lang.ArithmeticException: / by zero", null);
        DefaultOutputCustomizerTest.testStackTraceLine(outputCustomizer, false, 
                "   at org.test.Junk.main(Junk.java:6)", null);
        DefaultOutputCustomizerTest.testStackTraceLine(outputCustomizer, false,
                "   at org.test.TrashClass.throwItThere(Junk.java:51)", null);
        DefaultOutputCustomizerTest.testStackTraceLine(outputCustomizer, false,
                "   at MyClass$ThrowInConstructor.<init>(MyClass.java:16)", null);
        DefaultOutputCustomizerTest.testStackTraceLine(outputCustomizer, false, "   ... 1 more", null);
    }
}
