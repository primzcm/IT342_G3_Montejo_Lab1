DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'users'
          AND column_name = 'id'
          AND data_type = 'character varying'
    ) THEN
        IF NOT EXISTS (
            SELECT 1
            FROM pg_class
            WHERE relname = 'users_id_seq'
        ) THEN
            CREATE SEQUENCE users_id_seq;
        END IF;

        ALTER TABLE users ADD COLUMN id_new BIGINT;

        WITH ranked AS (
            SELECT id, row_number() OVER (ORDER BY created_at, username) AS rn
            FROM users
        )
        UPDATE users u
        SET id_new = r.rn
        FROM ranked r
        WHERE u.id = r.id;

        SELECT setval('users_id_seq', (SELECT COALESCE(MAX(id_new), 0) FROM users));

        ALTER TABLE users ALTER COLUMN id_new SET DEFAULT nextval('users_id_seq');
        ALTER TABLE users ALTER COLUMN id_new SET NOT NULL;

        ALTER TABLE users DROP CONSTRAINT IF EXISTS users_pkey;
        ALTER TABLE users DROP COLUMN id;
        ALTER TABLE users RENAME COLUMN id_new TO id;
        ALTER TABLE users ADD PRIMARY KEY (id);
    END IF;
END $$;
