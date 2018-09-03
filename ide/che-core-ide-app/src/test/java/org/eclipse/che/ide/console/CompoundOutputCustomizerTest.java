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
 * JUnit test for Stacktrace and Compilation Error/Message line detection in
 * CompoundOutputCustomizer that is constructed with: - JavaOutputCustomizer - that is expected to
 * process Java Stacktrace lines - CSharpOutputCustomizer - that is expected to process C#
 * Compilation Error/Warning and Stacktrace lines
 *
 * <p>See: CHE-15 - Java stacktrace support (From Platform to Che Workspace) See: Issue #5489 - .NET
 * C# stacktrace support #5489 See: Issue #5565 - C/CPP compilation error/warning messages support
 * #5565
 *
 * @author Victor Rubezhny
 */
@RunWith(GwtMockitoTestRunner.class)
public class CompoundOutputCustomizerTest extends BaseOutputCustomizerTest {
  @Mock AppContext appContext;
  @Mock EditorAgent editorAgent;

  @Before
  public void setUp() throws Exception {
    OutputCustomizer[] customizers =
        new OutputCustomizer[] {
          new JavaOutputCustomizer(appContext, editorAgent),
          new CSharpOutputCustomizer(appContext, editorAgent),
          new CPPOutputCustomizer(appContext, editorAgent)
        };

    setupTestCustomizers(new CompoundOutputCustomizer(customizers), customizers);
  }

  /**
   * Test for the detection of initial stacktrace lines in CompoundOutputCustomizer. These lines are
   * not to be customized, however these lines show an examples of beginning the StackTrace and
   * might be used in future to set up the customizer properly.
   *
   * @throws Exception
   */
  @Test
  public void testInitialStackTraceLines() throws Exception {
    // Java Stacktrace lines
    testStackTraceLine(
        "org.test.HighLevelException: org.test.MidLevelException: org.test.LowLevelException");
    testStackTraceLine("Caused by: org.test.MidLevelException: org.test.LowLevelException");
    testStackTraceLine("Caused by: org.test.LowLevelException");
    testStackTraceLine("Exception in thread \"main\" java.lang.ArithmeticException: / by zero");

    // .NET C# Stacktrace lines
    testStackTraceLine(
        "Unhandled Exception: System.NullReferenceException: Object reference not set to an instance of an object.");

    // CPP Compilation Messages
    testStackTraceLine("hello.cc: In function ‘int main()’:");
  }

  /**
   * Test for the detection of informative stacktrace lines in CompoundOutputCustomizer. These lines
   * have an information on qualified path, file name and line number for an exception
   *
   * @throws Exception
   */
  @Test
  public void testValuableStackTraceLines() throws Exception {
    // Java Stacktrace lines
    testStackTraceLine(
        JavaOutputCustomizer.class,
        "   at org.test.Junk.main(Junk.java:6)",
        "   at org.test.Junk.main(<a href='javascript:open(\"org.test.Junk.main\", \"Junk.java\", 6);'>Junk.java:6</a>)");
    testStackTraceLine(
        JavaOutputCustomizer.class,
        "   at org.test.TrashClass.throwItThere(Junk.java:51)",
        "   at org.test.TrashClass.throwItThere(<a href='javascript:open(\"org.test.TrashClass.throwItThere\", \"Junk.java\", 51);'>Junk.java:51</a>)");
    testStackTraceLine(
        JavaOutputCustomizer.class,
        "   at MyClass$ThrowInConstructor.<init>(MyClass.java:16)",
        "   at MyClass$ThrowInConstructor.<init>(<a href='javascript:open(\"MyClass$ThrowInConstructor.<init>\", \"MyClass.java\", 16);'>MyClass.java:16</a>)");

    // .NET C# Stacktrace lines
    testStackTraceLine(
        CSharpOutputCustomizer.class,
        "   at hwapp.ppp.PPPProgram.Main1(String[] args) in /home/jeremy/projects/csharp/hwapp/ppp/PPPProgram.cs:line 18",
        "   at hwapp.ppp.PPPProgram.Main1(String[] args) in <a href='javascript:openCSSTL(\"/home/jeremy/projects/csharp/hwapp/ppp/PPPProgram.cs\",18);'>/home/jeremy/projects/csharp/hwapp/ppp/PPPProgram.cs:line 18</a>");
    testStackTraceLine(
        CSharpOutputCustomizer.class,
        "   at hwapp.Program.Main(String[] args) in /home/jeremy/projects/csharp/hwapp/Program.cs:line 10",
        "   at hwapp.Program.Main(String[] args) in <a href='javascript:openCSSTL(\"/home/jeremy/projects/csharp/hwapp/Program.cs\",10);'>/home/jeremy/projects/csharp/hwapp/Program.cs:line 10</a>");

    // .NET C# Compilation Error/Warning message lines
    testStackTraceLine(
        CSharpOutputCustomizer.class,
        "Program.cs(2,13): error CS0234: The type or namespace name 'ppp' does not exist in the namespace 'hwapp' (are you missing an assembly reference?) [/home/jeremy/projects/csharp/hwapp/hwapp.csproj]",
        "<a href='javascript:openCSCM(\"Program.cs\",\"/home/jeremy/projects/csharp/hwapp/hwapp.csproj\",2,13);'>Program.cs(2,13)</a>: error CS0234: The type or namespace name 'ppp' does not exist in the namespace 'hwapp' (are you missing an assembly reference?) [/home/jeremy/projects/csharp/hwapp/hwapp.csproj]");
    testStackTraceLine(
        CSharpOutputCustomizer.class,
        "ppp/PPPProgram.cs(9,17): warning CS0219: The variable 'testIntValue' is assigned but its value is never used [/home/jeremy/projects/csharp/hwapp/hwapp.csproj]",
        "<a href='javascript:openCSCM(\"ppp/PPPProgram.cs\",\"/home/jeremy/projects/csharp/hwapp/hwapp.csproj\",9,17);'>ppp/PPPProgram.cs(9,17)</a>: warning CS0219: The variable 'testIntValue' is assigned but its value is never used [/home/jeremy/projects/csharp/hwapp/hwapp.csproj]");

    // CPP Compilation Messages
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
   * Test for the detection of other stacktrace lines in CompoundOutputCustomizer. Other lines that
   * can be a part of Stack Trace, however do not contain any useful information
   *
   * @throws Exception
   */
  @Test
  public void testOtherStackTraceLines() throws Exception {
    // Java Stacktrace lines
    testStackTraceLine("   ... 1 more");

    // CPP Compilation Messages
    testStackTraceLine("     return 0/0;");
    testStackTraceLine("             ^");
  }

  /**
   * Test for the detection of non-stacktrace lines in CompoundOutputCustomizer. Other lines that
   * might occur in output console
   *
   * @throws Exception
   */
  @Test
  public void testNonStackTraceLines() throws Exception {
    testStackTraceLine("[STDOUT] Listening for transport dt_socket at address: 4403");
    testStackTraceLine(
        "[STDOUT] 2017-07-06 08:58:34,647 [ForkJoinPool.commonPool-worker-3] DEBUG o.j.t.l.t.DocumentManager.findSelectedWord - Looking for word at Position 2 in 'textDocument/badWord:Warning:name:So bad! '");
  }
}
