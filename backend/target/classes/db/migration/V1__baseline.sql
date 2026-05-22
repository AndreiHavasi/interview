create table topic (
    id              bigserial primary key,
    slug            varchar(255) not null unique,
    parent_id       bigint references topic(id) on delete cascade,
    taxonomy_path   varchar(512) not null,
    title           varchar(255) not null,
    difficulty      smallint,
    created_at      timestamptz not null default now(),
    updated_at      timestamptz not null default now()
);

create index idx_topic_parent on topic(parent_id);
create index idx_topic_taxonomy_path on topic(taxonomy_path);

create table theory_entry (
    id              bigserial primary key,
    topic_id        bigint not null references topic(id) on delete cascade,
    slug            varchar(255) not null unique,
    difficulty      smallint,
    sources_json    jsonb not null default '[]'::jsonb,
    related_json    jsonb not null default '[]'::jsonb,
    ro_companies    jsonb not null default '[]'::jsonb,
    created_at      timestamptz not null default now(),
    updated_at      timestamptz not null default now()
);

create index idx_theory_entry_topic on theory_entry(topic_id);

create table theory_entry_body (
    id                bigserial primary key,
    theory_entry_id   bigint not null references theory_entry(id) on delete cascade,
    locale            varchar(2) not null,
    title             varchar(255) not null,
    body_md           text not null,
    updated_at        timestamptz not null default now(),
    constraint uq_entry_locale unique (theory_entry_id, locale),
    constraint ck_locale check (locale in ('en','ro'))
);

create table tag (
    id      bigserial primary key,
    name    varchar(128) not null unique
);

create table topic_tag (
    topic_id    bigint not null references topic(id) on delete cascade,
    tag_id      bigint not null references tag(id) on delete cascade,
    primary key (topic_id, tag_id)
);

create table theory_entry_tag (
    theory_entry_id bigint not null references theory_entry(id) on delete cascade,
    tag_id          bigint not null references tag(id) on delete cascade,
    primary key (theory_entry_id, tag_id)
);
