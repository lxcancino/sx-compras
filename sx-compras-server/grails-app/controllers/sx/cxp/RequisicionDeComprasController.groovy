package sx.cxp

import grails.compiler.GrailsCompileStatic
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController
import groovy.transform.CompileDynamic
import sx.reports.ReportService
import sx.tesoreria.PagoDeRequisicion
import sx.tesoreria.PagoDeRequisicionService
import sx.utils.Periodo

@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
@GrailsCompileStatic
class RequisicionDeComprasController extends RestfulController<RequisicionDeCompras> {

    static responseFormats = ['json']

    RequisicionDeComprasService requisicionDeComprasService

    PagoDeRequisicionService pagoDeRequisicionService

    ReportService reportService

    RequisicionDeComprasController() {
        super(RequisicionDeCompras)
    }

    @Override
    @CompileDynamic
    protected List<RequisicionDeCompras> listAllResources(Map params) {
        log.debug('List: {}', params)

        params.sort = 'lastUpdated'
        params.order = 'desc'
        params.max = 1000
        def periodo = params.periodo
        def query = RequisicionDeCompras.where{fecha >= periodo.fechaInicial && fecha<= periodo.fechaFinal}
        return query.list(params);
    }

    @CompileDynamic
    def update() {
        String id = params.id as String
        RequisicionDeCompras requisicion = RequisicionDeCompras.get(id)
        bindData requisicion, getObjectToBind()
        requisicion = requisicionDeComprasService.update(requisicion)
        respond requisicion, view: 'show'
    }

    @Override
    protected RequisicionDeCompras saveResource(RequisicionDeCompras resource) {
        return requisicionDeComprasService.save(resource)
    }

    @Override
    protected RequisicionDeCompras updateResource(RequisicionDeCompras resource) {
        return requisicionDeComprasService.update(resource)
    }

    /**
     * Elimina la requisicion
     *
     * @param resource
     */
    @Override
    protected void deleteResource(RequisicionDeCompras resource) {
        requisicionDeComprasService.delete(resource)
    }

    @CompileDynamic
    protected RequisicionDeCompras createResource() {
        RequisicionDeCompras res = new RequisicionDeCompras()
        bindData res, getObjectToBind()
        res.folio = 0L
        res.createUser = 'TEMPO'
        res.updateUser = 'TEMPO'
        if(!res.nombre)
            res.nombre = res.proveedor.nombre
        return res
    }


    /**
     *
     * @return La Lista de facturas pendientes
     *
     * @TODO Modificar el hql query para contemplar solo las analizadas y descriminar
     * las ya incluidas en una requisicion
     */
    def pendientes() {
        // log.debug('Facturas pendientes {}', params)
        String id = params.proveedorId
        List<CuentaPorPagar> facturas = CuentaPorPagar
                .findAll("from CuentaPorPagar c where c.proveedor.id = ? " +
                "  and c.saldoReal > 0 " +
                "  and c not in(select d.cxp from RequisicionDet d where d.requisicion.proveedor = c.proveedor)" +
                "  order by c.fecha desc",
                [id], [max: 400])
        respond facturas
    }

    def cerrar(RequisicionDeCompras requisicion) {
        if(requisicion == null) {
            notFound()
            return
        }
        requisicion = requisicionDeComprasService.cerrar(requisicion)
        respond requisicion, view: 'show'
    }

    def print( ) {
        Map repParams = [ID: params.id]
        repParams.MONEDA = params.moneda
        def pdf =  reportService.run('Requisicion.jrxml', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'Requisicion.pdf')
    }


}
