import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Alerta from '../../components/Alerta.jsx';
import Campo from '../../components/Campo.jsx';
import { useAutenticacao } from '../../contexts/AutenticacaoContext.jsx';
import { extrairErro } from '../../services/api.js';
import { alunoService } from '../../services/alunoService.js';
import { instituicaoService } from '../../services/instituicaoService.js';
import { mascaraCep, mascaraCpf } from '../../utils/formatos.js';

const FORMULARIO_VAZIO = {
  nome: '',
  email: '',
  senha: '',
  cpf: '',
  rg: '',
  curso: '',
  instituicaoId: '',
  endereco: { logradouro: '', numero: '', complemento: '', bairro: '', cidade: '', estado: '', cep: '' },
};

/** Converte o AlunoResponseDTO da API para o estado do formulário. */
function paraFormulario(aluno) {
  return {
    nome: aluno.nome,
    email: aluno.email,
    senha: '',
    cpf: mascaraCpf(aluno.cpf),
    rg: aluno.rg,
    curso: aluno.curso,
    instituicaoId: String(aluno.instituicao.id),
    endereco: {
      ...aluno.endereco,
      complemento: aluno.endereco.complemento ?? '',
      cep: mascaraCep(aluno.endereco.cep),
    },
  };
}

/** Converte o estado do formulário para o AlunoRequestDTO esperado pela API. */
function paraRequisicao(formulario) {
  return {
    ...formulario,
    senha: formulario.senha || null,
    instituicaoId: formulario.instituicaoId ? Number(formulario.instituicaoId) : null,
  };
}

/** Auto-cadastro público ou, com a propriedade edicao, alteração dos dados do aluno logado. */
export default function AlunoForm({ edicao = false }) {
  const { usuario, atualizarUsuario } = useAutenticacao();
  const id = edicao ? usuario.id : null;
  const navigate = useNavigate();

  const [formulario, setFormulario] = useState(FORMULARIO_VAZIO);
  const [instituicoes, setInstituicoes] = useState([]);
  const [erros, setErros] = useState({});
  const [mensagemErro, setMensagemErro] = useState('');
  const [carregando, setCarregando] = useState(true);
  const [salvando, setSalvando] = useState(false);

  useEffect(() => {
    Promise.all([instituicaoService.listar(), edicao ? alunoService.buscar(id) : null])
      .then(([listaInstituicoes, aluno]) => {
        setInstituicoes(listaInstituicoes);
        if (aluno) setFormulario(paraFormulario(aluno));
      })
      .catch((e) => setMensagemErro(extrairErro(e).mensagem))
      .finally(() => setCarregando(false));
  }, [id, edicao]);

  const alterar = (campo, mascara) => (evento) => {
    const valor = mascara ? mascara(evento.target.value) : evento.target.value;
    setFormulario((atual) => ({ ...atual, [campo]: valor }));
  };

  const alterarEndereco = (campo, mascara) => (evento) => {
    const valor = mascara ? mascara(evento.target.value) : evento.target.value;
    setFormulario((atual) => ({ ...atual, endereco: { ...atual.endereco, [campo]: valor } }));
  };

  async function salvar(evento) {
    evento.preventDefault();
    setSalvando(true);
    setErros({});
    setMensagemErro('');

    try {
      const dados = paraRequisicao(formulario);
      if (edicao) {
        const aluno = await alunoService.atualizar(id, dados);
        atualizarUsuario({ nome: aluno.nome, email: aluno.email });
        navigate('/aluno/perfil', { state: { sucesso: 'Seus dados foram atualizados.' } });
      } else {
        await alunoService.cadastrar(dados);
        navigate('/login', { state: { sucesso: 'Cadastro realizado. Entre com seu e-mail e sua senha.' } });
      }
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
          <h1>{edicao ? 'Alterar meu cadastro' : 'Cadastro de aluno'}</h1>
          <p>Todos os campos são obrigatórios, exceto o complemento do endereço.</p>
        </div>
      </div>

      <Alerta aoFechar={() => setMensagemErro('')}>{mensagemErro}</Alerta>

      <form className="formulario" onSubmit={salvar} noValidate>
        <fieldset>
          <legend>Dados pessoais</legend>
          <div className="grade">
            <Campo id="nome" rotulo="Nome completo" className="col-2" value={formulario.nome}
                   onChange={alterar('nome')} erro={erros.nome} autoComplete="name" />
            <Campo id="cpf" rotulo="CPF" value={formulario.cpf} onChange={alterar('cpf', mascaraCpf)}
                   erro={erros.cpf} inputMode="numeric" placeholder="000.000.000-00" />
            <Campo id="rg" rotulo="RG" value={formulario.rg} onChange={alterar('rg')} erro={erros.rg} />
          </div>
        </fieldset>

        <fieldset>
          <legend>Vínculo acadêmico</legend>
          <div className="grade">
            <Campo id="instituicaoId" rotulo="Instituição de ensino" as="select" className="col-2"
                   value={formulario.instituicaoId} onChange={alterar('instituicaoId')} erro={erros.instituicaoId}>
              <option value="">Selecione…</option>
              {instituicoes.map((instituicao) => (
                <option key={instituicao.id} value={instituicao.id}>{instituicao.nome}</option>
              ))}
            </Campo>
            <Campo id="curso" rotulo="Curso" className="col-2" value={formulario.curso}
                   onChange={alterar('curso')} erro={erros.curso} />
          </div>
        </fieldset>

        <fieldset>
          <legend>Endereço</legend>
          <div className="grade">
            <Campo id="endereco.cep" rotulo="CEP" value={formulario.endereco.cep}
                   onChange={alterarEndereco('cep', mascaraCep)} erro={erros['endereco.cep']}
                   inputMode="numeric" placeholder="00000-000" autoComplete="postal-code" />
            <Campo id="endereco.logradouro" rotulo="Logradouro" className="col-2" value={formulario.endereco.logradouro}
                   onChange={alterarEndereco('logradouro')} erro={erros['endereco.logradouro']} />
            <Campo id="endereco.numero" rotulo="Número" value={formulario.endereco.numero}
                   onChange={alterarEndereco('numero')} erro={erros['endereco.numero']} />
            <Campo id="endereco.complemento" rotulo="Complemento (opcional)" value={formulario.endereco.complemento}
                   onChange={alterarEndereco('complemento')} erro={erros['endereco.complemento']} />
            <Campo id="endereco.bairro" rotulo="Bairro" value={formulario.endereco.bairro}
                   onChange={alterarEndereco('bairro')} erro={erros['endereco.bairro']} />
            <Campo id="endereco.cidade" rotulo="Cidade" value={formulario.endereco.cidade}
                   onChange={alterarEndereco('cidade')} erro={erros['endereco.cidade']} />
            <Campo id="endereco.estado" rotulo="UF" value={formulario.endereco.estado}
                   onChange={alterarEndereco('estado')} erro={erros['endereco.estado']} maxLength={2}
                   placeholder="MG" />
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
          <Link to={edicao ? '/aluno/perfil' : '/'} className="botao botao--secundario">Cancelar</Link>
          <button type="submit" className="botao" disabled={salvando}>
            {salvando ? 'Salvando…' : edicao ? 'Salvar alterações' : 'Cadastrar'}
          </button>
        </div>
      </form>
    </section>
  );
}
