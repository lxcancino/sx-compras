package sx.compras

import groovy.transform.ToString
import groovy.transform.EqualsAndHashCode


@ToString(includes = 'producto,solicitado,importeNeto', includeNames = true, includePackage = false)
@EqualsAndHashCode(includes = 'id, producto,  descripcion, solicitado')
class CambioDePrecioDet {

    String id

    CambioDePrecio cambio

    String producto

    String descripcion

    String unidad

    BigDecimal precioCredito
    BigDecimal precioCreditoAnterior

    BigDecimal precioContado
    BigDecimal precioContadoAnterior

    Date dateCreated
    Date lastUpdated

    String updateUser
    String createUser

    static constraints = {
        producto maxSize: 20
        comentario nullable: true
        unidad maxSize: 10
    }

    static mapping = {
        id generator: 'uuid'
    }

    static belongsTo = [cambio: CambioDePrecio]

}
