package sx.cxp

import grails.compiler.GrailsCompileStatic
import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j

import sx.core.FolioLog
import sx.core.LogUser
import sx.tesoreria.CuentaDeBanco
import sx.tesoreria.MovimientoDeCuentaService
import sx.utils.MonedaUtils


@Transactional
@GrailsCompileStatic
@Slf4j
class RequisicionDeComprasService implements LogUser, FolioLog{


    RequisicionDeCompras save(RequisicionDeCompras requisicion) {
        log.debug("Salvando requisicion  {}", requisicion)
        if(!requisicion.id )
            requisicion.folio = nextFolio('REQUISICION', 'COMPRAS_CXP')
        actualizarImportes(requisicion)
        logEntity(requisicion)
        requisicion.save failOnError: true, flush: true
        return requisicion
    }

    RequisicionDeCompras update(RequisicionDeCompras requisicion) {
        actualizarImportes(requisicion)
        logEntity(requisicion)
        requisicion.save failOnError: true, flush: true
        return requisicion
    }

    @CompileDynamic
    def actualizarImportes(RequisicionDeCompras requisicion) {
        log.debug('Actualizando importes de la requisicion {}', requisicion)
        requisicion.partidas.each {RequisicionDet det ->
            CuentaPorPagar cxp = det.cxp
            det.total = cxp.getSaldo()
            det.apagar = det.total
            if(requisicion.descuentof > 0.0){
                det.descuentof = requisicion.descuentof
                det.descuentofImporte = (det.descuentof/100) * det.total
                BigDecimal apagar = det.total - det.descuentofImporte
                det.apagar = MonedaUtils.round(apagar, 2)
            }
            /*
            def compensaciones = AplicacionDePago.where{cxp == cxp && (nota.concepto != 'DESCUENTO') }.list().sum 0.0, { it.importe}
            det.total = cxp.importePorPagar - compensaciones
            log.info("Fac {} Total docto: {} Analizado: {}", cxp.folio, cxp.total, cxp.importePorPagar)
            det.descuentof = requisicion.descuentof
            if(requisicion.descuentof){
                det.descuentofImporte = (det.descuentof/100) * det.total
                BigDecimal apagar = det.total - det.descuentofImporte
                det.apagar = MonedaUtils.round(apagar, 2)
            } else {
                det.descuentofImporte = 0.0
                det.apagar = det.total
            }
            */
            log.info('Partida Total: {} Apagar:{} Saldo:{}', det.total, det.apagar, cxp.saldoReal)
        }
        requisicion.total = requisicion.partidas.sum 0.0, {RequisicionDet det -> det.total}
        requisicion.descuentofImporte = requisicion.partidas.sum 0.0, {RequisicionDet det -> det.descuentofImporte}
        requisicion.apagar = requisicion.partidas.sum 0.0, {RequisicionDet det -> det.apagar}

    }

    RequisicionDeCompras cerrar(RequisicionDeCompras requisicion) {
        log.debug("CERRANDO requisicion de comras  {}", requisicion)
        logEntity(requisicion)
        requisicion.cerrada = new Date()
        requisicion.save flush: true
        return requisicion
    }

    void delete(RequisicionDeCompras requisicion) throws RequisicionException{
        if(requisicion.cerrada) throw new RequisicionCerradaException(requisicion)
        requisicion.delete flush: true
    }


}

class RequisicionException extends RuntimeException {

    RequisicionException(String msg) {
        super(msg)
    }
}

class RequisicionCerradaException extends RequisicionException {

    RequisicionCerradaException(Requisicion requisicion){
        super("La requisicion ${requisicion.id} ya esta cerrada no se puede eliminar")
    }
}
