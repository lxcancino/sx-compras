import { Producto } from 'app/productos/models/producto';
import { ProveedorProducto } from 'app/proveedores/models/proveedorProducto';
import { aplicarDescuentosEnCascada } from 'app/utils/money-utils';
import { Sucursal } from 'app/models';

export interface OrdenDeCompraDet {
  id?: string;
  producto: Partial<Producto>;
  clave: string;
  descripcion: string;
  unidad: string;
  sucursal?: Partial<Sucursal>;
  solicitado: number;
  precio: number;
  descuento1: number;
  descuento2: number;
  descuento3: number;
  descuento4: number;
  costo: number;
  importeBruto: number;
  importeNeto: number;
  depuracion?: string;
  recibido: number;
  depurado: number;
  porRecibir: number;
  comentario: string;
  selected?: boolean;
}

export function buildCompraDet(
  provProd: ProveedorProducto
): Partial<OrdenDeCompraDet> {
  return {
    producto: provProd.producto,
    clave: provProd.clave,
    descripcion: provProd.descripcion,
    unidad: provProd.unidad,
    solicitado: 0,
    precio: provProd.precioBruto,
    descuento1: provProd.desc1,
    descuento2: provProd.desc2,
    descuento3: provProd.desc3,
    descuento4: provProd.desc4,
    costo: provProd.precio,
    importeBruto: 0,
    importeNeto: 0
  };
}

export function actualizarPartidaDeCompra(row: OrdenDeCompraDet) {
  const {
    unidad,
    producto,
    solicitado,
    precio,
    descuento1,
    descuento2,
    descuento3,
    descuento4
  } = row;
  const factor = unidad === 'MIL' ? 1000 : 1;
  const importeBruto = precio * (solicitado / factor);
  const importeNeto = aplicarDescuentosEnCascada(importeBruto, [
    descuento1,
    descuento2,
    descuento3,
    descuento4
  ]);
  row.importeBruto = importeBruto;
  row.importeNeto = importeNeto;
}
