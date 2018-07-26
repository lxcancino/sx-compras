package sx.compras

import groovy.transform.ToString
import groovy.transform.EqualsAndHashCode
import sx.core.Sucursal
import sx.core.Proveedor



@ToString(excludes = 'dateCreated,lastUpdated,version,partidas',includeNames=true,includePackage=false)
@EqualsAndHashCode(includes='id,sucursal,folio')
class Compra {


    String id

    Proveedor proveedor

    Sucursal sucursal

    Long folio

    Date fecha

    Date cerrada

    Date entrega

    String comentario

    Date ultimaDepuracion

    BigDecimal importeBruto = 0.0

    BigDecimal importeNeto = 0.0

    BigDecimal importeDescuento = 0.0

    BigDecimal impuestos = 0.0

    BigDecimal total = 0.0

    String moneda = 'MXN'

    BigDecimal tipoDeCambio = 1.0

    Boolean pendiente = true

    Boolean consolidada = false

    Boolean centralizada = false

    Boolean especial= false

    Boolean nacional = true

    List partidas  = []

    String sw2

    Date dateCreated

    Date lastUpdated

    String createdBy

    String lastUpdatedBy

    String status


    static constraints = {
        comentario nullable:true
        ultimaDepuracion nullable:true
        entrega nullable:true
        folio unique:'sucursal'
        sw2 nullable:true
        createdBy nullable: true
        lastUpdatedBy nullable: true
        cerrada nullable: true

    }

    static hasMany =[partidas:CompraDet]

    static transients = ['status']

    static mapping = {
        id generator:'uuid'
        partidas cascade: "all-delete-orphan"
        fecha type:'date', index: 'COMPRA_IDX1'
        entrega type:'date'
        folio index: 'COMPRA_IDX2'
        cerrada type: 'date'
    }

    def beforeUpdate() {
        actualizarStatus()
    }

    def actualizarStatus() {
        this.pendiente = this.partidas.find{it.getPorRecibir() > 0.0 } == null ? true: false
    }

    def pendientes() {
        return this.partidas.findAll{ CompraDet det -> det.getPorRecibir() > 0}
    }

    def getStatus() {
        if(pendiente && cerrada) return 'T'
        else if(pendiente)
            return 'P'
        else
            return 'A'
    }

}

