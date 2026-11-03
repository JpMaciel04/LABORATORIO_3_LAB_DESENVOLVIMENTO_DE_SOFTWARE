/**
 * Rótulo + controle de formulário + mensagem de erro vinda do back-end.
 * Use as="select" para listas de opções (passadas como children).
 */
export default function Campo({ id, rotulo, erro, dica, className = '', as: Controle = 'input', children, ...props }) {
  const idMensagem = `${id}-mensagem`;
  const mensagem = erro || dica;

  return (
    <div className={`campo ${className}`}>
      <label htmlFor={id}>{rotulo}</label>
      <Controle
        id={id}
        name={id}
        aria-invalid={Boolean(erro)}
        aria-describedby={mensagem ? idMensagem : undefined}
        {...props}
      >
        {children}
      </Controle>
      {mensagem && (
        <small id={idMensagem} className={erro ? 'campo__erro' : 'campo__dica'}>
          {mensagem}
        </small>
      )}
    </div>
  );
}
