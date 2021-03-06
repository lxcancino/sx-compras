import { Proveedor } from '../../proveedores/models/proveedor';
import { Periodo } from '../../_core/models/periodo';

export interface CuentaPorPagar {
  id: string;
  nombre: string;
  tipo: string;
  folio: string;
  uuid: string;
  serie: string;
  fecha: string;
  subTotal: number;
  descuento: number;
  impuestoTrasladado: number;
  impuestoRetenido: number;
  moneda: string;
  tipoDeCambio: number;
  tcContable?: number;
  total: number;
  pagos?: number;
  compensaciones?: number;
  saldo?: number;
  saldoReal?: number;
  importePorPagar?: number;
  vencimiento: string;
  selected?: boolean;
  comprobanteFiscal: any;
  analizada?: boolean;
  analisis?: string;
  gastoAnalizado?: number;
  gastoAnalizadoFecha?: string;
}

export class CxPFilter {
  fechaInicial?: Date;
  fechaFinal?: Date;
  proveedor?: Partial<Proveedor>;
  registros?: number;
}

export function createCxPFilter(): CxPFilter {
  const { fechaInicial, fechaFinal } = Periodo.fromNow(30);
  const registros = 500;
  return {
    fechaInicial,
    fechaFinal,
    registros
  };
}
