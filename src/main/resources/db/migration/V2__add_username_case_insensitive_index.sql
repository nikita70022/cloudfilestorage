ALTER TABLE auth.users DROP CONSTRAINT IF EXISTS users_username_key;
CREATE UNIQUE INDEX users_username_lower_idx ON auth.users (LOWER(username));