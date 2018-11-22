package sx.integracion

import org.apache.commons.lang3.builder.ToStringBuilder
import org.springframework.stereotype.Component
import sx.tesoreria.PagoDeNomina

import java.sql.SQLException

import groovy.sql.*

import groovy.util.logging.Slf4j
import org.apache.commons.lang3.exception.ExceptionUtils



@Slf4j
@Component("importadorDePagosDeNomina")
class ImportadorDePagosDeNomina {


    def importar(Integer ejercicio, String periodicidad, Integer folio, String tipo) {
        def select = """
            SELECT n.id as nomina,n.tipo,n.periodicidad,n.forma_de_pago formaDePago,n.folio,n.pago,n.ejercicio,n.total
            ,n.id nominaEmpleado,e.id empleadoId,count(*) as empleados,false pensionAlimenticia
            ,'PAPEL, SA DE CV' AS empleado,'PAPEL, SA DE CV' afavor
            FROM nomina n join nomina_por_empleado ne on(ne.nomina_id=n.id) join empleado e on(ne.empleado_id=e.id)
            where n.forma_de_pago='TRANSFERENCIA' and n.tipo<>'ASIMILADOS' and ne.total>0 and n.ejercicio=@EJERCICIO and n.periodicidad='@PERIODICIDAD' and n.tipo='@TIPO' AND n.folio=@FOLIO
            group by n.id
            union
            SELECT n.id as nomina,n.tipo,n.periodicidad,n.forma_de_pago,n.folio,n.pago,n.ejercicio,ne.total
            ,ne.id as nomina_empleado,ne.empleado_id,1 as num_empleados,false pension_alimenticia
            ,CONCAT(ifnull(E.apellido_paterno,'')," ",ifnull(E.apellido_materno,'')," ",E.nombres) AS empleado
            ,CONCAT(ifnull(E.apellido_paterno,'')," ",ifnull(E.apellido_materno,'')," ",E.nombres) AS  afavor
            FROM nomina n join nomina_por_empleado ne on(ne.nomina_id=n.id) join empleado e on(ne.empleado_id=e.id)
            where n.forma_de_pago='TRANSFERENCIA' and n.tipo='ASIMILADOS' and ne.total>0 and n.ejercicio=@EJERCICIO and n.periodicidad='@PERIODICIDAD' and n.tipo='@TIPO' AND n.folio=@FOLIO
            union
            SELECT n.id as nomina,n.tipo,n.periodicidad,n.forma_de_pago,n.folio,n.pago,n.ejercicio,ne.total
            ,ne.id as nomina_empleado,ne.empleado_id,1 as num_empleados,false pension_alimenticia
            ,CONCAT(ifnull(E.apellido_paterno,'')," ",ifnull(E.apellido_materno,'')," ",E.nombres) AS empleado
            ,CONCAT(ifnull(E.apellido_paterno,'')," ",ifnull(E.apellido_materno,'')," ",E.nombres) AS  afavor
            FROM nomina n join nomina_por_empleado ne on(ne.nomina_id=n.id) join empleado e on(ne.empleado_id=e.id)
            where n.forma_de_pago='CHEQUE' and ne.total>0 and n.ejercicio=@EJERCICIO and n.periodicidad='@PERIODICIDAD' and n.tipo='@TIPO' AND n.folio=@FOLIO
            union            
            SELECT n.id as nomina,n.tipo,n.periodicidad,p.forma_de_pago,n.folio,n.pago,n.ejercicio
            ,(SELECT d.importe_excento FROM nomina_por_empleado_det d where d.parent_id=ne.id and d.concepto_id=7) as total
            ,ne.id as nomina_empleado,ne.empleado_id,0 as num_empleados,true pension_alimenticia
            ,CONCAT(ifnull(E.apellido_paterno,'')," ",ifnull(E.apellido_materno,'')," ",E.nombres) AS empleado,p.beneficiario afavor
            FROM nomina n join nomina_por_empleado ne on(ne.nomina_id=n.id) join empleado e on(ne.empleado_id=e.id) join pension_alimenticia p on(p.empleado_id=e.id)
            where p.tipo_de_pago<>'MENSUAL' and ne.total>0 and n.ejercicio=@EJERCICIO and n.periodicidad='@PERIODICIDAD' and n.tipo='@TIPO' AND n.folio=@FOLIO
            union
            SELECT n.id as nomina,n.tipo,n.periodicidad,p.forma_de_pago,max(n.folio) as folio,n.pago,n.ejercicio
            ,sum((SELECT sum(d.importe_excento) FROM nomina_por_empleado_det d where d.parent_id=ne.id and d.concepto_id=7)) as total
            ,ne.id as nomina_empleado,ne.empleado_id,0 as num_empleados,true pension_alimenticia
            ,CONCAT(ifnull(E.apellido_paterno,'')," ",ifnull(E.apellido_materno,'')," ",E.nombres) AS empleado,p.beneficiario afavor
            FROM calendario c join calendario_det d on(d.calendario_id=c.id) join nomina n on(n.calendario_det_id=d.id)
            join nomina_por_empleado ne on(ne.nomina_id=n.id) join empleado e on(ne.empleado_id=e.id) join pension_alimenticia p on(p.empleado_id=e.id)
            where p.tipo_de_pago='MENSUAL' and n.ejercicio=@EJERCICIO and ne.total>0 and n.periodicidad='@PERIODICIDAD'  and n.tipo='@TIPO'
            and concat(d.mes,'-',@FOLIO)=( SELECT concat(x.mes,'-',max(folio)) FROM calendario_det x join calendario y on(x.calendario_id=y.id) where y.comentario='NOMINA' and y.ejercicio=c.ejercicio and y.tipo=substr(n.periodicidad,1,LENGTH(n.periodicidad)-1)  and d.mes=x.mes )
            group by e.id
    	"""
        select = select.replaceAll("@EJERCICIO", ejercicio.toString())
        select = select.replaceAll("@PERIODICIDAD", periodicidad)
        select = select.replaceAll("@FOLIO", folio.toString())
        select = select.replaceAll("@TIPO", tipo)
        log.info('SQL: ', select)
        def rows = getRows(select)
        log.info('Rows: ', rows)
        def res =  []
        rows.each { row ->
            try {
                res << importarRegistro(row)
            }catch(Exception ex) {
                def message = ExceptionUtils.getRootCauseMessage(ex)
                log.error('Error importando {} {}', row, message)
            }
        }
        return res
    }


    def importarRegistro(def row) {

        log.info("Importando {}", row)
        PagoDeNomina pagoDeNomina = new PagoDeNomina()
        pagoDeNomina.properties = row
        pagoDeNomina.save failOnError: true, flush: true
    }


    def getRows(String sql) {
        def db = getSql()
        try {
            return db.rows(sql)
        }catch (SQLException e){
            e.printStackTrace()
            def c = ExceptionUtils.getRootCause(e)
            def message = ExceptionUtils.getRootCauseMessage(e)
            throw new RuntimeException(message,c)
        }finally {
            db.close()
        }
    }


    def getSql() {
        String user = 'root'
        String password = 'sys'
        String driver = 'com.mysql.jdbc.Driver'
        String dbUrl = 'jdbc:mysql://10.10.1.229/sx_rh'
        Sql db = Sql.newInstance(dbUrl, user, password, driver)
        return db
    }


}