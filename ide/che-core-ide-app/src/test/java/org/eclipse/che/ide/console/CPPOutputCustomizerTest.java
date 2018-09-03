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
 * JUnit test for C/CPP Compilation Error/Warning line detection in CPPOutputCustomizer.
 *
 * <p>See: Issue #5565 - C/CPP compilation error/warning messages support #5565
 *
 * @author Victor Rubezhny
 */
@RunWith(GwtMockitoTestRunner.class)
public class CPPOutputCustomizerTest extends BaseOutputCustomizerTest {
  @Mock AppContext appContext;
  @Mock EditorAgent editorAgent;

  @Before
  public void setUp() throws Exception {
    OutputCustomizer outputCustomizer = new CPPOutputCustomizer(appContext, editorAgent);
    setupTestCustomizers(outputCustomizer, new OutputCustomizer[] {outputCustomizer});
  }

  /**
   * Test for the detection of initial Compilation Message lines in CPPOutputCustomizer. These lines
   * are not to be customized, however these lines show an examples of beginning of a compilation
   * message and might be used in future to set up the customizer properly.
   *
   * @throws Exception
   */
  @Test
  public void testInitialCompilationMessageLines() throws Exception {
    testStackTraceLine("hello.cc: In function ‘int main()’:");
    testStackTraceLine("hello.o: In function `main':");
  }

  /**
   * Test for the detection of Compilation Message lines in CPPOutputCustomizer. These lines have an
   * information on file relative path, project file path and line and column numbers for a
   * Compilation Error/Warning Message
   *
   * @throws Exception
   */
  @Test
  public void testValuableCompilationMessagesLines() throws Exception {
    testStackTraceLine(
        CPPOutputCustomizer.class,
        "hello.cc:8:13: warning: division by zero [-Wdiv-by-zero]",
        "<a href='javascript:openCM(\"hello.cc\",8,13);'>hello.cc:8:13</a>: warning: division by zero [-Wdiv-by-zero]");
    testStackTraceLine(
        CPPOutputCustomizer.class,
        "hello.cc:8:2: error: ‘Module’ was not declared in this scope",
        "<a href='javascript:openCM(\"hello.cc\",8,2);'>hello.cc:8:2</a>: error: ‘Module’ was not declared in this scope");
    testStackTraceLine(
        CPPOutputCustomizer.class,
        "hello.cc:4:25: fatal error: module/Module: No such file or directory",
        "<a href='javascript:openCM(\"hello.cc\",4,25);'>hello.cc:4:25</a>: fatal error: module/Module: No such file or directory");
    testStackTraceLine(
        CPPOutputCustomizer.class,
        "/projects/console-cc-simple/hello.cc:23: undefined reference to `Module::sayHello[abi:cxx11]()'",
        "<a href='javascript:openLM(\"/projects/console-cc-simple/hello.cc\",23);'>/projects/console-cc-simple/hello.cc:23</a>: undefined reference to `Module::sayHello[abi:cxx11]()'");
  }

  /**
   * Test for the detection of other Compilation Message lines in CPPOutputCustomizer. Other lines
   * that can be a part of Compilation Message, however do not contain any useful information
   *
   * @throws Exception
   */
  @Test
  public void testOtherCompilationMessageLines() throws Exception {
    testStackTraceLine("     return 0/0;");
    testStackTraceLine("             ^");
    testStackTraceLine("compilation terminated.");
    testStackTraceLine("collect2: error: ld returned 1 exit status");
  }

  /**
   * Test for the detection of non-Compilation Message lines and parts of other kinds of stacktraces
   * that must not be customized in CPPOutputCustomizer. Other lines that might occur in output
   * console
   *
   * @throws Exception
   */
  @Test
  public void testNonCPPCompilationMessageLines() throws Exception {
    // Terminal Console
    testStackTraceLine("[STDOUT] Listening for transport dt_socket at address: 4403");
    testStackTraceLine(
        "[STDOUT] 2017-07-06 08:58:34,647 [ForkJoinPool.commonPool-worker-3] DEBUG o.j.t.l.t.DocumentManager.findSelectedWord - Looking for word at Position 2 in 'textDocument/badWord:Warning:name:So bad! '");

    // C#
    testStackTraceLine(
        "Unhandled Exception: System.NullReferenceException: Object reference not set to an instance of an object.");
    testStackTraceLine(
        "   at hwapp.ppp.PPPProgram.Main1(String[] args) in /home/jeremy/projects/csharp/hwapp/ppp/PPPProgram.cs:line 18");
    testStackTraceLine(
        "   at hwapp.Program.Main(String[] args) in /home/jeremy/projects/csharp/hwapp/Program.cs:line 10");
    testStackTraceLine(
        "Program.cs(2,13): error CS0234: The type or namespace name 'ppp' does not exist in the namespace 'hwapp' (are you missing an assembly reference?) [/home/jeremy/projects/csharp/hwapp/hwapp.csproj]");
    testStackTraceLine(
        "ppp/PPPProgram.cs(9,17): warning CS0219: The variable 'testIntValue' is assigned but its value is never used [/home/jeremy/projects/csharp/hwapp/hwapp.csproj]");
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
  }
}
