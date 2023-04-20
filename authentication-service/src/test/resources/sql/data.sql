INSERT INTO accounts (id, email, password, role, is_account_non_expired, is_account_non_locked, is_credentials_non_expired, is_enabled)
VALUES (1, 'test@gmail.com', 'test', 'USER', false, false, false, false),
       (2, 'dummy@gmail.com', 'dummy', 'ADMIN', false, false, false, false);
SELECT SETVAL('accounts_id_seq', (SELECT MAX(id) FROM accounts));

-- INSERT INTO tokens (id, expired, revoked, token, token_type, account_id)
-- VALUES (9999, false, false, 'dummy_token', 'BEARER', 1);
