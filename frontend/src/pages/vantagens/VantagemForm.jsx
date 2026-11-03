import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import Alerta from '../../components/Alerta.jsx';
import Campo from '../../components/Campo.jsx';
import { extrairErro, urlDaFoto } from '../../services/api.js';
import { vantagemService } from '../../services/vantagemService.js';

const TAMANHO_MAXIMO_FOTO = 5 * 1024 * 1024;

export default function VantagemForm() {
  const { id } = useParams();
  const edicao = Boolean(id);
  const navigate = useNavigate();

  const [formulario, setFormulario] = useState({ nome: '', descricao: '', custoMoedas: '' });
  const [foto, setFoto] = useState(null);
  const [previa, setPrevia] = useState('');
  const [erros, setErros] = useState({});
  const [mensagemErro, setMensagemErro] = useState('');
  const [carregando, setCarregando] = useState(edicao);
  const [salvando, setSalvando] = useState(false);

  useEffect(() => {
    if (!edicao) return;
    vantagemService.buscar(id)
      .then((vantagem) => {
        setFormulario({ nome: vantagem.nome, descricao: vantagem.descricao, custoMoedas: String(vantagem.custoMoedas) });
        setPrevia(urlDaFoto(vantagem.fotoUrl));
      })
      .catch((e) => setMensagemErro(extrairErro(e).mensagem))
      .finally(() => setCarregando(false));
  }, [id, edicao]);

  const alterar = (campo) => (evento) => {
    setFormulario((atual) => ({ ...atual, [campo]: evento.target.value }));
  };

  function escolherFoto(evento) {
    const arquivo = evento.target.files[0] ?? null;
    if (arquivo && arquivo.size > TAMANHO_MAXIMO_FOTO) {
      setErros((atuais) => ({ ...atuais, foto: 'A foto deve ter no máximo 5 MB.' }));
      evento.target.value = '';
      return;
    }
    setErros((atuais) => ({ ...atuais, foto: undefined }));
    setFoto(arquivo);
    if (arquivo) setPrevia(URL.createObjectURL(arquivo));
  }

  async function salvar(evento) {
    evento.preventDefault();
    setSalvando(true);
    setErros({});
    setMensagemErro('');

    try {
      const dados = {
        nome: formulario.nome,
        descricao: formulario.descricao,
        custoMoedas: formulario.custoMoedas === '' ? null : Number(formulario.custoMoedas),
      };
      const vantagem = edicao ? await vantagemService.atualizar(id, dados, foto) : await vantagemService.cadastrar(dados, foto);
      const acao = edicao ? 'atualizada' : 'cadastrada';
      navigate('/minhas-vantagens', { state: { sucesso: `Vantagem "${vantagem.nome}" ${acao} com sucesso.` } });
    } catch (e) {
      const { mensagem, campos } = extrairErro(e);
      setMensagemErro(mensagem);
      setErros(campos);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } finally {
      setSalvando(false);
    }
  }

  if (carregando) return <p className="carregando">Carregando…</p>;

  return (
    <section>
      <div className="cabecalho-pagina">
        <div>
          <h1>{edicao ? 'Editar vantagem' : 'Nova vantagem'}</h1>
          <p>Todos os campos são obrigatórios. Esses dados são exibidos aos alunos no catálogo.</p>
        </div>
      </div>

      <Alerta aoFechar={() => setMensagemErro('')}>{mensagemErro}</Alerta>

      <form className="formulario" onSubmit={salvar} noValidate>
        <fieldset>
          <legend>Dados da vantagem</legend>
          <div className="grade">
            <Campo id="nome" rotulo="Nome" className="col-3" maxLength={120} value={formulario.nome}
                   onChange={alterar('nome')} erro={erros.nome} />
            <Campo id="custoMoedas" rotulo="Custo em moedas" type="number" min="1" step="1" inputMode="numeric"
                   value={formulario.custoMoedas} onChange={alterar('custoMoedas')} erro={erros.custoMoedas} />
            <Campo id="descricao" rotulo="Descrição" as="textarea" className="col-4" rows={3} maxLength={1000}
                   value={formulario.descricao} onChange={alterar('descricao')} erro={erros.descricao} />
          </div>
        </fieldset>

        <fieldset>
          <legend>Foto do produto</legend>
          <div className="grade">
            <Campo id="foto" rotulo={edicao ? 'Trocar foto (opcional)' : 'Foto'} type="file" className="col-2"
                   accept="image/jpeg,image/png" onChange={escolherFoto} erro={erros.foto}
                   dica="JPG ou PNG, com até 5 MB." />
            {previa && (
              <div className="col-2">
                <img className="previa-foto" src={previa} alt="Prévia da foto selecionada" />
              </div>
            )}
          </div>
        </fieldset>

        <div className="formulario__acoes">
          <Link to="/minhas-vantagens" className="botao botao--secundario">Cancelar</Link>
          <button type="submit" className="botao" disabled={salvando}>
            {salvando ? 'Salvando…' : edicao ? 'Salvar alterações' : 'Cadastrar'}
          </button>
        </div>
      </form>
    </section>
  );
}
