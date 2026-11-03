import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Alerta from '../../components/Alerta.jsx';
import { useAutenticacao } from '../../contexts/AutenticacaoContext.jsx';
import { useMensagemFlash } from '../../hooks/useMensagemFlash.js';
import { extrairErro } from '../../services/api.js';
import { empresaService } from '../../services/empresaService.js';
import { mascaraCnpj } from '../../utils/formatos.js';

export default function EmpresaPerfil() {
  const { usuario, sair } = useAutenticacao();
  const [empresa, setEmpresa] = useState(null);
  const [erro, setErro] = useState('');
  const [sucesso, setSucesso] = useMensagemFlash();

  useEffect(() => {
    empresaService.buscar(usuario.id)
      .then(setEmpresa)
      .catch((e) => setErro(extrairErro(e).mensagem));
  }, [usuario.id]);

  async function excluir() {
    const confirmacao = 'Excluir a empresa? Suas vantagens deixarão de ser exibidas aos alunos e esta ação não pode ser desfeita.';
    if (!window.confirm(confirmacao)) return;

    try {
      await empresaService.excluir(usuario.id);
      sair('A empresa foi excluída.');
    } catch (e) {
      setErro(extrairErro(e).mensagem);
    }
  }

  return (
    <section>
      <div className="cabecalho-pagina">
        <div>
          <h1>Minha empresa</h1>
          <p>Somente sua empresa pode alterar estes dados.</p>
        </div>
        {empresa && (
          <div className="cartao__acoes">
            <Link to="/empresa/perfil/editar" className="botao">Alterar dados</Link>
            <button type="button" className="botao botao--perigo" onClick={excluir}>Excluir empresa</button>
          </div>
        )}
      </div>

      <Alerta tipo="sucesso" aoFechar={() => setSucesso('')}>{sucesso}</Alerta>
      <Alerta aoFechar={() => setErro('')}>{erro}</Alerta>

      {!empresa ? (
        !erro && <p className="carregando">Carregando…</p>
      ) : (
        <div className="painel">
          <dl className="detalhes">
            <div><dt>Nome</dt><dd>{empresa.nome}</dd></div>
            <div><dt>CNPJ</dt><dd className="mono">{mascaraCnpj(empresa.cnpj)}</dd></div>
            <div><dt>E-mail (login)</dt><dd>{empresa.email}</dd></div>
          </dl>
        </div>
      )}
    </section>
  );
}
