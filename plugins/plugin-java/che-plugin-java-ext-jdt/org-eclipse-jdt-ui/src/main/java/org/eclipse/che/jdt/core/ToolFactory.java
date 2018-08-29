/**
 * ***************************************************************************** Copyright (c)
 * 2012-2015 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jdt.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.core.util.ClassFileReader;
import org.eclipse.jdt.internal.core.util.Disassembler;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatter;

/**
 * Factory for creating various compiler tools, such as scanners, parsers and compilers.
 *
 * <p>This class provides static methods only.
 *
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 * @since 2.0
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class ToolFactory {

  /**
   * This mode is used for formatting new code when some formatter options should not be used. In
   * particular, options that preserve the indentation of comments are not used. In the future,
   * newly added options may be ignored as well.
   *
   * <p>Clients that are formatting new code are recommended to use this mode.
   *
   * @see
   *     org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants#FORMATTER_NEVER_INDENT_BLOCK_COMMENTS_ON_FIRST_COLUMN
   * @see
   *     org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants#FORMATTER_NEVER_INDENT_LINE_COMMENTS_ON_FIRST_COLUMN
   * @see #createCodeFormatter(java.util.Map, int)
   * @since 3.3
   */
  public static final int M_FORMAT_NEW = new Integer(0).intValue();

  /**
   * This mode is used for formatting existing code when all formatter options should be used. In
   * particular, options that preserve the indentation of comments are used.
   *
   * <p>Clients that are formatting existing code are recommended to use this mode.
   *
   * @see
   *     org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants#FORMATTER_NEVER_INDENT_BLOCK_COMMENTS_ON_FIRST_COLUMN
   * @see
   *     org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants#FORMATTER_NEVER_INDENT_LINE_COMMENTS_ON_FIRST_COLUMN
   * @see #createCodeFormatter(java.util.Map, int)
   * @since 3.3
   */
  public static final int M_FORMAT_EXISTING = new Integer(1).intValue();

  //    /**
  //     * Create an instance of a code formatter. A code formatter implementation can be
  // contributed via the
  //     * extension point "org.eclipse.jdt.core.codeFormatter". If unable to find a registered
  // extension, the factory
  //     * will default to using the default code formatter.
  //     *
  //     * @return an instance of a code formatter
  //     * @see org.eclipse.jdt.core.ICodeFormatter
  //     * @see org.eclipse.jdt.core.ToolFactory#createDefaultCodeFormatter(java.util.Map)
  //     * @deprecated The extension point has been deprecated, use {@link
  // #createCodeFormatter(java.util.Map)} instead.
  //     */
  //    public static ICodeFormatter createCodeFormatter() {
  //
  //        Plugin jdtCorePlugin = org.eclipse.jdt.core.JavaCore.getPlugin();
  //        if (jdtCorePlugin == null) return null;
  //
  //        IExtensionPoint extension =
  // jdtCorePlugin.getDescriptor().getExtensionPoint(JavaModelManager.FORMATTER_EXTPOINT_ID);
  //        if (extension != null) {
  //            IExtension[] extensions = extension.getExtensions();
  //            for (int i = 0; i < extensions.length; i++) {
  //                IConfigurationElement[] configElements =
  // extensions[i].getConfigurationElements();
  //                for (int j = 0; j < configElements.length; j++) {
  //                    try {
  //                        Object execExt = configElements[j].createExecutableExtension("class");
  // //$NON-NLS-1$
  //                        if (execExt instanceof ICodeFormatter) {
  //                            // use first contribution found
  //                            return (ICodeFormatter)execExt;
  //                        }
  //                    } catch (CoreException e) {
  //                        // unable to instantiate extension, will answer default formatter
  // instead
  //                    }
  //                }
  //            }
  //        }
  //        // no proper contribution found, use default formatter
  //        return createDefaultCodeFormatter(null);
  //    }
  //
  /**
   * Create an instance of the built-in code formatter.
   *
   * <p>The given options should at least provide the source level ({@link
   * org.eclipse.jdt.core.JavaCore#COMPILER_SOURCE}), the compiler compliance level ({@link
   * org.eclipse.jdt.core.JavaCore#COMPILER_COMPLIANCE}) and the target platform ({@link
   * org.eclipse.jdt.core.JavaCore#COMPILER_CODEGEN_TARGET_PLATFORM}). Without these options, it is
   * not possible for the code formatter to know what kind of source it needs to format.
   *
   * <p>Note this is equivalent to <code>createCodeFormatter(options, M_FORMAT_NEW)</code>. Thus
   * some code formatter options may be ignored. See @{link {@link #M_FORMAT_NEW} for more details.
   *
   * @param options - the options map to use for formatting with the default code formatter.
   *     Recognized options are documented on <code>JavaCore#getDefaultOptions()</code>. If set to
   *     <code>null</code>, then use the current settings from <code>JavaCore#getOptions</code>.
   * @return an instance of the built-in code formatter
   * @see org.eclipse.jdt.core.formatter.CodeFormatter
   * @see org.eclipse.jdt.core.JavaCore#getOptions()
   * @since 3.0
   */
  public static CodeFormatter createCodeFormatter(Map options) {
    return createCodeFormatter(options, M_FORMAT_NEW);
  }

  /**
   * Create an instance of the built-in code formatter.
   *
   * <p>The given options should at least provide the source level ({@link
   * org.eclipse.jdt.core.JavaCore#COMPILER_SOURCE}), the compiler compliance level ({@link
   * org.eclipse.jdt.core.JavaCore#COMPILER_COMPLIANCE}) and the target platform ({@link
   * org.eclipse.jdt.core.JavaCore#COMPILER_CODEGEN_TARGET_PLATFORM}). Without these options, it is
   * not possible for the code formatter to know what kind of source it needs to format.
   *
   * <p>The given mode determines what options should be enabled when formatting the code. It can
   * have the following values: {@link #M_FORMAT_NEW}, {@link #M_FORMAT_EXISTING}, but other values
   * may be added in the future.
   *
   * @param options the options map to use for formatting with the default code formatter.
   *     Recognized options are documented on <code>JavaCore#getDefaultOptions()</code>. If set to
   *     <code>null</code>, then use the current settings from <code>JavaCore#getOptions</code>.
   * @param mode the given mode to modify the given options.
   * @return an instance of the built-in code formatter
   * @see org.eclipse.jdt.core.formatter.CodeFormatter
   * @see org.eclipse.jdt.core.JavaCore#getOptions()
   * @since 3.3
   */
  public static CodeFormatter createCodeFormatter(Map options, int mode) {
    if (options == null) options = org.eclipse.jdt.core.JavaCore.getOptions();
    Map currentOptions = new HashMap(options);
    if (mode == M_FORMAT_NEW) {
      // disable the option for not formatting comments starting on first column
      currentOptions.put(
          DefaultCodeFormatterConstants
              .FORMATTER_COMMENT_FORMAT_LINE_COMMENT_STARTING_ON_FIRST_COLUMN,
          DefaultCodeFormatterConstants.TRUE);
      // disable the option for not indenting comments starting on first column
      currentOptions.put(
          DefaultCodeFormatterConstants.FORMATTER_NEVER_INDENT_BLOCK_COMMENTS_ON_FIRST_COLUMN,
          DefaultCodeFormatterConstants.FALSE);
      currentOptions.put(
          DefaultCodeFormatterConstants.FORMATTER_NEVER_INDENT_LINE_COMMENTS_ON_FIRST_COLUMN,
          DefaultCodeFormatterConstants.FALSE);
    }
    return new DefaultCodeFormatter(currentOptions);
  }

  /**
   * Create a classfile bytecode disassembler, able to produce a String representation of a given
   * classfile.
   *
   * @return a classfile bytecode disassembler
   * @see org.eclipse.jdt.core.util.ClassFileBytesDisassembler
   * @since 2.1
   */
  public static ClassFileBytesDisassembler createDefaultClassFileBytesDisassembler() {
    return new Disassembler();
  }

  /**
   * Create a classfile bytecode disassembler, able to produce a String representation of a given
   * classfile.
   *
   * @return a classfile bytecode disassembler
   * @see org.eclipse.jdt.core.util.IClassFileDisassembler
   * @deprecated Use {@link #createDefaultClassFileBytesDisassembler()} instead
   */
  public static org.eclipse.jdt.core.util.IClassFileDisassembler
      createDefaultClassFileDisassembler() {
    class DeprecatedDisassembler extends Disassembler
        implements org.eclipse.jdt.core.util.IClassFileDisassembler {
      // for backward compatibility, defines a disassembler which implements IClassFileDisassembler
    }
    return new DeprecatedDisassembler();
  }

  /**
   * Create a classfile reader onto a classfile Java element. Create a default classfile reader,
   * able to expose the internal representation of a given classfile according to the decoding flag
   * used to initialize the reader. Answer null if the file named fileName doesn't represent a valid
   * .class file.
   *
   * <p>The decoding flags are described in IClassFileReader.
   *
   * @param classfile the classfile element to introspect
   * @param decodingFlag the flag used to decode the class file reader.
   * @return a default classfile reader
   * @see org.eclipse.jdt.core.util.IClassFileReader
   */
  public static IClassFileReader createDefaultClassFileReader(
      IClassFile classfile, int decodingFlag) {

    IPackageFragmentRoot root =
        (IPackageFragmentRoot) classfile.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
    if (root != null) {
      try {
        if (root instanceof JarPackageFragmentRoot) {
          String archiveName = null;
          ZipFile jar = null;
          try {
            jar = ((JarPackageFragmentRoot) root).getJar();
            archiveName = jar.getName();
          } finally {
            JavaModelManager.getJavaModelManager().closeZipFile(jar);
          }
          PackageFragment packageFragment = (PackageFragment) classfile.getParent();
          String classFileName = classfile.getElementName();
          String entryName =
              org.eclipse.jdt.internal.core.util.Util.concatWith(
                  packageFragment.names, classFileName, '/');
          return createDefaultClassFileReader(archiveName, entryName, decodingFlag);
        } else {
          InputStream in = null;
          try {
            in = ((IFile) ((JavaElement) classfile).resource()).getContents();
            return createDefaultClassFileReader(in, decodingFlag);
          } finally {
            if (in != null)
              try {
                in.close();
              } catch (IOException e) {
                // ignore
              }
          }
        }
      } catch (CoreException e) {
        // unable to read
      }
    }
    return null;
  }

  /**
   * Create a default classfile reader, able to expose the internal representation of a given
   * classfile according to the decoding flag used to initialize the reader. Answer null if the
   * input stream contents cannot be retrieved
   *
   * <p>The decoding flags are described in IClassFileReader.
   *
   * @param stream the given input stream to read
   * @param decodingFlag the flag used to decode the class file reader.
   * @return a default classfile reader
   * @see org.eclipse.jdt.core.util.IClassFileReader
   * @since 3.2
   */
  public static IClassFileReader createDefaultClassFileReader(
      InputStream stream, int decodingFlag) {
    try {
      return new ClassFileReader(Util.getInputStreamAsByteArray(stream, -1), decodingFlag);
    } catch (ClassFormatException e) {
      return null;
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * Create a default classfile reader, able to expose the internal representation of a given
   * classfile according to the decoding flag used to initialize the reader. Answer null if the file
   * named fileName doesn't represent a valid .class file. The fileName has to be an absolute OS
   * path to the given .class file.
   *
   * <p>The decoding flags are described in IClassFileReader.
   *
   * @param fileName the name of the file to be read
   * @param decodingFlag the flag used to decode the class file reader.
   * @return a default classfile reader
   * @see org.eclipse.jdt.core.util.IClassFileReader
   */
  public static IClassFileReader createDefaultClassFileReader(String fileName, int decodingFlag) {
    try {
      return new ClassFileReader(Util.getFileByteContent(new File(fileName)), decodingFlag);
    } catch (ClassFormatException e) {
      return null;
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * Create a default classfile reader, able to expose the internal representation of a given
   * classfile according to the decoding flag used to initialize the reader. Answer null if the file
   * named zipFileName doesn't represent a valid zip file or if the zipEntryName is not a valid
   * entry name for the specified zip file or if the bytes don't represent a valid .class file
   * according to the JVM specifications.
   *
   * <p>The decoding flags are described in IClassFileReader.
   *
   * @param zipFileName the name of the zip file
   * @param zipEntryName the name of the entry in the zip file to be read
   * @param decodingFlag the flag used to decode the class file reader.
   * @return a default classfile reader
   * @see org.eclipse.jdt.core.util.IClassFileReader
   */
  public static IClassFileReader createDefaultClassFileReader(
      String zipFileName, String zipEntryName, int decodingFlag) {
    ZipFile zipFile = null;
    try {
      if (JavaModelManager.ZIP_ACCESS_VERBOSE) {
        System.out.println(
            "("
                + Thread.currentThread()
                + ") [ToolFactory.createDefaultClassFileReader()] Creating ZipFile on "
                + zipFileName); // $NON-NLS-1$	//$NON-NLS-2$
      }
      zipFile = new ZipFile(zipFileName);
      ZipEntry zipEntry = zipFile.getEntry(zipEntryName);
      if (zipEntry == null) {
        return null;
      }
      if (!zipEntryName.toLowerCase().endsWith(SuffixConstants.SUFFIX_STRING_class)) {
        return null;
      }
      byte classFileBytes[] = Util.getZipEntryByteContent(zipEntry, zipFile);
      return new ClassFileReader(classFileBytes, decodingFlag);
    } catch (ClassFormatException e) {
      return null;
    } catch (IOException e) {
      return null;
    } finally {
      if (zipFile != null) {
        try {
          zipFile.close();
        } catch (IOException e) {
          // ignore
        }
      }
    }
  }

  //	/**
  //	 * Create an instance of the default code formatter.
  //	 *
  //	 * @param options - the options map to use for formatting with the default code formatter.
  // Recognized options
  //	 * 	are documented on <code>JavaCore#getDefaultOptions()</code>. If set to <code>null</code>,
  // then use
  //	 * 	the current settings from <code>JavaCore#getOptions</code>.
  //	 * @return an instance of the built-in code formatter
  //	 * @see org.eclipse.jdt.core.ICodeFormatter
  //	 * @see org.eclipse.jdt.core.ToolFactory#createCodeFormatter()
  //	 * @see org.eclipse.jdt.core.JavaCore#getOptions()
  //	 * @deprecated Use {@link #createCodeFormatter(java.util.Map)} instead but note the different
  // options
  //	 */
  //	public static ICodeFormatter createDefaultCodeFormatter(Map options){
  //		if (options == null) options = org.eclipse.jdt.core.JavaCore.getOptions();
  //		return new org.eclipse.jdt.internal.formatter.old.CodeFormatter(options);
  //	}
  //
  //	/**
  //	 * Create a scanner, indicating the level of detail requested for tokenizing. The scanner can
  // then be
  //	 * used to tokenize some source in a Java aware way.
  //	 * Here is a typical scanning loop:
  //	 *
  //	 * <code>
  //	 * <pre>
  //	 *   IScanner scanner = ToolFactory.createScanner(false, false, false, false);
  //	 *   scanner.setSource("int i = 0;".toCharArray());
  //	 *   while (true) {
  //	 *     int token = scanner.getNextToken();
  //	 *     if (token == ITerminalSymbols.TokenNameEOF) break;
  //	 *     System.out.println(token + " : " + new String(scanner.getCurrentTokenSource()));
  //	 *   }
  //	 * </pre>
  //	 * </code>
  //	 *
  //	 * <p>By default the compliance used to create the scanner is the workspace's compliance when
  // running inside the IDE
  //	 * or 1.4 if running from outside of a headless eclipse.
  //	 * </p>
  //	 *
  //	 * @param tokenizeComments if set to <code>false</code>, comments will be silently consumed
  //	 * @param tokenizeWhiteSpace if set to <code>false</code>, white spaces will be silently
  // consumed,
  //	 * @param assertMode if set to <code>false</code>, occurrences of 'assert' will be reported as
  // identifiers
  //	 * ({@link org.eclipse.jdt.core.compiler.ITerminalSymbols#TokenNameIdentifier}), whereas if set
  // to <code>true</code>, it
  //	 * would report assert keywords ({@link
  // org.eclipse.jdt.core.compiler.ITerminalSymbols#TokenNameassert}). Java 1.4 has introduced
  //	 * a new 'assert' keyword.
  //	 * @param recordLineSeparator if set to <code>true</code>, the scanner will record positions of
  // encountered line
  //	 * separator ends. In case of multi-character line separators, the last character position is
  // considered. These positions
  //	 * can then be extracted using {@link org.eclipse.jdt.core.compiler.IScanner#getLineEnds()}.
  // Only non-unicode escape sequences are
  //	 * considered as valid line separators.
  //  	 * @return a scanner
  //	 * @see org.eclipse.jdt.core.compiler.IScanner
  //	 * @see #createScanner(boolean, boolean, boolean, String, String)
  //	 */
  //	public static IScanner createScanner(boolean tokenizeComments, boolean tokenizeWhiteSpace,
  // boolean assertMode, boolean
  // recordLineSeparator){
  //		// use default workspace compliance
  //		long complianceLevelValue = CompilerOptions
  //
  // .versionToJdkLevel(org.eclipse.jdt.core.JavaCore.getOption(org.eclipse.jdt.core.JavaCore.COMPILER_COMPLIANCE));
  //		if (complianceLevelValue == 0) complianceLevelValue = ClassFileConstants.JDK1_4; //
  // fault-tolerance
  //		PublicScanner scanner =
  //			new PublicScanner(
  //				tokenizeComments,
  //				tokenizeWhiteSpace,
  //				false/*nls*/,
  //				assertMode ? ClassFileConstants.JDK1_4 : ClassFileConstants.JDK1_3/*sourceLevel*/,
  //				complianceLevelValue,
  //				null/*taskTags*/,
  //				null/*taskPriorities*/,
  //				true/*taskCaseSensitive*/);
  //		scanner.recordLineSeparator = recordLineSeparator;
  //		return scanner;
  //	}
  //
  //	/**
  //	 * Create a scanner, indicating the level of detail requested for tokenizing. The scanner can
  // then be
  //	 * used to tokenize some source in a Java aware way.
  //	 * Here is a typical scanning loop:
  //	 *
  //	 * <code>
  //	 * <pre>
  //	 *   IScanner scanner = ToolFactory.createScanner(false, false, false, false);
  //	 *   scanner.setSource("int i = 0;".toCharArray());
  //	 *   while (true) {
  //	 *     int token = scanner.getNextToken();
  //	 *     if (token == ITerminalSymbols.TokenNameEOF) break;
  //	 *     System.out.println(token + " : " + new String(scanner.getCurrentTokenSource()));
  //	 *   }
  //	 * </pre>
  //	 * </code>
  //	 *
  //	 * <p>By default the compliance used to create the scanner is the workspace's compliance when
  // running inside the IDE
  //	 * or 1.4 if running from outside of a headless eclipse.
  //	 * </p>
  //	 *
  //	 * @param tokenizeComments if set to <code>false</code>, comments will be silently consumed
  //	 * @param tokenizeWhiteSpace if set to <code>false</code>, white spaces will be silently
  // consumed,
  //	 * @param recordLineSeparator if set to <code>true</code>, the scanner will record positions of
  // encountered line
  //	 * separator ends. In case of multi-character line separators, the last character position is
  // considered. These positions
  //	 * can then be extracted using {@link org.eclipse.jdt.core.compiler.IScanner#getLineEnds()}.
  // Only non-unicode escape sequences are
  //	 * considered as valid line separators.
  //	 * @param sourceLevel if set to <code>&quot;1.3&quot;</code> or <code>null</code>, occurrences
  // of 'assert' will be reported as
  // identifiers
  //	 * ({@link org.eclipse.jdt.core.compiler.ITerminalSymbols#TokenNameIdentifier}), whereas if set
  // to <code>&quot;1.4&quot;</code>, it
  //	 * would report assert keywords ({@link
  // org.eclipse.jdt.core.compiler.ITerminalSymbols#TokenNameassert}). Java 1.4 has introduced
  //	 * a new 'assert' keyword.
  //	 * @return a scanner
  //	 * @see org.eclipse.jdt.core.compiler.IScanner
  //	 * @see #createScanner(boolean, boolean, boolean, String, String)
  //	 * @since 3.0
  //	 */
  //	public static IScanner createScanner(boolean tokenizeComments, boolean tokenizeWhiteSpace,
  // boolean recordLineSeparator, String
  // sourceLevel) {
  //		// use default workspace compliance
  //		long complianceLevelValue =
  // CompilerOptions.versionToJdkLevel(org.eclipse.jdt.core.JavaCore.getOption(JavaCore
  // .COMPILER_COMPLIANCE));
  //		if (complianceLevelValue == 0) complianceLevelValue = ClassFileConstants.JDK1_4; //
  // fault-tolerance
  //		long sourceLevelValue = CompilerOptions.versionToJdkLevel(sourceLevel);
  //		if (sourceLevelValue == 0) sourceLevelValue = ClassFileConstants.JDK1_3; // fault-tolerance
  //		PublicScanner scanner =
  //			new PublicScanner(
  //				tokenizeComments,
  //				tokenizeWhiteSpace,
  //				false/*nls*/,
  //				sourceLevelValue /*sourceLevel*/,
  //				complianceLevelValue,
  //				null/*taskTags*/,
  //				null/*taskPriorities*/,
  //				true/*taskCaseSensitive*/);
  //		scanner.recordLineSeparator = recordLineSeparator;
  //		return scanner;
  //	}
  //
  //	/**
  //	 * Create a scanner, indicating the level of detail requested for tokenizing. The scanner can
  // then be
  //	 * used to tokenize some source in a Java aware way.
  //	 * Here is a typical scanning loop:
  //	 *
  //	 * <code>
  //	 * <pre>
  //	 *   IScanner scanner = ToolFactory.createScanner(false, false, false, false);
  //	 *   scanner.setSource("int i = 0;".toCharArray());
  //	 *   while (true) {
  //	 *     int token = scanner.getNextToken();
  //	 *     if (token == ITerminalSymbols.TokenNameEOF) break;
  //	 *     System.out.println(token + " : " + new String(scanner.getCurrentTokenSource()));
  //	 *   }
  //	 * </pre>
  //	 * </code>
  //	 *
  //	 * @param tokenizeComments if set to <code>false</code>, comments will be silently consumed
  //	 * @param tokenizeWhiteSpace if set to <code>false</code>, white spaces will be silently
  // consumed,
  //	 * @param recordLineSeparator if set to <code>true</code>, the scanner will record positions of
  // encountered line
  //	 * separator ends. In case of multi-character line separators, the last character position is
  // considered. These positions
  //	 * can then be extracted using {@link org.eclipse.jdt.core.compiler.IScanner#getLineEnds()}.
  // Only non-unicode escape sequences are
  //	 * considered as valid line separators.
  //	 * @param sourceLevel if set to <code>&quot;1.3&quot;</code> or <code>null</code>, occurrences
  // of 'assert' will be reported as
  // identifiers
  //	 * ({@link org.eclipse.jdt.core.compiler.ITerminalSymbols#TokenNameIdentifier}), whereas if set
  // to <code>&quot;1.4&quot;</code>, it
  //	 * would report assert keywords ({@link
  // org.eclipse.jdt.core.compiler.ITerminalSymbols#TokenNameassert}). Java 1.4 has introduced
  //	 * a new 'assert' keyword.
  //	 * @param complianceLevel This is used to support the Unicode 4.0 character sets. if set to 1.5
  // or above,
  //	 * the Unicode 4.0 is supported, otherwise Unicode 3.0 is supported.
  //	 * @return a scanner
  //	 * @see org.eclipse.jdt.core.compiler.IScanner
  //	 *
  //	 * @since 3.1
  //	 */
  //	public static IScanner createScanner(boolean tokenizeComments, boolean tokenizeWhiteSpace,
  // boolean recordLineSeparator, String sourceLevel, String complianceLevel) {
  //		PublicScanner scanner = null;
  //		long sourceLevelValue = CompilerOptions.versionToJdkLevel(sourceLevel);
  //		if (sourceLevelValue == 0) sourceLevelValue = ClassFileConstants.JDK1_3; // fault-tolerance
  //		long complianceLevelValue = CompilerOptions.versionToJdkLevel(complianceLevel);
  //		if (complianceLevelValue == 0) complianceLevelValue = ClassFileConstants.JDK1_4; //
  // fault-tolerance
  //		scanner = new PublicScanner(tokenizeComments, tokenizeWhiteSpace,
  // false/*nls*/,sourceLevelValue /*sourceLevel*/, complianceLevelValue, null/*taskTags*/,
  // null/*taskPriorities*/, true/*taskCaseSensitive*/);
  //		scanner.recordLineSeparator = recordLineSeparator;
  //		return scanner;
  //	}
}
