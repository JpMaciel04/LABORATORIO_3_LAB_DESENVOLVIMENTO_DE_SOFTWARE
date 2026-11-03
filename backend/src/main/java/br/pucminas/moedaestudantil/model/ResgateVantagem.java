package br.pucminas.moedaestudantil.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/** Troca de moedas do aluno por uma vantagem, identificada por um código de cupom único (RN10, RN11). */
@Entity
@DiscriminatorValue("RESGATE_VANTAGEM")
public class ResgateVantagem extends Transacao {

    @Column(name = "codigo_cupom", unique = true, length = 20)
    private String codigoCupom;

    @ManyToOne
    @JoinColumn(name = "vantagem_id")
    private Vantagem vantagem;

    protected ResgateVantagem() {
    }

    public ResgateVantagem(Aluno aluno, Vantagem vantagem, String codigoCupom) {
        super(vantagem.getCustoMoedas(), aluno.getConta(), null);
        this.vantagem = vantagem;
        this.codigoCupom = codigoCupom;
    }

    @Override
    public String getDescricao() {
        return "Resgate: " + vantagem.getNome();
    }

    public String getCodigoCupom() {
        return codigoCupom;
    }

    public Vantagem getVantagem() {
        return vantagem;
    }
}
