# --- !Ups
CREATE TABLE EVENT (
  EVENT_ID bigint(20) NOT NULL AUTO_INCREMENT,
  EVENT_NAME text NOT NULL,
  EVENT_DATE date NOT NULL,
  IS_PRIVATE boolean,
  PRIMARY KEY (EVENT_ID)
);

CREATE TABLE ACCOUNT (
  ACCOUNT_ID bigint(20) NOT NULL AUTO_INCREMENT,
  ACCOUNT text NOT NULL,
  PRIMARY KEY (ACCOUNT_ID)
);

CREATE TABLE EVENT_MEMBER (
  EVENT_ID bigint(20) NOT NULL,
  ACCOUNT_ID bigint(20) NOT NULL,
  AUTHORITY TEXT NOT NULL,
  FOREIGN KEY (EVENT_ID) REFERENCES EVENT(EVENT_ID),
  FOREIGN KEY (ACCOUNT_ID) REFERENCES ACCOUNT(ACCOUNT_ID)
);

INSERT INTO EVENT (EVENT_NAME, EVENT_DATE, IS_PRIVATE) VALUES ('test', '2014-04-29', false);
INSERT INTO ACCOUNT (ACCOUNT) VALUES ('testuser');
INSERT INTO EVENT_MEMBER (EVENT_ID, ACCOUNT_ID, AUTHORITY) VALUES ('1', '1', 'Normal');

# --- !Downs
 
DROP TABLE EVENT;
DROP TABLE ACCOUNT;
DROP TABLE EVENT_MEMBER;
