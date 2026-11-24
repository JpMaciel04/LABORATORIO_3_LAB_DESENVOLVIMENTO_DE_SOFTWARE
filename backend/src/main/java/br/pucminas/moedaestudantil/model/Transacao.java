package br.pucminas.moedaestudantil.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Movimentação de moedas entre contas.
 * Herança mapeada com SINGLE_TABLE: o extrato lê todos os tipos em uma única consulta, sem junções.
 */
@Entity
@Table(name = "transacao")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING, length = 30)
public abstract class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int valor;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @ManyToOne
    @JoinColumn(name = "conta_origem_id")
    private Conta contaOrigem;

    @ManyToOne
    @JoinColumn(name = "conta_destino_id")
    private Conta contaDestino;

    protected Transacao() {
    }

    protected Transacao(int valor, Conta contaOrigem, Conta contaDestino) {
        this.valor = valor;
        this.contaOrigem = contaOrigem;
        this.contaDestino = contaDestino;
        this.dataHora = LocalDateTime.now();
    }

    public abstract String getDescricao();

    /**
     * GRASP Especialista na Informação: a própria transação sabe se tem determinada conta como destino.
     */
    public boolean temComoDestino(Conta conta) {
        return contaDestino != null && conta != null && contaDestino.getId().equals(conta.getId());
    }

    /**
     * GRASP Especialista na Informação: fornece os IDs das contas envolvidas na transação.
     */
    public Set<Long> getIdsContasEnvolvidas() {
        Set<Long> ids = new HashSet<>();
        if (contaOrigem != null && contaOrigem.getId() != null) {
            ids.add(contaOrigem.getId());
        }
        if (contaDestino != null && contaDestino.getId() != null) {
            ids.add(contaDestino.getId());
        }
        return ids;
    }

    public Long getId() {
        return id;
    }

    public int getValor() {
        return valor;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public Conta getContaOrigem() {
        return contaOrigem;
    }

    public Conta getContaDestino() {
        return contaDestino;
    }
}
