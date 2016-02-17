/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.actions;
//
//import org.eclipse.che.api.analytics.client.logger.AnalyticsEventLogger;
//import org.eclipse.che.api.vfs.gwt.client.VfsServiceClient;
//import org.eclipse.che.api.vfs.shared.dto.ReplacementSet;
//import org.eclipse.che.api.vfs.shared.dto.Variable;
//import org.eclipse.che.ide.api.action.Action;
//import org.eclipse.che.ide.api.action.ActionEvent;
//import org.eclipse.che.ide.api.app.AppContext;
//import org.eclipse.che.ide.dto.DtoFactory;
//import org.eclipse.che.ide.rest.AsyncRequestCallback;
//import org.eclipse.che.ide.util.loging.Log;
//
//import javax.inject.Inject;
//import java.util.Arrays;
//import java.util.Map;
//
///**
// * @author Sergii Leschenko
// */
//public class FindReplaceAction extends Action {
//    private final VfsServiceClient     vfsServiceClient;
//    private final DtoFactory           dtoFactory;
//    private final AppContext           appContext;
//    private final AnalyticsEventLogger eventLogger;
//
//    @Inject
//    public FindReplaceAction(VfsServiceClient vfsServiceClient,
//                             DtoFactory dtoFactory,
//                             AppContext appContext,
//                             AnalyticsEventLogger eventLogger) {
//        this.vfsServiceClient = vfsServiceClient;
//        this.dtoFactory = dtoFactory;
//        this.appContext = appContext;
//        this.eventLogger = eventLogger;
//    }
//
//    @Override
//    public void actionPerformed(ActionEvent event) {
//        eventLogger.log(this);
//
//        if (appContext.getCurrentProject() == null || appContext.getCurrentProject().getRootProject() == null) {
//            Log.error(getClass(), "Can not run find/replace without opened project\n");
//            return;
//        }
//
//        if (event.getParameters() == null) {
//            Log.error(getClass(), "Can not run find/replace without parameters");
//            return;
//        }
//
//        final Map<String, String> parameters = event.getParameters();
//
//        String file = parameters.get("in");
//        String find = parameters.get("find");
//        String replace = parameters.get("replace");
//        String mode = parameters.get("replaceMode");
//
//        final ReplacementSet replacementSet = dtoFactory.createDto(ReplacementSet.class).withFiles(Arrays.asList(file))
//                                                        .withEntries(Arrays.asList(dtoFactory.createDto(Variable.class)
//                                                                                             .withFind(find)
//                                                                                             .withReplace(replace)
//                                                                                             .withReplacemode(mode)));
//
//        vfsServiceClient.replace(appContext.getWorkspaceId(),
//                                 appContext.getCurrentProject().getRootProject().getPath(),
//                                 Arrays.asList(replacementSet),
//                                 new AsyncRequestCallback<Void>() {
//                                     @Override
//                                     protected void onSuccess(Void result) {
//                                         //TODO Send event described in IDEX-1743
//                                     }
//
//                                     @Override
//                                     protected void onFailure(Throwable exception) {
//
//                                     }
//                                 });
//    }
//}
