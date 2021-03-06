package sx.tesoreria


import grails.compiler.GrailsCompileStatic
import grails.gorm.DetachedCriteria
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.*
import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j

import org.apache.commons.lang3.exception.ExceptionUtils

import sx.reports.ReportService
import sx.utils.Periodo

@Slf4j
@GrailsCompileStatic
@Secured("ROLE_TESORERIA")
class PagoDeMorrallaController extends RestfulController<PagoDeMorralla> {

    static responseFormats = ['json']

    PagoDeMorrallaService pagoDeMorrallaService

    ReportService reportService

    PagoDeMorrallaController() {
        super(PagoDeMorralla)
    }

    @Override
    protected List listAllResources(Map params) {
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'
        params.max = 500
        log.debug('List : {}', params)
        Periodo periodo = (Periodo)params.periodo

        def criteria = new DetachedCriteria(PagoDeMorralla).build {
            between("fecha", periodo.fechaInicial, periodo.fechaFinal)
        }
        return criteria.list(params)
    }


    @Override
    protected PagoDeMorralla saveResource(PagoDeMorralla resource) {
        return pagoDeMorrallaService.registrar(resource)
    }

    @Override
    @CompileDynamic
    protected void deleteResource(PagoDeMorralla resource) {
        pagoDeMorrallaService.delete(resource.id)
    }

    def pendientes() {
        /*

        def criteria = new DetachedCriteria(Morralla).build {
            gt('fecha', Date.parse('dd/MM/yyyy', '31/12/2017'))
            lt('importe', 0)
        }
        respond criteria.list([sort: 'fecha', order: 'asc'])
        */
        respond Morralla
                .findAll("""
            from Morralla m 
            where m.importe > 0 
            and date(m.fecha) > ? 
            and  m not in (select p from PagoDeMorralla m join m.partidas p) 
            order by m.fecha""", [Date.parse("dd/MM/yyyy", "31/12/2017")])
    }

    def handleException(Exception e) {
        String message = ExceptionUtils.getRootCauseMessage(e)
        e.printStackTrace()
        log.error(message, ExceptionUtils.getRootCause(e))
        respond([message: message], status: 500)
    }
}
