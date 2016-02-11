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

import java.util.Collections;
import java.util.Map;

import org.eclipse.ltk.internal.core.refactoring.history.DefaultRefactoringDescriptor;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringContributionManager;

/**
 * Partial implementation of refactoring contribution objects which are capable
 * of creating refactoring descriptors or refactoring arguments.
 * <p>
 * Clients which would like to add refactoring history and refactoring scripting
 * support to a refactoring are required to register a subclass of
 * {@link RefactoringContribution} with the extension point
 * <code>org.eclipse.ltk.core.refactoring.refactoringContributions</code> to
 * participate in the refactoring services. Refactoring contributions are
 * stateless objects. They are instantiated on demand by the refactoring
 * framework in the following cases:
 * <ul>
 * <li> When a refactoring script is executed, the refactoring framework
 * retrieves a corresponding refactoring contribution for each refactoring
 * persisted in the script and calls
 * {@link #createDescriptor(String, String, String, String, Map, int)} with the
 * appropriate arguments read from the refactoring script to obtain a
 * language-specific refactoring descriptor. This refactoring descriptor is then
 * used to dynamically construct the corresponding refactoring object and to
 * initialize the refactoring object afterwards. The returned refactoring object
 * is completely initialized and ready to be executed, ie. by
 * {@link PerformRefactoringOperation}. </li>
 * <li> After a refactoring has been executed, the refactoring framework stores
 * the returned refactoring descriptor into the global refactoring history.
 * During serialization of the descriptor, the refactoring framework calls
 * {@link #retrieveArgumentMap(RefactoringDescriptor)} of the refactoring
 * contribution associated with the executed refactoring to obtain a neutral
 * key-value representation of the state of the language-specific refactoring
 * descriptor. </li>
 * </ul>
 * </p>
 * Refactorings for which a refactoring contribution has been registered should
 * also create a {@link RefactoringDescriptor} during change generation. Their
 * {@link Refactoring#createChange(org.eclipse.core.runtime.IProgressMonitor)} should
 * return a change object whose {@link Change#getDescriptor()} method returns a
 * {@link RefactoringChangeDescriptor} that encapsulates the {@link RefactoringDescriptor}.
 * </p>
 * <p>
 * Since 3.3, refactoring contributions may serve also as a uniform API to
 * expose language-specific refactorings. Clients wishing to provide
 * customizable refactoring descriptors may reimplement the method
 * {@link #createDescriptor()}.
 * </p>
 * Note: Clients which extend this class are required to reimplement the method
 * {@link #retrieveArgumentMap(RefactoringDescriptor)} in subclasses to capture
 * the state of a language-specific refactoring descriptor in a neutral
 * key-value representation used by the refactoring framework. The default
 * implementation in this class only handles refactoring descriptors associated
 * with refactorings for which no corresponding refactoring contribution has
 * been registered.
 * </p>
 *
 * @since 3.2
 */
public abstract class RefactoringContribution {

	/**
	 * Creates a new customizable refactoring descriptor initialized with its
	 * default values.
	 * <p>
	 * This method may be reimplemented to return a language-specified
	 * refactoring descriptor which can be initialized using language-specific
	 * features. Refactoring tool providers may reimplement this method to
	 * provide a uniform API to expose refactoring functionality in the form of
	 * refactoring descriptors.
	 * </p>
	 * <p>
	 * Callers of this method are supposed to cast the resulting refactoring
	 * descriptor to the corresponding language-specific refactoring descriptor
	 * provided by the API of the refactoring tooling provider.
	 * </p>
	 * <p>
	 * Note: this method is supposed to be reimplemented by clients wishing to
	 * provide customizable refactoring descriptors.
	 * </p>
	 *
	 * @return the refactoring descriptor, or <code>null</code> if the
	 *         refactoring represented by this contribution does not expose
	 *         customizable refactoring descriptors
	 *
	 * @see #createDescriptor(String, String, String, String, Map, int)
	 *
	 * @since 3.3
	 */
	public RefactoringDescriptor createDescriptor() {
		return null;
	}

