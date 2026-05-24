ALTER TABLE users
DROP CONSTRAINT IF EXISTS users_username_check;

ALTER TABLE users
DROP CONSTRAINT IF EXISTS users_password_check;

ALTER TABLE users
DROP CONSTRAINT IF EXISTS users_role_check;

