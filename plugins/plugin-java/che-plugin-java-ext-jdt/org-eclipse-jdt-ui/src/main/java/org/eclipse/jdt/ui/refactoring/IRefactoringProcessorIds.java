/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.ui.refactoring;

import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceMappingContext;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.refactoring.IJavaElementMapper;
import org.eclipse.ltk.core.refactoring.IResourceMapper;

/**
 * Interface to define the processor IDs provided by the JDT refactoring.
 *
 * <p>This interface declares static final fields only; it is not intended to be implemented.
 *
 * @see org.eclipse.jdt.core.refactoring.participants.IRefactoringProcessorIds
 * @since 3.0
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IRefactoringProcessorIds {

  /**
   * Processor ID of the rename Java project processor (value <code>
   * "org.eclipse.jdt.ui.renameJavaProjectProcessor"</code>).
   *
   * <p>The rename Java project processor loads the following participants:
   *
   * <ul>
   *   <li>participants registered for renaming <code>IJavaProject</code>.
   *   <li>participants registered for renaming <code>IProject</code>.
   * </ul>
   */
  public static String RENAME_JAVA_PROJECT_PROCESSOR =
      "org.eclipse.jdt.ui.renameJavaProjectProcessor"; // $NON-NLS-1$

  /**
   * Processor ID of the rename source folder (value <code>
   * "org.eclipse.jdt.ui.renameSourceFolderProcessor"</code>).
   *
   * <p>The rename package fragment root processor loads the following participants:
   *
   * <ul>
   *   <li>participants registered for renaming <code>IPackageFragmentRoot</code>.
   *   <li>participants registered for renaming <code>IFolder</code>.
   * </ul>
   */
  public static String RENAME_SOURCE_FOLDER_PROCESSOR =
      "org.eclipse.jdt.ui.renameSourceFolderProcessor"; // $NON-NLS-1$

  /**
   * Processor ID of the rename package fragment processor (value <code>
   * "org.eclipse.jdt.ui.renamePackageProcessor"</code>).
   *
   * <p>The rename package fragment processor loads the following participants:
   *
   * <ul>
   *   <li>participants registered for renaming <code>IPackageFragment</code>.
   *   <li>participants registered for moving <code>IFile</code> to participate in the file moves
   *       caused by the package fragment rename.
   *   <li>participants registered for creating <code>IFolder</code> if the package rename results
   *       in creating a new destination folder.
   *   <li>participants registered for deleting <code>IFolder</code> if the package rename results
   *       in deleting the folder corresponding to the package fragment to be renamed.
   * </ul>
   *
   * <p>Since 3.3:
   *
   * <p>The refactoring processor moves and renames Java elements and resources. Rename package
   * fragment participants can retrieve the new location of Java elements and resources through the
   * interfaces {@link IJavaElementMapper} and {@link IResourceMapper}, which can be retrieved from
   * the processor using the getAdapter() method.
   */
  public static String RENAME_PACKAGE_FRAGMENT_PROCESSOR =
      "org.eclipse.jdt.ui.renamePackageProcessor"; // $NON-NLS-1$

  /**
   * Processor ID of the rename compilation unit processor (value <code>
   * "org.eclipse.jdt.ui.renameCompilationUnitProcessor"</code>).
   *
   * <p>The rename compilation unit processor loads the following participants:
   *
   * <ul>
   *   <li>participants registered for renaming <code>ICompilationUnit</code>.
   *   <li>participants registered for renaming <code>IFile</code>.
   *   <li>participants registered for renaming <code>IType</code> if the compilation unit contains
   *       a top level type.
   * </ul>
   */
  public static String RENAME_COMPILATION_UNIT_PROCESSOR =
      "org.eclipse.jdt.ui.renameCompilationUnitProcessor"; // $NON-NLS-1$

  /**
   * Processor ID of the rename type processor (value <code>"org.eclipse.jdt.ui.renameTypeProcessor"
   * </code>).
   *
   * <p>The rename type processor loads the following participants:
   *
   * <ul>
   *   <li>participants registered for renaming <code>IType</code>.
   *   <li>participants registered for renaming <code>ICompilationUnit</code> if the type is a
   *       public top level type.
   *   <li>participants registered for renaming <code>IFile</code> if the compilation unit gets
   *       rename as well.
   * </ul>
   *
   * <p>Since 3.2:
   *
   * <p>Participants that declare
   *
   * <pre> &lt;param name="handlesSimilarDeclarations" value="false"/&gt; </pre>
   *
   * in their extension contribution will not be loaded if the user selects the "update similar
   * declarations" feature.
   *
   * <p>Rename type participants can retrieve information about similar declarations by casting the
   * RenameArguments to RenameTypeArguments. The new signatures of similar declarations (and of
   * other Java elements or resources) are available through the interfaces {@link
   * IJavaElementMapper} and {@link IResourceMapper}, which can be retrieved from the processor
   * using the getAdapter() method.
   */
  public static String RENAME_TYPE_PROCESSOR =
      "org.eclipse.jdt.ui.renameTypeProcessor"; // $NON-NLS-1$

  /**
   * Processor ID of the rename method processor (value <code>
   * "org.eclipse.jdt.ui.renameMethodProcessor"</code>).
   *
   * <p>The rename method processor loads the following participants:
   *
   * <ul>
   *   <li>participants registered for renaming <code>IMethod</code>. Renaming virtual methods will
   *       rename methods with the same name in the type hierarchy of the type declaring the method
   *       to be renamed as well. For those derived methods participants will be loaded as well.
   * </ul>
   */
  public static String RENAME_METHOD_PROCESSOR =
      "org.eclipse.jdt.ui.renameMethodProcessor"; // $NON-NLS-1$

  /**
   * Processor ID of the rename field processor (value <code>
   * "org.eclipse.jdt.ui.renameFieldProcessor"</code>).
   *
   * <p>The rename filed processor loads the following participants:
   *
   * <ul>
   *   <li>participants registered for renaming <code>IField</code>.
   *   <li>participants registered for renaming <code>IMethod</code> if corresponding setter and
   *       getter methods are renamed as well.
   * </ul>
   */
  public static String RENAME_FIELD_PROCESSOR =
      "org.eclipse.jdt.ui.renameFieldProcessor"; // $NON-NLS-1$

  /**
   * Processor ID of the rename enum constant processor (value <code>
   * "org.eclipse.jdt.ui.renameEnumConstProcessor"</code>).
   *
   * <p>The rename filed processor loads the following participants:
   *
   * <ul>
   *   <li>participants registered for renaming <code>IField</code>.
   * </ul>
   *
   * @since 3.1
   */
  public static String RENAME_ENUM_CONSTANT_PROCESSOR =
      "org.eclipse.jdt.ui.renameEnumConstProcessor"; // $NON-NLS-1$

  /**
   * Processor ID of the rename resource processor (value <code>
   * "org.eclipse.jdt.ui.renameResourceProcessor"</code>).
   *
   * <p>The rename resource processor loads the following participants:
   *
   * <ul>
   *   <li>participants registered for renaming <code>IResource</code>.
   * </ul>
   */
  public static String RENAME_RESOURCE_PROCESSOR =
      "org.eclipse.jdt.ui.renameResourceProcessor"; // $NON-NLS-1$

  /**
   * Processor ID of the move resource processor (value <code>"org.eclipse.jdt.ui.MoveProcessor"
   * </code>).
   *
   * <p>The move processor loads the following participants, depending on the type of element that
   * gets moved:
   *
   * <ul>
   *   <li><code>IPackageFragmentRoot</code>: participants registered for moving package fragment
   *       roots together with participants moving a <code>IFolder
   *       </code>.
   *   <li><code>IPackageFragment</code>: participants registered for moving package fragments.
   *       Additionally move file, create folder and delete folder participants are loaded to
   *       reflect the resource changes caused by a moving a package fragment.
   *   <li><code>ICompilationUnit</code>: participants registered for moving compilation units and
   *       <code>IFile</code>. If the compilation unit contains top level types, participants for
   *       these types are loaded as well.
   *   <li><code>IResource</code>: participants registered for moving resources.
   * </ul>
   */
  public static String MOVE_PROCESSOR = "org.eclipse.jdt.ui.MoveProcessor"; // $NON-NLS-1$

  /**
   * Processor ID of the move static member processor (value <code>
   * "org.eclipse.jdt.ui.MoveStaticMemberProcessor"</code>).
   *
   * <p>The move static members processor loads participants registered for the static Java element
   * that gets moved. No support is available to participate in non static member moves.
   */
  public static String MOVE_STATIC_MEMBERS_PROCESSOR =
      "org.eclipse.jdt.ui.MoveStaticMemberProcessor"; // $NON-NLS-1$

  /**
   * Processor ID of the delete resource processor (value <code>"org.eclipse.jdt.ui.DeleteProcessor"
   * </code>).
   *
   * <p>The delete processor loads the following participants, depending on the type of element that
   * gets deleted:
   *
   * <ul>
   *   <li><code>IJavaProject</code>: participants registered for deleting <code>IJavaProject
   *       </code> and <code>IProject</code>.
   *   <li><code>IPackageFragmentRoot</code>: participants registered for deleting <code>
   *       IPackageFragmentRoot</code> and <code>IFolder</code>.
   *   <li><code>IPackageFragment</code>: participants registered for deleting <code>
   *       IPackageFragment</code>. Additionally delete file and delete folder participants are
   *       loaded to reflect the resource changes caused by deleting a package fragment.
   *   <li><code>ICompilationUnit</code>: participants registered for deleting compilation units and
   *       files. Additionally type delete participants are loaded to reflect the deletion of the
   *       top level types declared in the compilation unit.
   *   <li><code>IType</code>: participants registered for deleting types. Additional compilation
   *       unit and file delete participants are loaded if the type to be deleted is the only top
   *       level type of a compilation unit.
   *   <li><code>IMember</code>: participants registered for deleting members.
   *   <li><code>IResource</code>: participants registered for deleting resources.
   * </ul>
   */
  public static String DELETE_PROCESSOR = "org.eclipse.jdt.ui.DeleteProcessor"; // $NON-NLS-1$

  /**
   * Processor ID of the copy processor (value <code>"org.eclipse.jdt.ui.CopyProcessor"</code>).
   *
   * <p>The copy processor is used when copying elements via drag and drop or when pasting elements
   * from the clipboard. The copy processor loads the following participants, depending on the type
   * of the element that gets copied:
   *
   * <ul>
   *   <li><code>IJavaProject</code>: no participants are loaded.
   *   <li><code>IPackageFragmentRoot</code>: participants registered for copying <code>
   *       IPackageFragmentRoot</code> and <code>ResourceMapping</code>.
   *   <li><code>IPackageFragment</code>: participants registered for copying <code>IPackageFragment
   *       </code> and <code>ResourceMapping</code>.
   *   <li><code>ICompilationUnit</code>: participants registered for copying <code>ICompilationUnit
   *       </code> and <code>ResourceMapping</code>.
   *   <li><code>IType</code>: like ICompilationUnit if the primary top level type is copied.
   *       Otherwise no participants are loaded.
   *   <li><code>IMember</code>: no participants are loaded.
   *   <li><code>IFolder</code>: participants registered for copying folders.
   *   <li><code>IFile</code>: participants registered for copying files.
   * </ul>
   *
   * <p>Use the method {@link ResourceMapping#accept(ResourceMappingContext context,
   * IResourceVisitor visitor, IProgressMonitor monitor)} to enumerate the resources which form the
   * Java element. <code>ResourceMappingContext.LOCAL_CONTEXT</code> should be use as the <code>
   * ResourceMappingContext</code> passed to the accept method.
   *
   * @see org.eclipse.core.resources.mapping.ResourceMapping
   * @since 3.3
   */
  public static String COPY_PROCESSOR = "org.eclipse.jdt.ui.CopyProcessor"; // $NON-NLS-1$
}
