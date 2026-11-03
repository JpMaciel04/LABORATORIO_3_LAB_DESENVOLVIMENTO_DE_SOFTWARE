import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Alerta from '../../components/Alerta.jsx';
import { extrairErro } from '../../services/api.js';
import { alunoService } from '../../services/alunoService.js';

/** Alunos da instituição do professor logado (RN06). */
export default function AlunoLista() {
  const [alunos, setAlunos] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');

  useEffect(() => {
    alunoService.listar()
      .then(setAlunos)
      .catch((e) => setErro(extrairErro(e).mensagem))
      .finally(() => setCarregando(false));
  }, []);

  return (
    <section>
      <div className="cabecalho-pagina">
        <div>
          <h1>Alunos</h1>
          <p>Alunos da sua instituição, que podem receber moedas de você.</p>
        </div>
      </div>

      <Alerta aoFechar={() => setErro('')}>{erro}</Alerta>

      {carregando ? (
        <p className="carregando">Carregando alunos…</p>
      ) : alunos.length === 0 ? (
        !erro && (
          <div className="vazio">
            <p>Nenhum aluno da sua instituição está cadastrado ainda.</p>
          </div>
        )
      ) : (
        <div className="tabela-container">
          <table className="tabela">
            <thead>
              <tr>
                <th>Nome</th>
                <th>Curso</th>
                <th><span className="sr-only">Ações</span></th>
              </tr>
            </thead>
            <tbody>
              {alunos.map((aluno) => (
                <tr key={aluno.id}>
                  <td data-rotulo="Nome">{aluno.nome}</td>
                  <td data-rotulo="Curso">{aluno.curso}</td>
                  <td>
                    <div className="acoes">
                      <Link to={`/moedas/enviar?aluno=${aluno.id}`} className="botao botao--pequeno botao--secundario">
                        Enviar moedas
                      </Link>
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
