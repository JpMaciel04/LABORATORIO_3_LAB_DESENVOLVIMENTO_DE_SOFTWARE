package br.pucminas.moedaestudantil.controller;

import br.pucminas.moedaestudantil.dto.EnvioMoedasDTO;
import br.pucminas.moedaestudantil.dto.ExtratoDTO;
import br.pucminas.moedaestudantil.dto.TransacaoDTO;
import br.pucminas.moedaestudantil.security.UsuarioAutenticado;
import br.pucminas.moedaestudantil.service.ExtratoService;
import br.pucminas.moedaestudantil.service.MoedaService;
import br.pucminas.moedaestudantil.service.ResgateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Movimentações de moedas: quem as realiza é sempre o usuário autenticado, nunca um id informado na requisição. */
@RestController
@RequestMapping("/api/transacoes")
public class TransacaoController {

    private final MoedaService moedaService;
    private final ResgateService resgateService;
    private final ExtratoService extratoService;

    public TransacaoController(MoedaService moedaService, ResgateService resgateService, ExtratoService extratoService) {
        this.moedaService = moedaService;
        this.resgateService = resgateService;
        this.extratoService = extratoService;
    }

    @PostMapping("/envios")
    @ResponseStatus(HttpStatus.CREATED)
    public TransacaoDTO enviarMoedas(@RequestBody @Valid EnvioMoedasDTO dto,
                                     @AuthenticationPrincipal UsuarioAutenticado professor) {
        return TransacaoDTO.from(moedaService.enviarMoedas(professor.id(), dto.alunoId(), dto.valor(), dto.motivo()));
    }

    @PostMapping("/resgates/{vantagemId}")
    @ResponseStatus(HttpStatus.CREATED)
    public TransacaoDTO resgatarVantagem(@PathVariable Long vantagemId,
                                         @AuthenticationPrincipal UsuarioAutenticado aluno) {
        return TransacaoDTO.from(resgateService.resgatarVantagem(aluno.id(), vantagemId));
    }

    @GetMapping("/extrato")
    public ExtratoDTO consultarExtrato(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ExtratoDTO.from(extratoService.consultarExtrato(usuario.id()));
    }
}
