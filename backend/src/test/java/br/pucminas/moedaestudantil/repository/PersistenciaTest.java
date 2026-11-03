package br.pucminas.moedaestudantil.repository;

import br.pucminas.moedaestudantil.model.Aluno;
import br.pucminas.moedaestudantil.model.CreditoSemestral;
import br.pucminas.moedaestudantil.model.EmpresaParceira;
import br.pucminas.moedaestudantil.model.Endereco;
import br.pucminas.moedaestudantil.model.EnvioMoedas;
import br.pucminas.moedaestudantil.model.InstituicaoEnsino;
import br.pucminas.moedaestudantil.model.Professor;
import br.pucminas.moedaestudantil.model.ResgateVantagem;
import br.pucminas.moedaestudantil.model.Transacao;
import br.pucminas.moedaestudantil.model.Vantagem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Valida a estratégia de persistência: entidades JPA + repositórios Spring Data (DAO)
 * sobre o esquema criado pelas migrações Flyway (ddl-auto=validate).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles({"h2", "teste"})
class PersistenciaTest {

    @Autowired
    private InstituicaoEnsinoRepository instituicaoEnsinoRepository;
    @Autowired
    private ProfessorRepository professorRepository;
    @Autowired
    private AlunoRepository alunoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private EmpresaParceiraRepository empresaParceiraRepository;
    @Autowired
    private VantagemRepository vantagemRepository;
    @Autowired
    private TransacaoRepository transacaoRepository;

    @Test
    void cargaInicialCriaInstituicoesEProfessoresComConta() {
        assertThat(instituicaoEnsinoRepository.findAllByOrderByNomeAsc())
                .extracting(InstituicaoEnsino::getNome)
                .hasSize(3)
                .isSorted();

        List<Professor> professores = professorRepository.findAll();
        assertThat(professores).hasSize(4);
        assertThat(professores).allSatisfy(professor -> {
            assertThat(professor.getInstituicao()).isNotNull();
            assertThat(professor.getConta().getSaldo()).isZero();
            assertThat(professor.getSenha()).startsWith("$2a$");
        });
    }

    @Test
    void salvarAlunoPersisteContaEmCascataEHerancaJoined() {
        Aluno aluno = alunoRepository.saveAndFlush(novoAluno("12345678909", "joana@email.com"));

        assertThat(aluno.getConta().getId()).isNotNull();
        assertThat(alunoRepository.existsByCpf("12345678909")).isTrue();
        assertThat(usuarioRepository.findByEmailAndAtivoTrue("joana@email.com")).get().isInstanceOf(Aluno.class);
        assertThat(alunoRepository.findByInstituicaoIdAndAtivoTrueOrderByNomeAsc(aluno.getInstituicao().getId()))
                .extracting(Aluno::getNome).contains("Joana Silva");
    }

    @Test
    void usuarioDesativadoNaoEEncontradoParaLogin() {
        Aluno aluno = alunoRepository.saveAndFlush(novoAluno("12345678909", "joana@email.com"));

        aluno.desativar();
        alunoRepository.saveAndFlush(aluno);

        assertThat(usuarioRepository.findByEmailAndAtivoTrue("joana@email.com")).isEmpty();
        assertThat(usuarioRepository.existsByIdAndAtivoTrue(aluno.getId())).isFalse();
    }

    @Test
    void titularesTrazemONomeDoDonoDeCadaConta() {
        Professor professor = professorRepository.findAll().getFirst();
        Aluno aluno = alunoRepository.saveAndFlush(novoAluno("12345678909", "joana@email.com"));
        List<Long> contas = List.of(professor.getConta().getId(), aluno.getConta().getId());

        assertThat(alunoRepository.findTitulares(contas))
                .singleElement()
                .satisfies(titular -> {
                    assertThat(titular.getContaId()).isEqualTo(aluno.getConta().getId());
                    assertThat(titular.getNome()).isEqualTo("Joana Silva");
                });
        assertThat(professorRepository.findTitulares(contas))
                .singleElement()
                .satisfies(titular -> assertThat(titular.getNome()).isEqualTo(professor.getNome()));
    }

