-- Create the user if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'casse-local') THEN
CREATE ROLE casse-local WITH LOGIN PASSWORD 'makemusic';
END IF;
END
$$;

-- Create the database if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'casse_db') THEN
        CREATE DATABASE casse_db OWNER casse-local;
END IF;
END
$$;
