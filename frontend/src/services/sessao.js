const CHAVE = 'moeda-estudantil.sessao';

/**
 * Token JWT e dados do usuário logado (componente "Sessão" do diagrama de componentes).
 * O sessionStorage é separado por aba: a sessão termina ao fechar a aba, e abas diferentes
 * podem estar logadas com usuários diferentes.
 */
export const sessao = {
  ler() {
    try {
      return JSON.parse(sessionStorage.getItem(CHAVE));
    } catch {
      return null;
    }
  },
  salvar(dados) {
    sessionStorage.setItem(CHAVE, JSON.stringify(dados));
  },
  limpar() {
    sessionStorage.removeItem(CHAVE);
  },
};
