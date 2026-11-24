package br.pucminas.moedaestudantil.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/** Aluno cadastrado no sistema de mérito (RN01). */
@Entity
@Table(name = "aluno")
@DiscriminatorValue("ALUNO")
public class Aluno extends Usuario {

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(nullable = false, length = 20)
    private String rg;

    @Column(nullable = false, length = 100)
    private String curso;

    @Embedded
    private Endereco endereco;

    @ManyToOne(optional = false)
    @JoinColumn(name = "instituicao_id", nullable = false)
    private InstituicaoEnsino instituicao;

    /** A conta é criada junto com o aluno, com saldo 0, e persistida em cascata. */
    @OneToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "conta_id", nullable = false, unique = true)
    private Conta conta = new Conta();

    @Override
    public Perfil getPerfil() {
        return Perfil.ALUNO;
    }

    /**
     * GRASP Especialista na Informação: encapsula o crédito de moedas na conta do aluno.
     */
    public void receberMoedas(int valor) {
        this.conta.creditar(valor);
    }

    /**
     * GRASP Especialista na Informação e Criador: debita o custo da vantagem do saldo do aluno (RN10)
     * e instancia a transação ResgateVantagem com código de cupom único (RN11).
     */
    public ResgateVantagem resgatar(Vantagem vantagem) {
        this.conta.debitar(vantagem.getCustoMoedas());
        return new ResgateVantagem(this, vantagem);
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getRg() {
        return rg;
    }

    public void setRg(String rg) {
        this.rg = rg;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public InstituicaoEnsino getInstituicao() {
        return instituicao;
    }

    public void setInstituicao(InstituicaoEnsino instituicao) {
        this.instituicao = instituicao;
    }

    @Override
    public Conta getConta() {
        return conta;
    }
}
