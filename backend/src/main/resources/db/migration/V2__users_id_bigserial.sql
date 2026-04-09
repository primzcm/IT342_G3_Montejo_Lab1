DO $$
BEGIN
    ALTER TABLE users ADD COLUMN IF NOT EXISTS firstname VARCHAR(100);
    ALTER TABLE users ADD COLUMN IF NOT EXISTS lastname VARCHAR(100);
    ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(20);
    ALTER TABLE users ALTER COLUMN role SET DEFAULT 'USER';

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'users'
          AND column_name = 'username'
    ) THEN
        UPDATE users
        SET firstname = COALESCE(firstname, username)
        WHERE firstname IS NULL;
    END IF;

    UPDATE users
    SET lastname = COALESCE(lastname, '')
    WHERE lastname IS NULL;

    UPDATE users
    SET role = COALESCE(role, 'USER')
    WHERE role IS NULL;

    ALTER TABLE users ALTER COLUMN firstname SET NOT NULL;
    ALTER TABLE users ALTER COLUMN lastname SET NOT NULL;
    ALTER TABLE users ALTER COLUMN role SET NOT NULL;

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
            SELECT id, row_number() OVER (ORDER BY created_at, email) AS rn
            FROM users
        )
        UPDATE users u
        SET id_new = r.rn
        FROM ranked r
        WHERE u.id = r.id;

        PERFORM setval(
            'users_id_seq',
            GREATEST((SELECT COALESCE(MAX(id_new), 0) FROM users), 1),
            true
        );

        ALTER TABLE users ALTER COLUMN id_new SET DEFAULT nextval('users_id_seq');
        ALTER TABLE users ALTER COLUMN id_new SET NOT NULL;

        ALTER TABLE users DROP CONSTRAINT IF EXISTS users_pkey;
        ALTER TABLE users DROP COLUMN id;
        ALTER TABLE users RENAME COLUMN id_new TO id;
        ALTER TABLE users ADD PRIMARY KEY (id);
    END IF;

    CREATE TABLE IF NOT EXISTS refresh_tokens (
        id BIGSERIAL PRIMARY KEY,
        user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
        token VARCHAR(255) NOT NULL,
        expiry_date TIMESTAMPTZ NOT NULL
    );

    CREATE UNIQUE INDEX IF NOT EXISTS uk_refresh_tokens_token
        ON refresh_tokens (token);
END $$;
