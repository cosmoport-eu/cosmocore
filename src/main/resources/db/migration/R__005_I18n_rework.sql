drop trigger if exists EVENT_TYPE_AD;
drop trigger if exists EVENT_TYPE_CATEGORY_AD;

alter table event_state add column i18n_code varchar(255);
update event_state set i18n_code = (select tag from i18n where id = event_state.i18n_state)
where exists (select * from i18n where i18n.id = event_state.i18n_state);

alter table event_status add column i18n_code varchar(255);
update event_status set i18n_code = (select tag from i18n where id = event_status.i18n_status)
where exists (select * from i18n where i18n.id = event_status.i18n_status);


alter table event_type add column i18n_name_code varchar(255);
alter table event_type add column i18n_desc_code varchar(255);
update event_type set i18n_name_code = (select tag from i18n where id = event_type.i18n_event_type_name)
where exists (select * from i18n where i18n.id = event_type.i18n_event_type_name);
update event_type set i18n_desc_code = (select tag from i18n where id = event_type.i18n_event_type_description)
where exists (select * from i18n where i18n.id = event_type.i18n_event_type_description);

alter table event_type_category add column i18n_code varchar(255);
update event_type_category set i18n_code = (select tag from i18n where id = event_type_category.i18n_event_type_category_name)
where exists (select * from i18n where i18n.id = event_type_category.i18n_event_type_category_name);

CREATE TABLE TRANSLATIONS
(
    id        INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    locale_id INTEGER NOT NULL,
    code      TEXT    NOT NULL,
    text      TEXT    NOT NULL,

    CONSTRAINT unique_code_locale UNIQUE (code, locale_id),

    FOREIGN KEY (locale_id) REFERENCES LOCALE (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

insert into translations (locale_id, code, text)
select t.locale_id, i.tag, t.tr_text from TRANSLATION t join i18n i on t.i18n_id = i.id;


