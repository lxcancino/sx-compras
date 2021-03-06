package sx.contabilidad.ingresos

import grails.compiler.GrailsCompileStatic
import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Component
import sx.contabilidad.*
import sx.utils.MonedaUtils
import sx.core.Sucursal
import sx.core.Cliente
import sx.cxc.ChequeDevuelto
import sx.cxc.CuentaPorCobrar
import sx.tesoreria.MovimientoDeCuenta


@Slf4j
@Component
class TraspasosCxcTask implements  AsientoBuilder {


    @Override
    def generarAsientos(Poliza poliza, Map params = [:]) {

        def tipo = params.tipo

        log.info("Generando asientos contables para Ingresos {} {}", poliza.fecha)

        String select = getSelect().replaceAll('@FECHA', toSqlDate(poliza.fecha)).replaceAll('@TIPO',tipo)

        // println select

        List rows = getAllRows(select, [])
        log.info('Actualizando poliza {} procesando {} registros', poliza.id, rows.size())
        rows.each { row ->

            
            /* 
            if(row.asiento.startsWith("NOTA_DE_CARGO")){
                
                abonoMoratorios(poliza, row)
                abonoIvaNoTrasladado(poliza, row)
            } */
        
            if(row.asiento.startsWith("CHEQUE_DEVUELTO") && tipo == 'CHE'){
                cargoClientes(poliza, row)
               abonoBanco(poliza,row)
                CuentaContable cuenta = buscarCuenta('208-0001-0000-0000')
                String descripcion  = !row.origen ?
                        "${row.asiento}":
                        "CHE: ${row.documento} ${row.fecha.format('dd/MM/yyyy')} ${row.documentoTipo} ${row.sucursal}"
                PolizaDet det = new PolizaDet(
                        cuenta: cuenta,
                        concepto: cuenta.descripcion,
                        descripcion: descripcion,
                        asiento: row.asiento,
                        referencia: row.referencia2,
                        referencia2: row.referencia2,
                        origen: row.origen,
                        entidad: 'CuentaPorCobrar',
                        documento: row.documento,
                        documentoTipo: row.documentoTipo,
                        documentoFecha: row.fecha,
                        sucursal: row.sucursal,
                        haber: 0.0,
                        debe: row.impuesto
                )
                poliza.addToPartidas(det)

                poliza.addToPartidas( new PolizaDet(
                        cuenta: buscarCuenta('209-0001-0000-0000'),
                        concepto: cuenta.descripcion,
                        descripcion: descripcion,
                        asiento: row.asiento,
                        referencia: row.referencia2,
                        referencia2: row.referencia2,
                        origen: row.origen,
                        entidad: 'CuentaPorCobrar',
                        documento: row.documento,
                        documentoTipo: row.documentoTipo,
                        documentoFecha: row.fecha,
                        sucursal: row.sucursal,
                        haber: row.impuesto,
                        debe: 0.0
                ))


            }

            if(row.asiento.startsWith("TRASPASO_JUR") && tipo == 'JUR'){
                cargoClientes(poliza, row)
                abonoClienteOrigen(poliza, row)

            }
            
        }
    }

    def cargoClientes(Poliza poliza, def row) {
      
        if(row.cta_cliente == null) {
            throw new RuntimeException("No eixste cuenta en Clienes para ${row}")
        }
       
       CuentaContable cuenta = buscarCuenta(row.cta_cliente)
        String descripcion  = !row.origen ?
                "${row.asiento}":
                "CAR: ${row.documento} ${row.fecha.format('dd/MM/yyyy')} ${row.documentoTipo} ${row.sucursal}"
        PolizaDet det = new PolizaDet(
                cuenta: cuenta,
                concepto: cuenta.descripcion,
                descripcion: descripcion,
                asiento: row.asiento,
                referencia: row.referencia2,
                referencia2: row.referencia2,
                origen: row.origen,
                entidad: 'CuentaPorCobrar',
                documento: row.documento,
                documentoTipo: row.documentoTipo,
                documentoFecha: row.fecha,
                sucursal: row.sucursal,
                haber: 0.0,
                debe: row.total
        )
        poliza.addToPartidas(det)
    }

