package br.pucminas.moedaestudantil.model;

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

    public String getCpf() {
        return cpf;
    }

    public String getDepartamento() {
        return departamento;
    }

    public InstituicaoEnsino getInstituicao() {
        return instituicao;
    }

    public Conta getConta() {
        return conta;
    }
}
