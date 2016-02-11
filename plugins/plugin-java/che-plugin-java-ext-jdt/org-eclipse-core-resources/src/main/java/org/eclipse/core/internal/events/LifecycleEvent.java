/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Serge Beauchamp (Freescale Semiconductor) - [229633] Group Support
 *******************************************************************************/
package org.eclipse.core.internal.events;

import org.eclipse.core.resources.IResource;

/**
 * Class used for broadcasting internal workspace lifecycle events.  There is a
 * singleton instance, so no listener is allowed to keep references to the event
 * after the notification is finished.
 */
public class LifecycleEvent {
	//constants for kinds of internal workspace lifecycle events
	public static final int PRE_PROJECT_CLOSE = 0x01;
	public static final int POST_PROJECT_CHANGE = 0x02;
	public static final int PRE_PROJECT_COPY = 0x04;
	public static final int PRE_PROJECT_CREATE = 0x08;

	public static final int PRE_PROJECT_DELETE = 0x10;
	public static final int PRE_PROJECT_OPEN = 0x20;
	public static final int PRE_PROJECT_MOVE = 0x40;

	public static final int PRE_LINK_COPY = 0x100;
	public static final int PRE_LINK_CREATE = 0x200;
	public static final int PRE_LINK_DELETE = 0x400;
	public static final int PRE_LINK_MOVE = 0x800;
	public static final int PRE_REFRESH = 0x1000;

	public static final int PRE_GROUP_COPY = 0x2000;
	public static final int PRE_GROUP_CREATE = 0x4000;
	public static final int PRE_GROUP_DELETE = 0x8000;
	public static final int PRE_GROUP_MOVE = 0x10000;

	public static final int PRE_FILTER_ADD = 0x20000;
	public static final int PRE_FILTER_REMOVE = 0x40000;

	public static final int PRE_LINK_CHANGE = 0x80000;

	/**
	 * The kind of event
	 */
	public int kind;
	/**
	 * For events that only involve one resource, this is it.  More
	 * specifically, this is used for all events that don't involve a more or
	 * copy. For copy/move events, this resource represents the source of the
	 * copy/move.
	 */
	public IResource resource;
	/**
	 * For copy/move events, this resource represents the destination of the
	 * copy/move.
	 */
	public IResource newResource;

	/**
	 * The update flags for the event.
	 */
	public int updateFlags;

	private static final LifecycleEvent instance = new LifecycleEvent();

	private LifecycleEvent() {
		super();
	}

	public static LifecycleEvent newEvent(int kind, IResource resource) {
		instance.kind = kind;
		instance.resource = resource;
		instance.newResource = null;
		instance.updateFlags = 0;
		return instance;
	}

	public static LifecycleEvent newEvent(int kind, IResource oldResource, IResource newResource, int updateFlags) {
		instance.kind = kind;
		instance.resource = oldResource;
		instance.newResource = newResource;
		instance.updateFlags = updateFlags;
		return instance;
	}
}
