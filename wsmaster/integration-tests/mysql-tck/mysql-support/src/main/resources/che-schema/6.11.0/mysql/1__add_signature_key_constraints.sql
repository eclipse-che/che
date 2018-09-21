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
DELETE kp FROM che_sign_key_pair AS kp
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
DELETE FROM che_sign_key_pair
WHERE (
      SELECT cnt FROM (SELECT count(*) AS cnt FROM che_sign_key_pair kp2
      JOIN che_sign_key_pair ON (che_sign_key_pair.private_key = kp2.private_key
      OR che_sign_key_pair.private_key = kp2.public_key)) AS C
      ) > 1;

-- remove keys which have no more key pair references to it
DELETE FROM che_sign_key
WHERE NOT EXISTS (
      SELECT * FROM (SELECT public_key FROM che_sign_key_pair kp
      JOIN che_sign_key ON (kp.private_key = che_sign_key.id
      OR kp.public_key = che_sign_key.id)) AS S
      );

-- add pair uniqueness constraint
CREATE UNIQUE INDEX index_sign_public_private_key_id ON che_sign_key_pair (public_key, private_key);

-- add foreign key indexes
CREATE INDEX index_sign_public_key_id ON che_sign_key_pair (public_key);
CREATE INDEX index_sign_private_key_id ON che_sign_key_pair (private_key);

-- add keys table constraints
ALTER TABLE che_sign_key_pair ADD CONSTRAINT fk_sign_public_key_id FOREIGN KEY (public_key) REFERENCES che_sign_key (id);
ALTER TABLE che_sign_key_pair ADD CONSTRAINT fk_sign_private_key_id FOREIGN KEY (private_key) REFERENCES che_sign_key (id);
