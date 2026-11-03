import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Alerta from '../../components/Alerta.jsx';
import { useAutenticacao } from '../../contexts/AutenticacaoContext.jsx';
import { useMensagemFlash } from '../../hooks/useMensagemFlash.js';
import { extrairErro } from '../../services/api.js';
import { alunoService } from '../../services/alunoService.js';
import { formatarMoedas, mascaraCep, mascaraCpf } from '../../utils/formatos.js';

export default function AlunoPerfil() {
  const { usuario, sair } = useAutenticacao();
  const [aluno, setAluno] = useState(null);
  const [erro, setErro] = useState('');
  const [sucesso, setSucesso] = useMensagemFlash();

  useEffect(() => {
    alunoService.buscar(usuario.id)
      .then(setAluno)
      .catch((e) => setErro(extrairErro(e).mensagem));
  }, [usuario.id]);

  async function excluir() {
    if (!window.confirm('Excluir sua conta? Você perderá o acesso ao sistema e esta ação não pode ser desfeita.')) return;

    try {
      await alunoService.excluir(usuario.id);
      sair('Sua conta foi excluída.');
    } catch (e) {
      setErro(extrairErro(e).mensagem);
    }
  }

  return (
    <section>
      <div className="cabecalho-pagina">
        <div>
          <h1>Meu cadastro</h1>
          <p>Somente você tem acesso a estes dados.</p>
        </div>
        {aluno && (
          <div className="cartao__acoes">
            <Link to="/aluno/perfil/editar" className="botao">Alterar dados</Link>
            <button type="button" className="botao botao--perigo" onClick={excluir}>Excluir conta</button>
          </div>
        )}
      </div>

      <Alerta tipo="sucesso" aoFechar={() => setSucesso('')}>{sucesso}</Alerta>
      <Alerta aoFechar={() => setErro('')}>{erro}</Alerta>

      {!aluno ? (
        !erro && <p className="carregando">Carregando…</p>
      ) : (
        <div className="painel">
          <dl className="detalhes">
            <div><dt>Nome</dt><dd>{aluno.nome}</dd></div>
            <div><dt>E-mail (login)</dt><dd>{aluno.email}</dd></div>
            <div><dt>CPF</dt><dd className="mono">{mascaraCpf(aluno.cpf)}</dd></div>
            <div><dt>RG</dt><dd>{aluno.rg}</dd></div>
            <div><dt>Instituição de ensino</dt><dd>{aluno.instituicao.nome}</dd></div>
            <div><dt>Curso</dt><dd>{aluno.curso}</dd></div>
            <div className="detalhes__largo">
              <dt>Endereço</dt>
              <dd>
                {aluno.endereco.logradouro}, {aluno.endereco.numero}
                {aluno.endereco.complemento && ` — ${aluno.endereco.complemento}`}, {aluno.endereco.bairro},{' '}
                {aluno.endereco.cidade}/{aluno.endereco.estado}, CEP {mascaraCep(aluno.endereco.cep)}
              </dd>
            </div>
            <div><dt>Saldo</dt><dd className="moedas">{formatarMoedas(aluno.saldo)}</dd></div>
          </dl>
        </div>
      )}
    </section>
  );
}