        def abonoMoratorios(Poliza poliza, def row) {

        String tipo = row.documentoTipo
       
        CuentaContable cuenta = buscarCuenta('702-0002-0000-0000')

        switch (tipo) {
            case 'CHE':
                cuenta= buscarCuenta('704-0004-0000-0000')
                break
            case 'CHO':
                cuenta = buscarCuenta('702-0005-0000-0000')
                break
        }

        if(!cuenta)
            throw new RuntimeException("No existe cuenta contable para el abono a ventas del reg: ${row}")
        String descripcion  = !row.origen ?
                "${row.asiento}":
                "CAR: ${row.documento} ${row.fecha.format('dd/MM/yyyy')} ${row.documentoTipo} ${row.sucursal}"
        PolizaDet det = new PolizaDet(
                cuenta: cuenta,
                concepto: cuenta.descripcion,
                descripcion: descripcion,
                asiento: row.asiento,
                referencia: row.referencia2,
                referencia2: row.referencia2,
                origen: row.origen,
                entidad: 'CuentaPorCobrar',
                documento: row.documento,
                documentoTipo: row.documentoTipo,
                documentoFecha: row.fecha,
                sucursal: row.sucursal,
                haber: row.subtotal,
                debe: 0.0
        )
        poliza.addToPartidas(det)
    }

    def abonoIvaNoTrasladado(Poliza poliza, def row) {
        CuentaContable cuenta = buscarCuenta('209-0001-0000-0000')
        String descripcion  = !row.origen ?
                "${row.asiento}":
                "CAR: ${row.documento} ${row.fecha.format('dd/MM/yyyy')} ${row.documentoTipo} ${row.sucursal}"
        PolizaDet det = new PolizaDet(
                cuenta: cuenta,
                concepto: cuenta.descripcion,
                descripcion: descripcion,
                asiento: row.asiento,
                referencia: row.referencia2,
                referencia2: row.referencia2,
                origen: row.origen,
                entidad: 'CuentaPorCobrar',
                documento: row.documento,
                documentoTipo: row.documentoTipo,
                documentoFecha: row.fecha,
                sucursal: row.sucursal,
                haber: row.impuesto,
                debe: 0.0
        )
        poliza.addToPartidas(det)
    }

    def abonoBanco(Poliza poliza, def row){

        CuentaPorCobrar cxc = CuentaPorCobrar.get(row.origen) 

        def cheque =  ChequeDevuelto.findByCxc(cxc)
        
        CuentaContable cuenta =  buscarCuenta("102-0001-0002-0000")
        
        if (cheque) {
            MovimientoDeCuenta movCuenta =cheque.egreso
            cuenta = buscarCuenta('102-0001-'+movCuenta.cuenta.subCuentaOperativa+"-0000")
        }
           
        

        

        if(!cuenta)
            throw new RuntimeException("No existe cuenta contable para el abono a ventas del reg: ${row}")

        String descripcion  = !row.origen ?
                "${row.asiento}":
                "CAR: ${row.documento} ${row.fecha.format('dd/MM/yyyy')} ${row.documentoTipo} ${row.sucursal}"

        PolizaDet det = new PolizaDet(
                cuenta: cuenta,
                concepto: cuenta.descripcion,
                descripcion: descripcion,
                asiento: row.asiento,
                referencia: row.referencia2,
                referencia2: row.referencia2,
                origen: row.origen,
                entidad: 'CuentaPorCobrar',
                documento: row.documento,
                documentoTipo: row.documentoTipo,
                documentoFecha: row.fecha,
                sucursal: row.sucursal,
                haber: row.total,
                debe: 0.0
        )

        poliza.addToPartidas(det)

    }

    def abonoClienteOrigen(Poliza poliza, def row){


        def cxc = CuentaPorCobrar.get(row.origen)
        def claveSuc = ''
        if( new Integer(cxc.sucursal.clave).intValue() < 10){
            claveSuc = "000" + cxc.sucursal.clave
        }else{
            claveSuc = "00" + cxc.sucursal.clave
        }

        def cte = Cliente.get(row.cliente)

        def ctaOperativa = CuentaOperativaCliente.findByCliente(cte).cuentaOperativa

        CuentaContable cuenta = null

        if(row.documentoTipo == 'CON'){
             cuenta = buscarCuenta('105-0001-'+claveSuc+"-0000")
        }
         if(row.documentoTipo == 'COD'){
              cuenta = buscarCuenta('105-0002-'+claveSuc+"-0000")
        }
         if(row.documentoTipo == 'CRE'){
             cuenta = buscarCuenta('105-0003-'+ctaOperativa+"-0000")
        }
         if(row.documentoTipo == 'CHE'){
              cuenta = buscarCuenta('106-0001-'+ctaOperativa+"-0000")
        }



           if(row.cta_cliente == null) {
            throw new RuntimeException("No eixste cuenta en Clientes para ${row}")
        }
       
        String descripcion  = !row.origen ?
                "${row.asiento}":
                "CAR: ${row.documento} ${row.fecha.format('dd/MM/yyyy')} ${row.documentoTipo} ${row.sucursal}"
        PolizaDet det = new PolizaDet(
                cuenta: cuenta,
                concepto: cuenta.descripcion,
                descripcion: descripcion,
                asiento: row.asiento,
                referencia: row.referencia2,
                referencia2: row.referencia2,
                origen: row.origen,
                entidad: 'CuentaPorCobrar',
                documento: row.documento,
                documentoTipo: row.documentoTipo,
                documentoFecha: row.fecha,
                sucursal: row.sucursal,
                haber: row.total,
                debe: 0.0
        )
        poliza.addToPartidas(det) 

    }

