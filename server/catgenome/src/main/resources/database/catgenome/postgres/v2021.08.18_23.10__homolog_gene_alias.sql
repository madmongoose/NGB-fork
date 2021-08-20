CREATE SEQUENCE IF NOT EXISTS CATGENOME.S_HOMOLOG_GENE_ALIAS START WITH 1 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS CATGENOME.HOMOLOG_GENE_ALIAS (
    ID BIGINT NOT NULL PRIMARY KEY,
    GENE_ID BIGINT NOT NULL,
    NAME VARCHAR(500) NOT NULL,
    CONSTRAINT alias_gene_id_fkey FOREIGN KEY (GENE_ID) REFERENCES CATGENOME.HOMOLOG_GENE_DESC(ID)
);
