package sx.cxc


import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController


import groovy.util.logging.Slf4j
import org.apache.commons.lang3.exception.ExceptionUtils
import sx.core.LogUser
import sx.core.Sucursal
import sx.reports.ReportService
import sx.utils.Periodo



@Slf4j
@Secured(['ROLE_CREDITO_CXC',  'ROLE_GASTOS', 'ROLE_CONTABILIDAD'])
class NotaDeCreditoController extends RestfulController<NotaDeCredito> implements LogUser {

    NotaDeCreditoService notaDeCreditoService

    ReportService reportService

    static responseFormats = ['json']

    NotaDeCreditoController() {
        super(NotaDeCredito)
    }

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
    protected NotaDeCredito updateResource(NotaDeCredito resource) {
        logEntity(resource)
        return notaDeCreditoService.updateNota(resource)
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

    def generarCfdi(NotaDeCredito nota) {
        if(nota == null) {
            notFound()
            return
        }
        nota = notaDeCreditoService.generarCfdi(nota)
        respond nota, view: 'show'
    }

    def aplicar(NotaDeCredito nota) {
        if(nota == null) {
            notFound()
            return
        }
        nota = notaDeCreditoService.aplicar(nota)
        respond nota, view: 'show'
    }


    def handleException(Exception e) {
        String message = ExceptionUtils.getRootCauseMessage(e)
        log.error(message, ExceptionUtils.getRootCause(e))
        respond([message: message], status: 500)
    }
}
