package sx.core

import grails.compiler.GrailsCompileStatic
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController

@GrailsCompileStatic
@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
class ProductoController extends RestfulController<Producto> {

    static responseFormats = ['json']

    ProductoService productoService

    ProductoController() {
        super(Producto)
    }


    @Override
    protected List<Producto> listAllResources(Map params) {
        def query = Producto.where {}
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'
        params.max = 1000

        if(params.term){
            def search = '%' + params.term + '%'
            query = query.where { clave =~ search || descripcion =~ search}
        }

        Boolean activos = this.params.getBoolean('activos')
        if(activos) query = query.where {activo == activos}

        Boolean deLinea = this.params.getBoolean('deLinea')
        if(deLinea) query = query.where {deLinea == deLinea}

        return query.list(params)
    }
    /*
    @Override
    protected Producto updateResource(Producto resource) {
        return super.updateResource(resource)
    }

    @Override
    protected Producto saveResource(Producto resource) {
        return super.saveResource(resource)
    }
    */
}
