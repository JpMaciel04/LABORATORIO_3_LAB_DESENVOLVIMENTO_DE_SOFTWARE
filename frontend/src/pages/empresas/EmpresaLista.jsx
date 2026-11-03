import { useEffect, useState } from 'react';
import Alerta from '../../components/Alerta.jsx';
import { extrairErro } from '../../services/api.js';
import { empresaService } from '../../services/empresaService.js';
import { mascaraCnpj } from '../../utils/formatos.js';

export default function EmpresaLista() {
  const [empresas, setEmpresas] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');

  useEffect(() => {
    empresaService.listar()
      .then(setEmpresas)
      .catch((e) => setErro(extrairErro(e).mensagem))
      .finally(() => setCarregando(false));
  }, []);

  return (
    <section>
      <div className="cabecalho-pagina">
        <div>
          <h1>Empresas parceiras</h1>
          <p>Empresas que oferecem vantagens em troca de moedas.</p>
        </div>
      </div>

      <Alerta aoFechar={() => setErro('')}>{erro}</Alerta>

      {carregando ? (
        <p className="carregando">Carregando empresas…</p>
      ) : empresas.length === 0 ? (
        !erro && (
          <div className="vazio">
            <p>Nenhuma empresa parceira cadastrada ainda.</p>
          </div>
        )
      ) : (
        <div className="tabela-container">
          <table className="tabela">
            <thead>
              <tr>
                <th>Nome</th>
                <th>CNPJ</th>
                <th>E-mail</th>
              </tr>
            </thead>
            <tbody>
              {empresas.map((empresa) => (
                <tr key={empresa.id}>
                  <td data-rotulo="Nome">{empresa.nome}</td>
                  <td data-rotulo="CNPJ" className="mono">{mascaraCnpj(empresa.cnpj)}</td>
                  <td data-rotulo="E-mail">{empresa.email}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}
