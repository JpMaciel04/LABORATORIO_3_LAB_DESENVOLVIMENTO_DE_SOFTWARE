import api from './api';

export const autenticacaoService = {
  login: (email, senha) => api.post('/auth/login', { email, senha }).then((resposta) => resposta.data),
};
