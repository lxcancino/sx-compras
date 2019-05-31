package sx.cxc

import grails.compiler.GrailsCompileStatic
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController
import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.exception.ExceptionUtils
import sx.core.LogUser
import sx.core.Sucursal
import sx.reports.ReportService
import sx.utils.Periodo

@Slf4j
@GrailsCompileStatic
@Secured(['ROLE_CREDITO_CXC',  'ROLE_GASTOS', 'ROLE_CONTABILIDAD'])
class NotaDeCreditoController extends RestfulController<NotaDeCredito> implements LogUser {

    NotaDeCreditoService notaDeCreditoService

    ReportService reportService

    static responseFormats = ['json']

    NotaDeCreditoController() {
        super(NotaDeCredito)
    }

    @CompileDynamic
    protected List<NotaDeCredito> listAllResources(Map params) {

        params.sort = params.sort ?: 'lastUpdated'
        params.order = params.order ?: 'desc'
        params.max = params.registros ?: 15
        def cartera = params.cartera ?: 'CRE'
        def stipo = params.tipo ?: 'BONIFICACION'

        log.info('List: params:{} ', params)
        log.info('Cartera: {} Tipo:{} ', cartera, stipo)

        def query = NotaDeCredito.where {tipoCartera == cartera && tipo == stipo}

        if(cartera == 'CON') {
            query = query.where{ tipoCartera in ['CON', 'COD']}
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

    @Override
    protected NotaDeCredito saveResource(NotaDeCredito resource) {
        logEntity(resource)
        return notaDeCreditoService.saveNota(resource)
    }

    @Override
    protected void deleteResource(NotaDeCredito resource) {
        super.deleteResource(resource)
    }

    @Override
    protected NotaDeCredito createResource() {
        NotaDeCredito nota = new NotaDeCredito(folio: -1L)
        bindData(nota, getObjectToBind())
        nota.sucursal = Sucursal.where{nombre == 'OFICINAS'}.find()
        String prefix = nota.tipo == 'BONIFICACION' ? 'BON' : 'DEV'
        String serie = "${prefix}${nota.tipoCartera}"
        nota.serie = serie
        return nota
    }

    @CompileDynamic
    def handleException(Exception e) {
        String message = ExceptionUtils.getRootCauseMessage(e)
        log.error(message, ExceptionUtils.getRootCause(e))
        respond([message: message], status: 500)
    }
}
