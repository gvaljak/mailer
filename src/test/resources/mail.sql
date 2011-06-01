CREATE SCHEMA etb;
COMMENT ON SCHEMA etb IS 'Element toolbox';

CREATE SEQUENCE etb.seq_mail;
CREATE TABLE etb.mail
(
  id bigint DEFAULT nextval('etb.seq_mail') -- PK: int sequence
    CONSTRAINT pk_etb_mail_id PRIMARY KEY
    CONSTRAINT ck_etb_mail_id CHECK (id > 0),
  sent_from text NOT NULL, -- Mail source
  subject text NOT NULL, -- E-mail subject line
  text_body text, -- E-mail body - plain text
  html_body text, -- E-mail body - html text (optional)
  created_at timestamptz NOT NULL DEFAULT now() -- When entry was added to the list
  updated_at timestamptz NOT NULL DEFAULT now() -- When entry was added to the list
)
WITH (
  OIDS=FALSE
);
COMMENT ON TABLE etb.mail IS 'Mail queue (no spam)';
COMMENT ON COLUMN etb.mail.id IS 'PK: int sequence';
COMMENT ON COLUMN etb.mail.sent_from IS 'Mail source';
COMMENT ON COLUMN etb.mail.sent_to IS 'Mail destination';
COMMENT ON COLUMN etb.mail.subject IS 'E-mail subject line';
COMMENT ON COLUMN etb.mail.text_body IS 'E-mail body - plain text';
COMMENT ON COLUMN etb.mail.html_body IS 'E-mail body - html text (optional)';
COMMENT ON COLUMN etb.mail.created_at IS 'When entry was added to the list';
COMMENT ON COLUMN etb.mail.updated_at IS 'When entry was added to the list';


CREATE SEQUENCE etb.seq_attachments;
CREATE TABLE etb.attachments
(
  id bigint NOT NULL DEFAULT nextval('etb.seq_attachments') -- Primary_key
    CONSTRAINT pk_etb_attachments_id PRIMARY KEY
    CONSTRAINT ck_etb_attachments_id CHECK (id > 0),
  file_type_ext text NOT NULL -- File extension
    CONSTRAINT fk_etb_attachments_file_type_ext_2_etb_file_type_ext REFERENCES etb.file_type (ext) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE NO ACTION,
  filename text NOT NULL, -- File name.ext
  size int NOT NULL -- Size in bytes
    CONSTRAINT ck_etb_attachments_size CHECK (size > 0),
  body bytea NOT NULL -- File encoded in base64
    CONSTRAINT ck_etb_attachments_body CHECK (length(body) = size),
  hash bytea NOT NULL -- File's body hash
    CONSTRAINT ck_etb_attachments_hash CHECK (decode(md5(body), 'hex'::text) = hash),
  mod_time timestamptz NOT NULL, -- File's last modification time
  uploaded_at timestamptz NOT NULL DEFAULT now(), -- Date of file detail's insertion
  created_at timestamptz NOT NULL DEFAULT now() -- Date of row creation
)
WITH (
  OIDS=FALSE
);

COMMENT ON TABLE etb.attachments IS 'Attachments details';
COMMENT ON COLUMN etb.attachments.id IS 'Primary_key';
COMMENT ON COLUMN etb.attachments.file_type_ext IS 'File extension';
COMMENT ON COLUMN etb.attachments.filename IS 'File name.ext';
COMMENT ON COLUMN etb.attachments.size IS 'Size in bytes';
COMMENT ON COLUMN etb.attachments.body IS 'File encoded in base64';
COMMENT ON COLUMN etb.attachments.hash IS 'File''s body hash';
COMMENT ON COLUMN etb.attachments.mod_time IS 'File''s last modification time';
COMMENT ON COLUMN etb.attachments.uploaded_at IS 'Date of file detail''s insertion';
COMMENT ON COLUMN etb.attachments.created_at IS 'Date of row creation';
ALTER TABLE etb.attachments ALTER COLUMN body SET STORAGE EXTERNAL;

-- Index: etb.idx_etb_attachments_ext
-- DROP INDEX etb.idx_etb_attachments_ext;

CREATE INDEX idx_etb_attachments_ext
  ON etb.attachments
  USING btree
  (file_type_ext);

--DROP TABLE etb.file_type;
--DROP SEQUENCE etb.seq_file_type;

