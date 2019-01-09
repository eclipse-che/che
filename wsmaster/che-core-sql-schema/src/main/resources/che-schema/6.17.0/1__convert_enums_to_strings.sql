--
-- Copyright (c) 2012-2019 Red Hat, Inc.
-- This program and the accompanying materials are made
-- available under the terms of the Eclipse Public License 2.0
-- which is available at https://www.eclipse.org/legal/epl-2.0/
--
-- SPDX-License-Identifier: EPL-2.0
--
-- Contributors:
--   Red Hat, Inc. - initial API and implementation
--

UPDATE che_k8s_machine SET status = 'STARTING' WHERE status = '0';
UPDATE che_k8s_machine SET status = 'RUNNING' WHERE status = '1';
UPDATE che_k8s_machine SET status = 'STOPPED' WHERE status = '2';
UPDATE che_k8s_machine SET status = 'FAILED' WHERE status = '3';

UPDATE che_k8s_runtime SET status = 'STARTING' WHERE status = '0';
UPDATE che_k8s_runtime SET status = 'RUNNING' WHERE status = '1';
UPDATE che_k8s_runtime SET status = 'STOPPING' WHERE status = '2';
UPDATE che_k8s_runtime SET status = 'STOPPED' WHERE status = '3';

UPDATE che_k8s_server SET status = 'RUNNING' WHERE status = '0';
UPDATE che_k8s_server SET status = 'STOPPED' WHERE status = '1';
UPDATE che_k8s_server SET status = 'UNKNOWN' WHERE status = '2';
