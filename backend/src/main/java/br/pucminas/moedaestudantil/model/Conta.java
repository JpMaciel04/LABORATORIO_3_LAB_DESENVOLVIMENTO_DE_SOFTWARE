package br.pucminas.moedaestudantil.model;

import br.pucminas.moedaestudantil.exception.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/** Saldo de moedas de um aluno ou professor. O saldo nunca fica negativo. */
@Entity
@Table(name = "conta")
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int saldo;

    /** Bloqueio otimista: duas operações simultâneas não podem gastar o mesmo saldo (RN05, RN10). */
    @Version
    @Column(nullable = false)
    private long versao;

    public void creditar(int valor) {
        validarValor(valor);
        saldo += valor;
    }

    public void debitar(int valor) {
        validarValor(valor);
        if (!possuiSaldo(valor)) {
            throw new RegraNegocioException("Saldo insuficiente: disponível " + saldo + ", necessário " + valor + ".");
        }
        saldo -= valor;
    }

    public boolean possuiSaldo(int valor) {
        return saldo >= valor;
    }

    private static void validarValor(int valor) {
        if (valor <= 0) {
            throw new RegraNegocioException("A quantidade de moedas deve ser um inteiro positivo.");
        }
    }

    public Long getId() {
        return id;
    }

    public int getSaldo() {
        return saldo;
    }
}
