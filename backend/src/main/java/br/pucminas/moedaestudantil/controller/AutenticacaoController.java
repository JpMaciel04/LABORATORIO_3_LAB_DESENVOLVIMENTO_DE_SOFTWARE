package br.pucminas.moedaestudantil.controller;

import br.pucminas.moedaestudantil.dto.LoginDTO;
import br.pucminas.moedaestudantil.dto.TokenDTO;
import br.pucminas.moedaestudantil.service.AutenticacaoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AutenticacaoController {

    private final AutenticacaoService autenticacaoService;

    public AutenticacaoController(AutenticacaoService autenticacaoService) {
        this.autenticacaoService = autenticacaoService;
    }

    @PostMapping("/login")
    public TokenDTO login(@RequestBody @Valid LoginDTO dto) {
        return TokenDTO.from(autenticacaoService.autenticar(dto.email(), dto.senha()));
    }
}
