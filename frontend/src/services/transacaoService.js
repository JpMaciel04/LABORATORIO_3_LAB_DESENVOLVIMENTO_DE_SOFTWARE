import api from './api';

const RECURSO = '/transacoes';

export const transacaoService = {
  enviarMoedas: (envio) => api.post(`${RECURSO}/envios`, envio).then((resposta) => resposta.data),
  resgatarVantagem: (vantagemId) => api.post(`${RECURSO}/resgates/${vantagemId}`).then((resposta) => resposta.data),
  consultarExtrato: () => api.get(`${RECURSO}/extrato`).then((resposta) => resposta.data),
};
