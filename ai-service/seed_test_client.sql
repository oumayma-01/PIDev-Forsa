-- ============================================================
-- SEED : client test avec wallet + 6 mois de transactions
-- Adapté aux vraies colonnes de ForsaBD
-- ============================================================

USE ForsaBD;

-- ── 1. Créer l'utilisateur test ───────────────────────────────
-- Colonnes réelles : id, created_at, email, expiry_date, is_active,
--                   password_hash, reset_token, username, id_role, user_id
INSERT IGNORE INTO user
  (id, created_at, email, is_active, password_hash, username, id_role, user_id)
VALUES
  (999,
   DATE_SUB(NOW(), INTERVAL 7 MONTH),
   'test.client@forsa.tn',
   1,
   '$2a$10$dummyhash999testclientforsa1234',
   'test_client_999',
   2,      -- id_role 2 = CLIENT (adapte si besoin)
   NULL);

-- ── 2. Créer le wallet ────────────────────────────────────────
-- Colonnes réelles : id, balance, owner_id
INSERT IGNORE INTO wallet (id, balance, owner_id)
VALUES (999, 850.00, 999);

-- ── 3. Transactions sur 6 mois ───────────────────────────────
-- Colonnes réelles : id, amount, date, type, wallet_id
INSERT IGNORE INTO transaction (id, amount, date, type, wallet_id) VALUES
  -- Mois -6
  (9901, 1200.00, DATE_SUB(NOW(), INTERVAL 6 MONTH),                               'DEPOSIT',  999),
  (9902,  350.00, DATE_SUB(NOW(), INTERVAL 6 MONTH) + INTERVAL 5  DAY,             'WITHDRAW', 999),
  (9903,  120.00, DATE_SUB(NOW(), INTERVAL 6 MONTH) + INTERVAL 10 DAY,             'WITHDRAW', 999),

  -- Mois -5
  (9904, 1200.00, DATE_SUB(NOW(), INTERVAL 5 MONTH),                               'DEPOSIT',  999),
  (9905,  350.00, DATE_SUB(NOW(), INTERVAL 5 MONTH) + INTERVAL 5  DAY,             'WITHDRAW', 999),
  (9906,   80.00, DATE_SUB(NOW(), INTERVAL 5 MONTH) + INTERVAL 12 DAY,             'WITHDRAW', 999),

  -- Mois -4
  (9907, 1250.00, DATE_SUB(NOW(), INTERVAL 4 MONTH),                               'DEPOSIT',  999),
  (9908,  350.00, DATE_SUB(NOW(), INTERVAL 4 MONTH) + INTERVAL 5  DAY,             'WITHDRAW', 999),
  (9909,  150.00, DATE_SUB(NOW(), INTERVAL 4 MONTH) + INTERVAL 15 DAY,             'WITHDRAW', 999),

  -- Mois -3
  (9910, 1200.00, DATE_SUB(NOW(), INTERVAL 3 MONTH),                               'DEPOSIT',  999),
  (9911,  350.00, DATE_SUB(NOW(), INTERVAL 3 MONTH) + INTERVAL 5  DAY,             'WITHDRAW', 999),
  (9912,   90.00, DATE_SUB(NOW(), INTERVAL 3 MONTH) + INTERVAL 8  DAY,             'WITHDRAW', 999),

  -- Mois -2
  (9913, 1200.00, DATE_SUB(NOW(), INTERVAL 2 MONTH),                               'DEPOSIT',  999),
  (9914,  350.00, DATE_SUB(NOW(), INTERVAL 2 MONTH) + INTERVAL 5  DAY,             'WITHDRAW', 999),
  (9915,  200.00, DATE_SUB(NOW(), INTERVAL 2 MONTH) + INTERVAL 20 DAY,             'WITHDRAW', 999),

  -- Mois -1
  (9916, 1200.00, DATE_SUB(NOW(), INTERVAL 1 MONTH),                               'DEPOSIT',  999),
  (9917,  350.00, DATE_SUB(NOW(), INTERVAL 1 MONTH) + INTERVAL 5  DAY,             'WITHDRAW', 999),
  (9918,  110.00, DATE_SUB(NOW(), INTERVAL 1 MONTH) + INTERVAL 18 DAY,             'WITHDRAW', 999);

-- ── 4. Vérification ──────────────────────────────────────────
SELECT
  u.id,
  u.username,
  ROUND(DATEDIFF(NOW(), u.created_at) / 30, 1)  AS age_mois,
  w.balance                                       AS balance_actuelle,
  (SELECT SUM(t.amount) FROM transaction t
   WHERE t.wallet_id = w.id AND t.type IN ('DEPOSIT','TRANSFER_IN'))   AS total_revenus,
  (SELECT SUM(t.amount) FROM transaction t
   WHERE t.wallet_id = w.id AND t.type IN ('WITHDRAW','TRANSFER_OUT')) AS total_depenses
FROM user u
JOIN wallet w ON w.owner_id = u.id
WHERE u.id = 999;
