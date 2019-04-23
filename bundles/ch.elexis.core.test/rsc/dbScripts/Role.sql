INSERT INTO ROLE (ID, ISSYSTEMROLE) VALUES ('user', '1');
INSERT INTO ROLE (ID, ISSYSTEMROLE) VALUES ('user_external', '1');
INSERT INTO ROLE (ID, ISSYSTEMROLE) VALUES ('executive_doctor', '1');
INSERT INTO ROLE (ID, ISSYSTEMROLE) VALUES ('doctor', '1');
INSERT INTO ROLE (ID, ISSYSTEMROLE) VALUES ('assistant', '1');
INSERT INTO ROLE (ID, ISSYSTEMROLE) VALUES ('patient', '1');

INSERT INTO RIGHT_ (ID, NAME, PARENTID) VALUES ('root', 'root', '');

INSERT INTO USER_ROLE_JOINT (ID, USER_ID) VALUES ( 'user', 'user');
INSERT INTO USER_ROLE_JOINT (ID, USER_ID) VALUES ( 'doctor', 'user');