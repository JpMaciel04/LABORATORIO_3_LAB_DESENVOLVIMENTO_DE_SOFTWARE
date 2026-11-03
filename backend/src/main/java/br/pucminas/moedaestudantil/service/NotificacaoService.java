package br.pucminas.moedaestudantil.service;

import br.pucminas.moedaestudantil.model.Aluno;
import br.pucminas.moedaestudantil.model.EnvioMoedas;
import br.pucminas.moedaestudantil.model.Professor;
import br.pucminas.moedaestudantil.model.ResgateVantagem;

/**
 * Notificações do sistema (UC10). As implementações entregam a mensagem somente depois que a transação
 * for confirmada, e uma falha na entrega não desfaz a operação (US04).
 */
public interface NotificacaoService {

    void notificarRecebimentoMoedas(EnvioMoedas envio, Professor professor, Aluno aluno);

    void enviarCupomAluno(ResgateVantagem resgate, Aluno aluno);

    void notificarEmpresaParceira(ResgateVantagem resgate, Aluno aluno);
}
