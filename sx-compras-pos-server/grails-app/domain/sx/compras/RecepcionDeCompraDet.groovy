package sx.compras

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.core.Inventario
import sx.core.Producto
import sx.core.Sucursal
import sx.compras.RecepcionDeCompra

@ToString(includes = 'producto,cantidad', includeNames = true, includePackage = false)
@EqualsAndHashCode(includes = 'id')
class RecepcionDeCompraDet {

    String id

    Inventario inventario

    String inventariox

    CompraDet compraDet

    Producto producto

    BigDecimal cantidad = 0

    BigDecimal devuelto = 0

    BigDecimal analizado = 0

    BigDecimal kilos = 0

    String comentario

    String sw2

    Date dateCreated

    Date lastUpdated


    static constraints = {
        sw2 nullable:true
        comentario nullable: true
        inventario nullable: true
        compraDet nullable: true
        inventariox nullable: true
    }

    static mapping = {
        id generator:'uuid'
        // devuelto formula:'(select COALESCE(sum(x.cantidad),0) from devolucion_de_compra_det x where x.recepcion_de_compra_det_id=id)'
        // analizado formula:'(select COALESCE(sum(x.cantidad),0) from analisis_de_factura_det x where x.com_id=id)'

    }

    static belongsTo = [recepcion:RecepcionDeCompra]
}
