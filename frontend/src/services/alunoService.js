import api from './api';

const RECURSO = '/alunos';

export const alunoService = {
  listar: () => api.get(RECURSO).then((resposta) => resposta.data),
  buscar: (id) => api.get(`${RECURSO}/${id}`).then((resposta) => resposta.data),
  cadastrar: (aluno) => api.post(RECURSO, aluno).then((resposta) => resposta.data),
  atualizar: (id, aluno) => api.put(`${RECURSO}/${id}`, aluno).then((resposta) => resposta.data),
  excluir: (id) => api.delete(`${RECURSO}/${id}`),
};
