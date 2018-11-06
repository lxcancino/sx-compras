package sx.costos

import grails.compiler.GrailsCompileStatic
import grails.gorm.transactions.Transactional
import grails.transaction.NotTransactional
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import org.apache.commons.lang.exception.ExceptionUtils

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import sx.core.Existencia
import sx.core.Inventario
import sx.core.Producto
import sx.inventario.Transformacion
import sx.utils.Periodo

import javax.sql.DataSource
import java.sql.SQLException

@Transactional
// @GrailsCompileStatic
@Slf4j
class CostoPromedioService {

    @Autowired
    @Qualifier('dataSource')
    DataSource dataSource

    def generar(Integer ejercicio , Integer mes) {
        PeriodoDeCosteo per = new PeriodoDeCosteo(ejercicio, mes)
        PeriodoDeCosteo anterior = per.getPeriodoAnterior()
        List<CostoPromedio> costos = []
        List<CostoPromedio> anteriores = CostoPromedio.where {ejercicio == anterior.ejercicio && mes == anterior.mes}.list()
        log.info('Trasladando  {} registros de costo promedio del period anterior: {} al {}', anteriores.size(), anterior, per)

        anteriores.each {
            CostoPromedio cp = CostoPromedio.findOrSaveWhere(ejercicio: per.ejercicio, mes: per.mes, producto: it.producto)
            cp.costo = it.costo
            cp.costoAnterior = it.costo
            cp.clave = it.clave
            cp.descripcion = it.descripcion
            cp.save failOnError: true
            costos << cp
        }
        return costos
    }

    def costearExistenciaInicial(Integer ejercicio, Integer mes) {
        log.info('Calculando el costo inicial de la existencia {} - {}', ejercicio, mes)
        Integer ejercicioAnterior = ejercicio
        Integer mesAnterior = mes - 1
        if(mes == 1) {
            ejercicioAnterior = ejercicio - 1
            mesAnterior = 12
        }
        log.info('Ejercicio anterior: {}-{}', ejercicioAnterior, mesAnterior)
        String sql = """ 
                UPDATE EXISTENCIA E JOIN PRODUCTO P ON(E.PRODUCTO_ID = P.ID)
                SET COSTO = IFNULL((SELECT C.COSTO FROM COSTO_PROMEDIO C WHERE C.EJERCICIO = ? AND C.MES = ? AND C.PRODUCTO_ID = E.PRODUCTO_ID ), 0)
                WHERE P.DE_LINEA IS TRUE AND E.ANIO = ? AND E.MES = ? 
                """
        executeUdate(sql, [ejercicioAnterior, mesAnterior, ejercicio, mes])
    }


    def costearTransformaciones(Integer ejercicio , Integer mes) {
        PeriodoDeCosteo per = new PeriodoDeCosteo(ejercicio, mes)
        PeriodoDeCosteo anterior = per.getPeriodoAnterior()
        Periodo periodo = Periodo.getPeriodoEnUnMes(mes - 1, ejercicio)

        def trs = Transformacion.where{ fecha >= periodo.fechaInicial && fecha <= periodo.fechaFinal }
        log.debug("Costeando {} registros de transformaciones para el periodo {} ", trs.size(), periodo)
        trs.each {
            def costo = null
            it.partidas.sort{it.cantidad}.sort{it.sw2}.each { tr ->
                if(tr.cantidad < 0) {
                    CostoPromedio cp = CostoPromedio.where{ejercicio == anterior.ejercicio && mes == anterior.mes && producto == tr.producto}.find()
                    if(cp){
                        costo = cp.costo
                        // log "Salida  ${tr.producto.clave} Cantidad: ${tr.cantidad} CostoU: ${costo} (sw2:${tr.sw2})"
                    } else {
                        log.error("Error no se enctontro Costo promedio en el periodo anterior...")
                        costo = null
                    }

                } else {
                    if(costo) {
                        try {
                            Inventario iv = tr.inventario
                            if(iv) {
                                iv.costo = costo
                                iv.save flush: true
                                // println " Entrada costeada ${tr.producto.clave}  Cantidad:${tr.cantidad}  CostoU: ${costo}(sw2:${tr.sw2})"
                            } else {
                                log.info("TRS sin inventario {}", tr)
                            }
                        }catch(Exception ex) {
                            ex.printStackTrace()
                            log.error("Error costeando  ${it.tipo} ${it.documento} {}", ex.message)
                        }
                    }

                }

            }
        }

    }

