package br.pucminas.moedaestudantil.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.time.LocalDate;

/** Crédito de moedas ao professor no início de cada semestre (RN04). */
@Entity
@DiscriminatorValue("CREDITO_SEMESTRAL")
public class CreditoSemestral extends Transacao {

    public static final int MOEDAS_POR_SEMESTRE = 1000;

    /** Semestre no formato AAAA-S, por exemplo "2026-2". */
    @Column(length = 6)
    private String semestre;

    protected CreditoSemestral() {
    }

    public CreditoSemestral(Professor professor, String semestre) {
        super(MOEDAS_POR_SEMESTRE, null, professor.getConta());
        this.semestre = semestre;
    }

    /** Janeiro a junho formam o semestre 1; julho a dezembro, o semestre 2. */
    public static String semestreDe(LocalDate data) {
        return data.getYear() + "-" + (data.getMonthValue() <= 6 ? 1 : 2);
    }

    @Override
    public String getDescricao() {
        return "Crédito semestral " + semestre;
    }

    public String getSemestre() {
        return semestre;
    }
}
