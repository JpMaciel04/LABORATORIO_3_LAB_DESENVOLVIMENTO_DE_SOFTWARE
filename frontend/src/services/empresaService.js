import api from './api';

const RECURSO = '/empresas';

export const empresaService = {
  listar: () => api.get(RECURSO).then((resposta) => resposta.data),
  buscar: (id) => api.get(`${RECURSO}/${id}`).then((resposta) => resposta.data),
  cadastrar: (empresa) => api.post(RECURSO, empresa).then((resposta) => resposta.data),
  atualizar: (id, empresa) => api.put(`${RECURSO}/${id}`, empresa).then((resposta) => resposta.data),
  excluir: (id) => api.delete(`${RECURSO}/${id}`),
};
