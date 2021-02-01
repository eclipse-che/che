--
-- Copyright (c) 2012-2020 Red Hat, Inc.
-- This program and the accompanying materials are made
-- available under the terms of the Eclipse Public License 2.0
-- which is available at https://www.eclipse.org/legal/epl-2.0/
--
-- SPDX-License-Identifier: EPL-2.0
--
-- Contributors:
--   Red Hat, Inc. - initial API and implementation
--

DROP TABLE IF EXISTS che_factory_image;

ALTER TABLE che_factory DROP CONSTRAINT fk_che_f_button_id;
ALTER TABLE che_factory DROP COLUMN button_id;
DROP TABLE IF EXISTS che_factory_button;
