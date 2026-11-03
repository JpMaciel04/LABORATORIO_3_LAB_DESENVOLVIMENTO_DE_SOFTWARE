package br.pucminas.moedaestudantil.dto;

import br.pucminas.moedaestudantil.model.Extrato;

import java.util.List;

/** Saldo atual e transações da conta, da mais recente para a mais antiga (RN08). */
public record ExtratoDTO(int saldoAtual, List<TransacaoDTO> transacoes) {

    public static ExtratoDTO from(Extrato extrato) {
        return new ExtratoDTO(extrato.getSaldoAtual(),
                extrato.getTransacoes().stream().map(transacao -> TransacaoDTO.from(transacao, extrato)).toList());
    }
}
