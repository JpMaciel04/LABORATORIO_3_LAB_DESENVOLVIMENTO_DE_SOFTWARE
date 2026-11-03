import { NavLink, Outlet } from 'react-router-dom';
import { useAutenticacao } from '../contexts/AutenticacaoContext.jsx';

const MENUS = {
  ALUNO: [
    { rota: '/vantagens', rotulo: 'Vantagens' },
    { rota: '/extrato', rotulo: 'Extrato' },
    { rota: '/empresas', rotulo: 'Empresas parceiras' },
    { rota: '/aluno/perfil', rotulo: 'Meu cadastro' },
  ],
  PROFESSOR: [
    { rota: '/moedas/enviar', rotulo: 'Enviar moedas' },
    { rota: '/alunos', rotulo: 'Alunos' },
    { rota: '/extrato', rotulo: 'Extrato' },
    { rota: '/empresas', rotulo: 'Empresas parceiras' },
  ],
  EMPRESA: [
    { rota: '/minhas-vantagens', rotulo: 'Minhas vantagens' },
    { rota: '/empresa/perfil', rotulo: 'Minha empresa' },
  ],
};

export default function Layout() {
  const { usuario, sair } = useAutenticacao();

  return (
    <>
      <header className="topo">
        <div className="topo__conteudo">
          <NavLink to="/" className="marca">
            <span className="marca__moeda" aria-hidden="true">M</span>
            Moeda Estudantil
          </NavLink>
          <nav className="menu" aria-label="Navegação principal">
            {usuario ? (
              MENUS[usuario.perfil].map(({ rota, rotulo }) => (
                <NavLink key={rota} to={rota}>{rotulo}</NavLink>
              ))
            ) : (
              <NavLink to="/login">Entrar</NavLink>
            )}
          </nav>
          {usuario && (
            <div className="topo__usuario">
              <span>{usuario.nome}</span>
              <button type="button" className="botao botao--pequeno botao--topo" onClick={() => sair()}>Sair</button>
            </div>
          )}
        </div>
      </header>

      <main className="conteudo">
        <Outlet />
      </main>

      <footer className="rodape">
        PUC Minas · Projeto de Software · Lab03 — Sprint 3
      </footer>
    </>
  );
}
