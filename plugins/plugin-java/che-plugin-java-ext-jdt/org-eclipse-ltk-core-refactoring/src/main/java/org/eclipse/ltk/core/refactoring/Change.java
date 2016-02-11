/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;

/**
 * An abstract base implementation for object representing a generic change
 * to the workbench. A <code>Change</code> object is typically created by
 * calling {@link Refactoring#createChange(IProgressMonitor)}. This class should be
 * subclassed by clients wishing to provide new changes.
 * <p>
 * Changes are best executed by using a {@link PerformChangeOperation}. If clients
 * execute a change directly then the following life cycle has to be honored:
 * <ul>
 *   <li>After a single change or a tree of changes has been created, the
 *       method <code>initializeValidationData</code> has to be called.</li>
 *   <li>The method <code>isValid</code> can be used to determine if a change
 *       can still be applied to the workspace. If the method returns a {@link
 *       RefactoringStatus} with a severity of FATAL then the change has to be
 *       treated as invalid. Performing an invalid change isn't allowed and
 *       results in an unspecified result. This method can be called multiple
 *       times.
 *   <li>Then the method <code>perform</code> can be called. A disabled change
 *       must not be executed. The <code>perform</code> method can only be called
 *       once. After a change has been executed, only the method <code>dispose</code>
 *       must be called.</li>
 *   <li>the method <code>dispose</code> has to be called either after the
 *       <code>perform</code> method
 *       has been called or if a change is no longer needed. The second case
 *       for example occurs when the undo stack gets flushed and all change
 *       objects managed by the undo stack are no longer needed. The method
 *       <code>dispose</code> is typically implemented to unregister listeners
 *       registered during the
 *       method <code>initializeValidationData</code>. There is no guarantee
 *       that <code>initializeValidationData</code>, <code>isValid</code>,
 *       or <code>perform</code> has been called before <code>dispose</code>
 *       is called.
 * </ul>
 * Here is a code snippet that can be used to execute a change:
 * <pre>
 *   Change change= createChange();
 *   try {
 *     change.initializeValidationData(pm);
 * 
 *     ....
 *
 *     if (!change.isEnabled())
 *         return;
 *     RefactoringStatus valid= change.isValid(new SubProgressMonitor(pm, 1));
 *     if (valid.hasFatalError())
 *         return;
 *     Change undo= change.perform(new SubProgressMonitor(pm, 1));
 *     if (undo != null) {
 *        undo.initializeValidationData(new SubProgressMonitor(pm, 1));
 *        // do something with the undo object
 *     }
 *   } finally {
 *     change.dispose();
 *   }
 * </pre>
 * </p>
 * <p>
 * It is important that implementors of this abstract class provide an adequate
 * implementation of <code>isValid</code> and that they provide an undo change
 * via the return value of the method <code>perform</code>. If no undo can be
 * provided then the <code>perform</code> method is allowed to return <code>null</code>. But
 * implementors should be aware that not providing an undo object for a change
 * object that is part of a larger change tree will result in the fact that for
 * the whole change tree no undo object will be present.
 * </p>
 * <p>
 * Changes which are returned as top-level changes (e.g. by <code>Refactoring.createChange()</code>)
 * can optionally return a descriptor object of the refactoring which created this change object.
 * </p>
 * <p>
 * Clients may subclass this class.
 * </p>
 * 
 * @since 3.0
 */
public abstract class Change implements IAdaptable {

	private Change fParent;
	private boolean fIsEnabled= true;

	/**
	 * Constructs a new change object.
	 */
	protected Change() {
	}

	/**
	 * Returns a descriptor of this change.
	 * <p>
	 * Subclasses of changes created by
	 * {@link Refactoring#createChange(IProgressMonitor)} should override this
	 * method to return a {@link RefactoringChangeDescriptor}. A change tree
	 * created by a particular refactoring is supposed to contain at most one
	 * change which returns a refactoring descriptor. Refactorings usually
	 * return an instance of {@link CompositeChange} in their
	 * {@link Refactoring#createChange(IProgressMonitor)} method which
	 * implements this method. The refactoring framework searches the change
	 * tree top-down until a refactoring descriptor is found.
	 * </p>
	 *
	 * @return a descriptor of this change, or <code>null</code> if this
	 *         change does not provide a change descriptor.
	 *
	 * @since 3.2
	 */
	public ChangeDescriptor getDescriptor() {
		return null;
	}

	/**
	 * Returns the human readable name of this change. The
	 * name <em>MUST</em> not be <code>null</code>.
	 *
	 * @return the human readable name of this change
	 */
	public abstract String getName();

	/**
	 * Returns whether this change is enabled or not. Disabled changes
	 * must not be executed.
	 *
	 * @return <code>true</code> if the change is enabled; <code>false</code>
	 *  otherwise.
	 */
	public boolean isEnabled() {
		return fIsEnabled;
	}

