package br.pucminas.moedaestudantil.service;

import br.pucminas.moedaestudantil.model.CreditoSemestral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Dispara o crédito semestral (UC11; RN04). A verificação é diária e o crédito não se repete no semestre, então
 * o primeiro dia do semestre recebe o crédito e os dias seguintes recuperam eventuais falhas e novos professores.
 */
@Component
public class CreditoSemestralScheduler {

    private static final Logger log = LoggerFactory.getLogger(CreditoSemestralScheduler.class);

    private final MoedaService moedaService;
    private final boolean creditarAoIniciar;

    public CreditoSemestralScheduler(MoedaService moedaService,
                                     @Value("${app.credito-semestral.ao-iniciar}") boolean creditarAoIniciar) {
        this.moedaService = moedaService;
        this.creditarAoIniciar = creditarAoIniciar;
    }

    @Scheduled(cron = "${app.credito-semestral.cron}")
    public void executar() {
        String semestre = CreditoSemestral.semestreDe(LocalDate.now());
        int creditados = moedaService.creditarMoedasSemestrais(semestre);
        if (creditados > 0) {
            log.info("Crédito semestral {}: {} professor(es) receberam {} moedas.",
                    semestre, creditados, CreditoSemestral.MOEDAS_POR_SEMESTRE);
        }
    }

    /** Cobre o caso de a aplicação estar fora do ar quando o semestre começou. */
    @EventListener(ApplicationReadyEvent.class)
    public void aoIniciar() {
        if (creditarAoIniciar) {
            executar();
        }
    }
}