    PolizaDet mapRow(String cuentaClave, String descripcion, Map row, def debe = 0.0, def haber = 0.0) {

        CuentaContable cuenta = buscarCuenta(cuentaClave)
        
        PolizaDet det = new PolizaDet(
                cuenta: cuenta,
                concepto: cuenta.descripcion,
                descripcion: descripcion,
                asiento: row.asiento,
                referencia: row.referencia2,
                referencia2: row.referencia2,
                origen: row.origen,
                entidad: row.entidad,
                documento: row.documento,
                documentoTipo: row.documentoTipo,
                documentoFecha: row.fecha,
                sucursal: row.sucursal,
                debe: debe.abs(),
                haber: haber.abs()
        )
        // Datos del complemento
       // asignarComprobanteNacional(det, row)
        // asignarComplementoDePago(det, row)
        return det
    }

    String getSelect() {
        String res = """
        SELECT 
        x.subtotal,
        x.impuesto,
        round(x.total * x.tc, 2) as total,
        x.total as montoTotal,
        x.fecha,
        x.moneda,
        x.tc,
        x.documento,
        x.sucursal,
        x.documentoTipo,
        x.asiento,
        x.referencia2,
        x.origen,
        x.cliente,
        x.cta_cliente,
        x.rfc,
        x.uuid,
        x.entidad
        FROM (
        SELECT concat(f.tipo_documento,'_',f.tipo) as asiento,f.id as origen,f.tipo as documentoTipo,f.fecha,f.documento
        ,f.moneda,f.tipo_de_cambio as tc,f.subtotal,f.impuesto,f.total,c.nombre referencia2,s.nombre as sucursal,f.cliente_id as cliente
        ,concat('106-0001-',(SELECT x.cuenta_operativa FROM cuenta_operativa_cliente x where x.cliente_id=c.id ),'-0000') as cta_cliente, c.rfc,null uuid,'cuentaPorCobrar' entidad   
        FROM cuenta_por_cobrar f join cliente c on(f.cliente_id=c.id)  join sucursal s on(f.sucursal_id=s.id) 
        where  f.fecha='@FECHA' and f.tipo_documento in('CHEQUE_DEVUELTO') and f.tipo='@TIPO' and f.sw2 is null
        union        
        SELECT 'CHEQUE_DEVUELTO_CHE' as asiento,convert(f.id,char) as origen,'CHE' as documentoTipo,f.fecha,f.referencia documento
        ,'MXN' moneda,1.00 as tc,round(-f.importe/1.16,2) subtotal,-f.importe - round(-f.importe/1.16,2) impuesto,-f.importe total
        ,'MOSTRADOR' referencia2,'OFICINAS' as sucursal,'402880fc5e4ec411015e4ecc5dfc0554' as cliente
        ,'106-0001-0510-0000' as cta_cliente,'XAXX010101000' rfc,null uuid,'movimientoDeTesoreria' entidad
        FROM movimiento_de_tesoreria f 
        where  f.fecha='@FECHA' and f.comentario like '%CHEQ%DEV%'       
        UNION   
        SELECT concat('TRASPASO_JUR') as asiento,f.id as origen,f.tipo as documentoTipo,f.fecha,f.documento
        ,f.moneda,f.tipo_de_cambio as tc,f.subtotal,f.impuesto,f.total,c.nombre referencia2,s.nombre sucursal,f.cliente_id   
        ,concat('106-0002-',(SELECT x.cuenta_operativa FROM cuenta_operativa_cliente x where x.cliente_id=c.id ),'-0000') as cta_cliente, c.rfc, x.uuid,'cuentaPorCobrar' entidad
        FROM cuenta_por_cobrar f join cliente c on(f.cliente_id=c.id)  
        join sucursal s on(f.sucursal_id=s.id) left  join cfdi x on(f.cfdi_id=x.id)
        join juridico j on(j.cxc_id=f.id)
        where j.traspaso='@FECHA'         
        ) as x
        """
    }

}
