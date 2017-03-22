--
--  [2012] - [2017] Codenvy, S.A.
--  All Rights Reserved.
--
-- NOTICE:  All information contained herein is, and remains
-- the property of Codenvy S.A. and its suppliers,
-- if any.  The intellectual and technical concepts contained
-- herein are proprietary to Codenvy S.A.
-- and its suppliers and may be covered by U.S. and Foreign Patents,
-- patents in process, and are protected by trade secret or copyright law.
-- Dissemination of this information or reproduction of this material
-- is strictly forbidden unless prior written permission is obtained
-- from Codenvy S.A..
--

UPDATE che_factory
SET name = concat('f', right(id, 9)) WHERE name IS NULL OR name = '';

WITH dupes AS
  ( SELECT id, user_id, name
    FROM che_factory
    WHERE (name,user_id)
    IN (SELECT name, user_id
      FROM che_factory
      GROUP BY name, user_id
      HAVING count(*) > 1)
  ),
  uniques AS
  ( WITH q as
    ( SELECT *, row_number()
      OVER (PARTITION BY name,user_id ORDER BY name,user_id)
      AS rn FROM che_factory
    )
    SELECT id FROM q WHERE rn = 1
  )
UPDATE che_factory
SET name = concat(che_factory.name, '-', right(dupes.id, 9))
FROM dupes, uniques
WHERE dupes.id = che_factory.id AND NOT EXISTS (SELECT id FROM uniques WHERE che_factory.id = uniques.id);


CREATE UNIQUE INDEX index_name_plus_userid ON che_factory (user_id, name);
