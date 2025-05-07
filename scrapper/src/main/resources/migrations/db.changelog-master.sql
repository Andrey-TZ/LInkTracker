--liquibase formatted sql

-- changeset andrey:1-init-schema
CREATE TABLE CHAT(
    id BIGSERIAL PRIMARY KEY,
    chat_id BIGINT NOT NULL UNIQUE
);

CREATE TABLE LINK(
    id BIGSERIAL PRIMARY KEY,
    url TEXT NOT NULL,
    filter TEXT,
    creation_date TIMESTAMP NOT NULL,
    user_id BIGINT,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES chat(id)
);

CREATE TABLE TAG(
    id BIGSERIAL PRIMARY KEY,
    tag TEXT NOT NULL
);

CREATE TABLE LINK_TAG (
    link BIGINT NOT NULL REFERENCES link(id),
    tag BIGINT NOT NULL REFERENCES tag(id)
);


