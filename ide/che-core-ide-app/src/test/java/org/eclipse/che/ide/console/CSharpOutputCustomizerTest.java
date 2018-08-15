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

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

/**
 * JUnit test for C# Compilation Error/Warning and Stacktrace line detection in
 * CSharpOutputCustomizer.
 *
 * <p>See: Issue #5489 - .NET C# stacktrace support #5489
 *
 * @author Victor Rubezhny
 */
@RunWith(GwtMockitoTestRunner.class)
public class CSharpOutputCustomizerTest extends BaseOutputCustomizerTest {
  @Mock AppContext appContext;
  @Mock EditorAgent editorAgent;

  @Before
  public void setUp() throws Exception {
    OutputCustomizer outputCustomizer = new CSharpOutputCustomizer(appContext, editorAgent);
    setupTestCustomizers(outputCustomizer, new OutputCustomizer[] {outputCustomizer});
  }

  /**
   * Test for the detection of initial stacktrace lines in CSharpOutputCustomizer. These lines are
   * not to be customized, however these lines show an examples of beginning the StackTrace and
   * might be used in future to set up the customizer properly.
   *
   * @throws Exception
   */
  @Test
  public void testInitialStackTraceLines() throws Exception {
    testStackTraceLine(
        "Unhandled Exception: System.NullReferenceException: Object reference not set to an instance of an object.");
  }

  /**
   * Test for the detection of informative stacktrace lines in CSharpOutputCustomizer. These lines
   * have an information on file path and line number for an exception
   *
   * @throws Exception
   */
  @Test
  public void testValuableStackTraceLines() throws Exception {
    testStackTraceLine(
        CSharpOutputCustomizer.class,
        "   at hwapp.ppp.PPPProgram.Main1(String[] args) in /home/jeremy/projects/csharp/hwapp/ppp/PPPProgram.cs:line 18",
        "   at hwapp.ppp.PPPProgram.Main1(String[] args) in <a href='javascript:openCSSTL(\"/home/jeremy/projects/csharp/hwapp/ppp/PPPProgram.cs\",18);'>/home/jeremy/projects/csharp/hwapp/ppp/PPPProgram.cs:line 18</a>");
    testStackTraceLine(
        CSharpOutputCustomizer.class,
        "   at hwapp.Program.Main(String[] args) in /home/jeremy/projects/csharp/hwapp/Program.cs:line 10",
        "   at hwapp.Program.Main(String[] args) in <a href='javascript:openCSSTL(\"/home/jeremy/projects/csharp/hwapp/Program.cs\",10);'>/home/jeremy/projects/csharp/hwapp/Program.cs:line 10</a>");
  }

  /**
   * Test for the detection of compilation message lines in CSharpOutputCustomizer. These lines have
   * an information on file relative path, project file path and line and column numbers for a
   * Compilation Error/Warning Message
   *
   * @throws Exception
   */
  @Test
  public void testValuableCompilationMessagesLines() throws Exception {
    testStackTraceLine(
        CSharpOutputCustomizer.class,
        "Program.cs(2,13): error CS0234: The type or namespace name 'ppp' does not exist in the namespace 'hwapp' (are you missing an assembly reference?) [/home/jeremy/projects/csharp/hwapp/hwapp.csproj]",
        "<a href='javascript:openCSCM(\"Program.cs\",\"/home/jeremy/projects/csharp/hwapp/hwapp.csproj\",2,13);'>Program.cs(2,13)</a>: error CS0234: The type or namespace name 'ppp' does not exist in the namespace 'hwapp' (are you missing an assembly reference?) [/home/jeremy/projects/csharp/hwapp/hwapp.csproj]");
    testStackTraceLine(
        CSharpOutputCustomizer.class,
        "ppp/PPPProgram.cs(9,17): warning CS0219: The variable 'testIntValue' is assigned but its value is never used [/home/jeremy/projects/csharp/hwapp/hwapp.csproj]",
        "<a href='javascript:openCSCM(\"ppp/PPPProgram.cs\",\"/home/jeremy/projects/csharp/hwapp/hwapp.csproj\",9,17);'>ppp/PPPProgram.cs(9,17)</a>: warning CS0219: The variable 'testIntValue' is assigned but its value is never used [/home/jeremy/projects/csharp/hwapp/hwapp.csproj]");
  }

  /**
   * Test for the detection of non-stacktrace lines and parts of other kinds of stacktraces (not the
   * Java ones) that must not be customized in CSharpOutputCustomizer. Other lines that might occur
   * in output console
   *
   * @throws Exception
   */
  @Test
  public void testNonCSharpStackTraceLines() throws Exception {
    // Terminal Console
    testStackTraceLine("[STDOUT] Listening for transport dt_socket at address: 4403");
    testStackTraceLine(
        "[STDOUT] 2017-07-06 08:58:34,647 [ForkJoinPool.commonPool-worker-3] DEBUG o.j.t.l.t.DocumentManager.findSelectedWord - Looking for word at Position 2 in 'textDocument/badWord:Warning:name:So bad! '");

    // Java Stacktrace
    testStackTraceLine(
        "org.test.HighLevelException: org.test.MidLevelException: org.test.LowLevelException");
    testStackTraceLine("Caused by: org.test.MidLevelException: org.test.LowLevelException");
    testStackTraceLine("Caused by: org.test.LowLevelException");
    testStackTraceLine("Exception in thread \"main\" java.lang.ArithmeticException: / by zero");
    testStackTraceLine("   at org.test.Junk.main(Junk.java:6)");
    testStackTraceLine("   at org.test.TrashClass.throwItThere(Junk.java:51)");
    testStackTraceLine("   at MyClass$ThrowInConstructor.<init>(MyClass.java:16)");
    testStackTraceLine("   ... 1 more");

    // C/CPP
    testStackTraceLine("hello.cc: In function ‘int main()’:");
    testStackTraceLine("hello.cc:8:13: warning: division by zero [-Wdiv-by-zero]");
    testStackTraceLine("hello.cc:8:2: error: ‘Module’ was not declared in this scope");
    testStackTraceLine("hello.cc:4:25: fatal error: module/Module: No such file or directory");
    testStackTraceLine(
        "/projects/console-cc-simple/hello.cc:23: undefined reference to `Module::sayHello[abi:cxx11]()'");
    testStackTraceLine("     return 0/0;");
    testStackTraceLine("             ^");
  }
}
