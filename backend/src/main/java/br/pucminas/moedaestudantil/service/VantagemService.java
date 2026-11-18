package br.pucminas.moedaestudantil.service;

import br.pucminas.moedaestudantil.exception.RecursoNaoEncontradoException;
import br.pucminas.moedaestudantil.model.EmpresaParceira;
import br.pucminas.moedaestudantil.model.Vantagem;
import br.pucminas.moedaestudantil.repository.EmpresaParceiraRepository;
import br.pucminas.moedaestudantil.repository.VantagemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** Manutenção (UC05) e consulta (UC08) das vantagens, com as regras RN09, US13 e US14. */
@Service
public class VantagemService {

    private final VantagemRepository vantagemRepository;
    private final EmpresaParceiraRepository empresaParceiraRepository;
    private final ArmazenamentoImagemService armazenamentoImagemService;

    public VantagemService(VantagemRepository vantagemRepository, EmpresaParceiraRepository empresaParceiraRepository,
                           ArmazenamentoImagemService armazenamentoImagemService) {
        this.vantagemRepository = vantagemRepository;
        this.empresaParceiraRepository = empresaParceiraRepository;
        this.armazenamentoImagemService = armazenamentoImagemService;
    }

    @Transactional
    public Vantagem cadastrar(Long empresaId, Vantagem vantagem, MultipartFile foto) {
        EmpresaParceira empresa = empresaParceiraRepository.findByIdAndAtivoTrue(empresaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa parceira " + empresaId + " não encontrada."));
        vantagem.setEmpresa(empresa);
        vantagem.setFotoUrl(armazenamentoImagemService.salvar(foto));
        return vantagemRepository.save(vantagem);
    }

    /** Catálogo exibido aos alunos: somente vantagens ativas (US06). */
    @Transactional(readOnly = true)
    public List<Vantagem> listarDisponiveis() {
        return vantagemRepository.findByAtivaTrueOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public List<Vantagem> listarDaEmpresa(Long empresaId) {
        return vantagemRepository.findByEmpresaIdOrderByNomeAsc(empresaId);
    }

    @Transactional(readOnly = true)
    public Vantagem buscarDaEmpresa(Long empresaId, Long id) {
        return vantagemRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Vantagem " + id + " não encontrada."));
    }

    /** Sem uma nova foto, a foto atual é mantida. A troca da foto faz parte da mesma transação. */
    @Transactional
    public Vantagem atualizar(Long empresaId, Long id, Vantagem dados, MultipartFile foto) {
        Vantagem vantagem = buscarDaEmpresa(empresaId, id);
        vantagem.atualizar(dados.getNome(), dados.getDescricao(), dados.getCustoMoedas());
        if (foto != null && !foto.isEmpty()) {
            String fotoAnterior = vantagem.getFotoUrl();
            vantagem.setFotoUrl(armazenamentoImagemService.salvar(foto));
            armazenamentoImagemService.remover(fotoAnterior);
        }
        return vantagem;
    }

    /** A vantagem sai do catálogo, mas continua no extrato de quem já a resgatou (US14). */
    @Transactional
    public void excluir(Long empresaId, Long id) {
        buscarDaEmpresa(empresaId, id).desativar();
    }
}
