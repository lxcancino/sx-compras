package sx.core

import grails.compiler.GrailsCompileStatic
import groovy.transform.ToString
import groovy.transform.EqualsAndHashCode

@ToString(includes ='nombre, clave, rfc',includeNames=true,includePackage=false)
@EqualsAndHashCode(includes='id, nombre, rfc')
@GrailsCompileStatic
class Proveedor {

    String id

    String nombre

    String clave

    String rfc = 'XAXX010101000'

    Boolean activo = true

    Boolean nacional = true

    String tipo = 'COMPRAS'

    String comentario

    String telefono1

    String telefono2

    String telefono3

    String	cuentaBancaria

    String banco

    Integer plazo = 0

    BigDecimal limiteDeCredito = 0.0

    Direccion direccion

    Long sw2

    BigDecimal descuentoF = 0.0

    Long diasDF = 0

    Boolean	fechaRevision = true

    Boolean	imprimirCosto = false


    Date dateCreated

    Date lastUpdated

    String createUser
    String updateUser

    static constraints = {
        rfc size:12..13
        nombre unique: true
        clave unique: true
        tipo inList:['COMPRAS','GASTOS', 'MIXTO']
        telefono1 nullable:true ,maxSize:30
        telefono2 nullable:true ,maxSize:30
        telefono3 nullable:true ,maxSize:30
        cuentaBancaria nullable: true
        direccion nullable: true
        sw2 nullable: true
        updateUser nullable: true
        createUser nullable: true
        comentario nullable: true
        banco nullable: true
    }

    static embedded = ['direccion']

    static mapping={
        id generator:'uuid'
    }



}
