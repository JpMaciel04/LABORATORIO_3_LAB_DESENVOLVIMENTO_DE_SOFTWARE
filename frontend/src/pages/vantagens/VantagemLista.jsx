import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Alerta from '../../components/Alerta.jsx';
import { useMensagemFlash } from '../../hooks/useMensagemFlash.js';
import { extrairErro, urlDaFoto } from '../../services/api.js';
import { vantagemService } from '../../services/vantagemService.js';
import { formatarMoedas } from '../../utils/formatos.js';

/** Vantagens da empresa logada, inclusive as desativadas (US14). */
export default function VantagemLista() {
  const [vantagens, setVantagens] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');
  const [sucesso, setSucesso] = useMensagemFlash();

  useEffect(() => {
    vantagemService.listarMinhas()
      .then(setVantagens)
      .catch((e) => setErro(extrairErro(e).mensagem))
      .finally(() => setCarregando(false));
  }, []);

  async function desativar(vantagem) {
    const confirmacao = `Desativar "${vantagem.nome}"? Ela deixará de aparecer para os alunos, mas continuará nos extratos de quem já a resgatou.`;
    if (!window.confirm(confirmacao)) return;

    try {
      await vantagemService.excluir(vantagem.id);
      setVantagens((lista) => lista.map((v) => (v.id === vantagem.id ? { ...v, ativa: false } : v)));
      setErro('');
      setSucesso(`Vantagem "${vantagem.nome}" desativada.`);
    } catch (e) {
      setErro(extrairErro(e).mensagem);
    }
  }

  return (
    <section>
      <div className="cabecalho-pagina">
        <div>
          <h1>Minhas vantagens</h1>
          <p>Produtos e descontos que sua empresa oferece aos alunos em troca de moedas.</p>
        </div>
        <Link to="/minhas-vantagens/nova" className="botao">Nova vantagem</Link>
      </div>

      <Alerta tipo="sucesso" aoFechar={() => setSucesso('')}>{sucesso}</Alerta>
      <Alerta aoFechar={() => setErro('')}>{erro}</Alerta>

      {carregando ? (
        <p className="carregando">Carregando vantagens…</p>
      ) : vantagens.length === 0 ? (
        !erro && (
          <div className="vazio">
            <p>Sua empresa ainda não oferece nenhuma vantagem.</p>
            <Link to="/minhas-vantagens/nova" className="botao">Cadastrar a primeira vantagem</Link>
          </div>
        )
      ) : (
        <div className="tabela-container">
          <table className="tabela">
            <thead>
              <tr>
                <th>Foto</th>
                <th>Nome</th>
                <th>Situação</th>
                <th className="numero">Custo</th>
                <th><span className="sr-only">Ações</span></th>
              </tr>
            </thead>
            <tbody>
              {vantagens.map((vantagem) => (
                <tr key={vantagem.id}>
                  <td><img className="miniatura" src={urlDaFoto(vantagem.fotoUrl)} alt="" /></td>
                  <td data-rotulo="Nome">{vantagem.nome}</td>
                  <td data-rotulo="Situação">
                    <span className={`selo ${vantagem.ativa ? 'selo--ativo' : 'selo--neutro'}`}>
                      {vantagem.ativa ? 'Ativa' : 'Desativada'}
                    </span>
                  </td>
                  <td data-rotulo="Custo" className="numero">{formatarMoedas(vantagem.custoMoedas)}</td>
                  <td>
                    <div className="acoes">
                      <Link to={`/minhas-vantagens/${vantagem.id}/editar`} className="botao botao--pequeno botao--secundario">
                        Editar
                      </Link>
                      {vantagem.ativa && (
                        <button type="button" className="botao botao--pequeno botao--perigo" onClick={() => desativar(vantagem)}>
                          Desativar
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}
