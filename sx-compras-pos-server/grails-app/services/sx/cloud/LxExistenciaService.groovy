package sx.cloud

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import java.sql.SQLException

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import groovy.sql.*

import grails.util.Environment
import org.springframework.scheduling.annotation.Scheduled

import com.google.firebase.cloud.FirestoreClient
import com.google.cloud.firestore.Query
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.SetOptions
import com.google.cloud.firestore.WriteResult
import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.CollectionReference
import com.google.api.core.ApiFuture

import org.apache.commons.lang3.exception.ExceptionUtils

import sx.core.AppConfig
import sx.core.LogUser
import sx.core.FolioLog
import sx.core.Producto
import sx.core.Existencia
import sx.core.Sucursal
import sx.utils.Periodo

import sx.audit.Audit
import sx.audit.AuditLog

@Slf4j
class LxExistenciaService {


    FirebaseService firebaseService

    def dataSource

    static String TIME_FORMAT = 'dd/MM/yyyy HH:mm'

    static String ENTITY = 'CloudExis'

    def publishAllExistenciaCollection() {
        Date start = new Date()
        Sucursal sucursal = AppConfig.first().sucursal
        def ejercicio = Periodo.obtenerYear(new Date())
        def mes = Periodo.obtenerMes(new Date()) + 1
        def select = """
            select 
                e.sucursal_nombre as almacen, 
                e.anio as ejercicio, 
                e.mes as mes, 
                p.clave, 
                p.descripcion, 
                CAST(e.cantidad AS UNSIGNED)as cantidad,
                CAST(e.recorte AS UNSIGNED)as recorte, 
                recorte_comentario as recorteComentario
            from existencia e
            join producto p on (e.producto_id = p.id)
            where e.sucursal_id = :sucursal
            and e.anio = :ejercicio  
            and e.mes = :mes
            and p.activo = true
            order by p.clave 
            limit 2
        """
        List<Map> rows = getRows(select, ['ejercicio': ejercicio, 'mes': mes, 'sucursal': sucursal.id])
        
        int updates = 0
        
        rows.each { row ->
            
            try {
                Map exis = [clave: clave, descripcion: data.descripcion]
                DocumentReference docRef = firebaseService
                .getFirestore()
                .collection('existencias')
                .document(exis.clave)
                .put(exis)
            }catch (Exception ex) {
                def c = ExceptionUtils.getRootCause(ex)
                def message = ExceptionUtils.getRootCauseMessage(ex)
                log.error('Error: {}', message)
            }
            updates++
            
        }
        logChange(sucursal.nombre, 'PUBLISH_EXISTENCIA_COLLECTION', start, updates)
        return updates
    }

    def publishAll() {
        Date start = new Date()
        Sucursal sucursal = AppConfig.first().sucursal
        def ejercicio = Periodo.obtenerYear(new Date())
        def mes = Periodo.obtenerMes(new Date()) + 1
        def select = """
            select 
                e.sucursal_nombre as almacen, 
                e.anio as ejercicio, 
                e.mes as mes, 
                p.clave, 
                p.descripcion, 
                CAST(e.cantidad AS UNSIGNED)as cantidad,
                CAST(e.recorte AS UNSIGNED)as recorte, 
                recorte_comentario as recorteComentario
            from existencia e
            join producto p on (e.producto_id = p.id)
            where e.sucursal_id = :sucursal
            and e.anio = :ejercicio  
            and e.mes = :mes
            and p.activo = true
            order by p.clave 
            limit 2
        """
        List<Map> rows = getRows(select, ['ejercicio': ejercicio, 'mes': mes, 'sucursal': sucursal.id])
        
        int updates = 0
        
        rows.each { row ->
            row.ejercicio = row.ejercicio as Integer
            row.mes = row.mes as Integer
            row.cantidad = row.cantidad.toLong()
            row.recorte = row.recorte.toLong()
            log.debug('Exis: {}', row)
            
            try {
                publish(row.clave, row)
            }catch (Exception ex) {
                def c = ExceptionUtils.getRootCause(ex)
                def message = ExceptionUtils.getRootCauseMessage(ex)
                log.error('Error: {}', message)
            }
            
            updates++
            
        }
        logChange(sucursal.nombre, 'PUBLISH_ALL', start)
        return updates
    }


    def publish(String clave, Map<String, Object> data) {
        
        DocumentReference docRef = firebaseService.getFirestore()
            .collection('existencias')
            .document(clave)

        DocumentSnapshot snapShot = docRef.get().get()

        ApiFuture<WriteResult> result = null
        
        if (!snapShot.exists()) {
            log.debug('No hay existencia GLOBAL para: {}', clave)
            Map exis = [clave: clave, descripcion: data.descripcion]
            docRef.set(exis)
            result = docRef
                .collection('almacenes')
                .document(data.almacen)
                .set(data)
        } else {
            log.debug('Actualizando existencia de {} en almancen: {}', clave, data.almacen)
            result = docRef
                .collection('almacenes')
                .document(data.almacen)
                .set(data)
            // return null
        }
        def updateTime = result.get().getUpdateTime()
        log.debug("Publish time : {} " , updateTime)
        return updateTime
    }


