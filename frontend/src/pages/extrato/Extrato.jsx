import { useEffect, useState } from 'react';
import Alerta from '../../components/Alerta.jsx';
import { extrairErro } from '../../services/api.js';
import { transacaoService } from '../../services/transacaoService.js';
import { formatarDataHora, formatarMoedas } from '../../utils/formatos.js';

const TIPOS = {
  CREDITO_SEMESTRAL: 'Crédito semestral',
  ENVIO_MOEDAS: 'Envio de moedas',
  RESGATE_VANTAGEM: 'Resgate de vantagem',
};

function Detalhes({ transacao }) {
  switch (transacao.tipo) {
    case 'CREDITO_SEMESTRAL':
      return `Moedas do semestre ${transacao.semestre}`;
    case 'ENVIO_MOEDAS':
      return (
        <>
          {transacao.entrada ? `De ${transacao.professor}` : `Para ${transacao.aluno}`}
          <span className="extrato__motivo">Motivo: {transacao.motivo}</span>
        </>
      );
    default:
      return (
        <>
          {transacao.vantagem} · {transacao.empresa}
          <span className="extrato__motivo">Cupom: <strong className="cupom">{transacao.codigoCupom}</strong></span>
        </>
      );
  }
}

/** Saldo e histórico do aluno ou professor logado (RN08). */
export default function Extrato() {
  const [extrato, setExtrato] = useState(null);
  const [erro, setErro] = useState('');

  useEffect(() => {
    transacaoService.consultarExtrato()
      .then(setExtrato)
      .catch((e) => setErro(extrairErro(e).mensagem));
  }, []);

  return (
    <section>
      <div className="cabecalho-pagina">
        <div>
          <h1>Extrato</h1>
          <p>Seu saldo e suas transações, da mais recente para a mais antiga.</p>
        </div>
        {extrato && (
          <div className="saldo">
            <span className="saldo__rotulo">Saldo atual</span>
            <strong className="saldo__valor">{formatarMoedas(extrato.saldoAtual)}</strong>
          </div>
        )}
      </div>

      <Alerta aoFechar={() => setErro('')}>{erro}</Alerta>

      {!extrato ? (
        !erro && <p className="carregando">Carregando extrato…</p>
      ) : extrato.transacoes.length === 0 ? (
        <div className="vazio">
          <p>Nenhuma transação ainda.</p>
        </div>
      ) : (
        <div className="tabela-container">
          <table className="tabela">
            <thead>
              <tr>
                <th>Data</th>
                <th>Tipo</th>
                <th>Detalhes</th>
                <th className="numero">Moedas</th>
              </tr>
            </thead>
            <tbody>
              {extrato.transacoes.map((transacao) => (
                <tr key={transacao.id}>
                  <td data-rotulo="Data" className="mono">{formatarDataHora(transacao.dataHora)}</td>
                  <td data-rotulo="Tipo">{TIPOS[transacao.tipo]}</td>
                  <td data-rotulo="Detalhes"><Detalhes transacao={transacao} /></td>
                  <td data-rotulo="Moedas" className={`numero ${transacao.entrada ? 'valor--entrada' : 'valor--saida'}`}>
                    {transacao.entrada ? '+' : '−'}{transacao.valor.toLocaleString('pt-BR')}
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
