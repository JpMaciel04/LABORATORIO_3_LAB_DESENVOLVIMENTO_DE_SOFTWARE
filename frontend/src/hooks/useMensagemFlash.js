import { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';

/**
 * Lê a mensagem de sucesso enviada por outra tela via navigate(rota, { state: { sucesso } })
 * e limpa o histórico, para que a mensagem não reapareça ao recarregar a página.
 */
export function useMensagemFlash() {
  const location = useLocation();
  const navigate = useNavigate();
  const [mensagem, setMensagem] = useState(location.state?.sucesso ?? '');

  useEffect(() => {
    if (location.state?.sucesso) {
      navigate(location.pathname, { replace: true, state: null });
    }
  }, [location, navigate]);

  return [mensagem, setMensagem];
}
