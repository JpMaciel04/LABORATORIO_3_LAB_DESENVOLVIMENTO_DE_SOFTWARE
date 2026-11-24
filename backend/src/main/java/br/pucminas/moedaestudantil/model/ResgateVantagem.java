package br.pucminas.moedaestudantil.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.security.SecureRandom;

/** Troca de moedas do aluno por uma vantagem, identificada por um código de cupom único (RN10, RN11). */
@Entity
@DiscriminatorValue("RESGATE_VANTAGEM")
public class ResgateVantagem extends Transacao {

    /** Sem os caracteres que se confundem na conferência presencial: 0/O e 1/I. */
    private static final String ALFABETO_CUPOM = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int GRUPOS_CUPOM = 3;
    private static final int CARACTERES_POR_GRUPO = 4;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    @Column(name = "codigo_cupom", unique = true, length = 20)
    private String codigoCupom;

    @ManyToOne
    @JoinColumn(name = "vantagem_id")
    private Vantagem vantagem;

    protected ResgateVantagem() {
    }

    public ResgateVantagem(Aluno aluno, Vantagem vantagem) {
        this(aluno, vantagem, gerarCodigoCupom());
    }

    public ResgateVantagem(Aluno aluno, Vantagem vantagem, String codigoCupom) {
        super(vantagem.getCustoMoedas(), aluno.getConta(), null);
        this.vantagem = vantagem;
        this.codigoCupom = codigoCupom;
    }

    /**
     * GRASP Especialista na Informação e Alta Coesão: a própria entidade ResgateVantagem
     * concentra a regra de geração do código de cupom no formato XXXX-XXXX-XXXX (RN11).
     */
    public static String gerarCodigoCupom() {
        StringBuilder codigo = new StringBuilder();
        for (int grupo = 0; grupo < GRUPOS_CUPOM; grupo++) {
            if (grupo > 0) {
                codigo.append('-');
            }
            for (int i = 0; i < CARACTERES_POR_GRUPO; i++) {
                codigo.append(ALFABETO_CUPOM.charAt(ALEATORIO.nextInt(ALFABETO_CUPOM.length())));
            }
        }
        return codigo.toString();
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
