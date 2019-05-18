CREATE TABLE CONTENTV2(
    ID VARCHAR(255) NOT NULL,
    OWNER_ID VARCHAR(255),
    TOPIC_ID VARCHAR(255),
    CONTENTS_ORDER INTEGER
);
ALTER TABLE CONTENTV2 ADD CONSTRAINT CONSTRAINT_8 PRIMARY KEY(ID);

CREATE TABLE IMAGEV2(
    ID VARCHAR(255) NOT NULL
);
ALTER TABLE IMAGEV2 ADD CONSTRAINT CONSTRAINT_9C PRIMARY KEY(ID);

CREATE TABLE NODEV2(
    ID VARCHAR(255) NOT NULL,
    NAME VARCHAR(255) NOT NULL,
    OWNER_ID VARCHAR(255),
    PARENT_NODE_ID VARCHAR(255),
    CHILD_NODES_ORDER INTEGER
);
ALTER TABLE NODEV2 ADD CONSTRAINT CONSTRAINT_89 PRIMARY KEY(ID);

CREATE TABLE PARAGRAPHV2(
    ID VARCHAR(255) NOT NULL
);
ALTER TABLE PARAGRAPHV2 ADD CONSTRAINT CONSTRAINT_A PRIMARY KEY(ID);

CREATE TABLE TEXTV2(
    TEXT VARCHAR(255),
    ID VARCHAR(255) NOT NULL
);
ALTER TABLE TEXTV2 ADD CONSTRAINT CONSTRAINT_93 PRIMARY KEY(ID);

CREATE TABLE TOPICV2(
    ID VARCHAR(255) NOT NULL
);
ALTER TABLE TOPICV2 ADD CONSTRAINT CONSTRAINT_E6 PRIMARY KEY(ID);

ALTER TABLE NODEV2 ADD CONSTRAINT FKNPB4XAOVXLBU45S3QWPJVA4I4 FOREIGN KEY(PARENT_NODE_ID) REFERENCES NODEV2(ID) NOCHECK;
ALTER TABLE IMAGEV2 ADD CONSTRAINT FKH4FYDI2T3N6CB08O1AS6H7YRK FOREIGN KEY(ID) REFERENCES CONTENTV2(ID) NOCHECK;
ALTER TABLE NODEV2 ADD CONSTRAINT FK3LM9DEEDCQEJL9FPDJDX3B2GT FOREIGN KEY(OWNER_ID) REFERENCES USERV2(ID) NOCHECK;
ALTER TABLE CONTENTV2 ADD CONSTRAINT FKMVUUAVO7FQTDAX300MDT9ILBM FOREIGN KEY(OWNER_ID) REFERENCES USERV2(ID) NOCHECK;
ALTER TABLE PARAGRAPHV2 ADD CONSTRAINT FK7JI5H81B5HHKWG58BYQP2QVS9 FOREIGN KEY(ID) REFERENCES NODEV2(ID) NOCHECK;
ALTER TABLE TEXTV2 ADD CONSTRAINT FK79PVJ2YN49ARICIEM04LNSBRC FOREIGN KEY(ID) REFERENCES CONTENTV2(ID) NOCHECK;
ALTER TABLE TOPICV2 ADD CONSTRAINT FKRYR06E1QIC06PDIO5OWB3GJ0R FOREIGN KEY(ID) REFERENCES NODEV2(ID) NOCHECK;
ALTER TABLE CONTENTV2 ADD CONSTRAINT FK4RF7S8M2UDMK9AWY6DYABO99I FOREIGN KEY(TOPIC_ID) REFERENCES TOPICV2(ID) NOCHECK;
