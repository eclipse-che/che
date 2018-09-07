--
-- Copyright (c) 2012-2018 Red Hat, Inc.
-- This program and the accompanying materials are made
-- available under the terms of the Eclipse Public License 2.0
-- which is available at https://www.eclipse.org/legal/epl-2.0/
--
-- SPDX-License-Identifier: EPL-2.0
--
-- Contributors:
--   Red Hat, Inc. - initial API and implementation
--


-- remove key pair records which linked to non-existing keys
DELETE FROM che_sign_key_pair kp
WHERE NOT EXISTS (
   SELECT id
   FROM   che_sign_key k
   WHERE  k.id = kp.private_key
   )
OR NOT EXISTS (
   SELECT id
   FROM   che_sign_key k
   WHERE  k.id = kp.public_key
   );

-- remove duplicated keys id if any
DELETE FROM che_sign_key_pair kp1
WHERE (
      SELECT count(*) FROM che_sign_key_pair kp2
      WHERE kp1.private_key = kp2.private_key
      OR kp1.private_key = kp2.public_key
      ) > 1;

-- remove keys which have no more key pair references to it
DELETE FROM che_sign_key k
WHERE NOT EXISTS (
      SELECT * FROM che_sign_key_pair kp
      WHERE kp.private_key = k.id
      OR kp.public_key = k.id
      );

-- add pair uniqueness constraint
CREATE UNIQUE INDEX index_sign_public_private_key_id ON che_sign_key_pair (public_key, private_key);

-- add foreign key indexes
CREATE INDEX index_sign_public_key_id ON che_sign_key_pair (public_key);
CREATE INDEX index_sign_private_key_id ON che_sign_key_pair (private_key);

-- add keys table constraints
ALTER TABLE che_sign_key_pair ADD CONSTRAINT fk_sign_public_key_id FOREIGN KEY (public_key) REFERENCES che_sign_key (id);
ALTER TABLE che_sign_key_pair ADD CONSTRAINT fk_sign_private_key_id FOREIGN KEY (private_key) REFERENCES che_sign_key (id);
