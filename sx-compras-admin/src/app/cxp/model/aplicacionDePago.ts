import { NotaDeCreditoCxP } from './notaDeCreditoCxP';
import { CuentaPorPagar } from './cuentaPorPagar';
import { Pago } from './pago';

export interface AplicacionDePago {
  id?: string;
  fecha: string;
  formaDePago: string;
  nota?: Partial<NotaDeCreditoCxP>;
  pago?: Partial<Pago>;
  cxp?: Partial<CuentaPorPagar>;
  importe: number;
  comentario?: string;
}
