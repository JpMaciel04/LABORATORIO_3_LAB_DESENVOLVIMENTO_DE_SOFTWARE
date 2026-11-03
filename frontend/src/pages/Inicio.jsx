import { Link, Navigate } from 'react-router-dom';
import { useAutenticacao } from '../contexts/AutenticacaoContext.jsx';

const PAGINA_INICIAL = {
  ALUNO: '/vantagens',
  PROFESSOR: '/moedas/enviar',
  EMPRESA: '/minhas-vantagens',
};

export default function Inicio() {
  const { usuario } = useAutenticacao();

  if (usuario) return <Navigate to={PAGINA_INICIAL[usuario.perfil]} replace />;

  return (
    <section className="inicio">
      <div className="inicio__texto">
        <h1>Reconhecimento do mérito estudantil</h1>
        <p>
          Professores distribuem moedas aos alunos como reconhecimento por participação e bom
          comportamento. Os alunos trocam as moedas por vantagens oferecidas pelas empresas parceiras.
        </p>
      </div>

      <div className="inicio__cartoes">
        <article className="cartao">
          <h2>Sou aluno</h2>
          <p>Cadastre-se informando seus dados e a sua instituição de ensino para começar a receber moedas.</p>
          <div className="cartao__acoes">
            <Link to="/alunos/novo" className="botao">Cadastrar aluno</Link>
            <Link to="/login" className="botao botao--secundario">Entrar</Link>
          </div>
        </article>

        <article className="cartao">
          <h2>Sou empresa parceira</h2>
          <p>Cadastre sua empresa para oferecer produtos e descontos aos alunos em troca de moedas.</p>
          <div className="cartao__acoes">
            <Link to="/empresas/novo" className="botao">Cadastrar empresa</Link>
            <Link to="/login" className="botao botao--secundario">Entrar</Link>
          </div>
        </article>

        <article className="cartao">
          <h2>Sou professor</h2>
          <p>Seu acesso é criado pela sua instituição. Entre para reconhecer seus alunos com moedas.</p>
          <div className="cartao__acoes">
            <Link to="/login" className="botao">Entrar</Link>
          </div>
        </article>
      </div>
    </section>
  );
}
