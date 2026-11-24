package br.pucminas.moedaestudantil.model;

import br.pucminas.moedaestudantil.exception.RegraNegocioException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/** Professor pré-cadastrado a partir da lista enviada pela instituição (RN03). */
@Entity
@Table(name = "professor")
@DiscriminatorValue("PROFESSOR")
public class Professor extends Usuario {

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(nullable = false, length = 100)
    private String departamento;

    @ManyToOne(optional = false)
    @JoinColumn(name = "instituicao_id", nullable = false)
    private InstituicaoEnsino instituicao;

    @OneToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "conta_id", nullable = false, unique = true)
    private Conta conta = new Conta();

    @Override
    public Perfil getPerfil() {
        return Perfil.PROFESSOR;
    }

    /**
     * GRASP Especialista na Informação: o professor detém a referência para sua instituição
     * e verifica se o aluno pertence à mesma instituição de ensino (RN06).
     */
    public boolean pertenceAMesmaInstituicao(Aluno aluno) {
        return aluno != null
                && this.instituicao != null
                && aluno.getInstituicao() != null
                && this.instituicao.getId().equals(aluno.getInstituicao().getId());
    }

    /**
     * GRASP Especialista na Informação e Criador: valida RN06, debita o saldo do professor (RN05),
     * credita o aluno e cria a transação EnvioMoedas.
     */
    public EnvioMoedas enviarMoedas(Aluno aluno, int valor, String motivo) {
        if (!pertenceAMesmaInstituicao(aluno)) {
            throw new RegraNegocioException("alunoId", "Só é possível enviar moedas a alunos da sua instituição.");
        }
        this.conta.debitar(valor);
        aluno.receberMoedas(valor);
        return new EnvioMoedas(this, aluno, valor, motivo);
    }

    /**
     * GRASP Especialista na Informação e Criador: credita as moedas do semestre na conta do professor
     * e cria a transação CreditoSemestral (RN04).
     */
    public CreditoSemestral receberCreditoSemestral(String semestre) {
        this.conta.creditar(CreditoSemestral.MOEDAS_POR_SEMESTRE);
        return new CreditoSemestral(this, semestre);
    }

    public String getCpf() {
        return cpf;
    }

    public String getDepartamento() {
        return departamento;
    }

    public InstituicaoEnsino getInstituicao() {
        return instituicao;
    }

    @Override
    public Conta getConta() {
        return conta;
    }
}
