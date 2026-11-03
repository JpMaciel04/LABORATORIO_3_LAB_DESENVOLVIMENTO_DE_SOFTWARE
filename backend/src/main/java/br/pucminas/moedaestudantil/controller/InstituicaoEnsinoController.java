package br.pucminas.moedaestudantil.controller;

import br.pucminas.moedaestudantil.dto.InstituicaoEnsinoDTO;
import br.pucminas.moedaestudantil.service.InstituicaoEnsinoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/instituicoes")
public class InstituicaoEnsinoController {

    private final InstituicaoEnsinoService instituicaoEnsinoService;

    public InstituicaoEnsinoController(InstituicaoEnsinoService instituicaoEnsinoService) {
        this.instituicaoEnsinoService = instituicaoEnsinoService;
    }

    @GetMapping
    public List<InstituicaoEnsinoDTO> listar() {
        return instituicaoEnsinoService.listar().stream().map(InstituicaoEnsinoDTO::from).toList();
    }
}
