package sx.compras

import grails.compiler.GrailsCompileStatic
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.*
import sx.reports.ReportService


@GrailsCompileStatic
@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
class ListaDePreciosProveedorController extends RestfulController<ListaDePreciosProveedor> {

    static responseFormats = ['json']

    ListaDePreciosProveedorService listaDePreciosProveedorService
    ReportService reportService

    ListaDePreciosProveedorController() {
        super(ListaDePreciosProveedor)
    }

    @Override
    protected List<ListaDePreciosProveedor> listAllResources(Map params) {
        if(params.proveedorId)
            return ListaDePreciosProveedor.where{ proveedor.id == params.proveedorId}.list()
        return super.listAllResources(params)
    }

    @Override
    protected ListaDePreciosProveedor saveResource(ListaDePreciosProveedor resource) {
        log.info('Salvando lista de precios {}', resource.proveedor)
        return listaDePreciosProveedorService.save(resource)
    }

    @Override
    protected ListaDePreciosProveedor updateResource(ListaDePreciosProveedor resource) {
        return listaDePreciosProveedorService.save(resource)
    }

    @Override
    protected ListaDePreciosProveedor createResource() {
        ListaDePreciosProveedor resource = new ListaDePreciosProveedor()
        bindData resource, getObjectToBind()
        resource.createUser = ''
        resource.updateUser = ''
        return resource
    }



    @Override
    protected void deleteResource(ListaDePreciosProveedor resource) {
        listaDePreciosProveedorService.delete(resource.id)
    }

    def aplicar(ListaDePreciosProveedor lista) {
        if(lista == null ){
            notFound()
            return
        }
        respond listaDePreciosProveedorService.aplicarListaDePrecios(lista)
    }

    def print( ) {
        Map repParams = [ID: params.long('id')]
        def pdf =  reportService.run('ListaDePrecios.jrxml', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: 'ListaDePrecios.pdf')
    }
}
