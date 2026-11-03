-- Sistema de Moeda Estudantil - Lab03S03
-- Bloqueio otimista do saldo: o Hibernate incrementa "versao" a cada alteração da conta e rejeita a
-- gravação de quem leu um valor antigo, evitando que dois envios ou resgates simultâneos gastem o mesmo saldo.

ALTER TABLE conta ADD COLUMN versao BIGINT NOT NULL DEFAULT 0;