    @NotTransactional
    def calcular(Integer ejercicio , Integer mes) {
        List<CostoPromedio> costos = CostoPromedio.where {ejercicio == ejercicio && mes == mes}.list()
        log.info('Actualizando costo a {} registros de costo promedio para el periodo {} - {}', costos.size(), mes, ejercicio)

        String sql ="""
            SELECT A.clave ,round(ifnull(SUM(a.IMP_COSTO)/SUM(A.CANT),0),2) AS costop
            FROM (
            SELECT 'EXI' as tipo,p.clave
            ,(case when SUM(x.existencia_inicial)>0 then SUM(x.existencia_inicial/(case when p.unidad ='MIL' then 1000 else 1 end)) else 0 end) AS CANT
            ,(case when SUM(x.existencia_inicial)>0 then SUM(x.existencia_inicial/(case when p.unidad ='MIL' then 1000 else 1 end)*COSTO) else 0 end) AS IMP_COSTO 
            FROM existencia x JOIN PRODUCTO P ON(X.producto_id=P.ID) WHERE anio=? and mes=? and p.de_linea is true and p.inventariable is true and existencia_inicial<>0
            group by clave
            union
            SELECT 'INV' as tipo,P.clave
            ,SUM(CANTIDAD/(case when p.unidad ='MIL' then 1000 else 1 end)) AS CANT,SUM(CANTIDAD/(case when p.unidad ='MIL' then 1000 else 1 end)*(COSTO+GASTO)) AS IMP_COSTO
             FROM INVENTARIO X JOIN PRODUCTO P ON(X.producto_id=P.ID) 
             WHERE x.costo>0 and year(x.fecha)=(?) and month(x.fecha)=(?) and x.tipo in('TRS','REC','COM')  and x.cantidad>0 and p.de_linea is true and p.inventariable is true
             GROUP BY P.CLAVE
             ) AS A
             GROUP BY A.CLAVE
        """
        List<CostoPromedio> actualizados = []
        getLocalRows(sql, [ejercicio, mes, ejercicio, mes]).each { row ->
            CostoPromedio cp = costos.find { it.clave == row.clave}
            if(!cp) {
                Producto producto = Producto.findByClave(row.clave)
                cp = new CostoPromedio(ejercicio: ejercicio, mes: mes, producto: producto)
                cp.clave = producto.clave
                cp.descripcion = producto.descripcion
                cp.costoAnterior = 0.0
                costos << cp
            }
            cp.costo = row.costop
            cp.save flush: true
            actualizados << cp
        }
        log.debug("{} Registros actualizados ", actualizados.size())
        return costos
    }

    def aplicar(Integer ejercicio, Integer mes) {
        costearExistencias(ejercicio, mes)
        costearInventario(ejercicio, mes)
    }


    /**
     * Traslada el costo promedio a la tabla de existencias
     *
     * @param ejercicio
     * @param mes
     * @return
     */
    def costearExistenciaFinal(Integer ejercicio, Integer mes) {
        log.debug("Costaeand el inventario final para {} - {}", mes, ejercicio)
        String sql = """ 
                UPDATE EXISTENCIA E JOIN PRODUCTO P ON(P.ID = E.PRODUCTO_ID)
                SET COSTO_PROMEDIO = IFNULL((SELECT C.COSTO FROM COSTO_PROMEDIO C WHERE C.EJERCICIO = E.ANIO AND C.MES = E.MES AND C.PRODUCTO_ID = E.PRODUCTO_ID ), 0)
                WHERE P.DE_LINEA IS TRUE AND E.ANIO = ? AND E.MES = ? 
                """
        executeUdate(sql, [ejercicio, mes])
    }

    def costearMovimientosDeInventario(Integer ejercicio, Integer mes) {
        log.debug("Costeando los movimientos de  inventario  {} - {}", mes, ejercicio)
        String sql = """ 
                UPDATE INVENTARIO E JOIN PRODUCTO P ON(P.ID = E.PRODUCTO_ID)
                SET COSTO_PROMEDIO = IFNULL((SELECT C.COSTO FROM COSTO_PROMEDIO C WHERE C.EJERCICIO = ? AND C.MES = ? AND C.PRODUCTO_ID = E.PRODUCTO_ID ), 0)
                WHERE P.DE_LINEA IS TRUE AND year(E.FECHA) = ? AND month(E.FECHA) = ? 
                """
        executeUdate(sql, [ejercicio, mes, ejercicio, mes])
    }

