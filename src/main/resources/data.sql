CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS airesponse (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
    request json NOT NULL,
    response_text varchar(2048) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);