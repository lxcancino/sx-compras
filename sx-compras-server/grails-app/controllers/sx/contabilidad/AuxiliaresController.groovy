package sx.contabilidad


import grails.rest.*
import grails.converters.*
import grails.validation.Validateable
import groovy.sql.Sql
import groovy.transform.Canonical
import groovy.transform.ToString
import org.apache.commons.lang3.exception.ExceptionUtils
import java.sql.SQLException
import groovy.util.logging.Slf4j
import grails.plugin.springsecurity.annotation.Secured
import sx.utils.Periodo

import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.OK

import sx.reports.ReportService


@Slf4j
@Secured("ROLE_CONTABILIDAD")
class AuxiliaresController {
	static responseFormats = ['json']
	AuxiliaresService auxiliaresService

    ReportService reportService
    
    def bancos(){
        Periodo periodo = params.periodo
        log.info('Ejecutando: {}', this.params)
        String select = auxiliaresService.getSelect('BANCOS')
            .replaceAll('@FECHA_INICIAL', auxiliaresService.toSqlDate(periodo.fechaInicial))
            .replaceAll('@FECHA_FINAL', auxiliaresService.toSqlDate(periodo.fechaFinal)).
        replaceAll('@CUENTA', params.cuenta)
        def rows = auxiliaresService.getAllRows(select,[])
        respond rows
    }
    
    def general(){
        // Periodo periodo = params.periodo
        String select = auxiliaresService.getSelect('GENERAL') // .replaceAll('@FECHA_INICIAL', auxiliaresService.toSqlDate(periodo.fechaInicial)).replaceAll('@FECHA_FINAL', auxiliaresService.toSqlDate(periodo.fechaFinal)).
        // replaceAll('@CUENTA', params.cuenta)
        def rows = auxiliaresService.getAllRows(select,[])
        respond rows
    }

    def auxiliar(AuxiliarCommand command) {
        if(command == null) {
            render status: NOT_FOUND
            return
        }
        log.info('Auxiliar: {}', command)

        def cuentaInicial = "${command.cuentaInicial}"
        if(cuentaInicial.split('-').size() < 4) {
            cuentaInicial += '%'
        }

        List<PolizaDetDTO> res = []
        

        if(command.cuentaFinal == null) {
            def hql = """
            select new sx.contabilidad.PolizaDetDTO(
                d.id,
                d.poliza.tipo,
                d.poliza.subtipo,
                d.poliza.folio,
                d.poliza.ejercicio,
                d.poliza.mes,
                d.poliza.fecha,
                d.cuenta.clave,
                d.cuenta.descripcion,
                d.concepto,
                d.descripcion,
                d.sucursal,
                d.asiento,
                d.debe,
                d.haber
                )  
                from PolizaDet d 
                    where d.poliza.fecha between ? and ? 
                    and d.cuenta.clave like ? 
                    and d.poliza.cierre != null
                    order by d.cuenta.clave 
            """
            log.info('Buscando movimientos de la cta:{}', cuentaInicial)
            
            res = PolizaDet.findAll(hql ,[
                command.periodo.fechaInicial, 
                command.periodo.fechaFinal, 
                cuentaInicial])

        } else {
            def cuentaFinal = "${command.cuentaFinal}"
            if(cuentaFinal.split('-').size() < 4) {
                cuentaFinal += '%'
            }
            def hql = """
            select new sx.contabilidad.PolizaDetDTO(
                d.id,
                d.poliza.tipo,
                d.poliza.subtipo,
                d.poliza.folio,
                d.poliza.ejercicio,
                d.poliza.mes,
                d.poliza.fecha,
                d.cuenta.clave,
                d.cuenta.descripcion,
                d.concepto,
                d.descripcion,
                d.sucursal,
                d.asiento,
                d.debe,
                d.haber
                )  
                from PolizaDet d 
                    where d.poliza.fecha between ? and ? 
                    and d.cuenta.clave between  ? and ? 
                    and d.poliza.cierre != null
                    order by d.cuenta.clave 
            """
            log.info('Buscando movimientos de la cta: {} a la: {}', cuentaInicial, cuentaFinal)
            res = PolizaDet.findAll(hql ,[
                command.periodo.fechaInicial,
                command.periodo.fechaFinal,
                cuentaInicial,
                cuentaFinal])

        }
        respond res
    }

    def printAuxiliar(AuxiliarCommand command) {
        if(command == null) {
            render status: NOT_FOUND
            return
        }
        log.info('Auxiliar: {}', command)
        def cuentaInicial = "${command.cuentaInicial}"
        def repParams = [
            FECHA_INI: command.periodo.fechaInicial,
            FECHA_FIN: command.periodo.fechaFinal
            ]

        if(cuentaInicial.split('-').size() < 4) {
            cuentaInicial += '%'
        }
        repParams.CUENTA_INI = cuentaInicial
        if(command.cuentaFinal) {
            def cuentaFinal = command.cuentaFinal
            if(cuentaFinal.split('-').size() < 4) {
                cuentaFinal += '%'
            }
            repParams.CUENTA_FIN = cuentaFinal
        }
        def pdf =  reportService.run('contabilidad/AuxiliarContable.jrxml', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'Poliza.pdf')

    }

    protected void notFound() {
        request.withFormat {
            '*'{ render status: NOT_FOUND }
        }
    }



}

@ToString
class AuxiliarCommand implements  Validateable{
    Periodo periodo
    String cuentaInicial
    String cuentaFinal

    static constraints = {
        cuentaFinal nullable: true
    }
}

@Canonical(excludes = "inicial, acumulado")
class PolizaDetDTO {
    Long id
    String tipo
    String subtipo
    Integer poliza
    Integer ejercicio
    Integer mes
    Date fecha
    String clave
    String ctaDescripcion
    String concepto
    String descripcion
    String sucursal
    String asiento
    BigDecimal debe
    BigDecimal haber
    BigDecimal inicial
    BigDecimal acumulado

}