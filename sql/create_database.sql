CREATE SEQUENCE seq_users;

CREATE SEQUENCE seq_user_comment;

CREATE SEQUENCE seq_user_content;

CREATE TABLE users
(
  id integer NOT NULL DEFAULT nextval('seq_users'),
  login character varying(100) NOT NULL,
  name character varying(100) NOT NULL,
  password_hash character(32) NOT NULL,
  created timestamp without time zone NOT NULL,
  CONSTRAINT pk_users PRIMARY KEY (id)
);

CREATE TABLE user_comment
(
  id integer NOT NULL DEFAULT nextval('seq_user_comment'),
  user_id integer NOT NULL,
  content_id integer NOT NULL,
  comment text NOT NULL,
  created timestamp without time zone NOT NULL,
  CONSTRAINT pk_user_comment PRIMARY KEY (id),
  CONSTRAINT fk_user_content_comment__user FOREIGN KEY (user_id)
      REFERENCES users (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE user_content
(
  id integer NOT NULL DEFAULT nextval('seq_user_content'),
  user_id integer NOT NULL,
  content_id integer NOT NULL,
  favorite boolean NOT NULL,
  created timestamp without time zone NOT NULL,
  content_type character varying(1000) NOT NULL,
  CONSTRAINT pk_favorites PRIMARY KEY (id),
  CONSTRAINT fk_user_content__user FOREIGN KEY (user_id)
      REFERENCES users (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