CREATE SEQUENCE etb.seq_file_type;
CREATE TABLE etb.file_type
(
  id bigint DEFAULT nextval('etb.seq_file_type')
    CONSTRAINT pk_etb_file_type_id PRIMARY KEY,
  ext text NOT NULL -- PK: file extension
    CONSTRAINT uq_etb_file_type_ext UNIQUE
    CONSTRAINT ck_etb_file_type_ext CHECK (ext ~ '^[\da-z]+$'::text),
  alt text -- Alternative extension
    CONSTRAINT uq_etb_file_type_alt UNIQUE
    CONSTRAINT ck_etb_file_type_alt CHECK (alt ~ '^[\da-z]+$'::text),
  "type" text NOT NULL, -- MIME type
  mime text NOT NULL -- Streaming mime/type
    CONSTRAINT uq_etb_file_type_mime UNIQUE
    CONSTRAINT ck_etb_file_type_mime CHECK (mime ~ '^(application|image|text)/[-\da-z.+]+$'::text),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
)
WITH (
  FILLFACTOR=100,
  OIDS=FALSE
);
COMMENT ON TABLE etb.file_type IS 'File mime types';
COMMENT ON COLUMN etb.file_type.id IS 'PK: seq';
COMMENT ON COLUMN etb.file_type.ext IS 'File extension; unique';
COMMENT ON COLUMN etb.file_type.alt IS 'Alternative extension';
COMMENT ON COLUMN etb.file_type."type" IS 'MIME type';
COMMENT ON COLUMN etb.file_type.mime IS 'Streaming mime/type';
COMMENT ON COLUMN etb.file_type.created_at IS 'Row created at';
COMMENT ON COLUMN etb.file_type.updated_at IS 'Row updated at';


-- Index: etb.idx_etb_file_type_type_and_ext
-- DROP INDEX etb.idx_etb_file_type_type_and_ext;

CREATE INDEX idx_etb_file_type_type_and_ext
  ON etb.file_type
  USING btree
  (type, ext);
ALTER TABLE etb.file_type CLUSTER ON idx_etb_file_type_type_and_ext;



CREATE SEQUENCE etb.seq_mail2attachments;
CREATE TABLE etb.mail2attachments(
  id bigint DEFAULT nextval('etb.seq_mail2attachments')
    CONSTRAINT pk_etb_mail2attachments_id PRIMARY KEY,
  mail_id bigint NOT NULL
    CONSTRAINT fk_etb_mail2attachments_mail_id_2_etb_mail_id REFERENCES etb.mail(id),
  attachments_id bigint NOT NULL
    CONSTRAINT fk_etb_mail2attachments_attachments_id_2_etb_attachments_id REFERENCES etb.attachments(id),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  CONSTRAINT uq_etb_mail2attachments_mail_id_attachments_id UNIQUE(mail_id, attachments_id)
);

COMMENT ON TABLE etb.mail2attachments IS 'Table for mail attachments';
COMMENT ON COLUMN etb.mail2attachments.id IS 'PK: row unique identifier';
COMMENT ON COLUMN etb.mail2attachments.mail_id IS 'FK: etb.mail(id)';
COMMENT ON COLUMN etb.mail2attachments.attachments_id IS 'FK: etb.attachments(id)';
COMMENT ON COLUMN etb.mail2attachments.created_at IS 'Row created at';
COMMENT ON COLUMN etb.mail2attachments.created_at IS 'Row updated at';


CREATE SEQUENCE etb.seq_mail2addresses;
CREATE TABLE etb.mail2addresses(
  id bigint DEFAULT nextval('etb.seq_mail2addresses')
    CONSTRAINT pk_etb_mail2addresses_id PRIMARY KEY,
  mail_id bigint NOT NULL
    CONSTRAINT fk_etb_mail2addresses_mail_id_2_etb_mail_id REFERENCES etb.mail(id),
  field_type text NOT NULL,
  address text NOT NULL,
  queued_at timestamptz, -- When entry was last queued for sending
  sent_at timestamptz, -- When e-mail was last successfully sent
  bounced integer -- Number of e-mail boomerangs (manual increment)
    CONSTRAINT ck_etb_mail_bounced CHECK (bounced > 0),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

COMMENT ON TABLE etb.mail2addresses IS 'Table for recipient addresses';
COMMENT ON TABLE etb.mail2addresses.id IS 'PK: row unique identifier';
COMMENT ON TABLE etb.mail2addresses.mail_id IS 'FK: into etb.mail(id)';
COMMENT ON TABLE etb.mail2addresses.field_type IS 'Recipient type';
COMMENT ON TABLE etb.mail2addresses.address IS 'Recipient email address';
COMMENT ON TABLE etb.mail2addresses.queued_at IS 'Time when queued for sending';
COMMENT ON TABLE etb.mail2addresses.sent_at IS 'Time when sent to recipient';
COMMENT ON TABLE etb.mail2addresses.bounced IS 'Number of e-mail boomerangs (manual increment)';
COMMENT ON TABLE etb.mail2addresses.created_at IS 'Time fo row creation';
COMMENT ON TABLE etb.mail2addresses.updated_at IS 'Time of row update';
