import api from './api';

const RECURSO = '/vantagens';

/** Requisição multipart: a parte "dados" leva o JSON e a parte "foto", o arquivo (opcional na alteração). */
function paraFormData(vantagem, foto) {
  const corpo = new FormData();
  corpo.append('dados', new Blob([JSON.stringify(vantagem)], { type: 'application/json' }));
  if (foto) corpo.append('foto', foto);
  return corpo;
}

export const vantagemService = {
  listar: () => api.get(RECURSO).then((resposta) => resposta.data),
  listarMinhas: () => api.get(`${RECURSO}/minhas`).then((resposta) => resposta.data),
  buscar: (id) => api.get(`${RECURSO}/${id}`).then((resposta) => resposta.data),
  cadastrar: (vantagem, foto) => api.post(RECURSO, paraFormData(vantagem, foto)).then((resposta) => resposta.data),
  atualizar: (id, vantagem, foto) =>
    api.put(`${RECURSO}/${id}`, paraFormData(vantagem, foto)).then((resposta) => resposta.data),
  excluir: (id) => api.delete(`${RECURSO}/${id}`),
};
