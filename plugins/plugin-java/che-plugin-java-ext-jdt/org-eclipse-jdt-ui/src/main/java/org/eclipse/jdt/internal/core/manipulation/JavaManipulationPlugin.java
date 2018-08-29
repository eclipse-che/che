/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.core.manipulation;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The main plug-in class to be used in the workbench. */
public class JavaManipulationPlugin /*extends Plugin */ {
  private static final Logger LOG = LoggerFactory.getLogger(JavaManipulationPlugin.class);
  //	//The shared instance.
  //	private static JavaManipulationPlugin fgDefault;
  //
  //	/**
  //	 * The constructor.
  //	 */
  //	public JavaManipulationPlugin() {
  //		fgDefault = this;
  //	}
  //
  //	/* (non-Javadoc)
  //	 * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
  //	 */
  //	public void start(BundleContext context) throws Exception {
  //		super.start(context);
  //	}
  //
  //	/* (non-Javadoc)
  //	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
  //	 */
  //	public void stop(BundleContext context) throws Exception {
  //		super.stop(context);
  //		fgDefault = null;
  //	}
  //
  //	/**
  //	 * Returns the shared instance.
  //	 *
  //	 * @return the shared instance.
  //	 */
  //	public static JavaManipulationPlugin getDefault() {
  //		return fgDefault;
  //	}
  //
  //	public static String getPluginId() {
  //		return JavaManipulation.ID_PLUGIN;
  //	}

  public static void log(IStatus status) {
    LOG.error(status.getMessage(), status.getException());
  }

  public static void logErrorMessage(String message) {
    log(
        new Status(
            IStatus.ERROR, "getPluginId()", /*IStatusConstants.INTERNAL_ERROR*/ 0, message, null));
  }

  //	public static void logErrorStatus(String message, IStatus status) {
  //		if (status == null) {
  //			logErrorMessage(message);
  //			return;
  //		}
  //		MultiStatus multi= new MultiStatus(getPluginId(), IStatusConstants.INTERNAL_ERROR, message,
  // null);
  //		multi.add(status);
  //		log(multi);
  //	}

  public static void log(Throwable e) {
    log(
        new Status(
            IStatus.ERROR,
            "getPluginId()",
            0, /*JavaManipulationMessages.JavaManipulationMessages_internalError*/
            "Internal Error",
            e));
  }
}
