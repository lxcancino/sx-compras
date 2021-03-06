package sx.core

import grails.compiler.GrailsCompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames=true,includePackage=false)
@EqualsAndHashCode
@GrailsCompileStatic
class AppConfig {

    String id

    Sucursal sucursal

    Boolean envioDeCorreosActivo = false

    String cfdiLocation

    Date dateCreated

    Date lastUpdated

    static constraints = {
        cfdiLocation nullable: true
        envioDeCorreosActivo nullable: true
    }

    static mapping = {
        id generator:'uuid'
    }
}
