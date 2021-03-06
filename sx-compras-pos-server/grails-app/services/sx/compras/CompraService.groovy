package sx.compras

import grails.compiler.GrailsCompileStatic
import grails.gorm.services.Service
import grails.plugin.springsecurity.SpringSecurityService
import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import sx.core.Folio

import sx.security.User
import sx.utils.MonedaUtils


@Slf4j
@GrailsCompileStatic
@Service(Compra)
abstract class CompraService {

    @Autowired
    @Qualifier('springSecurityService')
    SpringSecurityService springSecurityService

    abstract Compra save(Compra compra)

    abstract void delete(Serializable id)

    Compra saveCompra(Compra compra) {
        if(!compra.id) {
            compra.folio = nextFolio(compra)
            log.info('Compra nueva Serie: {}, Folio: {}', compra.serie, compra.folio)
        }
        actualizar(compra)
        logEntity(compra)
        compra.save failOnError: true, flush: true
        return compra

    }

    @CompileDynamic
    void actualizar(Compra compra) {
        compra.nombre = compra.proveedor.nombre
        compra.clave = compra.proveedor.rfc
        compra.rfc = compra.proveedor.rfc
        compra.partidas.each {
            actualizarPartida(it)
        }
        if(compra.proveedor.plazo)
            compra.entrega = compra.fecha + compra.proveedor.plazo
        compra.importeNeto = compra.partidas.sum 0.0, { it.importeNeto }
        compra.importeBruto = compra.partidas.sum 0.0, { it.importeBruto }
        compra.impuestos = MonedaUtils.calcularImpuesto(compra.importeNeto)
        if(compra.partidas) {
            def pendiente = compra.partidas.find{it.getPorRecibir()> 0.0 }
            compra.pendiente = pendiente != null
        } else {
            compra.pendiente = true
        }

        compra.total = compra.importeNeto + compra.impuestos
    }

    Compra cerrarCompra(Compra compra){
        compra.cerrada =  new Date()
        logEntity(compra)
        return save(compra)
    }

    Compra depurarCompra(Compra compra) {
        Date depuracion = new Date()
        compra.partidas.each {
            it.depurado = it.getPorRecibir()
            it.depuracion = depuracion
        }
        compra.ultimaDepuracion = depuracion
        compra.pendiente = false
        logEntity(compra)
        return save(compra)
    }



    Long  nextFolio(Compra compra){
        String serie = compra.sucursal.nombre
        compra.serie = serie
        Folio folio = Folio.findOrCreateWhere(entidad: 'COMPRA', serie: 'OFICINAS')
        Long res = folio.folio + 1
        folio.folio = res
        // folio.save flush: true
        return res
    }

    @CompileDynamic
    Compra actualizarTotales(Compra compra) {
        compra.importeNeto = compra.partidas.sum 0.0, { it.importeNeto }
        compra.importeBruto = compra.partidas.sum 0.0, { it.importeBruto }
        compra.impuestos = MonedaUtils.calcularImpuesto(compra.importeNeto)
        compra.total = compra.importeNeto + compra.impuestos
        return compra
    }

    def depurarNegativos(Compra compra) {
        compra.partidas.each{
            if(it.porRecibir < 0) {
                def ajuste  = it.depurado  - (it.porRecibir * -1)
                it.depurado = ajuste
                it.depuracion = new Date()
                compra.ultimaDepuracion = new Date()
            }
        }
        compra.save flush: true
    }

    void logEntity(Compra compra) {

        User user = (User)springSecurityService.getCurrentUser()
        if(user) {
            String username = user.username
            if(compra.id == null || compra.createdBy == null)
                compra.createdBy = username
            compra.lastUpdatedBy = username
        }

    }

}
