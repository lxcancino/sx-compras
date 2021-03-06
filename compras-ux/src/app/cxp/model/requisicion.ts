import { RequisicionDet } from './requisicionDet';
import { Proveedor } from 'app/proveedores/models/proveedor';
import { Periodo } from 'app/_core/models/periodo';

export interface Requisicion {
  id: string;
  folio: number;
  proveedor: { id: string };
  nombre: string;
  moneda: string;
  tipoDeCambio: number;
  fecha: string;
  fechaDePago: string;
  formaDePago: 'CHEQUE' | 'TRANSFERENCIA';
  total: number;
  descuentof: number;
  descuentofImporte: number;
  apagar: number;
  partidas: RequisicionDet[];
  comentario: string;
  selected: boolean;
  cerrada: string;
}

export class RequisicionesFilter {
  fechaInicial?: Date;
  fechaFinal?: Date;
  proveedor?: Partial<Proveedor>;
  registros?: number;
  pendientes?: boolean;
}

export function createRequisicionesFilter() {
  const { fechaInicial, fechaFinal } = Periodo.fromNow(10);
  const registros = 20;
  const pendientes = false;
  return {
    fechaInicial,
    fechaFinal,
    registros,
    pendientes
  };
}
