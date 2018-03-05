CREATE TEXT TABLE IF NOT EXISTS jdbs_aliases (
  id BIGINT IDENTITY PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  url VARCHAR(2000) NOT NULL,
  username VARCHAR(500),
  password VARCHAR(500),
  driver_id BIGINT NOT NULL
);


ALTER TABLE jdbs_aliases
ADD CONSTRAINT fk_jdbs_aliases_driver
FOREIGN KEY (driver_id)
REFERENCES jdbs_drivers (id)
ON DELETE CASCADE;

SET TABLE jdbs_aliases SOURCE 'aliases.csv'
