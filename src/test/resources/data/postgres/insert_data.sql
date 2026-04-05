TRUNCATE TABLE auth.users RESTART IDENTITY CASCADE;

INSERT INTO auth.users (username, password, role)
VALUES ('user1', '$2a$10$4QPUBmcIPbvUKyqgajE5i.n915T7Ov/IiPLSY0tcWUknDPDSWhAbm', 'USER');