CREATE SCHEMA etb;
COMMENT ON SCHEMA etb IS 'Element toolbox';

-- ###====--------------------====###
-- ##====---- "etb"."mail" ----====##
-- ###====--------------------====###

CREATE SEQUENCE etb.seq_mail;
-- Table: mail

-- DROP TABLE mail;

CREATE TABLE mail
(
  id bigint NOT NULL DEFAULT nextval('seq_mail'::regclass), -- PK: int sequence
  sent_from text NOT NULL, -- Mail source
  subject text NOT NULL, -- E-mail subject line
  text_body text, -- E-mail body - plain text
  html_body text, -- E-mail body - html text (optional)
  created_at timestamptz NOT NULL DEFAULT now(), -- When entry was added to the list
  updated_at timestamptz NOT NULL DEFAULT now(), -- When entry was added to the list
  CONSTRAINT pk_etb_mail_id PRIMARY KEY (id),
  CONSTRAINT ck_etb_mail_id CHECK (id > 0)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE mail OWNER TO etb;
COMMENT ON TABLE mail IS 'Mail queue (no spam)';
COMMENT ON COLUMN mail.id IS 'PK: int sequence';
COMMENT ON COLUMN mail.sent_from IS 'Mail source';
COMMENT ON COLUMN mail.subject IS 'E-mail subject line';
COMMENT ON COLUMN mail.text_body IS 'E-mail body - plain text';
COMMENT ON COLUMN mail.html_body IS 'E-mail body - html text (optional)';
COMMENT ON COLUMN mail.created_at IS 'When entry was added to the list';
COMMENT ON COLUMN mail.updated_at IS 'When entry was added to the list';


-- ###====-------------------------====###
-- ##====---- "etb"."file_type" ----====##
-- ###====-------------------------====###


CREATE SEQUENCE etb.seq_file_type;
-- Table: file_type

-- DROP TABLE file_type;

CREATE TABLE file_type
(
  id bigint NOT NULL DEFAULT nextval('seq_file_type'::regclass), -- PK: seq
  ext text NOT NULL, -- File extension; unique
  alt text, -- Alternative extension
  "type" text NOT NULL, -- MIME type
  mime text NOT NULL, -- Streaming mime/type
  created_at timestamptz NOT NULL DEFAULT now(), -- Row created at
  updated_at timestamptz NOT NULL DEFAULT now(), -- Row updated at
  CONSTRAINT pk_etb_file_type_id PRIMARY KEY (id),
  CONSTRAINT uq_etb_file_type_alt UNIQUE (alt),
  CONSTRAINT uq_etb_file_type_ext UNIQUE (ext),
  CONSTRAINT uq_etb_file_type_mime UNIQUE (mime),
  CONSTRAINT ck_etb_file_type_alt CHECK (alt ~ '^[\da-z]+$'::text),
  CONSTRAINT ck_etb_file_type_ext CHECK (ext ~ '^[\da-z]+$'::text),
  CONSTRAINT ck_etb_file_type_mime CHECK (mime ~ '^(application|image|text)/[-\da-z.+]+$'::text)
)
WITH (
  FILLFACTOR=100,
  OIDS=FALSE
);
ALTER TABLE file_type OWNER TO etb;
COMMENT ON TABLE file_type IS 'File mime types';
COMMENT ON COLUMN file_type.id IS 'PK: seq';
COMMENT ON COLUMN file_type.ext IS 'File extension; unique';
COMMENT ON COLUMN file_type.alt IS 'Alternative extension';
COMMENT ON COLUMN file_type."type" IS 'MIME type';
COMMENT ON COLUMN file_type.mime IS 'Streaming mime/type';
COMMENT ON COLUMN file_type.created_at IS 'Row created at';
COMMENT ON COLUMN file_type.updated_at IS 'Row updated at';


-- ###====---------------------------====###
-- ##====---- "etb"."attachment" ----====##
-- ###====---------------------------====###

CREATE SEQUENCE etb.seq_attachment;
-- Table: attachment

-- DROP TABLE attachment;

CREATE TABLE attachment
(
  id bigint NOT NULL DEFAULT nextval('seq_attachment'::regclass), -- Primary_key
  file_type_ext text NOT NULL, -- File extension
  filename text NOT NULL, -- File name.ext
  size integer NOT NULL, -- Size in bytes
  body bytea NOT NULL, -- File encoded in base64
  hash bytea NOT NULL, -- File's body hash
  mod_time timestamptz NOT NULL, -- File's last modification time
  uploaded_at timestamptz NOT NULL DEFAULT now(), -- Date of file detail's insertion
  created_at timestamptz NOT NULL DEFAULT now(), -- Date of row creation
  CONSTRAINT pk_etb_attachment_id PRIMARY KEY (id),
  CONSTRAINT fk_etb_attachment_file_type_ext_2_etb_file_type_ext FOREIGN KEY (file_type_ext)
      REFERENCES file_type (ext) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE NO ACTION,
  CONSTRAINT ck_etb_attachment_body CHECK (length(body) = size),
  CONSTRAINT ck_etb_attachment_hash CHECK (decode(md5(body), 'hex'::text) = hash),
  CONSTRAINT ck_etb_attachment_id CHECK (id > 0),
  CONSTRAINT ck_etb_attachment_size CHECK (size > 0)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE attachment OWNER TO etb;
COMMENT ON TABLE attachment IS 'Attachments details';
COMMENT ON COLUMN attachment.id IS 'Primary_key';
COMMENT ON COLUMN attachment.file_type_ext IS 'File extension';
COMMENT ON COLUMN attachment.filename IS 'File name.ext';
COMMENT ON COLUMN attachment.size IS 'Size in bytes';
COMMENT ON COLUMN attachment.body IS 'File encoded in base64';
ALTER TABLE attachment ALTER COLUMN body SET STORAGE EXTERNAL;
COMMENT ON COLUMN attachment.hash IS 'File''s body hash';
COMMENT ON COLUMN attachment.mod_time IS 'File''s last modification time';
COMMENT ON COLUMN attachment.uploaded_at IS 'Date of file detail''s insertion';
COMMENT ON COLUMN attachment.created_at IS 'Date of row creation';


-- Index: idx_etb_attachment_ext

-- DROP INDEX idx_etb_attachment_ext;

CREATE INDEX idx_etb_attachment_ext
  ON attachment
  USING btree
  (file_type_ext);

-- ###====--------------------------------====###
-- ##====---- "etb"."mail2attachments" ----====##
-- ###====--------------------------------====###


CREATE SEQUENCE etb.seq_mail2attachments;
-- Table: mail2attachments

-- DROP TABLE mail2attachments;

CREATE TABLE mail2attachments
(
  id bigint NOT NULL DEFAULT nextval('seq_mail2attachments'::regclass), -- PK: row unique identifier
  mail_id bigint NOT NULL, -- FK: etb.mail(id)
  attachment_id bigint NOT NULL, -- FK: etb.attachment(id)
  created_at timestamptz NOT NULL DEFAULT now(), -- Row updated at
  updated_at timestamptz NOT NULL DEFAULT now(),
  CONSTRAINT pk_etb_mail2attachments_id PRIMARY KEY (id),
  CONSTRAINT fk_etb_mail2attachments_attachment_id_2_etb_attachment_id FOREIGN KEY (attachment_id)
      REFERENCES attachment (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_etb_mail2attachments_mail_id_2_etb_mail_id FOREIGN KEY (mail_id)
      REFERENCES mail (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT uq_etb_mail2attachments_mail_id_attachment_id UNIQUE (mail_id, attachment_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE mail2attachments OWNER TO etb;
COMMENT ON TABLE mail2attachments IS 'Table for mail attachments';
COMMENT ON COLUMN mail2attachments.id IS 'PK: row unique identifier';
COMMENT ON COLUMN mail2attachments.mail_id IS 'FK: etb.mail(id)';
COMMENT ON COLUMN mail2attachments.attachment_id IS 'FK: etb.attachment(id)';
COMMENT ON COLUMN mail2attachments.created_at IS 'Row updated at';

-- ###====------------------------------====###
-- ##====---- "etb"."mail2addresses" ----====##
-- ###====------------------------------====###


CREATE SEQUENCE etb.seq_mail2addresses;
-- Table: mail2addresses

-- DROP TABLE mail2addresses;

CREATE TABLE mail2addresses
(
  id bigint NOT NULL DEFAULT nextval('seq_mail2addresses'::regclass), -- PK: row unique identifier
  mail_id bigint NOT NULL, -- FK: into etb.mail(id)
  field_type text NOT NULL, -- Recipient type
  address text NOT NULL, -- Recipient email address
  queued_at timestamptz NOT NULL, -- Time when queued for sending
  sent_at timestamptz, -- Time when sent to recipient
  bounced integer NOT NULL, -- Number of e-mail boomerangs (manual increment)
  created_at timestamptz NOT NULL DEFAULT now(), -- Time fo row creation
  updated_at timestamptz NOT NULL DEFAULT now(), -- Time of row update
  CONSTRAINT pk_etb_mail2addresses_id PRIMARY KEY (id),
  CONSTRAINT fk_etb_mail2addresses_mail_id_2_etb_mail_id FOREIGN KEY (mail_id)
      REFERENCES mail (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT ck_etb_mail_bounced CHECK (bounced > 0)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE mail2addresses OWNER TO etb;
COMMENT ON TABLE mail2addresses IS 'Table for recipient addresses';
COMMENT ON COLUMN mail2addresses.id IS 'PK: row unique identifier';
COMMENT ON COLUMN mail2addresses.mail_id IS 'FK: into etb.mail(id)';
COMMENT ON COLUMN mail2addresses.field_type IS 'Recipient type';
COMMENT ON COLUMN mail2addresses.address IS 'Recipient email address';
COMMENT ON COLUMN mail2addresses.queued_at IS 'Time when queued for sending';
COMMENT ON COLUMN mail2addresses.sent_at IS 'Time when sent to recipient';
COMMENT ON COLUMN mail2addresses.bounced IS 'Number of e-mail boomerangs (manual increment)';
COMMENT ON COLUMN mail2addresses.created_at IS 'Time fo row creation';
COMMENT ON COLUMN mail2addresses.updated_at IS 'Time of row update';