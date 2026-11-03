import { Route, Routes } from 'react-router-dom';
import Layout from './components/Layout.jsx';
import RotaProtegida from './components/RotaProtegida.jsx';
import Inicio from './pages/Inicio.jsx';
import Login from './pages/Login.jsx';
import NaoEncontrada from './pages/NaoEncontrada.jsx';
import AlunoForm from './pages/alunos/AlunoForm.jsx';
import AlunoLista from './pages/alunos/AlunoLista.jsx';
import AlunoPerfil from './pages/alunos/AlunoPerfil.jsx';
import EmpresaForm from './pages/empresas/EmpresaForm.jsx';
import EmpresaLista from './pages/empresas/EmpresaLista.jsx';
import EmpresaPerfil from './pages/empresas/EmpresaPerfil.jsx';
import Extrato from './pages/extrato/Extrato.jsx';
import EnvioMoedasForm from './pages/moedas/EnvioMoedasForm.jsx';
import VantagemCatalogo from './pages/vantagens/VantagemCatalogo.jsx';
import VantagemForm from './pages/vantagens/VantagemForm.jsx';
import VantagemLista from './pages/vantagens/VantagemLista.jsx';

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<Inicio />} />
        <Route path="login" element={<Login />} />
        <Route path="alunos/novo" element={<AlunoForm />} />
        <Route path="empresas/novo" element={<EmpresaForm />} />

        <Route element={<RotaProtegida perfis={['ALUNO']} />}>
          <Route path="vantagens" element={<VantagemCatalogo />} />
          <Route path="aluno/perfil" element={<AlunoPerfil />} />
          <Route path="aluno/perfil/editar" element={<AlunoForm edicao />} />
        </Route>

        <Route element={<RotaProtegida perfis={['PROFESSOR']} />}>
          <Route path="moedas/enviar" element={<EnvioMoedasForm />} />
          <Route path="alunos" element={<AlunoLista />} />
        </Route>

        <Route element={<RotaProtegida perfis={['ALUNO', 'PROFESSOR']} />}>
          <Route path="extrato" element={<Extrato />} />
          <Route path="empresas" element={<EmpresaLista />} />
        </Route>

        <Route element={<RotaProtegida perfis={['EMPRESA']} />}>
          <Route path="minhas-vantagens" element={<VantagemLista />} />
          <Route path="minhas-vantagens/nova" element={<VantagemForm />} />
          <Route path="minhas-vantagens/:id/editar" element={<VantagemForm />} />
          <Route path="empresa/perfil" element={<EmpresaPerfil />} />
          <Route path="empresa/perfil/editar" element={<EmpresaForm edicao />} />
        </Route>

        <Route path="*" element={<NaoEncontrada />} />
      </Route>
    </Routes>
  );
}
