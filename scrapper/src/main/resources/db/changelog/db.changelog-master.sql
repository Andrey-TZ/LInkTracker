--liquibase formatted sql

-- changeset andrey:1-init-schema
CREATE TABLE CHAT(
    id BIGSERIAL PRIMARY KEY,
    chatId BIGSERIAL NOT NULL UNIQUE
);

CREATE TABLE LINK(
    id BIGSERIAL PRIMARY KEY,
    url TEXT NOT NULL,
    filter TEXT,
    creation_date TIMESTAMP NOT NULL,
    userId BIGINT,
    CONSTRAINT fk_user FOREIGN KEY (userId) REFERENCES chat(id)
);

CREATE TABLE TAG(
    id BIGSERIAL PRIMARY KEY,
    tag TEXT NOT NULL
);

CREATE TABLE LINK_TAG (
    link BIGSERIAL NOT NULL REFERENCES link(id),
    tag BIGSERIAL NOT NULL REFERENCES tag(id)
);


