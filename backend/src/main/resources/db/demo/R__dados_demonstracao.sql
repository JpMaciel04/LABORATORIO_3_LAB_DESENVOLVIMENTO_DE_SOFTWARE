-- Sistema de Moeda Estudantil - Lab03S03
-- Dados usados na apresentação do protótipo; só são carregados no perfil "demo" (application-demo.properties).
-- Senha de todos os usuários abaixo: demo123 (armazenada como hash BCrypt).
-- Os documentos são apenas matematicamente válidos, para que os formulários de alteração aceitem os registros.

-- ===== Alunos: dois da PUC Minas e um da UFMG, para demonstrar a regra da mesma instituição (RN06) =====

INSERT INTO conta (saldo) VALUES (0);
INSERT INTO usuario (tipo, nome, email, senha, ativo) VALUES
    ('ALUNO', 'Joana Silva', 'joana.silva@aluno.pucminas.br',
     '$2a$10$Rm4wE6ZhT3kxLxlucCe/fedx9NcGlGB6n.gErE/fuzs8vrL//pzsq', TRUE);
INSERT INTO aluno (id, cpf, rg, curso, logradouro, numero, complemento, bairro, cidade, estado, cep, instituicao_id, conta_id)
SELECT u.id, '12345678909', 'MG-12.345.678', 'Engenharia de Software', 'Rua Dom José Gaspar', '500', 'Prédio 34',
       'Coração Eucarístico', 'Belo Horizonte', 'MG', '30535901', i.id, (SELECT MAX(c.id) FROM conta c)
FROM usuario u, instituicao_ensino i
WHERE u.email = 'joana.silva@aluno.pucminas.br' AND i.nome LIKE '%(PUC Minas)';

INSERT INTO conta (saldo) VALUES (0);
INSERT INTO usuario (tipo, nome, email, senha, ativo) VALUES
    ('ALUNO', 'Pedro Santos', 'pedro.santos@aluno.pucminas.br',
     '$2a$10$Rm4wE6ZhT3kxLxlucCe/fedx9NcGlGB6n.gErE/fuzs8vrL//pzsq', TRUE);
INSERT INTO aluno (id, cpf, rg, curso, logradouro, numero, complemento, bairro, cidade, estado, cep, instituicao_id, conta_id)
SELECT u.id, '98765432100', 'MG-98.765.432', 'Ciência da Computação', 'Avenida Itaú', '505', NULL,
       'Dom Cabral', 'Belo Horizonte', 'MG', '30535012', i.id, (SELECT MAX(c.id) FROM conta c)
FROM usuario u, instituicao_ensino i
WHERE u.email = 'pedro.santos@aluno.pucminas.br' AND i.nome LIKE '%(PUC Minas)';

INSERT INTO conta (saldo) VALUES (0);
INSERT INTO usuario (tipo, nome, email, senha, ativo) VALUES
    ('ALUNO', 'Lucas Almeida', 'lucas.almeida@aluno.ufmg.br',
     '$2a$10$Rm4wE6ZhT3kxLxlucCe/fedx9NcGlGB6n.gErE/fuzs8vrL//pzsq', TRUE);
INSERT INTO aluno (id, cpf, rg, curso, logradouro, numero, complemento, bairro, cidade, estado, cep, instituicao_id, conta_id)
SELECT u.id, '74185296355', 'MG-74.185.296', 'Sistemas de Informação', 'Avenida Antônio Carlos', '6627', NULL,
       'Pampulha', 'Belo Horizonte', 'MG', '31270901', i.id, (SELECT MAX(c.id) FROM conta c)
FROM usuario u, instituicao_ensino i
WHERE u.email = 'lucas.almeida@aluno.ufmg.br' AND i.nome LIKE '%(UFMG)';

-- ===== Empresas parceiras =====

INSERT INTO usuario (tipo, nome, email, senha, ativo) VALUES
    ('EMPRESA', 'Cantina Central', 'contato@cantinacentral.com.br',
     '$2a$10$Rm4wE6ZhT3kxLxlucCe/fedx9NcGlGB6n.gErE/fuzs8vrL//pzsq', TRUE);
INSERT INTO empresa_parceira (id, cnpj)
SELECT u.id, '11222333000181' FROM usuario u WHERE u.email = 'contato@cantinacentral.com.br';

INSERT INTO usuario (tipo, nome, email, senha, ativo) VALUES
    ('EMPRESA', 'Livraria Universitária', 'contato@livrariauniversitaria.com.br',
     '$2a$10$Rm4wE6ZhT3kxLxlucCe/fedx9NcGlGB6n.gErE/fuzs8vrL//pzsq', TRUE);
INSERT INTO empresa_parceira (id, cnpj)
SELECT u.id, '45723174000110' FROM usuario u WHERE u.email = 'contato@livrariauniversitaria.com.br';

INSERT INTO usuario (tipo, nome, email, senha, ativo) VALUES
    ('EMPRESA', 'Fundação Educacional Parceira', 'contato@fundacaoparceira.org.br',
     '$2a$10$Rm4wE6ZhT3kxLxlucCe/fedx9NcGlGB6n.gErE/fuzs8vrL//pzsq', TRUE);
INSERT INTO empresa_parceira (id, cnpj)
SELECT u.id, '60458123000100' FROM usuario u WHERE u.email = 'contato@fundacaoparceira.org.br';

-- ===== Vantagens: os exemplos citados no enunciado (restaurante, mensalidade e materiais) =====

INSERT INTO vantagem (nome, descricao, foto_url, custo_moedas, ativa, empresa_id)
SELECT 'Almoço no restaurante universitário', 'Um almoço completo, com bebida e sobremesa, no restaurante do campus.',
       '/api/fotos/demo-almoco.png', 30, TRUE, u.id
FROM usuario u WHERE u.email = 'contato@cantinacentral.com.br';

INSERT INTO vantagem (nome, descricao, foto_url, custo_moedas, ativa, empresa_id)
SELECT 'Combo café da manhã', 'Café, suco e pão de queijo na cantina do prédio 34.',
       '/api/fotos/demo-cafe.png', 15, TRUE, u.id
FROM usuario u WHERE u.email = 'contato@cantinacentral.com.br';

INSERT INTO vantagem (nome, descricao, foto_url, custo_moedas, ativa, empresa_id)
SELECT 'Desconto de 20% em livros técnicos', 'Cupom válido para um livro técnico de qualquer área.',
       '/api/fotos/demo-livros.png', 120, TRUE, u.id
FROM usuario u WHERE u.email = 'contato@livrariauniversitaria.com.br';

INSERT INTO vantagem (nome, descricao, foto_url, custo_moedas, ativa, empresa_id)
SELECT 'Kit de materiais para laboratório', 'Caderno quadriculado, lapiseira, escalímetro e pendrive.',
       '/api/fotos/demo-materiais.png', 80, TRUE, u.id
FROM usuario u WHERE u.email = 'contato@livrariauniversitaria.com.br';

INSERT INTO vantagem (nome, descricao, foto_url, custo_moedas, ativa, empresa_id)
SELECT 'Desconto de 10% na mensalidade', 'Abatimento aplicado na mensalidade do mês seguinte ao resgate.',
       '/api/fotos/demo-mensalidade.png', 500, TRUE, u.id
FROM usuario u WHERE u.email = 'contato@fundacaoparceira.org.br';