	/**
	 * Sets whether this change is enabled or not.
	 *
	 * @param enabled <code>true</code> to enable this change; <code>
	 *  false</code> otherwise
	 */
	public void setEnabled(boolean enabled) {
		fIsEnabled= enabled;
	}

	/**
	 * Sets the enablement state of this change in a shallow way.
	 * For changes having children this means that only this change's
	 * enablement state changes. The children are left untouched.
	 *
	 * @param enabled <code>true</code> to enable this change; <code>
	 *  false</code> otherwise
	 *
	 * @since 3.1
	 */
	public final void setEnabledShallow(boolean enabled) {
		fIsEnabled= enabled;
	}

	/**
	 * Returns the parent change. Returns <code>null</code> if no
	 * parent exists.
	 *
	 * @return the parent change
	 */
	public Change getParent() {
		return fParent;
	}

	/**
	 * Sets the parent of this change. Requires that this change isn't already
	 * connected to a parent. The parent can be <code>null</code> to disconnect
	 * this change from a parent.
	 *
	 * @param parent the parent of this change or <code>null</code>
	 */
	/* package */ void setParent(Change parent) {
		if (parent != null)
			Assert.isTrue(fParent == null);
		fParent= parent;
	}

	/**
	 * Hook method to initialize some internal state to provide an adequate answer
	 * for the <code>isValid</code> method. This method gets called after a change
	 * or a whole change tree has been created.
	 * <p>
	 * Typically this method is implemented in one of the following ways:
	 * <ul>
	 *   <li>the change hooks up a listener on some delta notification mechanism
	 *       and marks itself as invalid if it receives a certain delta. Is this
	 *       the case the implementor must take care of unhooking the listener
	 *       in <code>dispose</code>.</li>
	 *   <li>the change remembers some information allowing to decide if a change
	 *       object is still valid when <code>isValid</code> is called.</li>
	 * </ul>
	 * <p>
	 * For example, a change object that manipulates the content of an <code>IFile</code>
	 * could either listen to resource changes and detect that the file got changed or
	 * it could remember some content stamp and compare it with the actual content stamp
	 * when <code>isValid</code> is called.
	 * </p>
	 *
	 * @param pm a progress monitor
	 */
	public abstract void initializeValidationData(IProgressMonitor pm);

	/**
	 * Verifies that this change object is still valid and can be executed by calling
	 * <code>perform</code>. If a refactoring status  with a severity of {@link
	 * RefactoringStatus#FATAL} is returned then the change has to be treated as invalid
	 * and can no longer be executed. Performing such a change produces an unspecified
	 * result and will very likely throw an exception.
	 * <p>
	 * This method is also called by the {@link IUndoManager UndoManager} to decide if
	 * an undo or redo change is still valid and therefore can be executed.
	 * </p>
	 *
	 * @param pm a progress monitor.
	 *
	 * @return a refactoring status describing the outcome of the validation check
	 *
	 * @throws CoreException if an error occurred during validation check. The change
	 *  is to be treated as invalid if an exception occurs
	 *
	 * @throws OperationCanceledException if the validation check got canceled
	 */
	public abstract RefactoringStatus isValid(IProgressMonitor pm) throws CoreException, OperationCanceledException;

	/**
	 * Performs this change. If this method is called on an invalid or disabled change
	 * object the result is unspecified. Changes should in general not respond to
	 * {@link IProgressMonitor#isCanceled()} since canceling a change tree in the
	 * middle of its execution leaves the workspace in a half changed state.
	 *
	 * @param pm a progress monitor
	 *
	 * @return the undo change for this change object or <code>null</code> if no
	 *  undo is provided
	 *
	 * @throws CoreException if an error occurred during change execution
	 */
	public abstract Change perform(IProgressMonitor pm) throws CoreException;

	/**
	 * Disposes this change. Subclasses that override this method typically unregister listeners
	 * which got registered during the call to <code>
	 * initializeValidationData</code>.
	 * <p>
	 * Subclasses may override this method.
	 * </p>
	 */
	public void dispose() {
		// empty default implementation
	}

	/**
	 * Returns the element modified by this <code>Change</code>. The method may return
	 * <code>null</code> if the change isn't related to an element.
	 *
	 * @return the element modified by this change
	 */
	public abstract Object getModifiedElement();

	/**
	 * Returns the elements affected by this change or <code>null</code> if
	 * the affected elements cannot be determined. Returns an empty array
	 * if the change doesn't modify any elements.
	 * <p>
	 * This default implementation returns <code>null</code> to indicate that
	 * the affected elements are unknown. Subclasses should reimplement this method
	 * if they can compute the set of affected elements.
	 * </p>
	 *
	 * @return the elements affected by this change or <code>null</code> if
	 *  the affected elements cannot be determined
     *
     * @since 3.1
	 */
	public Object[] getAffectedObjects() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getAdapter(Class adapter) {
		Object result= Platform.getAdapterManager().getAdapter(this, adapter);
		if (result != null)
			return result;
		if (fParent != null)
			return fParent.getAdapter(adapter);
		return null;
	}
}
