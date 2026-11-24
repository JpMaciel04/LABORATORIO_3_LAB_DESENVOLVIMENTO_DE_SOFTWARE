package br.pucminas.moedaestudantil.model;

import java.util.List;
import java.util.Map;

/** Saldo e transações de uma conta (RN08). Montado em memória pelo ExtratoService; não é persistido. */
public class Extrato {

    private final Conta conta;
    private final List<Transacao> transacoes;
    private final Map<Long, String> titulares;

    /** @param titulares nome do dono de cada conta envolvida nas transações, por id da conta */
    public Extrato(Conta conta, List<Transacao> transacoes, Map<Long, String> titulares) {
        this.conta = conta;
        this.transacoes = transacoes;
        this.titulares = titulares;
    }

    public int getSaldoAtual() {
        return conta.getSaldo();
    }

    public List<Transacao> getTransacoes() {
        return transacoes;
    }

    /** Verdadeiro quando a transação adicionou moedas a esta conta (GRASP Especialista na Informação). */
    public boolean isEntrada(Transacao transacao) {
        return transacao.temComoDestino(conta);
    }

    public String getTitular(Conta outraConta) {
        return outraConta == null ? null : titulares.get(outraConta.getId());
    }
}
