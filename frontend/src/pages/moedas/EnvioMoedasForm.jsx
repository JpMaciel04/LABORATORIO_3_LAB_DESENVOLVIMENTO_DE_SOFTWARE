import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import Alerta from '../../components/Alerta.jsx';
import Campo from '../../components/Campo.jsx';
import { extrairErro } from '../../services/api.js';
import { alunoService } from '../../services/alunoService.js';
import { transacaoService } from '../../services/transacaoService.js';
import { formatarMoedas } from '../../utils/formatos.js';

/** Envio de moedas do professor a um aluno da sua instituição (RN05, RN06). */
export default function EnvioMoedasForm() {
  const [parametros] = useSearchParams();

  const [alunos, setAlunos] = useState([]);
  const [saldo, setSaldo] = useState(0);
  const [formulario, setFormulario] = useState({ alunoId: parametros.get('aluno') ?? '', valor: '', motivo: '' });
  const [erros, setErros] = useState({});
  const [mensagemErro, setMensagemErro] = useState('');
  const [sucesso, setSucesso] = useState('');
  const [carregando, setCarregando] = useState(true);
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    Promise.all([alunoService.listar(), transacaoService.consultarExtrato()])
      .then(([listaAlunos, extrato]) => {
        setAlunos(listaAlunos);
        setSaldo(extrato.saldoAtual);
      })
      .catch((e) => setMensagemErro(extrairErro(e).mensagem))
      .finally(() => setCarregando(false));
  }, []);

  const alterar = (campo) => (evento) => {
    setFormulario((atual) => ({ ...atual, [campo]: evento.target.value }));
  };

  async function enviar(evento) {
    evento.preventDefault();
    setEnviando(true);
    setErros({});
    setMensagemErro('');
    setSucesso('');

    try {
      const envio = await transacaoService.enviarMoedas({
        alunoId: formulario.alunoId ? Number(formulario.alunoId) : null,
        valor: formulario.valor === '' ? null : Number(formulario.valor),
        motivo: formulario.motivo,
      });
      const aluno = alunos.find((a) => a.id === Number(formulario.alunoId));
      setSaldo((atual) => atual - envio.valor);
      setSucesso(`${formatarMoedas(envio.valor)} enviadas a ${aluno.nome}. O aluno será avisado por e-mail.`);
      setFormulario({ alunoId: '', valor: '', motivo: '' });
    } catch (e) {
      const { mensagem, campos } = extrairErro(e);
      setMensagemErro(mensagem);
      setErros(campos);
    } finally {
      setEnviando(false);
    }
  }

  if (carregando) return <p className="carregando">Carregando…</p>;

  return (
    <section>
      <div className="cabecalho-pagina">
        <div>
          <h1>Enviar moedas</h1>
          <p>Reconheça um aluno da sua instituição por bom comportamento ou participação em aula.</p>
        </div>
        <div className="saldo">
          <span className="saldo__rotulo">Seu saldo</span>
          <strong className="saldo__valor">{formatarMoedas(saldo)}</strong>
        </div>
      </div>

      <Alerta tipo="sucesso" aoFechar={() => setSucesso('')}>{sucesso}</Alerta>
      <Alerta aoFechar={() => setMensagemErro('')}>{mensagemErro}</Alerta>

      {alunos.length === 0 && !mensagemErro ? (
        <div className="vazio">
          <p>Nenhum aluno da sua instituição está cadastrado ainda.</p>
          <Link to="/extrato" className="botao botao--secundario">Ver meu extrato</Link>
        </div>
      ) : (
        <form className="formulario" onSubmit={enviar} noValidate>
          <fieldset>
            <legend>Reconhecimento</legend>
            <div className="grade">
              <Campo id="alunoId" rotulo="Aluno" as="select" className="col-3" value={formulario.alunoId}
                     onChange={alterar('alunoId')} erro={erros.alunoId}>
                <option value="">Selecione…</option>
                {alunos.map((aluno) => (
                  <option key={aluno.id} value={aluno.id}>{aluno.nome} — {aluno.curso}</option>
                ))}
              </Campo>
              <Campo id="valor" rotulo="Moedas" type="number" min="1" step="1" inputMode="numeric"
                     value={formulario.valor} onChange={alterar('valor')} erro={erros.valor} />
              <Campo id="motivo" rotulo="Motivo do reconhecimento" as="textarea" className="col-4" rows={3} maxLength={500}
                     value={formulario.motivo} onChange={alterar('motivo')} erro={erros.motivo} />
            </div>
          </fieldset>

          <div className="formulario__acoes">
            <button type="submit" className="botao" disabled={enviando}>
              {enviando ? 'Enviando…' : 'Enviar moedas'}
            </button>
          </div>
        </form>
      )}
    </section>
  );
}
