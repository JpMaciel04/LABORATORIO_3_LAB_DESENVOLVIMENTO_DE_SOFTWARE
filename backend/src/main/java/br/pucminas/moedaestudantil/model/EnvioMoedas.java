package br.pucminas.moedaestudantil.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/** Moedas enviadas por um professor a um aluno, com o motivo do reconhecimento (RN05). */
@Entity
@DiscriminatorValue("ENVIO_MOEDAS")
public class EnvioMoedas extends Transacao {

    @Column(length = 500)
    private String motivo;

    protected EnvioMoedas() {
    }

    public EnvioMoedas(Professor professor, Aluno aluno, int valor, String motivo) {
        super(valor, professor.getConta(), aluno.getConta());
        this.motivo = motivo;
    }

    @Override
    public String getDescricao() {
        return "Envio de moedas: " + motivo;
    }

    public String getMotivo() {
        return motivo;
    }
}
