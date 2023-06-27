/*********************************************************************
 * Copyright (c) 2020-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
export const OAuthConstants: any = {
    /**
     * Value of OpenShift oAuth property determines how to login in installed application,
     * if 'false' as an user of application, if 'true' as a regular user of OCP.
     */
    TS_SELENIUM_VALUE_OPENSHIFT_OAUTH: process.env.TS_SELENIUM_VALUE_OPENSHIFT_OAUTH === 'true',

    /**
     * Log into OCP by using appropriate provider title.
     */
    TS_OCP_LOGIN_PAGE_PROVIDER_TITLE: process.env.TS_OCP_LOGIN_PAGE_PROVIDER_TITLE || '',

    /**
     * Regular username used to login in OCP.
     */
    TS_SELENIUM_OCP_USERNAME: process.env.TS_SELENIUM_OCP_USERNAME || '',

    /**
     * Password regular user used to login in OCP.
     */
    TS_SELENIUM_OCP_PASSWORD: process.env.TS_SELENIUM_OCP_PASSWORD || '',

    /**
     * Regular username used to login in Kubernetes.
     */
    TS_SELENIUM_K8S_USERNAME: process.env.TS_SELENIUM_K8S_USERNAME || '',

    /**
     * Password regular user used to login in Kubernetes.
     */
    TS_SELENIUM_K8S_PASSWORD: process.env.TS_SELENIUM_K8S_PASSWORD || '',

    /**
     * For login via github for example on https://che-dogfooding.apps.che-dev.x6e0.p1.openshiftapps.com
     */
    TS_SELENIUM_GIT_PROVIDER_OAUTH: process.env.TS_SELENIUM_GIT_PROVIDER_OAUTH === 'true',

    /**
     * Git repository username
     */
    TS_SELENIUM_GIT_PROVIDER_USERNAME: process.env.TS_SELENIUM_GIT_PROVIDER_USERNAME || '',

    /**
     * Git repository password
     */
    TS_SELENIUM_GIT_PROVIDER_PASSWORD: process.env.TS_SELENIUM_GIT_PROVIDER_PASSWORD || '',
};
