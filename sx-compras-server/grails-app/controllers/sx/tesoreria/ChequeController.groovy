package sx.tesoreria

import grails.compiler.GrailsCompileStatic
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.*

import org.apache.commons.lang3.exception.ExceptionUtils

import sx.reports.ReportService
import sx.utils.ImporteALetra
import sx.utils.Periodo

@GrailsCompileStatic
@Secured(['ROLE_TESORERIA', 'ROLE_CONTABILIDAD'])
class ChequeController extends RestfulController<Cheque> {

    static responseFormats = ['json']

    ReportService  reportService

    ChequeController() {
        super(Cheque)
    }

    @Override
    protected List<Cheque> listAllResources(Map params) {


        params.sort = 'folio'
        params.order = 'desc'
        params.max = params.registros ?: 20
        log.info('List: {}', params)

        def query = Cheque.where {}

        def impresos = this.params.getBoolean('impresos')

        if(impresos) {
            query = query.where {impresion != null}
        }


        if(params.periodo) {
            Periodo periodo = (Periodo)params.periodo
            query = query.where {fecha >= periodo.fechaInicial && fecha <= periodo.fechaFinal}
        }

        if(params.nombre) {
            query = query.where {nombre =~ params.nombre}
        }
        return query.list(params)
    }



    // @CompileDynamic
    def print( ) {
        Cheque cheque = Cheque.get(params.id.toString())
        String reportName = cheque.cuenta.impresionTemplate
        if(!reportName) {
            throw new RuntimeException("No existe formato de imresion para la cuenta ${cheque.cuenta.numero} (${cheque.cuenta.descripcion})")
        }
        cheque.impresion = new Date()
        cheque.save flush: true
        Map repParams = [ID: params.id]
        repParams.IMPLETRA = ImporteALetra.aLetra(cheque.importe.abs())
        def pdf =  reportService.run(reportName, repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'Requisicion.pdf')
    }

    def printPoliza( ) {
        Cheque cheque = Cheque.get(params.id.toString())
        Map repParams = [ID: params.id]
        repParams.IMPLETRA = ImporteALetra.aLetra(cheque.importe.abs())
        def pdf =  reportService.run('PolizaCheque.jrxml', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'PolizaCheque.pdf')
    }

    def chequesPendientes( ) {
        Map repParams = [:]
        def pdf =  reportService.run('ChequesPendientesDeCobro.jrxml', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'ChequesPendientesDeCobro.pdf')
    }

    def handleException(Exception e) {
        String message = ExceptionUtils.getRootCauseMessage(e)
        log.error(message, ExceptionUtils.getRootCause(e))
        respond([message: message], status: 500)
    }
}
