ALTER TABLE CATGENOME.TASK ADD COLUMN database_id BIGINT;
ALTER TABLE CATGENOME.TASK ADD CONSTRAINT BLAST_DATABASE_FKEY FOREIGN KEY (database_id) REFERENCES CATGENOME.BLAST_DATABASE(DATABASE_ID) ON DELETE SET NULL;
UPDATE CATGENOME.TASK t SET database_id = (SELECT database_id FROM CATGENOME.BLAST_DATABASE d WHERE d.path = t.database);
ALTER TABLE CATGENOME.TASK DROP COLUMN DATABASE;