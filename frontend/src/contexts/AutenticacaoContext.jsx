import { createContext, useContext, useState } from 'react';
import { autenticacaoService } from '../services/autenticacaoService.js';
import { sessao } from '../services/sessao.js';

const AutenticacaoContext = createContext(null);

/** Disponibiliza o usuário logado para todas as telas e mantém a sessão salva em dia. */
export function AutenticacaoProvider({ children }) {
  const [usuario, setUsuario] = useState(() => sessao.ler()?.usuario ?? null);
  const [avisoDeSaida, setAvisoDeSaida] = useState('');

  async function entrar(email, senha) {
    const { token, ...dados } = await autenticacaoService.login(email, senha);
    sessao.salvar({ token, usuario: dados });
    setAvisoDeSaida('');
    setUsuario(dados);
  }

  /** @param aviso mensagem exibida na tela de login, para onde a rota protegida redireciona */
  function sair(aviso = '') {
    sessao.limpar();
    setAvisoDeSaida(aviso);
    setUsuario(null);
  }

  /** Mantém o nome exibido no topo em dia depois que o usuário altera o próprio cadastro. */
  function atualizarUsuario(dados) {
    const atual = sessao.ler();
    const novoUsuario = { ...atual.usuario, ...dados };
    sessao.salvar({ ...atual, usuario: novoUsuario });
    setUsuario(novoUsuario);
  }

  return (
    <AutenticacaoContext.Provider value={{ usuario, avisoDeSaida, entrar, sair, atualizarUsuario }}>
      {children}
    </AutenticacaoContext.Provider>
  );
}

export const useAutenticacao = () => useContext(AutenticacaoContext);
