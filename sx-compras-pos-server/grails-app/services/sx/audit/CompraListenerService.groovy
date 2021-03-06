package sx.audit

import grails.events.annotation.Subscriber
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent
import org.grails.datastore.mapping.engine.event.PostDeleteEvent
import org.grails.datastore.mapping.engine.event.PostUpdateEvent

import org.springframework.beans.factory.annotation.Autowired

import sx.compras.Compra

@Slf4j
@CompileStatic
@Transactional
class CompraListenerService {

    @Autowired AuditLogDataService auditLogDataService

    String getId(AbstractPersistenceEvent event) {
        if ( event.entityObject instanceof Compra ) {
            return ((Compra) event.entityObject).id
        }
        null
    }

    Compra getCompra(AbstractPersistenceEvent event) {
        if ( event.entityObject instanceof Compra ) {
            return (Compra) event.entityObject
        }
        null
    }

    @Subscriber
    void afterUpdate(PostUpdateEvent event) {
        // log.debug('{} {} ', event.eventType.name(), event.entity.name)
        String id = getId(event)
        if ( id ) {
            log.debug('{} {} Id: {}', event.eventType.name(), event.entity.name, id)
            Compra compra = getCompra(event)
            logEntity(compra, 'UPDATE')
        }
    }

    @Subscriber
    void afterDelete(PostDeleteEvent event) {
        Compra compra = getCompra(event)
        if ( compra ) {
            logEntity(compra, 'DELETE')
        }
    }

    def logEntity(Compra compra, String type) {
        if(compra.cerrada != null) {
            Boolean central = compra.sucursal.clave.trim() == '1' ? true : false
            if(central) {
                ['SOLIS',
                 'TACUBA',
                 'ANDRADE',
                 'CALLE 4',
                 'CF5FEBRERO',
                 'VERTIZ 176',
                 'BOLIVAR'].each {
                    buildLog(compra, it, type)
                }
            } else {
                buildLog(compra, compra.sucursal.nombre, type)
            }
        }
    }

    def buildLog(Compra compra, String destino, String type) {
        Audit alog = new Audit(
                name: 'Compra',
                persistedObjectId: compra.id,
                source: 'CENTRAL',
                target: destino,
                tableName: 'compra',
                eventName: type
        )
        alog.save flush: true
        // auditLogDataService.save(log)

        compra.partidas.each {
            Audit logDet = new Audit(
                    name: 'CompraDet',
                    persistedObjectId: it.id,
                    source: 'CENTRAL',
                    target: destino,
                    tableName: 'compra_det',
                    eventName: type
            )
            logDet.save flush: true
            // auditLogDataService.save(logDet)
        }
    }
}
