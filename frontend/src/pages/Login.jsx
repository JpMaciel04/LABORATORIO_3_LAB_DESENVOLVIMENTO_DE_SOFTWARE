import { useEffect, useState } from 'react';
import { Link, Navigate, useLocation } from 'react-router-dom';
import Alerta from '../components/Alerta.jsx';
import Campo from '../components/Campo.jsx';
import { useAutenticacao } from '../contexts/AutenticacaoContext.jsx';
import { useMensagemFlash } from '../hooks/useMensagemFlash.js';
import { extrairErro } from '../services/api.js';

export default function Login() {
  const { usuario, avisoDeSaida, entrar } = useAutenticacao();
  const location = useLocation();
  const [sucesso, setSucesso] = useMensagemFlash();

  const [formulario, setFormulario] = useState({ email: '', senha: '' });
  const [erros, setErros] = useState({});
  const [mensagemErro, setMensagemErro] = useState('');
  const [entrando, setEntrando] = useState(false);
  const [demorando, setDemorando] = useState(false);

  // Na nuvem (plano gratuito do Render), a API é desligada após 15 minutos sem acesso e a primeira
  // requisição a liga de novo, o que leva cerca de um minuto: o aviso evita que pareça um travamento.
  useEffect(() => {
    if (!entrando) return undefined;
    const temporizador = setTimeout(() => setDemorando(true), 4000);
    return () => {
      clearTimeout(temporizador);
      setDemorando(false);
    };
  }, [entrando]);

  if (usuario) return <Navigate to={location.state?.destino ?? '/'} replace />;

  const alterar = (campo) => (evento) => {
    setFormulario((atual) => ({ ...atual, [campo]: evento.target.value }));
  };

  async function enviar(evento) {
    evento.preventDefault();
    setEntrando(true);
    setErros({});
    setMensagemErro('');
    setSucesso('');

    try {
      await entrar(formulario.email, formulario.senha);
    } catch (e) {
      const { mensagem, campos } = extrairErro(e);
      setMensagemErro(mensagem);
      setErros(campos);
      setEntrando(false);
    }
  }

  return (
    <section className="login">
      <div className="cabecalho-pagina">
        <div>
          <h1>Entrar</h1>
          <p>Use o e-mail e a senha da sua conta de aluno, professor ou empresa parceira.</p>
        </div>
      </div>

      <Alerta tipo="sucesso" aoFechar={() => setSucesso('')}>{sucesso || avisoDeSaida}</Alerta>
      <Alerta aoFechar={() => setMensagemErro('')}>{mensagemErro}</Alerta>
      <Alerta tipo="info">
        {demorando && 'O servidor estava em repouso e está sendo iniciado. Isso pode levar até um minuto.'}
      </Alerta>

      <form className="formulario" onSubmit={enviar} noValidate>
        <fieldset>
          <legend>Acesso ao sistema</legend>
          <div className="pilha">
            <Campo id="email" rotulo="E-mail" type="email" value={formulario.email} onChange={alterar('email')}
                   erro={erros.email} autoComplete="username" autoFocus />
            <Campo id="senha" rotulo="Senha" type="password" value={formulario.senha} onChange={alterar('senha')}
                   erro={erros.senha} autoComplete="current-password" />
          </div>
        </fieldset>

        <div className="formulario__acoes">
          <button type="submit" className="botao" disabled={entrando}>
            {entrando ? 'Entrando…' : 'Entrar'}
          </button>
        </div>
      </form>

      <p className="login__cadastro">
        Ainda não tem conta? <Link to="/alunos/novo">Cadastre-se como aluno</Link> ou{' '}
        <Link to="/empresas/novo">como empresa parceira</Link>.
      </p>
    </section>
  );
}
