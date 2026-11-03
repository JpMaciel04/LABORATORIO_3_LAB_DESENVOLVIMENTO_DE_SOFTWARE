package br.pucminas.moedaestudantil.service;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** Adia efeitos que o banco não consegue desfazer (e-mails, remoção de arquivos) até a transação ser confirmada. */
final class AposCommit {

    private AposCommit() {
    }

    static void executar(Runnable acao) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            acao.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                acao.run();
            }
        });
    }
}
