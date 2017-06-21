/*
 *  [2012] - [2017] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package org.eclipse.che.api.agent.server.activity;

import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.commons.schedule.ScheduleRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Notifies master about activity in workspace, but not more often than once per minute.
 *
 * @author Mihail Kuznyetsov
 * @author Anton Korneta
 */
@Singleton
public class WorkspaceActivityNotifier {
    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceActivityNotifier.class);

    private final AtomicBoolean          activeDuringThreshold;
    private final HttpJsonRequestFactory httpJsonRequestFactory;
    private final String                 apiEndpoint;
    private final String                 wsId;
    private final long                   threshold;

    private long lastUpdateTime;


    @Inject
    public WorkspaceActivityNotifier(HttpJsonRequestFactory httpJsonRequestFactory,
                                     @Named("che.api") String apiEndpoint,
                                     @Named("env.CHE_WORKSPACE_ID") String wsId,
                                     @Named("workspace.activity.notify_time_threshold_ms") long threshold) {
        this.httpJsonRequestFactory = httpJsonRequestFactory;
        this.apiEndpoint = apiEndpoint;
        this.wsId = wsId;
        this.activeDuringThreshold = new AtomicBoolean(false);
        this.threshold = threshold;
    }

    /**
     * Notify workspace master about activity in this workspace.
     * <p/>
     * After last notification, any consecutive activities that come within specific amount of time
     * - {@code threshold}, will not notify immediately, but trigger notification in scheduler method
     * {@link WorkspaceActivityNotifier#scheduleActivityNotification}
     */
    public void onActivity() {
        long currentTime = System.currentTimeMillis();
        if (currentTime < (lastUpdateTime + threshold)) {
            activeDuringThreshold.set(true);
        } else {
            notifyActivity();
            lastUpdateTime = currentTime;
        }

    }

    @ScheduleRate(periodParameterName = "workspace.activity.schedule_period_s")
    private void scheduleActivityNotification() {
        if (activeDuringThreshold.compareAndSet(true, false)) {
            notifyActivity();
        }
    }

    private void notifyActivity() {
        try {
            httpJsonRequestFactory.fromUrl(apiEndpoint + "/activity/" + wsId)
                                  .usePutMethod()
                                  .request();
        } catch (Exception e) {
            LOG.error("Cannot notify master about workspace " + wsId + " activity", e);
        }
    }
}
