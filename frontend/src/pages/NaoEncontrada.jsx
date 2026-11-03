import { Link } from 'react-router-dom';

export default function NaoEncontrada() {
  return (
    <section className="vazio">
      <h1>Página não encontrada</h1>
      <p>O endereço acessado não existe.</p>
      <Link to="/" className="botao">Voltar ao início</Link>
    </section>
  );
}
