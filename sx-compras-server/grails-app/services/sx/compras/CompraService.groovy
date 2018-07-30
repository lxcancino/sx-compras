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
            compra.folio = nextFolio()
            compra.nombre = compra.proveedor.nombre
            compra.clave = compra.proveedor.rfc
            compra.rfc = compra.proveedor.rfc
        }
        actualizar(compra)
        logEntity(compra)
        compra.save failOnError: true, flush: true
        return compra

    }

    @CompileDynamic
    void actualizar(Compra compra) {
        compra.partidas.each {
            it.sucursal?: compra.sucursal
            actualizarPartida(it)
        }
        if(compra.proveedor.plazo)
            compra.entrega = compra.fecha + compra.proveedor.plazo
        compra.importeNeto = compra.partidas.sum 0.0, { it.importeNeto }
        compra.importeBruto = compra.partidas.sum 0.0, { it.importeBruto }
        compra.impuestos = MonedaUtils.calcularImpuesto(compra.importeNeto)
        def pendiente = compra.partidas.find{it.getPorRecibir()> 0.0 }
        compra.pendiente = pendiente != null
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
        log.debug("Compra depurada: {}", compra)
        return save(compra)
    }


    void actualizarPartida(CompraDet partida) {
        BigDecimal factor = partida.producto.unidad == 'MIL' ? 1000 : 1
        BigDecimal cantidad = partida.solicitado / factor
        BigDecimal importeBruto = MonedaUtils.round(cantidad * partida.precio) as BigDecimal
        BigDecimal importeNeto = MonedaUtils.aplicarDescuentosEnCascada(
                importeBruto,
                partida.descuento1,
                partida.descuento2,
                partida.descuento3,
                partida.descuento4
        )
        BigDecimal costo = MonedaUtils.aplicarDescuentosEnCascada(
                partida.precio,
                partida.descuento1,
                partida.descuento2,
                partida.descuento3,
                partida.descuento4
        )

        partida.costo = costo
        partida.importeBruto = importeBruto
        partida.importeNeto = importeNeto
    }

    Long  nextFolio(){
        Folio folio = Folio.findOrCreateWhere(entidad: 'COMPRAS', serie: 'OFICINAS')
        Long res = folio.folio + 1
        log.info('Asignando folio de compra: {}', res)
        folio.folio = res
        folio.save flush: true
        return res
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
