-- Sistema de Moeda Estudantil - Lab04S02
-- Fotos das vantagens gravadas no banco, e não mais em disco: na nuvem (Render), o disco do servidor é apagado
-- a cada reinício ou nova implantação, e o banco é o único armazenamento que persiste.
-- A vantagem continua guardando apenas a URL da foto (foto_url = /api/fotos/{arquivo}).
-- BYTEA (equivalente ao MEDIUMBLOB do MySQL) comporta até 1 GB; a API aceita fotos de até 5 MB.

CREATE TABLE foto (
    arquivo  VARCHAR(45) NOT NULL,
    conteudo BYTEA       NOT NULL,
    CONSTRAINT pk_foto PRIMARY KEY (arquivo)
);
