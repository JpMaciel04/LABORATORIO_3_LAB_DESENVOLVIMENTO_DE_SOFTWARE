package br.pucminas.moedaestudantil.service;

import br.pucminas.moedaestudantil.exception.RecursoNaoEncontradoException;
import br.pucminas.moedaestudantil.exception.RegraNegocioException;
import br.pucminas.moedaestudantil.model.Aluno;
import br.pucminas.moedaestudantil.model.InstituicaoEnsino;
import br.pucminas.moedaestudantil.model.Professor;
import br.pucminas.moedaestudantil.repository.AlunoRepository;
import br.pucminas.moedaestudantil.repository.InstituicaoEnsinoRepository;
import br.pucminas.moedaestudantil.repository.ProfessorRepository;
import br.pucminas.moedaestudantil.repository.TransacaoRepository;
import br.pucminas.moedaestudantil.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** CRUD de aluno (UC02) com as regras RN01 (CPF e e-mail únicos) e RN02 (instituição pré-cadastrada). */
@Service
public class AlunoService {

    private final AlunoRepository alunoRepository;
    private final UsuarioRepository usuarioRepository;
    private final InstituicaoEnsinoRepository instituicaoEnsinoRepository;
    private final ProfessorRepository professorRepository;
    private final TransacaoRepository transacaoRepository;
    private final PasswordEncoder passwordEncoder;

    public AlunoService(AlunoRepository alunoRepository, UsuarioRepository usuarioRepository,
                        InstituicaoEnsinoRepository instituicaoEnsinoRepository, ProfessorRepository professorRepository,
                        TransacaoRepository transacaoRepository, PasswordEncoder passwordEncoder) {
        this.alunoRepository = alunoRepository;
        this.usuarioRepository = usuarioRepository;
        this.instituicaoEnsinoRepository = instituicaoEnsinoRepository;
        this.professorRepository = professorRepository;
        this.transacaoRepository = transacaoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Cria o aluno com uma conta de saldo 0. */
    @Transactional
    public Aluno cadastrar(Aluno aluno, Long instituicaoId) {
        validarUnicidade(aluno, null);
        aluno.setInstituicao(buscarInstituicao(instituicaoId));
        aluno.setSenha(passwordEncoder.encode(aluno.getSenha()));
        return alunoRepository.save(aluno);
    }

    @Transactional(readOnly = true)
    public Aluno buscarPorId(Long id) {
        return alunoRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno " + id + " não encontrado."));
    }

    /** Alunos que o professor pode reconhecer: os da mesma instituição (RN06). */
    @Transactional(readOnly = true)
    public List<Aluno> listarDaInstituicaoDoProfessor(Long professorId) {
        Professor professor = professorRepository.findByIdAndAtivoTrue(professorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Professor " + professorId + " não encontrado."));
        return alunoRepository.findByInstituicaoIdAndAtivoTrueOrderByNomeAsc(professor.getInstituicao().getId());
    }

    /** Altera os dados cadastrais. Senha em branco mantém a senha atual. */
    @Transactional
    public Aluno atualizar(Long id, Aluno dados, Long instituicaoId) {
        Aluno aluno = buscarPorId(id);
        validarUnicidade(dados, id);

        aluno.setNome(dados.getNome());
        aluno.setEmail(dados.getEmail());
        aluno.setCpf(dados.getCpf());
        aluno.setRg(dados.getRg());
        aluno.setCurso(dados.getCurso());
        aluno.setEndereco(dados.getEndereco());
        aluno.setInstituicao(buscarInstituicao(instituicaoId));
        if (dados.getSenha() != null && !dados.getSenha().isBlank()) {
            aluno.setSenha(passwordEncoder.encode(dados.getSenha()));
        }
        return aluno;
    }

    /**
     * Sem movimentações, o aluno e sua conta são removidos do banco. Com movimentações, a exclusão é
     * lógica: o aluno perde o acesso, mas as transações continuam nos extratos (RN08).
     */
    @Transactional
    public void excluir(Long id) {
        Aluno aluno = buscarPorId(id);
        if (transacaoRepository.existsByConta(aluno.getConta())) {
            aluno.desativar();
        } else {
            alunoRepository.delete(aluno);
        }
    }

    private void validarUnicidade(Aluno aluno, Long idAtual) {
        boolean cpfEmUso = idAtual == null
                ? alunoRepository.existsByCpf(aluno.getCpf())
                : alunoRepository.existsByCpfAndIdNot(aluno.getCpf(), idAtual);
        if (cpfEmUso) {
            throw new RegraNegocioException("cpf", "Já existe um aluno cadastrado com este CPF.");
        }

        boolean emailEmUso = idAtual == null
                ? usuarioRepository.existsByEmail(aluno.getEmail())
                : usuarioRepository.existsByEmailAndIdNot(aluno.getEmail(), idAtual);
        if (emailEmUso) {
            throw new RegraNegocioException("email", "Este e-mail já está em uso.");
        }
    }

    private InstituicaoEnsino buscarInstituicao(Long instituicaoId) {
        return instituicaoEnsinoRepository.findById(instituicaoId)
                .orElseThrow(() -> new RegraNegocioException("instituicaoId",
                        "Selecione uma das instituições de ensino participantes."));
    }
}
