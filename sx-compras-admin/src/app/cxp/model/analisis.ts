import { AnalisisDet } from './analisisDet';
import { CuentaPorPagar } from './cuentaPorPagar';

export interface Analisis {
  id: string;
  folio: number;
  proveedor: { id: string; nombre: string };
  fecha: string;
  fechaEntrada: string;
  factura: CuentaPorPagar;
  comentario?: string;
  importe: number;
  importeFlete: number;
  partidas: AnalisisDet[];
  selected?: boolean;
  cerrado?: string;
  updateUser?: string;
  createUser?: string;
  facturaInfo?: string;
  modificado?: string;
  uuid?: string;
}
