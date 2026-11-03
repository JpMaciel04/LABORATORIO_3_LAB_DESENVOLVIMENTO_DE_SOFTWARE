package br.pucminas.moedaestudantil.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Habilita as tarefas @Scheduled (crédito semestral - RN04). */
@Configuration
@EnableScheduling
public class AgendamentoConfig {
}
