package sx.core

import grails.compiler.GrailsCompileStatic
import grails.rest.*
import grails.converters.*

@GrailsCompileStatic
class MarcaController extends RestfulController<Marca> {
    static responseFormats = ['json']
    MarcaController() {
        super(Marca)
    }

    @Override
    protected List<Marca> listAllResources(Map params) {
        params.max = 500
        return super.listAllResources(params)
    }
}
