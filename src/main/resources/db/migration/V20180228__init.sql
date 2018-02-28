CREATE TEXT TABLE IF NOT EXISTS jdbs_drivers (
  id BIGINT IDENTITY PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  clazz VARCHAR(500),
  clazz_expr VARCHAR(500)
);

SET TABLE jdbs_drivers SOURCE 'drivers.csv'



CREATE TEXT TABLE IF NOT EXISTS jdbs_jars (
  id BIGINT IDENTITY PRIMARY KEY,
  driver_id BIGINT NOT NULL,
  path VARCHAR(1000) NOT NULL
);

ALTER TABLE jdbs_jars
ADD CONSTRAINT fk_jdbs_jars_driver
FOREIGN KEY (driver_id)
REFERENCES jdbs_drivers (id)
ON DELETE CASCADE;

SET TABLE jdbs_jars SOURCE 'jars.csv'



CREATE TEXT TABLE IF NOT EXISTS jdbs_deps (
  id BIGINT IDENTITY PRIMARY KEY,
  driver_id BIGINT NOT NULL,
  gav VARCHAR(200) NOT NULL
);

ALTER TABLE jdbs_deps
ADD CONSTRAINT fk_jdbs_deps_driver
FOREIGN KEY (driver_id)
REFERENCES jdbs_drivers (id)
ON DELETE CASCADE;

SET TABLE jdbs_deps SOURCE 'deps.csv'