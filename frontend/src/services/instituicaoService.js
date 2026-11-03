import api from './api';

export const instituicaoService = {
  listar: () => api.get('/instituicoes').then((resposta) => resposta.data),
};
