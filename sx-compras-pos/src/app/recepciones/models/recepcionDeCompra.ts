import { RecepcionDeCompraDet } from './recepcionDeCompraDet';
import { Compra } from '../../ordenes/models/compra';
import { Proveedor } from '../../proveedores/models/proveedor';
import { Periodo } from '../../_core/models/periodo';

export interface RecepcionDeCompra {
  id?: string;
  nombre?: string;
  sucursalNombre?: string;
  documento?: number;
  remision?: string;
  fechaRemision?: string;
  compra: { id: string };
  proveedor: { id: string };
  sucursal: { id: string };
  fecha?: string;
  comentario?: string;
  partidas: Partial<RecepcionDeCompraDet>[];
  creado?: string;
  modificado?: string;
  createUser?: string;
  updateUser?: string;
  fechaInventario?: string;
  cancelado?: string;
  compraFolio?: number;
  compraFecha?: string;
}

export function buildRecepcion(compra: Compra): Partial<RecepcionDeCompra> {
  return {
    nombre: compra.nombre,
    compra: { id: compra.id },
    proveedor: { id: compra.proveedor.id }
  };
}

export interface ComsFilter {
  fechaInicial?: Date;
  fechaFinal?: Date;
  proveedor?: Partial<Proveedor>;
  registros?: number;
  folio?: number;
}

export function buildFilter(registros: number = 50): ComsFilter {
  const periodo = Periodo.fromNow(30);
  return {
    fechaInicial: periodo.fechaInicial,
    fechaFinal: periodo.fechaFinal,
    registros
  };
}
