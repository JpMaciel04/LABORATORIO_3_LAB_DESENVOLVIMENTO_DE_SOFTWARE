import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAutenticacao } from '../contexts/AutenticacaoContext.jsx';

/**
 * Libera as rotas filhas apenas para os perfis informados. É uma conveniência de navegação:
 * quem garante as permissões é o back-end, a cada requisição.
 */
export default function RotaProtegida({ perfis }) {
  const { usuario } = useAutenticacao();
  const location = useLocation();

  if (!usuario) return <Navigate to="/login" replace state={{ destino: location.pathname }} />;
  if (!perfis.includes(usuario.perfil)) return <Navigate to="/" replace />;
  return <Outlet />;
}
