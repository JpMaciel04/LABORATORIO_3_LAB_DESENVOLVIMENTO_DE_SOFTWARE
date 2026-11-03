package br.pucminas.moedaestudantil.service;

import br.pucminas.moedaestudantil.model.InstituicaoEnsino;
import br.pucminas.moedaestudantil.repository.InstituicaoEnsinoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Consulta das instituições pré-cadastradas (UC03). */
@Service
public class InstituicaoEnsinoService {

    private final InstituicaoEnsinoRepository instituicaoEnsinoRepository;

    public InstituicaoEnsinoService(InstituicaoEnsinoRepository instituicaoEnsinoRepository) {
        this.instituicaoEnsinoRepository = instituicaoEnsinoRepository;
    }

    @Transactional(readOnly = true)
    public List<InstituicaoEnsino> listar() {
        return instituicaoEnsinoRepository.findAllByOrderByNomeAsc();
    }
}
