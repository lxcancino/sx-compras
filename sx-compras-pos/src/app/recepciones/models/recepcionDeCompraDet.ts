import { CompraDet } from '../../ordenes/models/compraDet';
import { Producto } from '../../productos/models/producto';

export interface RecepcionDeCompraDet {
  id?: string;
  clave?: string;
  descripcion?: string;
  unidad?: string;
  inventario?: { id: string };
  inventariox?: string;
  compraDet: Partial<CompraDet>;
  producto: Partial<Producto>;
  cantidad: number;
  solicitado: number;
  recibido?: number;
  devuelto: number;
  analizado: number;
  kilos: number;
  comentario?: string;
}

export function buildRecepcionDet(
  compraDet: CompraDet
): Partial<RecepcionDeCompraDet> {
  return {
    clave: compraDet.clave,
    descripcion: compraDet.descripcion,
    unidad: compraDet.unidad,
    compraDet: { id: compraDet.id },
    producto: compraDet.producto,
    cantidad: 0,
    solicitado: compraDet.porRecibir,
    recibido: compraDet.recibido
  };
}
