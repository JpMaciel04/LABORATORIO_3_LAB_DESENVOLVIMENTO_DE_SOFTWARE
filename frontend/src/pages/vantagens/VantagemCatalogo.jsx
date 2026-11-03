import { useEffect, useState } from 'react';
import Alerta from '../../components/Alerta.jsx';
import { extrairErro, urlDaFoto } from '../../services/api.js';
import { transacaoService } from '../../services/transacaoService.js';
import { vantagemService } from '../../services/vantagemService.js';
import { formatarMoedas } from '../../utils/formatos.js';

/** Catálogo de vantagens e resgate pelo aluno (US06, US07). */
export default function VantagemCatalogo() {
  const [vantagens, setVantagens] = useState([]);
  const [saldo, setSaldo] = useState(0);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');
  const [cupom, setCupom] = useState(null);
  const [resgatandoId, setResgatandoId] = useState(null);

  useEffect(() => {
    Promise.all([vantagemService.listar(), transacaoService.consultarExtrato()])
      .then(([lista, extrato]) => {
        setVantagens(lista);
        setSaldo(extrato.saldoAtual);
      })
      .catch((e) => setErro(extrairErro(e).mensagem))
      .finally(() => setCarregando(false));
  }, []);

  async function resgatar(vantagem) {
    const confirmacao = `Trocar ${formatarMoedas(vantagem.custoMoedas)} por "${vantagem.nome}"?`;
    if (!window.confirm(confirmacao)) return;

    setResgatandoId(vantagem.id);
    setErro('');
    setCupom(null);
    try {
      const resgate = await transacaoService.resgatarVantagem(vantagem.id);
      setSaldo((atual) => atual - resgate.valor);
      setCupom(resgate);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } catch (e) {
      setErro(extrairErro(e).mensagem);
    } finally {
      setResgatandoId(null);
    }
  }

  return (
    <section>
      <div className="cabecalho-pagina">
        <div>
          <h1>Vantagens</h1>
          <p>Troque suas moedas por produtos e descontos das empresas parceiras.</p>
        </div>
        {!carregando && (
          <div className="saldo">
            <span className="saldo__rotulo">Seu saldo</span>
            <strong className="saldo__valor">{formatarMoedas(saldo)}</strong>
          </div>
        )}
      </div>

      {cupom && (
        <Alerta tipo="sucesso" aoFechar={() => setCupom(null)}>
          Resgate de "{cupom.vantagem}" confirmado! Código do cupom: <strong className="cupom">{cupom.codigoCupom}</strong>.
          Enviamos o cupom para o seu e-mail; apresente o código na troca presencial.
        </Alerta>
      )}
      <Alerta aoFechar={() => setErro('')}>{erro}</Alerta>

      {carregando ? (
        <p className="carregando">Carregando vantagens…</p>
      ) : vantagens.length === 0 ? (
        !erro && (
          <div className="vazio">
            <p>Nenhuma vantagem disponível no momento.</p>
          </div>
        )
      ) : (
        <div className="catalogo">
          {vantagens.map((vantagem) => {
            const faltam = vantagem.custoMoedas - saldo;
            return (
              <article key={vantagem.id} className="vantagem">
                <img className="vantagem__foto" src={urlDaFoto(vantagem.fotoUrl)} alt="" />
                <div className="vantagem__corpo">
                  <h2>{vantagem.nome}</h2>
                  <p className="vantagem__empresa">{vantagem.empresa}</p>
                  <p>{vantagem.descricao}</p>
                  <div className="vantagem__rodape">
                    <strong className="moedas">{formatarMoedas(vantagem.custoMoedas)}</strong>
                    {faltam > 0 ? (
                      <span className="selo selo--neutro">Faltam {faltam.toLocaleString('pt-BR')}</span>
                    ) : (
                      <button type="button" className="botao botao--pequeno" disabled={resgatandoId !== null}
                              onClick={() => resgatar(vantagem)}>
                        {resgatandoId === vantagem.id ? 'Resgatando…' : 'Resgatar'}
                      </button>
                    )}
                  </div>
                </div>
              </article>
            );
          })}
        </div>
      )}
    </section>
  );
}
