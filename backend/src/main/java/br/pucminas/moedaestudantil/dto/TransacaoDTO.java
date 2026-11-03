package br.pucminas.moedaestudantil.dto;

import br.pucminas.moedaestudantil.model.CreditoSemestral;
import br.pucminas.moedaestudantil.model.EnvioMoedas;
import br.pucminas.moedaestudantil.model.Extrato;
import br.pucminas.moedaestudantil.model.ResgateVantagem;
import br.pucminas.moedaestudantil.model.Transacao;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * Uma movimentação de moedas. Os campos específicos de cada tipo ficam fora do JSON nos demais tipos.
 *
 * @param entrada verdadeiro quando as moedas entraram na conta consultada; nulo fora do extrato
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TransacaoDTO(
        Long id,
        Tipo tipo,
        LocalDateTime dataHora,
        int valor,
        Boolean entrada,
        String descricao,
        String semestre,
        String professor,
        String aluno,
        String motivo,
        String vantagem,
        String empresa,
        String codigoCupom) {

    public enum Tipo {
        CREDITO_SEMESTRAL,
        ENVIO_MOEDAS,
        RESGATE_VANTAGEM
    }

    /** Linha do extrato (RN08), com os nomes de quem enviou e de quem recebeu. */
    public static TransacaoDTO from(Transacao transacao, Extrato extrato) {
        return montar(transacao, extrato.isEntrada(transacao), extrato.getTitular(transacao.getContaOrigem()),
                extrato.getTitular(transacao.getContaDestino()));
    }

    /** Confirmação de um envio ou resgate: quem chamou a API já conhece as partes envolvidas. */
    public static TransacaoDTO from(Transacao transacao) {
        return montar(transacao, null, null, null);
    }

    private static TransacaoDTO montar(Transacao transacao, Boolean entrada, String titularOrigem, String titularDestino) {
        return switch (transacao) {
            case CreditoSemestral credito -> new TransacaoDTO(credito.getId(), Tipo.CREDITO_SEMESTRAL, credito.getDataHora(),
                    credito.getValor(), entrada, credito.getDescricao(), credito.getSemestre(), null, null, null, null, null, null);
            case EnvioMoedas envio -> new TransacaoDTO(envio.getId(), Tipo.ENVIO_MOEDAS, envio.getDataHora(),
                    envio.getValor(), entrada, envio.getDescricao(), null, titularOrigem, titularDestino, envio.getMotivo(), null, null, null);
            case ResgateVantagem resgate -> new TransacaoDTO(resgate.getId(), Tipo.RESGATE_VANTAGEM, resgate.getDataHora(),
                    resgate.getValor(), entrada, resgate.getDescricao(), null, null, titularOrigem, null,
                    resgate.getVantagem().getNome(), resgate.getVantagem().getEmpresa().getNome(), resgate.getCodigoCupom());
            default -> throw new IllegalArgumentException("Tipo de transação desconhecido: " + transacao.getClass());
        };
    }
}
