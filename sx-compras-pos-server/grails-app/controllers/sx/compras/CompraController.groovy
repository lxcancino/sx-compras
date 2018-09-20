package sx.compras

import grails.compiler.GrailsCompileStatic
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.*

import groovy.transform.CompileDynamic
import org.apache.commons.lang3.exception.ExceptionUtils
import sx.core.AppConfig
import sx.core.Proveedor
import sx.core.Sucursal
import sx.reports.ReportService
import sx.utils.Periodo



@Secured("ROLE_COMPRAS")
@GrailsCompileStatic
class CompraController extends RestfulController<Compra> {
    static responseFormats = ['json']
    CompraService compraService
    ReportService reportService

    CompraController() {
        super(Compra)
    }

    @Override
    protected Compra saveResource(Compra resource) {
        return compraService.saveCompra(resource)
    }

    @Override
    protected Compra createResource() {
        Compra compra = new Compra()
        bindData compra, getObjectToBind()
        compra.sucursal = AppConfig.first().sucursal
        compra.folio = -1l
        compra.fecha = new Date()
        return compra
    }

    @Override
    @CompileDynamic
    protected List<Compra> listAllResources(Map params) {

        params.sort = 'lastUpdated'
        params.order = 'desc'
        params.max = params.registros?: 50


        def query = Compra.where{}
        def pendientes = params.getBoolean('pendientes')


        if(pendientes){
            query = query.where{ pendiente == true}
        }

        if(params.proveedorId) {
            String proveedorId = params.proveedorId
            query = query.where{ proveedor.id == proveedorId}
        }

        if(params.periodo && !pendientes) {
            Periodo periodo = params.periodo
            query = query.where{fecha >= periodo.fechaInicial && fecha <= periodo.fechaFinal}
        }
        return  query.list(params)
    }


    def cerrar(Compra compra) {
        if(compra == null) {
            notFound()
            return
        }
        respond compraService.cerrarCompra(compra)
    }

    def pendientes() {
        log.info('Buscando compras pendientes {}', params)
        String id = params.proveedorId
        def query = Compra.where{ proveedor.id == id && pendiente == true}
        params.sort = 'lastUpdated'
        params.order = 'desc'
        /*
        List<Compra> compras =  query.list(params)
        List<Compra> res = compras.findAll{ Compra compra ->
            def pendiente = compra.partidas.find{it.getPorRecibir()> 0.0 }
            return pendiente != null
        }
        respond res
        */
        def res = CompraDet.findAll(
                'select distinct d.compra from CompraDet d where d.compra.proveedor.id = ? and d.solicitado - d.depurado - d.recibido > 0',
                [id], params)
        respond res

    }

    def depurar(Compra compra) {
        if(compra == null) {
            notFound()
            return
        }
        respond compraService.depurarCompra(compra)
    }

    // @CompileDynamic
    def print( ) {
        Map repParams = [ID: params.id]
        repParams.CLAVEPROV = params.getBoolean('clavesProveedor', false)? 'SI' : 'NO'
        repParams.IMPRIMIR_COSTO = 'SI'
        def pdf =  reportService.run('Compra.jrxml', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'ListaDePrecios.pdf')
    }

    def handleException(Exception e) {
        String message = ExceptionUtils.getRootCauseMessage(e)
        log.error(message, ExceptionUtils.getRootCause(e))
        respond([message: message], status: 500)
    }
}
