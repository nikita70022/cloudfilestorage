SELECT
    ccu.column_name,
    cc.constraint_name,
    cc.check_clause
FROM information_schema.check_constraints cc
         JOIN information_schema.constraint_column_usage ccu
              ON cc.constraint_name = ccu.constraint_name
WHERE ccu.table_name = 'users'
  AND ccu.column_name IN ('username', 'password', 'role');