    def actualizarMovimientosExistenciaSinCosto(Integer ejercicio, Integer mes) {
        def q = CostoPromedio.where{ejercicio == ejercicio && mes == mes && costo <= 0}
        q = q.where{costoAnterior > 0 }
        q.list().each {
           // log.info("${it.clave} ${it.costoAnterior} ${it.costo}")
            // Actualizar todos los movimientos
            Inventario.executeUpdate("update Inventario set costoPromedio = ? where year(fecha)=? and month(fecha)=? and producto =?", [it.costoAnterior, ejercicio, mes, it.producto])
            Existencia.executeUpdate("update Existencia set costoPromedio = ? where anio = ? and mes = ? and producto = ?", [it.costoAnterior, ejercicio, mes, it.producto])
        }
    }

    def analisisDeCosto(CostoPromedio cp) {
        def movimientos = Inventario.findAll("""
                from Inventario i 
                where i.producto = ? and year(fecha)= ? and month(fecha) = ? and tipo in('COM', 'TRS', 'REC')
                """, [cp.producto, cp.ejercicio, cp.mes])
        def existencias =  Existencia.findAll("from Existencia e where e.producto = ? and e.anio = ? and e.mes = ?",
                [cp.producto, cp.ejercicio, cp.mes])

        return rows
    }



    def executeUdate(String sql, List params) {
        Sql db = getLocalSql()
        try {
            db.executeUpdate(sql, params)
        }catch (SQLException e){
            e.printStackTrace()
            def c = ExceptionUtils.getRootCause(e)
            def message = ExceptionUtils.getRootCauseMessage(e)
            throw new RuntimeException(message,c)
        }finally {
            db.close()
        }
    }

