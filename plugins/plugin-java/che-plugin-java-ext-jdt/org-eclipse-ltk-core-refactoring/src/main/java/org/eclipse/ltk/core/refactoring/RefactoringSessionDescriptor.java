/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring;

import org.eclipse.core.runtime.Assert;

/**
 * Descriptor object of a refactoring session.
 * <p>
 * Refactoring session descriptors encapsulate a series of refactoring
 * descriptors. They are used to represent chunks of the global workspace
 * refactoring history or refactoring scripts created by the user.
 * </p>
 * <p>
 * Refactoring session descriptors contain the following information:
 * <ul>
 * <li> an optional comment string, which provides a full human-readable
 * description of the refactoring session. Comments are automatically generated
 * by refactorings and provide more refactoring-specific information, such as
 * which elements have participated in which refactorings. </li>
 * <li> a list of refactoring descriptors describing the refactorings executed
 * during a refactoring session. The refactoring list is sorted in ascending
 * order by the execution time of the refactorings. </li>
 * <li> a version tag describing version information of the refactoring session
 * descriptor format. The version tag is used to provide a means of schema
 * evolution on the refactoring framework level. Clients which would like to
 * version their refactoring descriptors are required to implement this in their
 * specific subclasses of {@link RefactoringDescriptor}. </li>
 * </ul>
 * </p>
 * <p>
 * Refactoring session descriptors are potentially heavy weight objects which
 * should not be held on to. Use refactoring descriptor proxies
 * {@link RefactoringDescriptorProxy} to present refactoring descriptors in the
 * user interface or otherwise manipulate refactoring histories. More details
 * about a particular refactoring session can be revealed in the comment, which
 * contains more text with refactoring-specific information.
 * </p>
 * <p>
 * All time stamps are measured as the milliseconds since January 1, 1970,
 * 00:00:00 GMT.
 * </p>
 * <p>
 * Note: this class is not indented to be subclassed outside the refactoring
 * framework.
 * </p>
 *
 * @see RefactoringDescriptor
 *
 * @since 3.2
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class RefactoringSessionDescriptor {

	/** The version constant for v1.0 (value: 1.0) */
	public static final String VERSION_1_0= "1.0"; //$NON-NLS-1$

	/** The comment , or <code>null</code> for no comment */
	private final String fComment;

	/** The refactoring descriptors */
	private final RefactoringDescriptor[] fDescriptors;

	/** The non-empty version string */
	private final String fVersion;

	/**
	 * Creates a new refactoring session descriptor.
	 *
	 * @param descriptors
	 *            the refactoring descriptors in executed order, or the empty
	 *            array
	 * @param version
	 *            the non-empty version tag, one of the <code>VERSION_xxx</code>
	 *            constants
	 * @param comment
	 *            the comment of the refactoring session, or <code>null</code>
	 *            for no comment
	 */
	public RefactoringSessionDescriptor(final RefactoringDescriptor[] descriptors, final String version, final String comment) {
		Assert.isNotNull(descriptors);
		Assert.isTrue(version != null && !"".equals(version)); //$NON-NLS-1$
		fDescriptors= new RefactoringDescriptor[descriptors.length];
		System.arraycopy(descriptors, 0, fDescriptors, 0, descriptors.length);
		fVersion= version;
		fComment= comment;
	}

	/**
	 * Returns the comment.
	 *
	 * @return the comment, or the empty string
	 */
	public final String getComment() {
		return (fComment != null) ? fComment : ""; //$NON-NLS-1$
	}

	/**
	 * Returns the refactoring descriptors.
	 *
	 * @return the array of refactoring descriptors in executed order, or the
	 *         empty array
	 */
	public final RefactoringDescriptor[] getRefactorings() {
		final RefactoringDescriptor[] result= new RefactoringDescriptor[fDescriptors.length];
		System.arraycopy(fDescriptors, 0, result, 0, result.length);
		return result;
	}

	/**
	 * Returns the version tag.
	 *
	 * @return the version tag
	 */
	public final String getVersion() {
		return fVersion;
	}
}
