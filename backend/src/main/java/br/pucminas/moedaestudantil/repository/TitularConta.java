package br.pucminas.moedaestudantil.repository;

/** Projeção usada pelo extrato: o nome do dono de cada conta, sem carregar as entidades completas. */
public interface TitularConta {

    Long getContaId();

    String getNome();
}
