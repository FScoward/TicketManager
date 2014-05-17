# --- !Ups
CREATE TABLE ACCOUNT (
  ACCOUNT_ID bigint(20) NOT NULL AUTO_INCREMENT,
  ACCOUNT varchar(255) NOT NULL,
  PRIMARY KEY (ACCOUNT)
);

CREATE TABLE EVENT (
  EVENT_ID varchar(255) NOT NULL,
  EVENT_NAME text NOT NULL,
  EVENT_LOCATION varchar(255) NOT NULL,
  EVENT_DATE date NOT NULL,
  OWNER varchar(255) NOT NULL,
  IS_PRIVATE boolean,
  FOREIGN KEY (OWNER) REFERENCES ACCOUNT(ACCOUNT)
);

CREATE TABLE EVENT_MEMBER (
  EVENT_ID varchar(255) NOT NULL,
  ACCOUNT varchar(255) NOT NULL,
  AUTHORITY TEXT NOT NULL,
  ATTEND_STATUS char(1), -- 1 参加 2 不参加 3 迷い中 0 未入力
  UPDATE_DATE timestamp,
  FOREIGN KEY (EVENT_ID) REFERENCES EVENT(EVENT_ID) ON DELETE CASCADE,
  FOREIGN KEY (ACCOUNT) REFERENCES ACCOUNT(ACCOUNT)
);

CREATE TABLE TICKET (
  TICKET_ID bigint(20) NOT NULL AUTO_INCREMENT,
  EVENT_ID varchar(255) NOT NULL,
  NUMBER int(2) NOT NULL,
  TICKET_HOLDER varchar(255),
  STATUS int(1), -- 0 応募予定 1 応募済 2 当選 3 落選
  PRIMARY KEY(TICKET_ID),
  UNIQUE KEY(EVENT_ID, TICKET_HOLDER),
  FOREIGN KEY(EVENT_ID) REFERENCES EVENT(EVENT_ID) ON DELETE CASCADE,
  FOREIGN KEY(TICKET_HOLDER) REFERENCES ACCOUNT(ACCOUNT)
);

CREATE TABLE EVENT_ADMIN (
    EVENT_ID varchar(255) NOT NULL,
    ADMIN_ACCOUNT varchar(255) NOT NULL,
    FOREIGN KEY (ADMIN_ACCOUNT) REFERENCES ACCOUNT(ACCOUNT)
);

--INSERT INTO EVENT (EVENT_NAME, EVENT_DATE, IS_PRIVATE) VALUES ('test', '2014-04-29', false);
--INSERT INTO ACCOUNT (ACCOUNT) VALUES ('testuser');
--INSERT INTO EVENT_MEMBER (EVENT_ID, ACCOUNT_ID, AUTHORITY) VALUES ('1', '1', 'Normal');

# --- !Downs
 
DROP TABLE EVENT;
DROP TABLE ACCOUNT;
DROP TABLE EVENT_MEMBER;
DROP TABLE TICKET;
DROP TABLE EVENT_ADMIN;
