-- Sistema de Moeda Estudantil - Lab03S02
-- Carga inicial: instituições participantes (RN02) e professores enviados pelas instituições (RN03).
-- Senha inicial de todos os professores: professor123 (armazenada como hash BCrypt).

INSERT INTO instituicao_ensino (nome) VALUES
    ('Pontifícia Universidade Católica de Minas Gerais (PUC Minas)'),
    ('Universidade Federal de Minas Gerais (UFMG)'),
    ('Centro Federal de Educação Tecnológica de Minas Gerais (CEFET-MG)');

-- Cada professor recebe uma conta própria (saldo 0 até o primeiro crédito semestral - RN04).

INSERT INTO conta (saldo) VALUES (0);
INSERT INTO usuario (tipo, nome, email, senha, ativo) VALUES
    ('PROFESSOR', 'Ana Paula Souza', 'ana.souza@pucminas.br',
     '$2a$10$dkE/AB2pd8xeVyMl/1qDBOm23xLxl6nLMamOyxCbdsM9kt4zgNlOy', TRUE);
INSERT INTO professor (id, cpf, departamento, instituicao_id, conta_id)
SELECT u.id, '52998224725', 'Departamento de Engenharia de Software', i.id, (SELECT MAX(c.id) FROM conta c)
FROM usuario u, instituicao_ensino i
WHERE u.email = 'ana.souza@pucminas.br' AND i.nome LIKE '%(PUC Minas)';

INSERT INTO conta (saldo) VALUES (0);
INSERT INTO usuario (tipo, nome, email, senha, ativo) VALUES
    ('PROFESSOR', 'Carlos Eduardo Lima', 'carlos.lima@pucminas.br',
     '$2a$10$dkE/AB2pd8xeVyMl/1qDBOm23xLxl6nLMamOyxCbdsM9kt4zgNlOy', TRUE);
INSERT INTO professor (id, cpf, departamento, instituicao_id, conta_id)
SELECT u.id, '39053344705', 'Departamento de Ciência da Computação', i.id, (SELECT MAX(c.id) FROM conta c)
FROM usuario u, instituicao_ensino i
WHERE u.email = 'carlos.lima@pucminas.br' AND i.nome LIKE '%(PUC Minas)';

INSERT INTO conta (saldo) VALUES (0);
INSERT INTO usuario (tipo, nome, email, senha, ativo) VALUES
    ('PROFESSOR', 'Beatriz Rocha', 'beatriz.rocha@ufmg.br',
     '$2a$10$dkE/AB2pd8xeVyMl/1qDBOm23xLxl6nLMamOyxCbdsM9kt4zgNlOy', TRUE);
INSERT INTO professor (id, cpf, departamento, instituicao_id, conta_id)
SELECT u.id, '27461803590', 'Departamento de Ciência da Computação', i.id, (SELECT MAX(c.id) FROM conta c)
FROM usuario u, instituicao_ensino i
WHERE u.email = 'beatriz.rocha@ufmg.br' AND i.nome LIKE '%(UFMG)';

INSERT INTO conta (saldo) VALUES (0);
INSERT INTO usuario (tipo, nome, email, senha, ativo) VALUES
    ('PROFESSOR', 'Marcos Vinícius Teixeira', 'marcos.teixeira@cefetmg.br',
     '$2a$10$dkE/AB2pd8xeVyMl/1qDBOm23xLxl6nLMamOyxCbdsM9kt4zgNlOy', TRUE);
INSERT INTO professor (id, cpf, departamento, instituicao_id, conta_id)
SELECT u.id, '83610452951', 'Departamento de Computação', i.id, (SELECT MAX(c.id) FROM conta c)
FROM usuario u, instituicao_ensino i
WHERE u.email = 'marcos.teixeira@cefetmg.br' AND i.nome LIKE '%(CEFET-MG)';
