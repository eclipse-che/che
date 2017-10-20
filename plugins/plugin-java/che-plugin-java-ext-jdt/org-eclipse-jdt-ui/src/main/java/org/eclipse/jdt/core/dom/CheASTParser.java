/**
 * ***************************************************************************** Copyright (c) 2004,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.core.dom;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.batch.Main;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.parser.RecoveryScanner;
import org.eclipse.jdt.internal.compiler.parser.RecoveryScannerData;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.BasicCompilationUnit;
import org.eclipse.jdt.internal.core.BinaryType;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.core.util.CodeSnippetParsingUtil;
import org.eclipse.jdt.internal.core.util.RecordedParsingInformation;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * A Java language parser for creating abstract syntax trees (ASTs).
 *
 * <p>Example: Create basic AST from source string
 *
 * <pre>
 * char[] source = ...;
 * ASTParser parser = ASTParser.newParser(AST.JLS3);  // handles JDK 1.0, 1.1, 1.2, 1.3, 1.4, 1.5, 1.6
 * parser.setSource(source);
 * // In order to parse 1.5 code, some compiler options need to be set to 1.5
 * Map options = JavaCore.getOptions();
 * JavaCore.setComplianceOptions(JavaCore.VERSION_1_5, options);
 * parser.setCompilerOptions(options);
 * CompilationUnit result = (CompilationUnit) parser.createAST(null);
 * </pre>
 *
 * Once a configured parser instance has been used to create an AST, the settings are automatically
 * reset to their defaults, ready for the parser instance to be reused.
 *
 * <p>There are a number of configurable features:
 *
 * <ul>
 *   <li>Source string from {@link #setSource(char[]) char[]}, {@link #setSource(ICompilationUnit)
 *       ICompilationUnit}, or {@link #setSource(IClassFile) IClassFile}, and limited to a specified
 *       {@linkplain #setSourceRange(int,int) subrange}.
 *   <li>Whether {@linkplain #setResolveBindings(boolean) bindings} will be created.
 *   <li>Which {@linkplain #setWorkingCopyOwner(WorkingCopyOwner) working copy owner} to use when
 *       resolving bindings.
 *   <li>A hypothetical {@linkplain #setUnitName(String) compilation unit file name} and {@linkplain
 *       #setProject(IJavaProject) Java project} for locating a raw source string in the Java model
 *       (when resolving bindings)
 *   <li>Which {@linkplain #setCompilerOptions(Map) compiler options} to use. This is especially
 *       important to use if the parsing/scanning of the source code requires a different version
 *       than the default of the workspace. For example, the workspace defaults are 1.4 and you want
 *       to create an AST for a source code that is using 1.5 constructs.
 *   <li>Whether to parse just {@linkplain #setKind(int) an expression, statements, or body
 *       declarations} rather than an entire compilation unit.
 *   <li>Whether to return a {@linkplain #setFocalPosition(int) abridged AST} focused on the
 *       declaration containing a given source position.
 * </ul>
 *
 * @since 3.0
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class CheASTParser {

  /** Kind constant used to request that the source be parsed as a single expression. */
  public static final int K_EXPRESSION = 0x01;

  /** Kind constant used to request that the source be parsed as a sequence of statements. */
  public static final int K_STATEMENTS = 0x02;

  /**
   * Kind constant used to request that the source be parsed as a sequence of class body
   * declarations.
   */
  public static final int K_CLASS_BODY_DECLARATIONS = 0x04;

  /** Kind constant used to request that the source be parsed as a compilation unit. */
  public static final int K_COMPILATION_UNIT = 0x08;

  /**
   * Creates a new object for creating a Java abstract syntax tree (AST) following the specified set
   * of API rules.
   *
   * @param level the API level; one of the <code>.JLS*</code> level constants declared on {@link
   *     AST}
   * @return new ASTParser instance
   */
  public static CheASTParser newParser(int level) {
    return new CheASTParser(level);
  }

  /** Level of AST API desired. */
  private final int apiLevel;

  /** Kind of parse requested. Defaults to an entire compilation unit. */
  private int astKind;

  /** Compiler options. Defaults to JavaCore.getOptions(). */
  private Map compilerOptions;

  /**
   * The focal point for a partial AST request. Only used when <code>partial</code> is <code>true
   * </code>.
   */
  private int focalPointPosition;

  /** Source string. */
  private char[] rawSource = null;

  /** Java model class file or compilation unit supplying the source. */
  private ITypeRoot typeRoot = null;

  /** Character-based offset into the source string where parsing is to begin. Defaults to 0. */
  private int sourceOffset = 0;

  /**
   * Character-based length limit, or -1 if unlimited. All characters in the source string between
   * <code>offset</code> and <code>offset+length-1</code> inclusive are parsed. Defaults to -1,
   * which means the rest of the source string.
   */
  private int sourceLength = -1;

  /** Working copy owner. Defaults to primary owner. */
  private WorkingCopyOwner workingCopyOwner = DefaultWorkingCopyOwner.PRIMARY;

  /** Java project used to resolve names, or <code>null</code> if none. Defaults to none. */
  private IJavaProject project = null;

  /**
   * Name of the compilation unit for resolving bindings, or <code>null</code> if none. Defaults to
   * none.
   */
  private String unitName = null;

  /** Classpath entries to use to resolve bindings when no java project are available. */
  private String[] classpaths;

  /** Sourcepath entries to use to resolve bindings when no java project are available. */
  private String[] sourcepaths;

  /** Encoding of the given sourcepaths entries. */
  private String[] sourcepathsEncodings;

  /** Bits used to set the different values from CompilationUnitResolver values. */
  private int bits;

  /**
   * Creates a new AST parser for the given API level.
   *
   * <p>N.B. This constructor is package-private.
   *
   * @param level the API level; one of the <code>JLS*</code> level constants declared on {@link
   *     AST}
   */
  CheASTParser(int level) {
    switch (level) {
      case AST.JLS2_INTERNAL:
      case AST.JLS3_INTERNAL:
      case AST.JLS4_INTERNAL:
      case AST.JLS8:
        break;
      default:
        throw new IllegalArgumentException();
    }
    this.apiLevel = level;
    initializeDefaults();
  }

  private List getClasspath() throws IllegalStateException {
    Main main =
        new Main(
            new PrintWriter(System.out),
            new PrintWriter(System.err),
            false /*systemExit*/,
            null /*options*/,
            null /*progress*/);
    ArrayList allClasspaths = new ArrayList();
    try {
      if ((this.bits & CompilationUnitResolver.INCLUDE_RUNNING_VM_BOOTCLASSPATH) != 0) {
        org.eclipse.jdt.internal.compiler.util.Util.collectRunningVMBootclasspath(allClasspaths);
      }
      if (this.sourcepaths != null) {
        for (int i = 0, max = this.sourcepaths.length; i < max; i++) {
          String encoding = this.sourcepathsEncodings == null ? null : this.sourcepathsEncodings[i];
          main.processPathEntries(
              Main.DEFAULT_SIZE_CLASSPATH,
              allClasspaths,
              this.sourcepaths[i],
              encoding,
              true,
              false);
        }
      }
      if (this.classpaths != null) {
        for (int i = 0, max = this.classpaths.length; i < max; i++) {
          main.processPathEntries(
              Main.DEFAULT_SIZE_CLASSPATH, allClasspaths, this.classpaths[i], null, false, false);
        }
      }
      ArrayList pendingErrors = main.pendingErrors;
      if (pendingErrors != null && pendingErrors.size() != 0) {
        throw new IllegalStateException("invalid environment settings"); // $NON-NLS-1$
      }
    } catch (IllegalArgumentException e) {
      throw new IllegalStateException("invalid environment settings"); // $NON-NLS-1$
    }
    return allClasspaths;
  }

  /** Sets all the setting to their default values. */
  private void initializeDefaults() {
    this.astKind = K_COMPILATION_UNIT;
    this.rawSource = null;
    this.typeRoot = null;
    this.bits = 0;
    this.sourceLength = -1;
    this.sourceOffset = 0;
    this.workingCopyOwner = DefaultWorkingCopyOwner.PRIMARY;
    this.unitName = null;
    this.project = null;
    this.classpaths = null;
    this.sourcepaths = null;
    this.sourcepathsEncodings = null;
    Map options = JavaCore.getOptions();
    options.remove(JavaCore.COMPILER_TASK_TAGS); // no need to parse task tags
    this.compilerOptions = options;
  }

  /**
   * Requests that the compiler should perform bindings recovery. When bindings recovery is enabled
   * the compiler returns incomplete bindings.
   *
   * <p>Default to <code>false</code>.
   *
   * <p>This should be set to true only if bindings are resolved. It has no effect if there is no
   * binding resolution.
   *
   * @param enabled <code>true</code> if incomplete bindings are expected, and <code>false</code> if
   *     only complete bindings are expected.
   * @see IBinding#isRecovered()
   * @since 3.3
   */
  public void setBindingsRecovery(boolean enabled) {
    if (enabled) {
      this.bits |= CompilationUnitResolver.BINDING_RECOVERY;
    } else {
      this.bits &= ~CompilationUnitResolver.BINDING_RECOVERY;
    }
  }

  /**
   * Sets the environment to be used when no {@link IJavaProject} is available.
   *
   * <p>The user has to make sure that all the required types are included either in the classpath
   * or source paths. All the paths containing binary types must be included in the <code>
   * classpathEntries</code> whereas all paths containing source types must be included in the
   * <code>sourcepathEntries</code>.
   *
   * <p>All paths in the <code>classpathEntries</code> and <code>sourcepathEntries</code> are
   * absolute paths.
   *
   * <p>If the source paths contain units using a specific encoding (other than the platform
   * encoding), then the given <code>encodings</code> must be set. When the <code>encodings</code>
   * is set to non <code>null</code>, its length must match the length of <code>sourcepathEntries
   * </code> or an IllegalArgumentException will be thrown.
   *
   * <p>If <code>encodings</code> is not <code>null</code>, the given <code>sourcepathEntries</code>
   * must not be <code>null</code>.
   *
   * @param classpathEntries the given classpath entries to be used to resolve bindings
   * @param sourcepathEntries the given sourcepath entries to be used to resolve bindings
   * @param encodings the encodings of the corresponding sourcepath entries or <code>null</code> if
   *     the platform encoding can be used.
   * @param includeRunningVMBootclasspath <code>true</code> if the bootclasspath of the running VM
   *     must be prepended to the given classpath and <code>false</code> if the bootclasspath of the
   *     running VM should be ignored.
   * @throws IllegalArgumentException if the size of the given encodings is not equals to the size
   *     of the given <code>
   * sourcepathEntries</code>
   * @since 3.6
   */
  public void setEnvironment(
      String[] classpathEntries,
      String[] sourcepathEntries,
      String[] encodings,
      boolean includeRunningVMBootclasspath) {
    this.classpaths = classpathEntries;
    this.sourcepaths = sourcepathEntries;
    this.sourcepathsEncodings = encodings;
    if (encodings != null) {
      if (sourcepathEntries == null || sourcepathEntries.length != encodings.length) {
        throw new IllegalArgumentException();
      }
    }
    if (includeRunningVMBootclasspath) {
      this.bits |= CompilationUnitResolver.INCLUDE_RUNNING_VM_BOOTCLASSPATH;
    }
  }
  /**
   * Sets the compiler options to be used when parsing.
   *
   * <p>Note that {@link #setSource(IClassFile)}, {@link #setSource(ICompilationUnit)}, and {@link
   * #setProject(IJavaProject)} reset the compiler options based on the Java project. In other
   * cases, compiler options default to {@link JavaCore#getOptions()}. In either case, and
   * especially in the latter, the caller should carefully weight the consequences of allowing
   * compiler options to be defaulted as opposed to being explicitly specified for the {@link
   * CheASTParser} instance. For instance, there is a compiler option called "Source Compatibility
   * Mode" which determines which JDK level the source code is expected to meet. If you specify
   * "1.4", then "assert" is treated as a keyword and disallowed as an identifier; if you specify
   * "1.3", then "assert" is allowed as an identifier. So this particular setting has a major
   * bearing on what is considered syntactically legal. By explicitly specifying the setting, the
   * client control exactly how the parser works. On the other hand, allowing default settings means
   * the parsing behaves like other JDT tools.
   *
   * @param options the table of options (key type: <code>String</code>; value type: <code>String
   *     </code>), or <code>null</code> to set it back to the default
   */
  public void setCompilerOptions(Map options) {
    if (options == null) {
      options = JavaCore.getOptions();
    } else {
      // copy client's options so as to not do any side effect on them
      options = new HashMap(options);
    }
    options.remove(JavaCore.COMPILER_TASK_TAGS); // no need to parse task tags
    this.compilerOptions = options;
  }

  /**
   * Requests that the compiler should provide binding information for the AST nodes it creates.
   *
   * <p>Defaults to <code>false</code> (no bindings).
   *
   * <p>If {@link #setResolveBindings(boolean) setResolveBindings(true)}, the various names and
   * types appearing in the AST can be resolved to "bindings" by calling the <code>resolveBinding
   * </code> methods. These bindings draw connections between the different parts of a program, and
   * generally afford a more powerful vantage point for clients who wish to analyze a program's
   * structure more deeply. These bindings come at a considerable cost in both time and space,
   * however, and should not be requested frivolously. The additional space is not reclaimed until
   * the AST, all its nodes, and all its bindings become garbage. So it is very important to not
   * retain any of these objects longer than absolutely necessary. Bindings are resolved at the time
   * the AST is created. Subsequent modifications to the AST do not affect the bindings returned by
   * <code>resolveBinding</code> methods in any way; these methods return the same binding as before
   * the AST was modified (including modifications that rearrange subtrees by reparenting nodes). If
   * {@link #setResolveBindings(boolean) setResolveBindings(false)}, (the default), the analysis
   * does not go beyond parsing and building the tree, and all <code>resolveBinding</code> methods
   * return <code>null</code> from the outset.
   *
   * <p>When bindings are requested, instead of considering compilation units on disk only, one can
   * also supply a <code>WorkingCopyOwner</code>. Working copies owned by this owner take precedence
   * over the underlying compilation units when looking up names and drawing the connections.
   *
   * <p>Note that working copy owners are used only if the <code>org.eclipse.jdt.core</code> bundle
   * is initialized.
   *
   * <p>Binding information is obtained from the Java model. This means that the compilation unit
   * must be located relative to the Java model. This happens automatically when the source code
   * comes from either {@link #setSource(ICompilationUnit) setSource(ICompilationUnit)} or {@link
   * #setSource(IClassFile) setSource(IClassFile)}. When source is supplied by {@link
   * #setSource(char[]) setSource(char[])}, the location must be established explicitly by setting
   * an environment using {@link #setProject(IJavaProject)} or {@link #setEnvironment(String[],
   * String[], String[], boolean)} and a unit name {@link #setUnitName(String)}. Note that the
   * compiler options that affect doc comment checking may also affect whether any bindings are
   * resolved for nodes within doc comments.
   *
   * @param enabled <code>true</code> if bindings are wanted, and <code>false</code> if bindings are
   *     not of interest
   */
  public void setResolveBindings(boolean enabled) {
    if (enabled) {
      this.bits |= CompilationUnitResolver.RESOLVE_BINDING;
    } else {
      this.bits &= ~CompilationUnitResolver.RESOLVE_BINDING;
    }
  }

  /**
   * Requests an abridged abstract syntax tree. By default, complete ASTs are returned.
   *
   * <p>When the given <code>position</code> is a valid position within the source code of the
   * compilation unit, the resulting AST does not have nodes for the entire compilation unit.
   * Rather, the AST is only fleshed out for the node that include the given source position. This
   * kind of limited AST is sufficient for certain purposes but totally unsuitable for others. In
   * places where it can be used, the limited AST offers the advantage of being smaller and faster
   * to construct.
   *
   * <p>The AST will include nodes for all of the compilation unit's package, import, and top-level
   * type declarations. It will also always contain nodes for all the body declarations for those
   * top-level types, as well as body declarations for any member types. However, some of the body
   * declarations may be abridged. In particular, the statements ordinarily found in the body of a
   * method declaration node will not be included (the block will be empty) unless the source
   * position falls somewhere within the source range of that method declaration node. The same is
   * true for initializer declarations; the statements ordinarily found in the body of initializer
   * node will not be included unless the source position falls somewhere within the source range of
   * that initializer declaration node. Field declarations are never abridged. Note that the AST for
   * the body of that one unabridged method (or initializer) is 100% complete; it has all its
   * statements, including any local or anonymous type declarations embedded within them. When the
   * given <code>position</code> is not located within the source range of any body declaration of a
   * top-level type, the AST returned will be a skeleton that includes nodes for all and only the
   * major declarations; this kind of AST is still quite useful because it contains all the
   * constructs that introduce names visible to the world outside the compilation unit.
   *
   * <p>This focal position is not used when the AST is built using {@link
   * #createASTs(ICompilationUnit[], String[], ASTRequestor, IProgressMonitor)}.
   *
   * @param position a position into the corresponding body declaration
   */
  public void setFocalPosition(int position) {
    this.bits |= CompilationUnitResolver.PARTIAL;
    this.focalPointPosition = position;
  }

  /**
   * Sets the kind of constructs to be parsed from the source. Defaults to an entire compilation
   * unit.
   *
   * <p>When the parse is successful the result returned includes the ASTs for the requested source:
   *
   * <ul>
   *   <li>{@link #K_COMPILATION_UNIT K_COMPILATION_UNIT}: The result node is a {@link
   *       CompilationUnit}.
   *   <li>{@link #K_CLASS_BODY_DECLARATIONS K_CLASS_BODY_DECLARATIONS}: The result node is a {@link
   *       TypeDeclaration} whose {@link TypeDeclaration#bodyDeclarations() bodyDeclarations} are
   *       the new trees. Other aspects of the type declaration are unspecified.
   *   <li>{@link #K_STATEMENTS K_STATEMENTS}: The result node is a {@link Block Block} whose {@link
   *       Block#statements() statements} are the new trees. Other aspects of the block are
   *       unspecified.
   *   <li>{@link #K_EXPRESSION K_EXPRESSION}: The result node is a subclass of {@link Expression
   *       Expression}. Other aspects of the expression are unspecified.
   * </ul>
   *
   * The resulting AST node is rooted under (possibly contrived) {@link CompilationUnit
   * CompilationUnit} node, to allow the client to retrieve the following pieces of information
   * available there:
   *
   * <ul>
   *   <li>{@linkplain CompilationUnit#getLineNumber(int) Line number map}. Line numbers start at 1
   *       and only cover the subrange scanned (<code>source[offset]</code> through <code>
   *       source[offset+length-1]</code>).
   *   <li>{@linkplain CompilationUnit#getMessages() Compiler messages} and {@linkplain
   *       CompilationUnit#getProblems() detailed problem reports}. Character positions are relative
   *       to the start of <code>source</code>; line positions are for the subrange scanned.
   *   <li>{@linkplain CompilationUnit#getCommentList() Comment list} for the subrange scanned.
   * </ul>
   *
   * The contrived nodes do not have source positions. Other aspects of the {@link CompilationUnit
   * CompilationUnit} node are unspecified, including the exact arrangement of intervening nodes.
   *
   * <p>Lexical or syntax errors detected while parsing can result in a result node being marked as
   * {@link ASTNode#MALFORMED MALFORMED}. In more severe failure cases where the parser is unable to
   * recognize the input, this method returns a {@link CompilationUnit CompilationUnit} node with at
   * least the compiler messages.
   *
   * <p>Each node in the subtree (other than the contrived nodes) carries source range(s)
   * information relating back to positions in the given source (the given source itself is not
   * remembered with the AST). The source range usually begins at the first character of the first
   * token corresponding to the node; leading whitespace and comments are <b>not</b> included. The
   * source range usually extends through the last character of the last token corresponding to the
   * node; trailing whitespace and comments are <b>not</b> included. There are a handful of
   * exceptions (including the various body declarations); the specification for these node type
   * spells out the details. Source ranges nest properly: the source range for a child is always
   * within the source range of its parent, and the source ranges of sibling nodes never overlap.
   *
   * <p>Binding information is only computed when <code>kind</code> is {@link #K_COMPILATION_UNIT}.
   *
   * <p>This kind is not used when the AST is built using {@link #createASTs(ICompilationUnit[],
   * String[], ASTRequestor, IProgressMonitor)}.
   *
   * @param kind the kind of construct to parse: one of {@link #K_COMPILATION_UNIT}, {@link
   *     #K_CLASS_BODY_DECLARATIONS}, {@link #K_EXPRESSION}, {@link #K_STATEMENTS}
   */
  public void setKind(int kind) {
    if ((kind != K_COMPILATION_UNIT)
        && (kind != K_CLASS_BODY_DECLARATIONS)
        && (kind != K_EXPRESSION)
        && (kind != K_STATEMENTS)) {
      throw new IllegalArgumentException();
    }
    this.astKind = kind;
  }

  /**
   * Sets the source code to be parsed.
   *
   * <p>This source is not used when the AST is built using {@link #createASTs(ICompilationUnit[],
   * String[], ASTRequestor, IProgressMonitor)}.
   *
   * <p>If this method is used, the user needs to specify compiler options explicitly using {@link
   * #setCompilerOptions(Map)} as 1.5 code will not be properly parsed without setting the
   * appropriate values for the compiler options: {@link JavaCore#COMPILER_SOURCE}, {@link
   * JavaCore#COMPILER_CODEGEN_TARGET_PLATFORM}, and {@link JavaCore#COMPILER_COMPLIANCE}.
   *
   * <p>Otherwise the default values for the compiler options will be used to parse the given
   * source.
   *
   * @param source the source string to be parsed, or <code>null</code> if none
   * @see JavaCore#setComplianceOptions(String, Map)
   */
  public void setSource(char[] source) {
    this.rawSource = source;
    // clear the type root
    this.typeRoot = null;
  }

  /**
   * Sets the source code to be parsed.
   *
   * <p>This method automatically sets the project (and compiler options) based on the given
   * compilation unit, in a manner equivalent to {@link #setProject(IJavaProject)
   * setProject(source.getJavaProject())}.
   *
   * <p>This source is not used when the AST is built using {@link #createASTs(ICompilationUnit[],
   * String[], ASTRequestor, IProgressMonitor)}.
   *
   * @param source the Java model compilation unit whose source code is to be parsed, or <code>null
   *     </code> if none
   */
  public void setSource(ICompilationUnit source) {
    setSource((ITypeRoot) source);
  }

  /**
   * Sets the source code to be parsed.
   *
   * <p>This method automatically sets the project (and compiler options) based on the given
   * compilation unit, in a manner equivalent to {@link #setProject(IJavaProject)
   * setProject(source.getJavaProject())}.
   *
   * <p>If the given class file has no source attachment, the creation of the ast will fail with an
   * {@link IllegalStateException}.
   *
   * <p>This source is not used when the AST is built using {@link #createASTs(ICompilationUnit[],
   * String[], ASTRequestor, IProgressMonitor)}.
   *
   * @param source the Java model class file whose corresponding source code is to be parsed, or
   *     <code>null</code> if none
   */
  public void setSource(IClassFile source) {
    setSource((ITypeRoot) source);
  }

  /**
   * Sets the source code to be parsed.
   *
   * <p>This method automatically sets the project (and compiler options) based on the given
   * compilation unit of class file, in a manner equivalent to {@link #setProject(IJavaProject)
   * setProject(source.getJavaProject())}.
   *
   * <p>If the source is a class file without source attachment, the creation of the ast will fail
   * with an {@link IllegalStateException}.
   *
   * <p>This source is not used when the AST is built using {@link #createASTs(ICompilationUnit[],
   * String[], ASTRequestor, IProgressMonitor)}.
   *
   * @param source the Java model compilation unit or class file whose corresponding source code is
   *     to be parsed, or <code>null</code> if none
   * @since 3.3
   */
  public void setSource(ITypeRoot source) {
    this.typeRoot = source;
    // clear the raw source
    this.rawSource = null;
    if (source != null) {
      this.project = source.getJavaProject();
      Map options = this.project.getOptions(true);
      options.remove(JavaCore.COMPILER_TASK_TAGS); // no need to parse task tags
      this.compilerOptions = options;
    }
  }

  /**
   * Sets the subrange of the source code to be parsed. By default, the entire source string will be
   * parsed (<code>offset</code> 0 and <code>length</code> -1).
   *
   * <p>This range is not used when the AST is built using {@link #createASTs(ICompilationUnit[],
   * String[], ASTRequestor, IProgressMonitor)}.
   *
   * @param offset the index of the first character to parse
   * @param length the number of characters to parse, or -1 if the remainder of the source string is
   *     to be parsed
   */
  public void setSourceRange(int offset, int length) {
    if (offset < 0 || length < -1) {
      throw new IllegalArgumentException();
    }
    this.sourceOffset = offset;
    this.sourceLength = length;
  }

  /**
   * Requests that the compiler should perform statements recovery. When statements recovery is
   * enabled the compiler tries to create statement nodes from code containing syntax errors
   *
   * <p>Default to <code>false</code>.
   *
   * @param enabled <code>true</code> if statements containing syntax errors are wanted, and <code>
   *     false</code> if these statements aren't wanted.
   * @since 3.2
   */
  public void setStatementsRecovery(boolean enabled) {
    if (enabled) {
      this.bits |= CompilationUnitResolver.STATEMENT_RECOVERY;
    } else {
      this.bits &= ~CompilationUnitResolver.STATEMENT_RECOVERY;
    }
  }

  /**
   * Requests an abstract syntax tree without method bodies.
   *
   * <p>When ignore method bodies is enabled, all method bodies are discarded. This has no impact on
   * the binding resolution.
   *
   * <p>This setting is not used when the kind used in {@link #setKind(int)} is either {@link
   * #K_EXPRESSION} or {@link #K_STATEMENTS}.
   *
   * @since 3.5.2
   */
  public void setIgnoreMethodBodies(boolean enabled) {
    if (enabled) {
      this.bits |= CompilationUnitResolver.IGNORE_METHOD_BODIES;
    } else {
      this.bits &= ~CompilationUnitResolver.IGNORE_METHOD_BODIES;
    }
  }

  /**
   * Sets the working copy owner used when resolving bindings, where <code>null</code> means the
   * primary owner. Defaults to the primary owner.
   *
   * @param owner the owner of working copies that take precedence over underlying compilation
   *     units, or <code>null</code> if the primary owner should be used
   */
  public void setWorkingCopyOwner(WorkingCopyOwner owner) {
    if (owner == null) {
      this.workingCopyOwner = DefaultWorkingCopyOwner.PRIMARY;
    } else {
      this.workingCopyOwner = owner;
    }
  }

  /**
   * Sets the name of the compilation unit that would hypothetically contains the source string.
   *
   * <p>This is used in conjunction with {@link #setSource(char[])} and {@link
   * #setProject(IJavaProject)} to locate the compilation unit relative to a Java project. Defaults
   * to none (<code>null</code>).
   *
   * <p>The name of the compilation unit must be supplied for resolving bindings. This name should
   * be suffixed by a dot ('.') followed by one of the {@link JavaCore#getJavaLikeExtensions()
   * Java-like extensions} and match the name of the main (public) class or interface declared in
   * the source.
   *
   * <p>This name must represent the full path of the unit inside the given project. For example, if
   * the source declares a public class named "Foo" in a project "P" where the source folder is the
   * project itself, the name of the compilation unit must be "/P/Foo.java". If the source declares
   * a public class name "Bar" in a package "p1.p2" in a project "P" in a source folder "src", the
   * name of the compilation unit must be "/P/src/p1/p2/Bar.java".
   *
   * <p>This unit name is not used when the AST is built using {@link
   * #createASTs(ICompilationUnit[], String[], ASTRequestor, IProgressMonitor)}.
   *
   * @param unitName the name of the compilation unit that would contain the source string, or
   *     <code>null</code> if none
   */
  public void setUnitName(String unitName) {
    this.unitName = unitName;
  }

  /**
   * Sets the Java project used when resolving bindings.
   *
   * <p>This method automatically sets the compiler options based on the given project:
   *
   * <pre>
   * setCompilerOptions(project.getOptions(true));
   * </pre>
   *
   * <p>See {@link #setCompilerOptions(Map)} for a discussion of the pros and cons of using these
   * options vs specifying compiler options explicitly.
   *
   * <p>This setting is used in conjunction with {@link #setSource(char[])}. For the purposes of
   * resolving bindings, types declared in the source string will hide types by the same name
   * available through the classpath of the given project.
   *
   * <p>Defaults to none (<code>null</code>).
   *
   * @param project the Java project used to resolve names, or <code>null</code> if none
   */
  public void setProject(IJavaProject project) {
    this.project = project;
    if (project != null) {
      Map options = project.getOptions(true);
      options.remove(JavaCore.COMPILER_TASK_TAGS); // no need to parse task tags
      this.compilerOptions = options;
    }
  }

  /**
   * Creates an abstract syntax tree.
   *
   * <p>A successful call to this method returns all settings to their default values so the object
   * is ready to be reused.
   *
   * @param monitor the progress monitor used to report progress and request cancellation, or <code>
   *     null</code> if none
   * @return an AST node whose type depends on the kind of parse requested, with a fallback to a
   *     <code>CompilationUnit</code> in the case of severe parsing errors
   * @exception IllegalStateException if the settings provided are insufficient, contradictory, or
   *     otherwise unsupported
   */
  public ASTNode createAST(IProgressMonitor monitor) {
    ASTNode result = null;
    if (monitor != null) monitor.beginTask("", 1); // $NON-NLS-1$
    try {
      if (this.rawSource == null && this.typeRoot == null) {
        throw new IllegalStateException("source not specified"); // $NON-NLS-1$
      }
      result = internalCreateAST(monitor);
    } finally {
      // reset to defaults to allow reuse (and avoid leaking)
      initializeDefaults();
      if (monitor != null) monitor.done();
    }
    return result;
  }

  /**
   * Creates ASTs for a batch of compilation units.
   *
   * <p>When bindings are being resolved, processing a batch of compilation units is more efficient
   * because much of the work involved in resolving bindings can be shared.
   *
   * <p>When bindings are being resolved, all compilation units must come from the same Java
   * project, which must be set beforehand with {@link #setProject(IJavaProject) setProject}.
   *
   * <p>The compilation units are processed one at a time in no specified order. For each of the
   * compilation units in turn,
   *
   * <ul>
   *   <li>{@link #createAST(IProgressMonitor) ASTParser.createAST} is called to parse it and create
   *       a corresponding AST. The calls to {@link #createAST(IProgressMonitor)
   *       ASTParser.createAST} all employ the same settings.
   *   <li>{@link ASTRequestor#acceptAST(ICompilationUnit, CompilationUnit) ASTRequestor.acceptAST}
   *       is called passing the compilation unit and the corresponding AST to <code>requestor
   *       </code>.
   * </ul>
   *
   * Note only ASTs from the given compilation units are reported to the requestor. If additional
   * compilation units are required to resolve the original ones, the corresponding ASTs are
   * <b>not</b> reported to the requestor.
   *
   * <p>Note also the following parser parameters are used, regardless of what may have been
   * specified:
   *
   * <ul>
   *   <li>The {@linkplain #setKind(int) parser kind} is <code>K_COMPILATION_UNIT</code>
   *   <li>The {@linkplain #setSourceRange(int,int) source range} is <code>(0, -1)</code>
   *   <li>The {@linkplain #setFocalPosition(int) focal position} is not set
   * </ul>
   *
   * <p>The <code>bindingKeys</code> parameter specifies bindings keys ({@link IBinding#getKey()})
   * that are to be looked up. These keys may be for elements either inside or outside the set of
   * compilation units being processed. When bindings are being resolved, the keys and corresponding
   * bindings (or <code>null</code> if none) are passed to {@link ASTRequestor#acceptBinding(String,
   * IBinding) ASTRequestor.acceptBinding}. Note that binding keys for elements outside the set of
   * compilation units being processed are looked up after all {@link
   * ASTRequestor#acceptAST(ICompilationUnit, CompilationUnit) ASTRequestor.acceptAST} callbacks
   * have been made. Binding keys for elements inside the set of compilation units being processed
   * are looked up and reported right after the corresponding {@link
   * ASTRequestor#acceptAST(ICompilationUnit, CompilationUnit) ASTRequestor.acceptAST} callback has
   * been made. No {@link ASTRequestor#acceptBinding(String, IBinding) ASTRequestor.acceptBinding}
   * callbacks are made unless bindings are being resolved.
   *
   * <p>A successful call to this method returns all settings to their default values so the object
   * is ready to be reused.
   *
   * @param compilationUnits the compilation units to create ASTs for
   * @param bindingKeys the binding keys to create bindings for
   * @param requestor the AST requestor that collects abstract syntax trees and bindings
   * @param monitor the progress monitor used to report progress and request cancellation, or <code>
   *     null</code> if none
   * @exception IllegalStateException if the settings provided are insufficient, contradictory, or
   *     otherwise unsupported
   * @since 3.1
   */
  public void createASTs(
      ICompilationUnit[] compilationUnits,
      String[] bindingKeys,
      ASTRequestor requestor,
      IProgressMonitor monitor) {
    try {
      int flags = 0;
      if ((this.bits & CompilationUnitResolver.STATEMENT_RECOVERY) != 0) {
        flags |= ICompilationUnit.ENABLE_STATEMENTS_RECOVERY;
      }
      if ((this.bits & CompilationUnitResolver.IGNORE_METHOD_BODIES) != 0) {
        flags |= ICompilationUnit.IGNORE_METHOD_BODIES;
      }
      if ((this.bits & CompilationUnitResolver.RESOLVE_BINDING) != 0) {
        if (this.project == null)
          throw new IllegalStateException("project not specified"); // $NON-NLS-1$
        if ((this.bits & CompilationUnitResolver.BINDING_RECOVERY) != 0) {
          flags |= ICompilationUnit.ENABLE_BINDINGS_RECOVERY;
        }
        CompilationUnitResolver.resolve(
            compilationUnits,
            bindingKeys,
            requestor,
            this.apiLevel,
            this.compilerOptions,
            this.project,
            this.workingCopyOwner,
            flags,
            monitor);
      } else {
        CompilationUnitResolver.parse(
            compilationUnits, requestor, this.apiLevel, this.compilerOptions, flags, monitor);
      }
    } finally {
      // reset to defaults to allow reuse (and avoid leaking)
      initializeDefaults();
    }
  }

  /**
   * Creates ASTs for a batch of compilation units. When bindings are being resolved, processing a
   * batch of compilation units is more efficient because much of the work involved in resolving
   * bindings can be shared.
   *
   * <p>When bindings are being resolved, all compilation units are resolved using the same
   * environment, which must be set beforehand with {@link #setEnvironment(String[], String[],
   * String[], boolean) setEnvironment}. The compilation units are processed one at a time in no
   * specified order. For each of the compilation units in turn,
   *
   * <ul>
   *   <li>{@link CheASTParser#createAST(IProgressMonitor) ASTParser.createAST} is called to parse
   *       it and create a corresponding AST. The calls to {@link
   *       CheASTParser#createAST(IProgressMonitor) ASTParser.createAST} all employ the same
   *       settings.
   *   <li>{@link FileASTRequestor#acceptAST(String, CompilationUnit) FileASTRequestor.acceptAST} is
   *       called passing the compilation unit path and the corresponding AST to <code>requestor
   *       </code>. The compilation unit path is the same path that is passed into the given <code>
   *       sourceFilePaths</code> parameter.
   * </ul>
   *
   * Note only ASTs from the given compilation units are reported to the requestor. If additional
   * compilation units are required to resolve the original ones, the corresponding ASTs are
   * <b>not</b> reported to the requestor.
   *
   * <p>Note also the following parser parameters are used, regardless of what may have been
   * specified:
   *
   * <ul>
   *   <li>The {@linkplain #setKind(int) parser kind} is <code>K_COMPILATION_UNIT</code>
   *   <li>The {@linkplain #setSourceRange(int,int) source range} is <code>(0, -1)</code>
   *   <li>The {@linkplain #setFocalPosition(int) focal position} is not set
   * </ul>
   *
   * <p>The <code>bindingKeys</code> parameter specifies bindings keys ({@link IBinding#getKey()})
   * that are to be looked up. These keys may be for elements either inside or outside the set of
   * compilation units being processed. When bindings are being resolved, the keys and corresponding
   * bindings (or <code>null</code> if none) are passed to {@link
   * FileASTRequestor#acceptBinding(String, IBinding) FileASTRequestor.acceptBinding}. Note that
   * binding keys for elements outside the set of compilation units being processed are looked up
   * after all {@link FileASTRequestor#acceptAST(String, CompilationUnit) ASTRequestor.acceptAST}
   * callbacks have been made. Binding keys for elements inside the set of compilation units being
   * processed are looked up and reported right after the corresponding {@link
   * FileASTRequestor#acceptAST(String, CompilationUnit) FileASTRequestor.acceptAST} callback has
   * been made. No {@link FileASTRequestor#acceptBinding(String, IBinding)
   * FileASTRequestor.acceptBinding} callbacks are made unless bindings are being resolved.
   *
   * <p>A successful call to this method returns all settings to their default values so the object
   * is ready to be reused.
   *
   * <p>The given <code>encodings</code> are used to properly parse the given source units. If the
   * platform encoding is sufficient, then the given encodings can be set to <code>null</code>.
   *
   * @param sourceFilePaths the compilation units to create ASTs for
   * @param encodings the given encoding for the source units
   * @param bindingKeys the binding keys to create bindings for
   * @param requestor the AST requestor that collects abstract syntax trees and bindings
   * @param monitor the progress monitor used to report progress and request cancellation, or <code>
   *     null</code> if none
   * @exception IllegalStateException if the settings provided are insufficient, contradictory, or
   *     otherwise unsupported
   * @since 3.6
   */
  public void createASTs(
      String[] sourceFilePaths,
      String[] encodings,
      String[] bindingKeys,
      FileASTRequestor requestor,
      IProgressMonitor monitor) {
    try {
      int flags = 0;
      if ((this.bits & CompilationUnitResolver.STATEMENT_RECOVERY) != 0) {
        flags |= ICompilationUnit.ENABLE_STATEMENTS_RECOVERY;
      }
      if ((this.bits & CompilationUnitResolver.IGNORE_METHOD_BODIES) != 0) {
        flags |= ICompilationUnit.IGNORE_METHOD_BODIES;
      }
      if ((this.bits & CompilationUnitResolver.RESOLVE_BINDING) != 0) {
        if (this.classpaths == null
            && this.sourcepaths == null
            && ((this.bits & CompilationUnitResolver.INCLUDE_RUNNING_VM_BOOTCLASSPATH) == 0)) {
          throw new IllegalStateException("no environment is specified"); // $NON-NLS-1$
        }
        if ((this.bits & CompilationUnitResolver.BINDING_RECOVERY) != 0) {
          flags |= ICompilationUnit.ENABLE_BINDINGS_RECOVERY;
        }
        CompilationUnitResolver.resolve(
            sourceFilePaths,
            encodings,
            bindingKeys,
            requestor,
            this.apiLevel,
            this.compilerOptions,
            getClasspath(),
            flags,
            monitor);
      } else {
        CompilationUnitResolver.parse(
            sourceFilePaths,
            encodings,
            requestor,
            this.apiLevel,
            this.compilerOptions,
            flags,
            monitor);
      }
    } finally {
      // reset to defaults to allow reuse (and avoid leaking)
      initializeDefaults();
    }
  }
  /**
   * Creates bindings for a batch of Java elements.
   *
   * <p>These elements are either enclosed in {@link ICompilationUnit ICompilationUnits} or in
   * {@link IClassFile IClassFiles}.
   *
   * <p>All enclosing compilation units and class files must come from the same Java project, which
   * must be set beforehand with {@link #setProject(IJavaProject) setProject}.
   *
   * <p>All elements must exist. If one doesn't exist, an {@link IllegalStateException} is thrown.
   *
   * <p>The returned array has the same size as the given elements array. At a given position it
   * contains the binding of the corresponding Java element, or <code>null</code> if no binding
   * could be created.
   *
   * <p>Note also the following parser parameters are used, regardless of what may have been
   * specified:
   *
   * <ul>
   *   <li>The {@linkplain #setResolveBindings(boolean) binding resolution flag} is <code>true
   *       </code>
   *   <li>The {@linkplain #setKind(int) parser kind} is <code>K_COMPILATION_UNIT</code>
   *   <li>The {@linkplain #setSourceRange(int,int) source range} is <code>(0, -1)</code>
   *   <li>The {@linkplain #setFocalPosition(int) focal position} is not set
   * </ul>
   *
   * <p>A successful call to this method returns all settings to their default values so the object
   * is ready to be reused.
   *
   * @param elements the Java elements to create bindings for
   * @return the bindings for the given Java elements, possibly containing <code>null</code>s if
   *     some bindings could not be created
   * @exception IllegalStateException if the settings provided are insufficient, contradictory, or
   *     otherwise unsupported
   * @since 3.1
   */
  public IBinding[] createBindings(IJavaElement[] elements, IProgressMonitor monitor) {
    try {
      if (this.project == null)
        throw new IllegalStateException("project or classpath not specified"); // $NON-NLS-1$
      int flags = 0;
      if ((this.bits & CompilationUnitResolver.STATEMENT_RECOVERY) != 0) {
        flags |= ICompilationUnit.ENABLE_STATEMENTS_RECOVERY;
      }
      if ((this.bits & CompilationUnitResolver.BINDING_RECOVERY) != 0) {
        flags |= ICompilationUnit.ENABLE_BINDINGS_RECOVERY;
      }
      if ((this.bits & CompilationUnitResolver.IGNORE_METHOD_BODIES) != 0) {
        flags |= ICompilationUnit.IGNORE_METHOD_BODIES;
      }
      return CompilationUnitResolver.resolve(
          elements,
          this.apiLevel,
          this.compilerOptions,
          this.project,
          this.workingCopyOwner,
          flags,
          monitor);
    } finally {
      // reset to defaults to allow reuse (and avoid leaking)
      initializeDefaults();
    }
  }

  private ASTNode internalCreateAST(IProgressMonitor monitor) {
    boolean needToResolveBindings = (this.bits & CompilationUnitResolver.RESOLVE_BINDING) != 0;
    switch (this.astKind) {
      case K_CLASS_BODY_DECLARATIONS:
      case K_EXPRESSION:
      case K_STATEMENTS:
        if (this.rawSource == null) {
          if (this.typeRoot != null) {
            // get the source from the type root
            if (this.typeRoot instanceof ICompilationUnit) {
              org.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceUnit =
                  (org.eclipse.jdt.internal.compiler.env.ICompilationUnit) this.typeRoot;
              this.rawSource = sourceUnit.getContents();
            } else if (this.typeRoot instanceof IClassFile) {
              try {
                String sourceString = this.typeRoot.getSource();
                if (sourceString != null) {
                  this.rawSource = sourceString.toCharArray();
                }
              } catch (JavaModelException e) {
                // an error occured accessing the java element
                StringWriter stringWriter = new StringWriter();
                PrintWriter writer = null;
                try {
                  writer = new PrintWriter(stringWriter);
                  e.printStackTrace(writer);
                } finally {
                  if (writer != null) writer.close();
                }
                throw new IllegalStateException(String.valueOf(stringWriter.getBuffer()));
              }
            }
          }
        }
        if (this.rawSource != null) {
          if (this.sourceOffset + this.sourceLength > this.rawSource.length) {
            throw new IllegalStateException();
          }
          return internalCreateASTForKind();
        }
        break;
      case K_COMPILATION_UNIT:
        CompilationUnitDeclaration compilationUnitDeclaration = null;
        try {
          NodeSearcher searcher = null;
          org.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceUnit = null;
          WorkingCopyOwner wcOwner = this.workingCopyOwner;
          if (this.typeRoot instanceof ICompilationUnit) {
            /*
             * this.compilationUnitSource is an instance of org.eclipse.jdt.internal.core.CompilationUnit that implements
             * both org.eclipse.jdt.core.ICompilationUnit and org.eclipse.jdt.internal.compiler.env.ICompilationUnit
             */
            sourceUnit = (org.eclipse.jdt.internal.compiler.env.ICompilationUnit) this.typeRoot;
            /*
             * use a BasicCompilation that caches the source instead of using the compilationUnitSource directly
             * (if it is a working copy, the source can change between the parse and the AST convertion)
             * (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=75632)
             */
            sourceUnit =
                new BasicCompilationUnit(
                    sourceUnit.getContents(),
                    sourceUnit.getPackageName(),
                    new String(sourceUnit.getFileName()),
                    this.project);
            wcOwner = ((ICompilationUnit) this.typeRoot).getOwner();
          } else if (this.typeRoot instanceof IClassFile) {
            try {
              String sourceString = this.typeRoot.getSource();
              if (sourceString == null) {
                throw new IllegalStateException();
              }
              PackageFragment packageFragment = (PackageFragment) this.typeRoot.getParent();
              BinaryType type = (BinaryType) this.typeRoot.findPrimaryType();
              IBinaryType binaryType = (IBinaryType) type.getElementInfo();
              // file name is used to recreate the Java element, so it has to be the toplevel .class
              // file name
              char[] fileName = binaryType.getFileName();
              int firstDollar = CharOperation.indexOf('$', fileName);
              if (firstDollar != -1) {
                char[] suffix = SuffixConstants.SUFFIX_class;
                int suffixLength = suffix.length;
                char[] newFileName = new char[firstDollar + suffixLength];
                System.arraycopy(fileName, 0, newFileName, 0, firstDollar);
                System.arraycopy(suffix, 0, newFileName, firstDollar, suffixLength);
                fileName = newFileName;
              }
              sourceUnit =
                  new BasicCompilationUnit(
                      sourceString.toCharArray(),
                      Util.toCharArrays(packageFragment.names),
                      new String(fileName),
                      this.project);
            } catch (JavaModelException e) {
              // an error occured accessing the java element
              StringWriter stringWriter = new StringWriter();
              PrintWriter writer = null;
              try {
                writer = new PrintWriter(stringWriter);
                e.printStackTrace(writer);
              } finally {
                if (writer != null) writer.close();
              }
              throw new IllegalStateException(String.valueOf(stringWriter.getBuffer()));
            }
          } else if (this.rawSource != null) {
            needToResolveBindings =
                ((this.bits & CompilationUnitResolver.RESOLVE_BINDING) != 0)
                    && this.unitName != null
                    && (this.project != null
                        || this.classpaths != null
                        || this.sourcepaths != null
                        || ((this.bits & CompilationUnitResolver.INCLUDE_RUNNING_VM_BOOTCLASSPATH)
                            != 0))
                    && this.compilerOptions != null;
            sourceUnit =
                new BasicCompilationUnit(
                    this.rawSource,
                    null,
                    this.unitName == null ? "" : this.unitName,
                    this.project); // $NON-NLS-1$
          } else {
            throw new IllegalStateException();
          }
          if ((this.bits & CompilationUnitResolver.PARTIAL) != 0) {
            searcher = new NodeSearcher(this.focalPointPosition);
          }
          int flags = 0;
          if ((this.bits & CompilationUnitResolver.STATEMENT_RECOVERY) != 0) {
            flags |= ICompilationUnit.ENABLE_STATEMENTS_RECOVERY;
          }
          if (searcher == null
              && ((this.bits & CompilationUnitResolver.IGNORE_METHOD_BODIES) != 0)) {
            flags |= ICompilationUnit.IGNORE_METHOD_BODIES;
          }
          if (needToResolveBindings) {
            if ((this.bits & CompilationUnitResolver.BINDING_RECOVERY) != 0) {
              flags |= ICompilationUnit.ENABLE_BINDINGS_RECOVERY;
            }
            try {
              // parse and resolve
              compilationUnitDeclaration =
                  CheCompilationUnitResolver.resolve(
                      sourceUnit,
                      this.project,
                      getClasspath(),
                      searcher,
                      this.compilerOptions,
                      this.workingCopyOwner,
                      flags,
                      monitor);
            } catch (JavaModelException e) {
              flags &= ~ICompilationUnit.ENABLE_BINDINGS_RECOVERY;
              compilationUnitDeclaration =
                  CompilationUnitResolver.parse(sourceUnit, searcher, this.compilerOptions, flags);
              needToResolveBindings = false;
            }
          } else {
            compilationUnitDeclaration =
                CompilationUnitResolver.parse(sourceUnit, searcher, this.compilerOptions, flags);
            needToResolveBindings = false;
          }
          CompilationUnit result =
              CompilationUnitResolver.convert(
                  compilationUnitDeclaration,
                  sourceUnit.getContents(),
                  this.apiLevel,
                  this.compilerOptions,
                  needToResolveBindings,
                  wcOwner,
                  needToResolveBindings ? new DefaultBindingResolver.BindingTables() : null,
                  flags,
                  monitor,
                  this.project != null);
          result.setTypeRoot(this.typeRoot);
          return result;
        } finally {
          if (compilationUnitDeclaration != null
              && ((this.bits & CompilationUnitResolver.RESOLVE_BINDING) != 0)) {
            compilationUnitDeclaration.cleanUp();
          }
        }
    }
    throw new IllegalStateException();
  }

  /**
   * Parses the given source between the bounds specified by the given offset (inclusive) and the
   * given length and creates and returns a corresponding abstract syntax tree.
   *
   * <p>When the parse is successful the result returned includes the ASTs for the requested source:
   *
   * <ul>
   *   <li>{@link #K_CLASS_BODY_DECLARATIONS K_CLASS_BODY_DECLARATIONS}: The result node is a {@link
   *       TypeDeclaration TypeDeclaration} whose {@link TypeDeclaration#bodyDeclarations()
   *       bodyDeclarations} are the new trees. Other aspects of the type declaration are
   *       unspecified.
   *   <li>{@link #K_STATEMENTS K_STATEMENTS}: The result node is a {@link Block Block} whose {@link
   *       Block#statements() statements} are the new trees. Other aspects of the block are
   *       unspecified.
   *   <li>{@link #K_EXPRESSION K_EXPRESSION}: The result node is a subclass of {@link Expression
   *       Expression}. Other aspects of the expression are unspecified.
   * </ul>
   *
   * The resulting AST node is rooted under an contrived {@link CompilationUnit CompilationUnit}
   * node, to allow the client to retrieve the following pieces of information available there:
   *
   * <ul>
   *   <li>{@linkplain CompilationUnit#getLineNumber(int) Line number map}. Line numbers start at 1
   *       and only cover the subrange scanned (<code>source[offset]</code> through <code>
   *       source[offset+length-1]</code>).
   *   <li>{@linkplain CompilationUnit#getMessages() Compiler messages} and {@linkplain
   *       CompilationUnit#getProblems() detailed problem reports}. Character positions are relative
   *       to the start of <code>source</code>; line positions are for the subrange scanned.
   *   <li>{@linkplain CompilationUnit#getCommentList() Comment list} for the subrange scanned.
   * </ul>
   *
   * The contrived nodes do not have source positions. Other aspects of the {@link CompilationUnit
   * CompilationUnit} node are unspecified, including the exact arrangment of intervening nodes.
   *
   * <p>Lexical or syntax errors detected while parsing can result in a result node being marked as
   * {@link ASTNode#MALFORMED MALFORMED}. In more severe failure cases where the parser is unable to
   * recognize the input, this method returns a {@link CompilationUnit CompilationUnit} node with at
   * least the compiler messages.
   *
   * <p>Each node in the subtree (other than the contrived nodes) carries source range(s)
   * information relating back to positions in the given source (the given source itself is not
   * remembered with the AST). The source range usually begins at the first character of the first
   * token corresponding to the node; leading whitespace and comments are <b>not</b> included. The
   * source range usually extends through the last character of the last token corresponding to the
   * node; trailing whitespace and comments are <b>not</b> included. There are a handful of
   * exceptions (including the various body declarations); the specification for these node type
   * spells out the details. Source ranges nest properly: the source range for a child is always
   * within the source range of its parent, and the source ranges of sibling nodes never overlap.
   *
   * <p>This method does not compute binding information; all <code>resolveBinding</code> methods
   * applied to nodes of the resulting AST return <code>null</code>.
   *
   * @return an AST node whose type depends on the kind of parse requested, with a fallback to a
   *     <code>CompilationUnit</code> in the case of severe parsing errors
   * @see ASTNode#getStartPosition()
   * @see ASTNode#getLength()
   */
  private ASTNode internalCreateASTForKind() {
    final ASTConverter converter = new ASTConverter(this.compilerOptions, false, null);
    converter.compilationUnitSource = this.rawSource;
    converter.compilationUnitSourceLength = this.rawSource.length;
    converter.scanner.setSource(this.rawSource);

    AST ast = AST.newAST(this.apiLevel);
    ast.setDefaultNodeFlag(ASTNode.ORIGINAL);
    ast.setBindingResolver(new BindingResolver());
    if ((this.bits & CompilationUnitResolver.STATEMENT_RECOVERY) != 0) {
      ast.setFlag(ICompilationUnit.ENABLE_STATEMENTS_RECOVERY);
    }
    converter.setAST(ast);
    CodeSnippetParsingUtil codeSnippetParsingUtil =
        new CodeSnippetParsingUtil((this.bits & CompilationUnitResolver.IGNORE_METHOD_BODIES) != 0);
    CompilationUnit compilationUnit = ast.newCompilationUnit();
    if (this.sourceLength == -1) {
      this.sourceLength = this.rawSource.length;
    }
    switch (this.astKind) {
      case K_STATEMENTS:
        ConstructorDeclaration constructorDeclaration =
            codeSnippetParsingUtil.parseStatements(
                this.rawSource,
                this.sourceOffset,
                this.sourceLength,
                this.compilerOptions,
                true,
                (this.bits & CompilationUnitResolver.STATEMENT_RECOVERY) != 0);
        RecoveryScannerData data = constructorDeclaration.compilationResult.recoveryScannerData;
        if (data != null) {
          Scanner scanner = converter.scanner;
          converter.scanner = new RecoveryScanner(scanner, data.removeUnused());
          converter.docParser.scanner = converter.scanner;
          converter.scanner.setSource(scanner.source);

          compilationUnit.setStatementsRecoveryData(data);
        }
        RecordedParsingInformation recordedParsingInformation =
            codeSnippetParsingUtil.recordedParsingInformation;
        int[][] comments = recordedParsingInformation.commentPositions;
        if (comments != null) {
          converter.buildCommentsTable(compilationUnit, comments);
        }
        compilationUnit.setLineEndTable(recordedParsingInformation.lineEnds);
        Block block = ast.newBlock();
        block.setSourceRange(this.sourceOffset, this.sourceOffset + this.sourceLength);
        org.eclipse.jdt.internal.compiler.ast.Statement[] statements =
            constructorDeclaration.statements;
        if (statements != null) {
          int statementsLength = statements.length;
          for (int i = 0; i < statementsLength; i++) {
            if (statements[i] instanceof org.eclipse.jdt.internal.compiler.ast.LocalDeclaration) {
              converter.checkAndAddMultipleLocalDeclaration(statements, i, block.statements());
            } else {
              Statement statement = converter.convert(statements[i]);
              if (statement != null) {
                block.statements().add(statement);
              }
            }
          }
        }
        rootNodeToCompilationUnit(ast, compilationUnit, block, recordedParsingInformation, data);
        ast.setDefaultNodeFlag(0);
        ast.setOriginalModificationCount(ast.modificationCount());
        return block;
      case K_EXPRESSION:
        org.eclipse.jdt.internal.compiler.ast.Expression expression =
            codeSnippetParsingUtil.parseExpression(
                this.rawSource, this.sourceOffset, this.sourceLength, this.compilerOptions, true);
        recordedParsingInformation = codeSnippetParsingUtil.recordedParsingInformation;
        comments = recordedParsingInformation.commentPositions;
        if (comments != null) {
          converter.buildCommentsTable(compilationUnit, comments);
        }
        compilationUnit.setLineEndTable(recordedParsingInformation.lineEnds);
        if (expression != null) {
          Expression expression2 = converter.convert(expression);
          rootNodeToCompilationUnit(
              expression2.getAST(),
              compilationUnit,
              expression2,
              codeSnippetParsingUtil.recordedParsingInformation,
              null);
          ast.setDefaultNodeFlag(0);
          ast.setOriginalModificationCount(ast.modificationCount());
          return expression2;
        } else {
          CategorizedProblem[] problems = recordedParsingInformation.problems;
          if (problems != null) {
            compilationUnit.setProblems(problems);
          }
          ast.setDefaultNodeFlag(0);
          ast.setOriginalModificationCount(ast.modificationCount());
          return compilationUnit;
        }
      case K_CLASS_BODY_DECLARATIONS:
        final org.eclipse.jdt.internal.compiler.ast.ASTNode[] nodes =
            codeSnippetParsingUtil.parseClassBodyDeclarations(
                this.rawSource,
                this.sourceOffset,
                this.sourceLength,
                this.compilerOptions,
                true,
                (this.bits & CompilationUnitResolver.STATEMENT_RECOVERY) != 0);
        recordedParsingInformation = codeSnippetParsingUtil.recordedParsingInformation;
        comments = recordedParsingInformation.commentPositions;
        if (comments != null) {
          converter.buildCommentsTable(compilationUnit, comments);
        }
        compilationUnit.setLineEndTable(recordedParsingInformation.lineEnds);
        if (nodes != null) {
          // source has no syntax error or the statement recovery is enabled
          TypeDeclaration typeDeclaration = converter.convert(nodes);
          typeDeclaration.setSourceRange(this.sourceOffset, this.sourceOffset + this.sourceLength);
          rootNodeToCompilationUnit(
              typeDeclaration.getAST(),
              compilationUnit,
              typeDeclaration,
              codeSnippetParsingUtil.recordedParsingInformation,
              null);
          ast.setDefaultNodeFlag(0);
          ast.setOriginalModificationCount(ast.modificationCount());
          return typeDeclaration;
        } else {
          // source has syntax error and the statement recovery is disabled
          CategorizedProblem[] problems = recordedParsingInformation.problems;
          if (problems != null) {
            compilationUnit.setProblems(problems);
          }
          ast.setDefaultNodeFlag(0);
          ast.setOriginalModificationCount(ast.modificationCount());
          return compilationUnit;
        }
    }
    throw new IllegalStateException();
  }

  private void propagateErrors(
      ASTNode astNode, CategorizedProblem[] problems, RecoveryScannerData data) {
    astNode.accept(new ASTSyntaxErrorPropagator(problems));
    if (data != null) {
      astNode.accept(new ASTRecoveryPropagator(problems, data));
    }
  }

  private void rootNodeToCompilationUnit(
      AST ast,
      CompilationUnit compilationUnit,
      ASTNode node,
      RecordedParsingInformation recordedParsingInformation,
      RecoveryScannerData data) {
    final int problemsCount = recordedParsingInformation.problemsCount;
    switch (node.getNodeType()) {
      case ASTNode.BLOCK:
        {
          Block block = (Block) node;
          if (problemsCount != 0) {
            // propagate and record problems
            final CategorizedProblem[] problems = recordedParsingInformation.problems;
            propagateErrors(block, problems, data);
            compilationUnit.setProblems(problems);
          }
          TypeDeclaration typeDeclaration = ast.newTypeDeclaration();
          Initializer initializer = ast.newInitializer();
          initializer.setBody(block);
          typeDeclaration.bodyDeclarations().add(initializer);
          compilationUnit.types().add(typeDeclaration);
        }
        break;
      case ASTNode.TYPE_DECLARATION:
        {
          TypeDeclaration typeDeclaration = (TypeDeclaration) node;
          if (problemsCount != 0) {
            // propagate and record problems
            final CategorizedProblem[] problems = recordedParsingInformation.problems;
            propagateErrors(typeDeclaration, problems, data);
            compilationUnit.setProblems(problems);
          }
          compilationUnit.types().add(typeDeclaration);
        }
        break;
      default:
        if (node instanceof Expression) {
          Expression expression = (Expression) node;
          if (problemsCount != 0) {
            // propagate and record problems
            final CategorizedProblem[] problems = recordedParsingInformation.problems;
            propagateErrors(expression, problems, data);
            compilationUnit.setProblems(problems);
          }
          ExpressionStatement expressionStatement = ast.newExpressionStatement(expression);
          Block block = ast.newBlock();
          block.statements().add(expressionStatement);
          Initializer initializer = ast.newInitializer();
          initializer.setBody(block);
          TypeDeclaration typeDeclaration = ast.newTypeDeclaration();
          typeDeclaration.bodyDeclarations().add(initializer);
          compilationUnit.types().add(typeDeclaration);
        }
    }
  }
}
