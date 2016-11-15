ALTER TABLE BESTELLUNGEN engine=InnoDB;

CREATE TABLE STOCK (
	ID VARCHAR(25) NOT NULL,
	LASTUPDATE BIGINT DEFAULT NULL,
	DELETED CHAR(1) DEFAULT '0',
	PRIORITY INTEGER DEFAULT 255,
	CODE CHAR(3), 
	DESCRIPTION VARCHAR(255), 
	LOCATION VARCHAR(255), 
	OWNER VARCHAR(25), 
	RESPONSIBLE VARCHAR(25),
	DRIVER_UUID VARCHAR(64), 
	DRIVER_CONFIG TEXT,
	PRIMARY KEY (ID)
);

INSERT INTO STOCK (ID, CODE, PRIORITY) VALUES ('STD', 'STD', '0');

CREATE TABLE STOCK_ENTRY (
	ID VARCHAR(25) NOT NULL,
	LASTUPDATE BIGINT DEFAULT NULL,
	DELETED CHAR(1) DEFAULT '0',
	STOCK VARCHAR(25) NOT NULL,
	ARTICLE_TYPE VARCHAR(255) NOT NULL,
	ARTICLE_ID VARCHAR(25) NOT NULL, 
	MIN INTEGER DEFAULT 0, 
	CURRENT INTEGER DEFAULT 0, 
	MAX INTEGER DEFAULT 0, 
	FRACTIONUNITS INTEGER DEFAULT 0,
	PROVIDER VARCHAR(25), 
	PRIMARY KEY (ID)
);

CREATE INDEX STOCK_ENTRY_STOCK0 ON STOCK_ENTRY(STOCK);
CREATE INDEX STOCK_ENTRY_TYPE0 ON STOCK_ENTRY(ARTICLE_TYPE);
CREATE INDEX STOCK_ENTRY_ID0 ON STOCK_ENTRY(ARTICLE_ID);

ALTER TABLE STOCK_ENTRY 
	ADD CONSTRAINT FK_STOCK_ENTRY_STOCK_ID FOREIGN KEY (STOCK) REFERENCES STOCK (ID);
	
CREATE TABLE IF NOT EXISTS BESTELLUNGEN (
	ID       	VARCHAR(80) NOT NULL,
 	LASTUPDATE 	BIGINT DEFAULT NULL,
  	DELETED 	CHAR(1) DEFAULT '0',
	DATUM  	    CHAR(8),
	CONTENTS 	BLOB,
	PRIMARY KEY (ID)
);

CREATE TABLE BESTELLUNG_ENTRY (
	ID VARCHAR(25) NOT NULL,
	LASTUPDATE BIGINT DEFAULT NULL,
	DELETED CHAR(1) DEFAULT '0',
	BESTELLUNG VARCHAR(80) NOT NULL, 
	STOCK VARCHAR(25), 
	ARTICLE_TYPE VARCHAR(255) NOT NULL,
	ARTICLE_ID VARCHAR(25) NOT NULL,
	COUNT INTEGER DEFAULT '0',
	PROVIDER VARCHAR(25),
	STATE INTEGER DEFAULT '0',
	PRIMARY KEY (ID)
);

ALTER TABLE BESTELLUNG_ENTRY 
	ADD CONSTRAINT FK_BESTELLUNG_ENTRY_BESTELLUNG_ID FOREIGN KEY (BESTELLUNG) REFERENCES BESTELLUNGEN (ID);