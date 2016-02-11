// Copyright 2012 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.eclipse.che.ide.util.executor;

import com.google.gwt.core.client.Scheduler;

/** Executor of a cancellable scheduled command. */
public abstract class ScheduledCommandExecutor {

    private boolean scheduled;
    private boolean cancelled;

    private final Scheduler.ScheduledCommand scheduledCommand = new Scheduler.ScheduledCommand() {

        @Override
        public void execute() {
            scheduled = false;

            if (cancelled) {
                return;
            }

            ScheduledCommandExecutor.this.execute();
        }
    };

    protected abstract void execute();

    public void scheduleFinally() {
        cancelled = false;

        if (!scheduled) {
            scheduled = true;
            Scheduler.get().scheduleFinally(scheduledCommand);
        }
    }

    public void scheduleDeferred() {
        cancelled = false;

        if (!scheduled) {
            scheduled = true;
            Scheduler.get().scheduleDeferred(scheduledCommand);
        }
    }

    public void cancel() {
        cancelled = true;
    }
}
