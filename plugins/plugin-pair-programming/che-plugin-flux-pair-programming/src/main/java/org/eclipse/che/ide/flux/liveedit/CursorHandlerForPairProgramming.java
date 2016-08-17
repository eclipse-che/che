/*******************************************************************************
 * Copyright (c) 2016 www.rnavagamuwa.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Randika Navagamuwa <randikanavagamuwa@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.flux.liveedit;

import org.eclipse.che.ide.api.editor.texteditor.HasTextMarkers;

/**
 * Che product information constant.
 *
 * @author Sun Seng David TAN
 * @author Randika Navagamuwa
 */
public class CursorHandlerForPairProgramming {
    String user;
    int userId;
    HasTextMarkers.MarkerRegistration markerRegistration;

    protected void setMarkerRegistration(HasTextMarkers.MarkerRegistration markerRegistration){
        this.markerRegistration = markerRegistration;
    }

    protected void setUser(String user){
        this.user = user;
    }

    protected String getUser(){
        return  this.user;
    }

    protected void setUserId(int userId){
        this.userId = userId;
    }

    protected int getUserId(){
        return this.userId;
    }

    protected HasTextMarkers.MarkerRegistration getMarkerRegistration(){
        return this.markerRegistration;
    }

    protected void clearMark(){
        this.markerRegistration.clearMark();
    }
}
