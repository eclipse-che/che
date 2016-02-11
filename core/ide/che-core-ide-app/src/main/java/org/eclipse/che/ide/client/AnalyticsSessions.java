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
package org.eclipse.che.ide.client;

/**
 * @author Alexander Reshetnyak
 */
public class AnalyticsSessions {
    private String id;
    private long   lastUsageTime;

    public AnalyticsSessions() {
        makeNew();
    }

    public String getId() {
        return id;
    }

    public void updateLogTime() {
    }

    public void updateUsageTime() {
        lastUsageTime = System.currentTimeMillis();
    }

    public void makeNew() {
        id = UUID.uuid();
        lastUsageTime = System.currentTimeMillis();
    }


    public long getIdleUsageTime() {
        return System.currentTimeMillis() - lastUsageTime;
    }

    public long getIdleLogTime() {
        return System.currentTimeMillis() - lastUsageTime;
    }

    public static class UUID {
        private static final char[] CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

        /**
         * Generate part of a RFC4122, version 4 ID.
         * RFC4122: "92329D39-6F5C-4520-ABFC-AAB64544E172"
         *
         * RFC4122 part:             "C-4520-ABFC-AAB64544E172"
         * Date part   : "14D05899-F01"
         *
         *
         * Result : "14D05899-F01C-4520-ABFC-AAB64544E172"
         *
         */
        private static String uuid() {
            String dateInHex = Long.toHexString(System.currentTimeMillis()).toUpperCase();
            String partDate = dateInHex.substring(0, 8) + "-" + dateInHex.substring(8);

            char[] uuid = new char[24];
            int r;

            // rfc4122 requires these characters
            uuid[1] = uuid[6] = uuid[11] = '-';
            uuid[0] = '4';

            // Fill in random data.  At i==19 set the high bits of clock sequence as
            // per rfc4122, sec. 4.1.5
            for (int i = 0; i < 24; i++) {
                if (uuid[i] == 0) {
                    r = (int)(Math.random() * 16);
                    uuid[i] = CHARS[(i == 19) ? (r & 0x3) | 0x8 : r & 0xf];
                }
            }
            return partDate +  new String(uuid);
        }
    }
}