    def publishFromAudit() {
        Date start = new Date()
        Sucursal sucursal = AppConfig.first().sucursal
        def list = Audit.where{name == 'CloudExis'}.list([sort: 'dateCreated', order: 'desc', max: 1])
        if(!list)
            return 
        def audit = list[0]
        def lastTime = Date.parse(TIME_FORMAT, audit.message)
        log.info('Actualizando con Audit desde {}', lastTime);
        def ids = AuditLog.findAll(
            'select distinct a.persistedObjectId from AuditLog a where a.name = ? and a.dateCreated >= ?',['Existencia', lastTime])
        if(!ids) {
            log.debug('No data to update since:{}', lastTime)
            return
        } else {
            log.debug("Existencias por actualizar: {}", ids.size())
        }
        int updates = 1
        ids.each {
            def exis = Existencia.get(it)
            
            Map data = [
                almacen: exis.sucursal.nombre,
                clave: exis.producto.clave, 
                descripcion: exis.producto.descripcion,
                ejercicio: exis.anio as Integer,
                mes: exis.mes as Integer,
                cantidad: exis.cantidad as Long,
                recorte: exis.recorte as Long,
                recorteComentario: exis.recorteComentario
                ]
            try{
                def published = publish(data.clave, data)
                updates++
            } catch(Exception ex) {
                def message = ExceptionUtils.getRootCauseMessage(ex)
                log.error('Error actualizando existencia {}', message)
            }
            
        }

        logChange(sucursal.nombre,'PUBLISH_FROM_AUDITLOG', start, updates )
    }


    def publishFromAuditLog(){
    
        Sucursal sucursal = AppConfig.first().sucursal
        def audits = AuditLog.where{name == 'Existencia' && replicatedCloud == null}.list([sort: 'dateCreated', order: 'desc'])
        if(!audits){
            return
        }
            Firestore db = firebaseService.getFirestore()
            CollectionReference existencias  = db.collection("existencias");
            

        audits.each{

            println it.persistedObjectId
            def exis = Existencia.get(it.persistedObjectId)

            if(exis){

                DocumentReference docRef =  existencias.document(exis.producto.id)
                DocumentSnapshot snapShot = docRef.get().get()
                
                Map data = [
                    almacen: exis.sucursal.nombre,
                    cantidad: exis.cantidad as Long,
                    recorte: exis.recorte as Long,
                    recorteComentario: exis.recorteComentario
                    ]

                ApiFuture<WriteResult> result = null
                
                if (!snapShot.exists()) {
                    
                    Map exist = [
                        id: exis.producto.id,
                        clave: exis.producto.clave, 
                        descripcion: exis.producto.descripcion,
                        producto:exis.producto.id,
                        ejercicio: exis.anio as Integer,
                        mes: exis.mes as Integer
                        ]
                    docRef.set(exist)
                    result = docRef
                        .collection('almacenes')
                        .document(data.almacen)
                        .set(data)
                } else {
             
                    result = docRef
                        .collection('almacenes')
                        .document(data.almacen)
                        .set(data)
                    // return null
                }
                def updateTime = result.get().getUpdateTime()
                log.debug("Publish time : {} " , updateTime)
                it.replicatedCloud = new Date()
                it.save failOnError: true, flush: true
                return updateTime
            }

        }
    }

    /*
    @Scheduled(fixedDelay = 60000L, initialDelay = 30000L)
    void syncWithFirebase() {
        Environment.executeForCurrentEnvironment {
            Date start = new Date()
            production {
                log.debug('Sincronizando existencias con FireBase [PROD] Start:{}', start)
                // lxExistenciaService.publishFromAudit()
            }
            development {
                log.debug('Sincronizando existencias con FireBase [DEV] Start: {}', start)
                publishFromAudit()
            }
        }
    }

    */

    def getRows(String sql, Map params) {
        Sql db = getSql()
        try {
            return db.rows(sql, params)
        }catch (SQLException e){
            e.printStackTrace()
            def c = ExceptionUtils.getRootCause(e)
            def message = ExceptionUtils.getRootCauseMessage(e)
            throw new RuntimeException(message,c)
        }finally {
            db.close()
        }
    }


    Sql getSql() {
        /*
        String user = 'root'
        String password = 'sys'
        String driver = 'com.mysql.jdbc.Driver'
        String dbUrl = 'jdbc:mysql://10.10.1.229/sx_rh'
        Sql db = Sql.newInstance(dbUrl, user, password, driver)
        */
        return new Sql(dataSource)
    }

    def logChange(String sucursal, String event, Date start, int registros) {
        def message = start.format(TIME_FORMAT)
        def id = new Date().getTime()
        Audit alog = new Audit(
                name: 'CloudExis',
                persistedObjectId: id,
                source: sucursal,
                target: 'FIREBASE',
                tableName: 'EXISTENCIA',
                eventName: event,
                message: message,
                registros: registros
        )
        alog.save flush: true

    }

}
