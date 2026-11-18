package br.pucminas.moedaestudantil.service;

import br.pucminas.moedaestudantil.exception.RecursoNaoEncontradoException;
import br.pucminas.moedaestudantil.exception.RegraNegocioException;
import br.pucminas.moedaestudantil.model.EmpresaParceira;
import br.pucminas.moedaestudantil.repository.EmpresaParceiraRepository;
import br.pucminas.moedaestudantil.repository.TransacaoRepository;
import br.pucminas.moedaestudantil.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** CRUD de empresa parceira (UC04): CNPJ e e-mail únicos. */
@Service
public class EmpresaParceiraService {

    private final EmpresaParceiraRepository empresaParceiraRepository;
    private final UsuarioRepository usuarioRepository;
    private final TransacaoRepository transacaoRepository;
    private final PasswordEncoder passwordEncoder;
    private final ArmazenamentoImagemService armazenamentoImagemService;

    public EmpresaParceiraService(EmpresaParceiraRepository empresaParceiraRepository,
                                  UsuarioRepository usuarioRepository, TransacaoRepository transacaoRepository,
                                  PasswordEncoder passwordEncoder, ArmazenamentoImagemService armazenamentoImagemService) {
        this.empresaParceiraRepository = empresaParceiraRepository;
        this.usuarioRepository = usuarioRepository;
        this.transacaoRepository = transacaoRepository;
        this.passwordEncoder = passwordEncoder;
        this.armazenamentoImagemService = armazenamentoImagemService;
    }

    @Transactional
    public EmpresaParceira cadastrar(EmpresaParceira empresa) {
        validarUnicidade(empresa, null);
        empresa.setSenha(passwordEncoder.encode(empresa.getSenha()));
        return empresaParceiraRepository.save(empresa);
    }

    @Transactional(readOnly = true)
    public EmpresaParceira buscarPorId(Long id) {
        return empresaParceiraRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa parceira " + id + " não encontrada."));
    }

    @Transactional(readOnly = true)
    public List<EmpresaParceira> listar() {
        return empresaParceiraRepository.findByAtivoTrueOrderByNomeAsc();
    }

    /** Altera os dados da empresa. Senha em branco mantém a senha atual. */
    @Transactional
    public EmpresaParceira atualizar(Long id, EmpresaParceira dados) {
        EmpresaParceira empresa = buscarPorId(id);
        validarUnicidade(dados, id);

        empresa.setNome(dados.getNome());
        empresa.setCnpj(dados.getCnpj());
        empresa.setEmail(dados.getEmail());
        if (dados.getSenha() != null && !dados.getSenha().isBlank()) {
            empresa.setSenha(passwordEncoder.encode(dados.getSenha()));
        }
        return empresa;
    }

    /**
     * Sem resgates, a empresa, suas vantagens e as fotos são removidas do banco. Com resgates, a exclusão é lógica:
     * as vantagens deixam de ser exibidas e o histórico de resgates é mantido nos extratos (US12).
     */
    @Transactional
    public void excluir(Long id) {
        EmpresaParceira empresa = buscarPorId(id);
        if (transacaoRepository.existsResgateByEmpresa(empresa)) {
            empresa.desativar();
        } else {
            empresa.getVantagens().forEach(vantagem -> armazenamentoImagemService.remover(vantagem.getFotoUrl()));
            empresaParceiraRepository.delete(empresa);
        }
    }

    private void validarUnicidade(EmpresaParceira empresa, Long idAtual) {
        boolean cnpjEmUso = idAtual == null
                ? empresaParceiraRepository.existsByCnpj(empresa.getCnpj())
                : empresaParceiraRepository.existsByCnpjAndIdNot(empresa.getCnpj(), idAtual);
        if (cnpjEmUso) {
            throw new RegraNegocioException("cnpj", "Já existe uma empresa parceira cadastrada com este CNPJ.");
        }

        boolean emailEmUso = idAtual == null
                ? usuarioRepository.existsByEmail(empresa.getEmail())
                : usuarioRepository.existsByEmailAndIdNot(empresa.getEmail(), idAtual);
        if (emailEmUso) {
            throw new RegraNegocioException("email", "Este e-mail já está em uso.");
        }
    }
}