	/**
	 * Creates a new refactoring descriptor initialized with the values provided
	 * by the arguments of this method.
	 * <p>
	 * This method is used by the refactoring framework to create a
	 * language-specific refactoring descriptor representing the refactoring
	 * instance corresponding to the specified arguments. Implementations of
	 * this method must never return <code>null</code>. The refactoring
	 * framework guarantees that this method is only called with <code>id</code>
	 * values for which the refactoring contribution has been registered with
	 * the extension point.
	 * </p>
	 *
	 * @param id
	 *            the unique id of the refactoring
	 * @param project
	 *            the non-empty name of the project associated with this
	 *            refactoring, or <code>null</code> for a workspace
	 *            refactoring
	 * @param description
	 *            a non-empty human-readable description of the particular
	 *            refactoring instance
	 * @param comment
	 *            the comment associated with the refactoring, or
	 *            <code>null</code> for no comment
	 * @param arguments
	 *            the argument map (element type: &lt;String, String&gt;). The
	 *            keys of the arguments are required to be non-empty strings
	 *            which must not contain spaces. The values must be non-empty
	 *            strings
	 * @param flags
	 *            the flags of the refactoring descriptor
	 * @return the refactoring descriptor
	 *
	 * @throws IllegalArgumentException if the argument map contains invalid keys/values
	 *
	 * @see #retrieveArgumentMap(RefactoringDescriptor)
	 */
	public abstract RefactoringDescriptor createDescriptor(String id, String project, String description, String comment, Map arguments, int flags) throws IllegalArgumentException;

	/**
	 * Returns the refactoring id for which this refactoring contribution has
	 * been registered with the extension point. Implementations of
	 * {@link #createDescriptor()} may use this method to initialize the
	 * resulting refactoring descriptor with the id of this refactoring
	 * contribution.
	 * <p>
	 * Note: this method is not intended to be extended or reimplemented by
	 * clients.
	 * </p>
	 *
	 * @return the unique id of the refactoring
	 *
	 * @since 3.3
	 *
	 * @nooverride This method is not intended to be re-implemented or extended by clients.
	 */
	public String getId() {
		return RefactoringContributionManager.getInstance().getRefactoringId(this);
	}

	/**
	 * Retrieves the argument map of the specified refactoring descriptor.
	 * <p>
	 * This method is used by the refactoring framework to obtain
	 * refactoring-specific arguments provided by the refactoring descriptor.
	 * These are the arguments which are specific to certain refactoring
	 * instances, and correspond to the argument map which has been passed to
	 * {@link #createDescriptor(String, String, String, String, Map, int)} upon
	 * creation of the refactoring descriptor.
	 * </p>
	 * <p>
	 * The returned argument map (element type: &lt;String, String&gt;) must
	 * satisfy the following conditions:
	 * <ul>
	 * <li>The keys of the arguments are required to be non-empty strings which
	 * must not contain spaces. </li>
	 * <li>The values must be non-empty</li>
	 * strings
	 * </ul>
	 * </p>
	 * <p>
	 * Note: Subclasses must extend this method to provide more specific
	 * implementation in order to let the refactoring framework retrieve the
	 * argument map from language-specific refactoring descriptors.
	 * Implementations of this method must never return <code>null</code>.
	 * The refactoring framework guarantees that this method is only called for
	 * refactoring descriptors which have been obtained by a previous call to
	 * {@link #createDescriptor(String, String, String, String, Map, int)}.
	 * </p>
	 *
	 * @param descriptor
	 *            the refactoring descriptor to retrieve its argument map
	 * @return the argument map of the specified refactoring descriptor
	 *
	 * @see #createDescriptor(String, String, String, String, Map, int)
	 */
	public Map retrieveArgumentMap(final RefactoringDescriptor descriptor) {
		if (descriptor instanceof DefaultRefactoringDescriptor) {
			final DefaultRefactoringDescriptor extended= (DefaultRefactoringDescriptor) descriptor;
			return extended.getArguments();
		}
		return Collections.EMPTY_MAP;
	}
}
