/*******************************************************************************
 *  Copyright (c) 2000, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.resources;

import java.util.EventListener;
import org.eclipse.core.runtime.CoreException;

/**
 * A participant in the saving of the workspace.
 * <p>
 * Plug-ins implement this interface and register to participate
 * in workspace save operations.
 * </p>
 * <p>
 * Clients may implement this interface.
 * </p>
 * @see IWorkspace#save(boolean, org.eclipse.core.runtime.IProgressMonitor)
 */
public interface ISaveParticipant extends EventListener {
	/**
	 * Tells this participant that the workspace save operation is now
	 * complete and it is free to go about its normal business.
	 * Exceptions are not expected to be thrown at this point, so they
	 * should be handled internally.
	 * <p>
	 * Note: This method is called by the platform; it is not intended
	 * to be called directly by clients.
	 * </p>
	 *
	 * @param context the save context object
	 */
	public void doneSaving(ISaveContext context);

	/**
	 * Tells this participant that the workspace is about to be
	 * saved. In preparation, the participant is expected to suspend
	 * its normal operation until further notice. <code>saving</code>
	 * will be next, followed by either <code>doneSaving</code>
	 * or <code>rollback</code> depending on whether the workspace
	 * save was successful.
	 * <p>
	 * Note: This method is called by the platform; it is not intended
	 * to be called directly by clients.
	 * </p>
	 *
	 * @param context the save context object
	 * @exception CoreException if this method fails to snapshot
	 *   the state of this workspace 
	 */
	public void prepareToSave(ISaveContext context) throws CoreException;

	/**
	 * Tells this participant to rollback its important state.
	 * The context's previous state number indicates what it was prior
	 * to the failed save. 
	 * Exceptions are not expected to be thrown at this point, so they
	 * should be handled internally.
	 * <p>
	 * Note: This method is called by the platform; it is not intended
	 * to be called directly by clients.
	 * </p>
	 *
	 * @param context the save context object
	 * @see ISaveContext#getPreviousSaveNumber()
	 */
	public void rollback(ISaveContext context);

	/**
	 * Tells this participant to save its important state because
	 * the workspace is being saved, as described in the supplied
	 * save context.
	 * <p>
	 * Note: This method is called by the platform; it is not intended
	 * to be called directly by clients.
	 * </p>
	 * <p>
	 * The basic contract for this method is the same for full saves,
	 * snapshots and project saves: the participant must absolutely guarantee that any
	 * important user data it has gathered will not be irrecoverably lost
	 * in the event of a crash. The only difference is in the space-time
	 * tradeoffs that the participant should make.
	 * <ul>
	 * <li>Full saves: the participant is 
	 * encouraged to save additional non-essential information that will aid 
	 * it in retaining user state and configuration information and quickly getting
	 * back in sync with the state of the platform at a later point. 
	 * </li>
	 * <li>Snapshots: the participant is discouraged from saving non-essential 
	 * information that could be recomputed in the unlikely event of a crash.
	 * This lifecycle event will happen often and participant actions should take
	 * an absolute minimum of time.
	 * </li>
	 * <li>Project saves: the participant should only save project related data. 
	 * It is discouraged from saving non-essential information that could be recomputed
	 * in the unlikely event of a crash.
	 * </li>
	 * </ul>
	 * For instance, the Java IDE gathers various user preferences and would want to
	 * make sure that the current settings are safe and sound after a 
	 * <code>save</code> (if not saved immediately).
	 * The Java IDE would likely save computed image builder state on full saves,
	 * because this would allow the Java IDE to be restarted later and not
	 * have to recompile the world at that time. On the other hand, the Java
	 * IDE would not save the image builder state on a snapshot because
	 * that information is non-essential; in the unlikely event of a crash,
	 * the image should be rebuilt either from scratch or from the last saved
	 * state.
	 * </p>
	 * <p>
	 * The following snippet shows how a plug-in participant would write 
	 * its important state to a file whose name is based on the save
	 * number for this save operation.
	 * <pre>
	 *     Plugin plugin = ...; // known
	 *     int saveNumber = context.getSaveNumber();
	 *     String saveFileName = "save-" + Integer.toString(saveNumber);
	 *     File f = plugin.getStateLocation().append(saveFileName).toFile();
	 *     plugin.writeImportantState(f);
	 *     context.map(new Path("save"), new Path(saveFileName));
	 *     context.needSaveNumber();
	 *     context.needDelta(); // optional
	 * </pre>
	 * When the plug-in is reactivated in a subsequent workspace session,
	 * it needs to re-register to participate in workspace saves. When it
	 * does so, it is handed back key information about what state it had last
	 * saved. If it's interested, it can also ask for a resource delta
	 * describing all resource changes that have happened since then, if this
	 * information is still available.
	 * The following snippet shows what a participant plug-in would
	 * need to do if and when it is reactivated:
	 * <pre>
	 *     IWorkspace ws = ...; // known
	 *     Plugin plugin = ...; // known
	 *     ISaveParticipant saver = ...; // known
	 *     ISavedState ss = ws.addSaveParticipant(plugin, saver);
	 *     if (ss == null) {
	 *         // activate for very first time
	 *         plugin.buildState();
	 *     } else {
	 *         String saveFileName = ss.lookup(new Path("save"));
	 *         File f = plugin.getStateLocation().append(saveFileName).toFile();
	 *         plugin.readImportantState(f);
	 *         IResourceChangeListener listener = new IResourceChangeListener() {
	 *             public void resourceChanged(IResourceChangeEvent event) {
	 *                 IResourceDelta delta = event.getDelta();
	 *                 if (delta != null) {
	 *                     // fast reactivation using delta
	 *                     plugin.updateState(delta);
	 *                 } else {
	 *                     // slower reactivation without benefit of delta
	 *                     plugin.rebuildState();
	 *                 }
	 *         };
	 *         ss.processResourceChangeEvents(listener);
	 *     }
	 * </pre>
	 * </p>
	 *
	 * @param context the save context object
	 * @exception CoreException if this method fails
	 * @see ISaveContext#getSaveNumber()
	 */
	public void saving(ISaveContext context) throws CoreException;
}
