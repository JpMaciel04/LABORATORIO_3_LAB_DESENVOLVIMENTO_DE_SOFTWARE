import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Alerta from '../../components/Alerta.jsx';
import Campo from '../../components/Campo.jsx';
import { useAutenticacao } from '../../contexts/AutenticacaoContext.jsx';
import { extrairErro } from '../../services/api.js';
import { empresaService } from '../../services/empresaService.js';
import { mascaraCnpj } from '../../utils/formatos.js';

const FORMULARIO_VAZIO = { nome: '', cnpj: '', email: '', senha: '' };

/** Auto-cadastro público ou, com a propriedade edicao, alteração dos dados da empresa logada. */
export default function EmpresaForm({ edicao = false }) {
  const { usuario, atualizarUsuario } = useAutenticacao();
  const id = edicao ? usuario.id : null;
  const navigate = useNavigate();

  const [formulario, setFormulario] = useState(FORMULARIO_VAZIO);
  const [erros, setErros] = useState({});
  const [mensagemErro, setMensagemErro] = useState('');
  const [carregando, setCarregando] = useState(edicao);
  const [salvando, setSalvando] = useState(false);

  useEffect(() => {
    if (!edicao) return;
    empresaService.buscar(id)
      .then((empresa) => setFormulario({ ...empresa, cnpj: mascaraCnpj(empresa.cnpj), senha: '' }))
      .catch((e) => setMensagemErro(extrairErro(e).mensagem))
      .finally(() => setCarregando(false));
  }, [id, edicao]);

  const alterar = (campo, mascara) => (evento) => {
    const valor = mascara ? mascara(evento.target.value) : evento.target.value;
    setFormulario((atual) => ({ ...atual, [campo]: valor }));
  };

  async function salvar(evento) {
    evento.preventDefault();
    setSalvando(true);
    setErros({});
    setMensagemErro('');

    try {
      const dados = {
        nome: formulario.nome,
        cnpj: formulario.cnpj,
        email: formulario.email,
        senha: formulario.senha || null,
      };
      if (edicao) {
        const empresa = await empresaService.atualizar(id, dados);
        atualizarUsuario({ nome: empresa.nome, email: empresa.email });
        navigate('/empresa/perfil', { state: { sucesso: 'Os dados da empresa foram atualizados.' } });
      } else {
        await empresaService.cadastrar(dados);
        navigate('/login', { state: { sucesso: 'Empresa cadastrada. Entre para oferecer suas vantagens.' } });
      }
    } catch (e) {
      const { mensagem, campos } = extrairErro(e);
      setMensagemErro(mensagem);
      setErros(campos);
    } finally {
      setSalvando(false);
    }
  }

  if (carregando) return <p className="carregando">Carregando…</p>;

  return (
    <section>
      <div className="cabecalho-pagina">
        <div>
          <h1>{edicao ? 'Alterar dados da empresa' : 'Cadastro de empresa parceira'}</h1>
          <p>
            {edicao
              ? 'Todos os campos são obrigatórios, exceto a senha.'
              : 'Depois do cadastro, entre no sistema para oferecer suas vantagens aos alunos.'}
          </p>
        </div>
      </div>

      <Alerta aoFechar={() => setMensagemErro('')}>{mensagemErro}</Alerta>

      <form className="formulario" onSubmit={salvar} noValidate>
        <fieldset>
          <legend>Dados da empresa</legend>
          <div className="grade">
            <Campo id="nome" rotulo="Nome da empresa" className="col-2" value={formulario.nome}
                   onChange={alterar('nome')} erro={erros.nome} autoComplete="organization" />
            <Campo id="cnpj" rotulo="CNPJ" className="col-2" value={formulario.cnpj}
                   onChange={alterar('cnpj', mascaraCnpj)} erro={erros.cnpj}
                   inputMode="numeric" placeholder="00.000.000/0000-00" />
          </div>
        </fieldset>

        <fieldset>
          <legend>Acesso ao sistema</legend>
          <div className="grade">
            <Campo id="email" rotulo="E-mail (login)" type="email" className="col-2" value={formulario.email}
                   onChange={alterar('email')} erro={erros.email} autoComplete="email" />
            <Campo id="senha" rotulo="Senha" type="password" className="col-2" value={formulario.senha}
                   onChange={alterar('senha')} erro={erros.senha} autoComplete="new-password"
                   dica={edicao ? 'Deixe em branco para manter a senha atual.' : 'Mínimo de 6 caracteres.'} />
          </div>
        </fieldset>

        <div className="formulario__acoes">
          <Link to={edicao ? '/empresa/perfil' : '/'} className="botao botao--secundario">Cancelar</Link>
          <button type="submit" className="botao" disabled={salvando}>
            {salvando ? 'Salvando…' : edicao ? 'Salvar alterações' : 'Cadastrar'}
          </button>
        </div>
      </form>
    </section>
  );
}
