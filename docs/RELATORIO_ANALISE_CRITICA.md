# Relatório de Análise Crítica do Projeto — Sistema de Moeda Estudantil

**Repositório Avaliado:** [`JpMaciel04/LABORATORIO_3_LAB_DESENVOLVIMENTO_DE_SOFTWARE`](https://github.com/JpMaciel04/LABORATORIO_3_LAB_DESENVOLVIMENTO_DE_SOFTWARE)

---

## 1. Arquitetura e Tecnologias Utilizadas

- **Arquitetura:** MVC com API REST/JSON *stateless* e back-end organizado em camadas (`controller` → `service` → `repository` → `model`).
- **Tecnologias:**
  - **Front-end (View):** React 19, Vite, React Router, Axios.
  - **Back-end (Controller e Model):** Java 21, Spring Boot 3.5 (Spring Web, Bean Validation, Spring Data JPA/Hibernate, Spring Security + JJWT + BCrypt, Spring Mail), Flyway, MySQL 8 / H2.
  - **Testes e Modelagem:** JUnit 5, MockMvc, Mockito e PlantUML.
- **Pontos positivos:** Uso de DTOs (`record`s), bloqueio otimista (`@Version`) na entidade `Conta` contra *double spending*, prevenção de N+1 com `@EntityGraph` e projeção `TitularConta`, envio de e-mails após o commit (`AposCommit`), exclusão híbrida (física sem histórico / lógica com histórico).
- **Pontos críticos:** Vazamento de `MultipartFile` da camada Web para a camada de Serviço (`VantagemService`); consulta ao banco a cada requisição autenticada no `JwtAutenticacaoFilter`; ausência de paginação nas listagens e no extrato; regras de domínio concentradas nos Services em vez das entidades (corrigido nas refatorações GRASP).

---

## 2. Organização do GitHub

- **Pontos positivos:** Boa separação de pastas (`backend/`, `frontend/`, `docs/sprint-01` a `sprint-03`), `.gitignore` adequado e fontes `.puml` versionadas junto com `.png` e `.pdf`.
- **Pontos críticos:**
  1. **Histórico de commits monolítico:** Na branch `main`, toda a implementação das Sprints 2 e 3 foi feita em um único commit massivo (`71f6dea`), sem commits atômicos por funcionalidade.
  2. **Branch `origin/lab04s02` não mesclada na `main`:** As entregas do Lab 04 (`docs/sprint-04` e `docs/sprint-05`, implantação na nuvem e banco PostgreSQL) ficaram isoladas na branch `origin/lab04s02` sem merge para a `main`.
  3. **Ausência de Issues, Milestones e CI/CD:** Não há Issues para rastrear as User Stories (`US01`–`US15`) nem workflow do GitHub Actions para rodar os testes automaticamente.

---

## 3. Dificuldade para Configuração do Ambiente Seguindo o README

- **Facilidades:** O uso do Maven Wrapper (`mvnw`), do perfil `h2,demo` (banco em memória com dados prontos) e o fallback de e-mail para o log do console tornam a execução local simples, sem exigir MySQL ou servidor SMTP instalados.
- **Problemas encontrados no README:**
  1. **Comandos exclusivos para PowerShell (Windows):** Todos os comandos usam `.\mvnw` e `$env:VAR="valor"`, que falham diretamente no Linux/macOS (onde o correto é `./mvnw` e `export VAR="valor"`).
  2. **Arquivo citado inexistente na `main`:** O README referencia o arquivo `frontend/.env.example` (seção 12), que não existe na branch `main`.
  3. **Exigência estrita do JDK 21:** O código usa *Pattern Matching for switch* do Java 21, falhando caso o ambiente tenha JDK 17.

---

## 4. Erros nos Diagramas Desenvolvidos

1. **Diagrama de Casos de Uso ([casos-de-uso.puml](sprint-03/casos-de-uso.puml)):**
   - **Uso incorreto de `<<include>>` para Login (`UC01`):** `UC05`, `UC06`, `UC07`, `UC08`, `UC09` e `UC14` incluem `UC01 (Realizar Login)` via `<<include>>`. Na UML, `<<include>>` significa que o caso de uso incluído é executado toda vez que o caso base roda; porém, estar logado é uma **pré-condição**, e não uma sub-rotina executada a cada operação.
   - **`UC13` sem associação direta com o ator `Professor`:** `UC13 (Consultar Alunos da Instituição)` aparece apenas como `<<include>>` de `UC06`, embora exista como funcionalidade autônoma no menu do professor.
2. **Diagrama de Sequência ([diagrama-sequencia-resgate.puml](sprint-03/diagrama-sequencia-resgate.puml)):**
   - **Ordem incorreta do `COMMIT` (linhas 46–49):** O diagrama mostra `Service --> Controller : ResgateVantagem`, encerra a ativação (`deactivate Service`) e só **depois** mostra `Service -> DB : COMMIT`. No Spring (`@Transactional`), o `COMMIT` ocorre antes de o retorno ser entregue ao Controller.
   - **Agrupamento de classes distintas na mesma linha de vida:** As lifelines `"Aluno / Conta (entidades)"` e `"Repositories (DAO)"` misturam múltiplos objetos/repositórios em uma única raia.
3. **Modelo ER ([modelo-er.puml](sprint-03/modelo-er.puml)):**
   - **Ausência de restrição de exclusividade (XOR) em `conta`:** As relações `aluno |o--|| conta` e `professor |o--|| conta` não indicam arco exclusivo, permitindo visualmente que uma mesma `conta` pertença simultaneamente a um aluno e a um professor.

---

## 5. Erros no Alinhamento entre o Projeto e a Implementação

1. **Tipos de retorno divergentes nos Controllers ([diagrama-classes-camadas.puml](sprint-03/diagrama-classes-camadas.puml)):** O diagrama declara que `cadastrar(...)` em `AlunoController`, `EmpresaParceiraController` e `VantagemController` retorna o DTO puro (`AlunoResponseDTO`, etc.), mas o código Java retorna `ResponseEntity<...>`.
2. **Tipos de retorno divergentes no `GlobalExceptionHandler` ([diagrama-classes-dto.puml](sprint-03/diagrama-classes-dto.puml)):** O diagrama declara retorno `ErroDTO` nos 9 métodos, mas a implementação retorna `ResponseEntity<ErroDTO>`.
3. **Classes e tipos existentes no código e omitidos nos diagramas:**
   - Classe `AposCommit` omitida do pacote `service` em `diagrama-classes-camadas.puml`.
   - Classe `Formatos` e enum `TransacaoDTO.Tipo` omitidos do pacote `dto` em `diagrama-classes-dto.puml`.
   - Método público `Extrato.getTransacoes()` omitido da classe `Extrato` em `diagrama-classes.puml`.
   - Classe `EmailNotificacaoService` representada vazia em `diagrama-classes-camadas.puml`, sem métodos nem dependências (`JavaMailSender`, `TaskExecutor`).

---

## 6. Refatorações Baseadas nos Padrões GRASP

### 6.1. Refatoração 1 — Polimorfismo e Especialista na Informação (`Usuario`, `Transacao`, `Extrato`, `ExtratoService`)

- **Padrões GRASP:** **Polimorfismo** (eliminar `switch` por tipo concreto em `ExtratoService.contaDe(Usuario)`) e **Especialista na Informação** (`Transacao` informa suas contas envolvidas e se uma conta é destino).

**Antes (`ExtratoService.java` e `Extrato.java`):**
```java
// ExtratoService.java
Conta conta = contaDe(usuario);
...
private static Conta contaDe(Usuario usuario) {
    return switch (usuario) {
        case Aluno aluno -> aluno.getConta();
        case Professor professor -> professor.getConta();
        default -> throw new RegraNegocioException("Este perfil não possui conta de moedas.");
    };
}

// Extrato.java
public boolean isEntrada(Transacao transacao) {
    return transacao.getContaDestino() != null && transacao.getContaDestino().getId().equals(conta.getId());
}
```

**Depois (`Usuario.java`, `Transacao.java`, `Extrato.java` e `ExtratoService.java`):**
```java
// Usuario.java (sobrescrito com @Override em Aluno e Professor)
public Conta getConta() {
    throw new RegraNegocioException("Este perfil não possui conta de moedas.");
}

// Transacao.java
public boolean temComoDestino(Conta conta) {
    return contaDestino != null && conta != null && contaDestino.getId().equals(conta.getId());
}

public Set<Long> getIdsContasEnvolvidas() {
    Set<Long> ids = new HashSet<>();
    if (contaOrigem != null && contaOrigem.getId() != null) ids.add(contaOrigem.getId());
    if (contaDestino != null && contaDestino.getId() != null) ids.add(contaDestino.getId());
    return ids;
}

// Extrato.java
public boolean isEntrada(Transacao transacao) {
    return transacao.temComoDestino(conta);
}

// ExtratoService.java
Conta conta = usuario.getConta();
```

- **Justificativa:** Remove a seleção condicional por subtipo (`switch (usuario)`) aplicando **Polimorfismo** em `Usuario.getConta()`, e delega à própria `Transacao` (**Especialista na Informação**) a verificação de conta de destino e extração das contas envolvidas.

---

### 6.2. Refatoração 2 — Especialista na Informação, Criador e Baixo Acoplamento (`Professor`, `Aluno`, `MoedaService`)

- **Padrões GRASP:** **Especialista na Informação**, **Criador (*Creator*)** e **Baixo Acoplamento** (o próprio `Professor` valida se o `Aluno` pertence à mesma instituição, realiza o débito/crédito e cria as transações `EnvioMoedas` e `CreditoSemestral`).

**Antes (`MoedaService.java`):**
```java
if (!professor.getInstituicao().getId().equals(aluno.getInstituicao().getId())) {
    throw new RegraNegocioException("alunoId", "Só é possível enviar moedas a alunos da sua instituição.");
}
professor.getConta().debitar(valor);
aluno.getConta().creditar(valor);
EnvioMoedas envio = transacaoRepository.save(new EnvioMoedas(professor, aluno, valor, motivo));
```

**Depois (`Professor.java`, `Aluno.java` e `MoedaService.java`):**
```java
// Professor.java
public boolean pertenceAMesmaInstituicao(Aluno aluno) {
    return aluno != null && this.instituicao != null && aluno.getInstituicao() != null
            && this.instituicao.getId().equals(aluno.getInstituicao().getId());
}

public EnvioMoedas enviarMoedas(Aluno aluno, int valor, String motivo) {
    if (!pertenceAMesmaInstituicao(aluno)) {
        throw new RegraNegocioException("alunoId", "Só é possível enviar moedas a alunos da sua instituição.");
    }
    this.conta.debitar(valor);
    aluno.receberMoedas(valor);
    return new EnvioMoedas(this, aluno, valor, motivo);
}

public CreditoSemestral receberCreditoSemestral(String semestre) {
    this.conta.creditar(CreditoSemestral.MOEDAS_POR_SEMESTRE);
    return new CreditoSemestral(this, semestre);
}

// MoedaService.java
EnvioMoedas envio = transacaoRepository.save(professor.enviarMoedas(aluno, valor, motivo));
```

- **Justificativa:** Elimina o encadeamento de chamadas (violação da Lei de Demeter) no Service e atribui a `Professor` (**Especialista na Informação** e **Criador**) a responsabilidade de validar a regra RN06, atualizar os saldos e instanciar `EnvioMoedas` e `CreditoSemestral`.

---

### 6.3. Refatoração 3 — Criador, Especialista na Informação e Alta Coesão (`Aluno`, `ResgateVantagem`, `ResgateService`)

- **Padrões GRASP:** **Criador (*Creator*)** (`Aluno.resgatar(Vantagem)` debita sua conta e cria `ResgateVantagem`), **Especialista na Informação** e **Alta Coesão** (`ResgateVantagem.gerarCodigoCupom()` encapsula a geração do código do cupom, deixando `ResgateService` focado apenas na orquestração).

**Antes (`ResgateService.java`):**
```java
aluno.getConta().debitar(vantagem.getCustoMoedas());
ResgateVantagem resgate = transacaoRepository.save(new ResgateVantagem(aluno, vantagem, gerarCodigoCupom()));
```

**Depois (`ResgateVantagem.java`, `Aluno.java` e `ResgateService.java`):**
```java
// ResgateVantagem.java
public ResgateVantagem(Aluno aluno, Vantagem vantagem) {
    this(aluno, vantagem, gerarCodigoCupom());
}

public static String gerarCodigoCupom() {
    // Gera código no formato XXXX-XXXX-XXXX (RN11)
    ...
}

// Aluno.java
public ResgateVantagem resgatar(Vantagem vantagem) {
    this.conta.debitar(vantagem.getCustoMoedas());
    return new ResgateVantagem(this, vantagem);
}

// ResgateService.java
ResgateVantagem resgate = transacaoRepository.save(aluno.resgatar(vantagem));
```

- **Justificativa:** Aumenta a coesão de `ResgateService` (**Alta Coesão**) ao mover a regra de formatação de cupom para a entidade detentora do dado (`ResgateVantagem`, **Especialista na Informação**) e atribui a `Aluno` (**Criador**) a criação do resgate junto ao débito de sua conta.
