ALTER TABLE CATGENOME.LINEAGE_TREE_NODE ADD COLUMN PROJECT_ID BIGINT;
ALTER TABLE CATGENOME.LINEAGE_TREE_NODE ADD CONSTRAINT node_project_id_fkey FOREIGN KEY (PROJECT_ID) REFERENCES CATGENOME.PROJECT(PROJECT_ID);