    def getLocalRows(String sql, List params) {
        def db = getLocalSql()
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

    def costeoMedidasEspeciales(def mes, def ejercicio){
    def productos = Producto.findAllByDeLineaAndInventariable(false,true)
    productos.each{producto ->
        costeoPorProducto(mes, ejercicio, producto)
    }
}

def costeoPorProducto(def mes, def ejercicio, def producto){
    
		def periodo = Periodo.getPeriodoEnUnMes(mes-1,ejercicio)   	

    	def fechaIni = periodo.fechaInicial
         
        def fechaFin = periodo.fechaFinal
    
        def inventarios =Inventario.executeQuery("from Inventario i  where date(i.fecha) between ? and ? and i.producto = ? ",[fechaIni,fechaFin,producto])

		def existencias= Existencia.executeQuery("from Existencia e where e.producto= ? and mes = month(?) and anio=year(?)",[producto,fechaFin,fechaFin])  
        
        if(inventarios){
            
          //  println "************************************************************************" + producto.clave +" **********************"+periodo
            
            def costoPromedio = 0.00
           
            def com=inventarios.find{it.tipo == 'COM'}
            
            def trs=inventarios.find{it.tipo == 'TRS' && it.cantidad >0 }
            
            def rec=inventarios.find{it.tipo == 'REC' && it.cantidad >0 }
           
            
            if(!com && !trs && !rec){
                //println "No tiene entradas  "  
             
                def row = existencias.find{it.costo != 0}
             	
             	if(row){
                 	costoPromedio= row.costo
             	}
                
                
            }else{

                         //println "Si tiene entradas  "  
                
                def inventariosEnt =Inventario.executeQuery("from Inventario i  where date(i.fecha) between ? and ? and i.producto = ? AND TIPO in ('COM','TRS','REC') and cantidad > 0 and costo>0 ",[fechaIni,fechaFin,producto])
                
                if(inventariosEnt.size()>=1 ){
                    
                    def existenciaInicial = existencias.sum{it.existenciaInicial}?:0.00
                    def existenciaCosto = existencias.find{ it.costo >0 }

                    def exisCosto = 0.00
                    if(existenciaCosto){
                        exisCosto = existenciaCosto.costo
                    }

                      if(existenciaInicial < 0){
                        existenciaInicial = 0.00
                        exisCosto = 0.00 
                    }

                    def movsTotal=inventariosEnt.sum{it.cantidad} ?: 0.00 
                    
                    def cantidadTotal=existenciaInicial + movsTotal

                    def costoInicial= existenciaInicial * exisCosto

                                      
                    def costoTotal=inventariosEnt.sum{it.cantidad*(it.costo+it.gasto)} + costoInicial

                    costoPromedio=costoTotal/cantidadTotal
                    
                    println " e ini:  "+existenciaInicial
                    println " c ini:  "+exisCosto
                    println "costo in: "+costoInicial

                    println " movs tot:  "+movsTotal
                    println " tot uni:  "+cantidadTotal
                    println " costo tot:  "+costoTotal
                    println "-------"+costoPromedio
                } 
            }
            
            if(costoPromedio){
            
                    inventarios.each{invent ->
                        println invent.id+"    "+invent.documento+"  "+invent.fecha+"    "+invent.tipo +" "+ invent.cantidad +" "+invent.costo+" "+invent.gasto+" "+invent.costoPromedio  	
                        invent.costoPromedio=costoPromedio
                        println invent.id+"    "+invent.documento+"  "+invent.fecha+"    "+invent.tipo +" "+ invent.cantidad +" "+invent.costo+" "+invent.gasto+" "+invent.costoPromedio
                       invent.save flush:true
                    }  
                    
                    existencias.each{
                        
                        if(it.producto.deLinea){
                            
                            def cp= CostoPromedio.findByMesAndEjercicioAndProducto(mes,ejercicio,it.producto)
                            if(cp){
                            	cp.costo=costoPromedio
                            	cp.save flush:true    
                            }
                            
                            
                        }
                        
                        println "Existencia:"+ it.costoPromedio +"  "+it.mes
                     		it.costoPromedio  = costoPromedio 
                        println "Existencia:"+ it.costoPromedio +"  "+it.mes
                       it.save flush:true
                    }
                    
                }
            
        }else{
            /// Costeando sin movimientos
            def existenciaTotal= existencias.sum{it.cantidad}
             if(existenciaTotal){
           
				 println "Producto sin movimientos   "+producto.clave +" pero con existencia:   "+existenciaTotal
                 existencias.each{exis ->
                  	println "Existencia: "+exis.existenciaInicial+"  -- "+exis.costo+" -- "+exis.cantidad+" -- "+ exis.costoPromedio
                     if(exis.costo){
                      	exis.costoPromedio=exis.costo
                         exis.save flush:true
                     }
                 }
             }            
        }
}



    Sql getLocalSql(){
        Sql sql = new Sql(this.dataSource)
        return sql
    }


}

class PeriodoDeCosteo {
    Integer ejercicio
    Integer mes
    PeriodoDeCosteo anterior

    PeriodoDeCosteo(Integer ejercicio, Integer mes) {
        this.ejercicio = ejercicio
        this.mes = mes
    }

    PeriodoDeCosteo getPeriodoAnterior() {
        Integer ejercicioAnterior = ejercicio
        Integer mesAnterior = mes - 1
        if(mes == 1) {
            ejercicioAnterior = ejercicio - 1
            mesAnterior = 12
        }
        return new PeriodoDeCosteo(ejercicioAnterior, mesAnterior)
    }

    String toString() {
        return "${ejercicio} - ${mes}"
    }

}

class AnalisisDeCosto {

    Integer ejercicio
    Integer mes
    String clave
    String descripcion
    BigDecimal inventarioInicial
    BigDecimal costoInicial

    List<Inventario> movimientos
    BigDecimal inventarioFinal
    BigDecimal costoFinal

    AnalisisDeCosto(CostoPromedio cp, List<Inventario> movimientos, List<Existencia> existencias ) {
        this.ejercicio = cp.ejercicio
        this.mes = cp.mes
        this.clave = cp.clave
        this.descripcion = cp.descripcion
        this.movimientos = movimientos
        calcularInentarioInicial(existencias)
    }


    void calcularInentarioInicial(List<Existencia> existencias) {
        this.costoInicial =  existencias.sum 0.0, { Existencia it ->
            def factor = it.producto.unidad == 'MIL' ? 1000 : 1
            return it.cantidad/factor * it.costo
        }
        this.inventarioInicial = existencias.sum 0.0, { it -> it.cantidad}
    }

    void calcilarCosto() {
        this.inventarioFinal =  this.movimientos.sum 0.0, { Inventario it ->
            def factor = it.producto.unidad == 'MIL' ? 1000 : 1
            return it.cantidad/factor * it.costo
        }
    }



}
