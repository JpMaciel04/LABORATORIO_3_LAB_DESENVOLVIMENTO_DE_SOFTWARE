const apenasDigitos = (valor = '') => valor.replace(/\D/g, '');

/** Aplica uma máscara em que "0" representa um dígito: aplicarMascara('12345', '00.000') -> '12.345'. */
function aplicarMascara(valor, padrao) {
  const digitos = apenasDigitos(valor);
  let resultado = '';
  let posicao = 0;
  for (const caractere of padrao) {
    if (posicao >= digitos.length) break;
    resultado += caractere === '0' ? digitos[posicao++] : caractere;
  }
  return resultado;
}

export const mascaraCpf = (valor) => aplicarMascara(valor, '000.000.000-00');
export const mascaraCnpj = (valor) => aplicarMascara(valor, '00.000.000/0000-00');
export const mascaraCep = (valor) => aplicarMascara(valor, '00000-000');

export const formatarMoedas = (quantidade) =>
  `${quantidade.toLocaleString('pt-BR')} ${quantidade === 1 ? 'moeda' : 'moedas'}`;

export const formatarDataHora = (dataHora) =>
  new Date(dataHora).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' });
