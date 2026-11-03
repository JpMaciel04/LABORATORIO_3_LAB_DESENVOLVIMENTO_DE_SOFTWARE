import axios from 'axios';
import { sessao } from './sessao';

const URL_BASE = import.meta.env.VITE_API_URL ?? '/api';

/** Cliente HTTP único da aplicação (componente "Cliente HTTP (Axios)" do diagrama de componentes). */
const api = axios.create({ baseURL: URL_BASE });

api.interceptors.request.use((config) => {
  const token = sessao.ler()?.token;
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

/** Token vencido ou conta encerrada: a sessão acabou e é preciso entrar novamente. */
api.interceptors.response.use(undefined, (erro) => {
  if (erro.response?.status === 401 && sessao.ler()) {
    sessao.limpar();
    window.location.assign('/login');
  }
  return Promise.reject(erro);
});

export default api;

/** A API devolve "/api/fotos/arquivo.png"; aqui o prefixo é trocado pela URL base configurada. */
export const urlDaFoto = (fotoUrl) => URL_BASE + fotoUrl.replace(/^\/api/, '');

/**
 * Converte o erro do Axios no formato usado pelas telas.
 * O back-end responde com { status, mensagem, campos: { "cpf": "...", "endereco.cep": "..." } }.
 */
export function extrairErro(erro) {
  const corpo = erro.response?.data;
  if (corpo?.mensagem) {
    return { mensagem: corpo.mensagem, campos: corpo.campos ?? {} };
  }
  if (!erro.response) {
    return {
      mensagem: 'Não foi possível conectar ao servidor. Verifique se o back-end está em execução.',
      campos: {},
    };
  }
  return { mensagem: `Erro inesperado (HTTP ${erro.response.status}).`, campos: {} };
}