    @Test
    void cpfDuplicadoViolaRestricaoUnique() {
        alunoRepository.saveAndFlush(novoAluno("12345678909", "joana@email.com"));

        assertThatThrownBy(() -> alunoRepository.saveAndFlush(novoAluno("12345678909", "outra@email.com")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void extratoReuneTransacoesDeOrigemEDestinoDaConta() {
        Professor professor = professorRepository.findAll().getFirst();
        Aluno aluno = alunoRepository.save(novoAluno("12345678909", "joana@email.com"));

        professor.getConta().creditar(CreditoSemestral.MOEDAS_POR_SEMESTRE);
        transacaoRepository.save(new CreditoSemestral(professor, "2026-2"));
        professor.getConta().debitar(50);
        aluno.getConta().creditar(50);
        transacaoRepository.saveAndFlush(new EnvioMoedas(professor, aluno, 50, "Participação na aula"));

        assertThat(transacaoRepository.findByConta(professor.getConta()))
                .extracting(Transacao::getValor)
                .containsExactly(50, 1000);
        assertThat(transacaoRepository.findByConta(aluno.getConta())).hasSize(1).first()
                .isInstanceOf(EnvioMoedas.class);
        assertThat(professor.getConta().getSaldo()).isEqualTo(950);
    }

    @Test
    void creditoSemestralNaoSeRepeteNoMesmoSemestre() {
        Professor professor = professorRepository.findAll().getFirst();
        transacaoRepository.saveAndFlush(new CreditoSemestral(professor, "2026-2"));

        assertThat(transacaoRepository.existsCreditoSemestral(professor, "2026-2")).isTrue();
        assertThat(transacaoRepository.existsCreditoSemestral(professor, "2027-1")).isFalse();
        assertThatThrownBy(() -> transacaoRepository.saveAndFlush(new CreditoSemestral(professor, "2026-2")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void excluirEmpresaDesativaVantagensEPreservaResgates() {
        EmpresaParceira empresa = new EmpresaParceira();
        empresa.setNome("Cantina Central");
        empresa.setCnpj("11222333000181");
        empresa.setEmail("contato@cantina.com");
        empresa.setSenha("hash");
        Vantagem vantagem = new Vantagem(empresa, "Almoço", "Um almoço no restaurante universitário", "/fotos/almoco.png", 30);
        empresa.getVantagens().add(vantagem);
        empresaParceiraRepository.saveAndFlush(empresa);

        Aluno aluno = alunoRepository.save(novoAluno("12345678909", "joana@email.com"));
        aluno.getConta().creditar(30);
        aluno.getConta().debitar(30);
        transacaoRepository.saveAndFlush(new ResgateVantagem(aluno, vantagem, "CUPOM-0001"));

        empresa.desativar();
        empresaParceiraRepository.saveAndFlush(empresa);

        assertThat(empresaParceiraRepository.findByIdAndAtivoTrue(empresa.getId())).isEmpty();
        assertThat(vantagemRepository.findByAtivaTrueOrderByNomeAsc()).isEmpty();
        assertThat(vantagemRepository.findByEmpresaIdOrderByNomeAsc(empresa.getId())).hasSize(1);
        assertThat(transacaoRepository.findByConta(aluno.getConta())).hasSize(1);
    }

    private Aluno novoAluno(String cpf, String email) {
        Aluno aluno = new Aluno();
        aluno.setNome("Joana Silva");
        aluno.setEmail(email);
        aluno.setSenha("hash");
        aluno.setCpf(cpf);
        aluno.setRg("MG-12.345.678");
        aluno.setCurso("Engenharia de Software");
        aluno.setEndereco(new Endereco("Rua Dom José Gaspar", "500", null, "Coração Eucarístico",
                "Belo Horizonte", "MG", "30535901"));
        aluno.setInstituicao(instituicaoEnsinoRepository.findAllByOrderByNomeAsc().getFirst());
        return aluno;
    }
}
