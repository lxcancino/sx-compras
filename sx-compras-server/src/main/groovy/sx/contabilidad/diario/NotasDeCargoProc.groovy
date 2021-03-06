package sx.contabilidad.diario

import groovy.util.logging.Slf4j
import org.springframework.stereotype.Component
import sx.contabilidad.*
import sx.core.Cliente
import sx.cxc.ChequeDevuelto
import sx.cxc.CuentaPorCobrar
import sx.tesoreria.MovimientoDeCuenta

@Slf4j
@Component
class NotasDeCargoProc implements  ProcesadorDePoliza{

    @Override
    String definirConcepto(Poliza poliza) {
        return "NOTAS DE CARGO ${poliza.fecha.format('dd/MM/yyyy')}"
    }

    @Override
    Poliza recalcular(Poliza poliza) {

        poliza.partidas.clear()
        String select = getSelect().replaceAll('@FECHA', toSqlDate(poliza.fecha))
        List rows = getAllRows(select, [])
        log.info('Actualizando poliza {} procesando {} registros', poliza.id, rows.size())
        rows.each { row ->

                cargoClientes(poliza, row)
            println row
            if(row.asiento.startsWith("NOTA_DE_CARGO")){
                
                abonoMoratorios(poliza, row)
                abonoIvaNoTrasladado(poliza, row)

                if(row.documentoTipo == 'CHO'){
                    ivaNotaCho(poliza, row)
                }
            }

            if(row.asiento.startsWith("CHEQUE_DEVUELTO")){
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

            if(row.asiento.startsWith("TRASPASO_JUR")){

                abonoClienteOrigen(poliza, row)

            }

            
        }

        return poliza
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
        asignarComprobanteNacional(det, row,)
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
        asignarComprobanteNacional(det, row,)
        poliza.addToPartidas(det)
    }

    def ivaNotaCho(Poliza poliza, def row) {

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
                haber: 0.0,
                debe: row.impuesto
        )
        poliza.addToPartidas(det)

        CuentaContable cuenta2= buscarCuenta('208-0001-0000-0000')
        PolizaDet det2 = new PolizaDet(
                cuenta: cuenta2,
                concepto: cuenta2.descripcion,
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
        poliza.addToPartidas(det2)

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

        MovimientoDeCuenta movCuenta = ChequeDevuelto.findByCxc(cxc).egreso

        CuentaContable cuenta = buscarCuenta('102-0001-'+movCuenta.cuenta.subCuentaOperativa+"-0000")

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
        asignarComprobanteNacional(det, row,)
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
        asignarComprobanteNacional(det, row,)
        poliza.addToPartidas(det) 

    }

    void asignarComprobanteNacional(PolizaDet det, def row) {
        det.uuid = row.uuid
        det.rfc = row.rfc
        det.montoTotal = row.montoTotal
        det.moneda = row.moneda
        det.tipCamb = row.tc
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
        x.uuid
        FROM (
        SELECT concat(f.tipo_documento,'_',f.tipo) as asiento,f.id as origen,f.tipo as documentoTipo,f.fecha,f.documento
        ,f.moneda,f.tipo_de_cambio as tc,f.subtotal,f.impuesto,f.total,c.nombre referencia2,s.nombre as sucursal,f.cliente_id as cliente
        ,concat('105-0002-',(case when s.clave>9 then concat('00',s.clave) else concat('000',s.clave) end),'-0000') as cta_cliente, c.rfc, x.uuid        
        FROM cuenta_por_cobrar f join cliente c on(f.cliente_id=c.id)  join nota_de_cargo n on(n.cuenta_por_cobrar_id=f.id)
        join sucursal s on(f.sucursal_id=s.id) LEFT join cfdi x on(n.cfdi_id=x.id)
        where  f.fecha='@FECHA' and f.tipo_documento in('NOTA_DE_CARGO') and f.tipo='COD' and f.sw2 is null and (x.cancelado is false or x.cancelado is null)         
        UNION        
        SELECT concat(f.tipo_documento,'_',f.tipo) as asiento,f.id as origen,f.tipo as documentoTipo,f.fecha,n.folio documento
        ,f.moneda,f.tipo_de_cambio as tc,f.subtotal,f.impuesto,f.total,c.nombre referencia2,s.nombre as sucursal,f.cliente_id as cliente
        ,concat('105-',(SELECT concat(case when x.cuenta_operativa='0266' then concat('0004-',x.cuenta_operativa) else concat('0003-',x.cuenta_operativa) end) FROM cuenta_operativa_cliente x where x.cliente_id=c.id ),'-0000') as cta_cliente, c.rfc, x.uuid           
        FROM cuenta_por_cobrar f join cliente c on(f.cliente_id=c.id)   join nota_de_cargo n on(n.cuenta_por_cobrar_id=f.id)
        join sucursal s on(f.sucursal_id=s.id) LEFT join cfdi x on(n.cfdi_id=x.id)
        where  f.fecha='@FECHA' and f.tipo_documento in('NOTA_DE_CARGO') and f.tipo='CRE' and f.sw2 is null and (x.cancelado is false or x.cancelado is null)    
                UNION
        SELECT concat(f.tipo_documento,'_',f.tipo) as asiento,f.id as origen,f.tipo as documentoTipo,f.fecha,f.documento
        ,f.moneda,f.tipo_de_cambio as tc,f.subtotal,f.impuesto,f.total,c.nombre referencia2,s.nombre as sucursal,f.cliente_id as cliente
        ,concat('106-0001-',(SELECT x.cuenta_operativa FROM cuenta_operativa_cliente x where x.cliente_id=c.id ),'-0000') as cta_cliente, c.rfc, x.uuid    
        FROM cuenta_por_cobrar f join cliente c on(f.cliente_id=c.id) join nota_de_cargo n on(n.cuenta_por_cobrar_id=f.id)
        join sucursal s on(f.sucursal_id=s.id) LEFT join cfdi x on(n.cfdi_id=x.id)
        where  f.fecha='@FECHA' and f.tipo_documento in('NOTA_DE_CARGO') and f.tipo='CHE' and f.sw2 is null and (x.cancelado is false or x.cancelado is null)        
        UNION    
        SELECT concat('TRASPASO_JUR') as asiento,f.id as origen,f.tipo as documentoTipo,f.fecha,f.documento
        ,f.moneda,f.tipo_de_cambio as tc,f.subtotal,f.impuesto,f.total,c.nombre referencia2,s.nombre sucursal,f.cliente_id   
        ,concat('106-0002-',(SELECT x.cuenta_operativa FROM cuenta_operativa_cliente x where x.cliente_id=c.id ),'-0000') as cta_cliente, c.rfc, x.uuid 
        FROM cuenta_por_cobrar f join cliente c on(f.cliente_id=c.id) join nota_de_cargo n on(n.cuenta_por_cobrar_id=f.id)
        join sucursal s on(f.sucursal_id=s.id) left  join cfdi x on(n.cfdi_id=x.id)
        join juridico j on(j.cxc_id=f.id)
        where  f.fecha='@FECHA' and f.tipo_documento in('NOTA_DE_CARGO') and f.tipo='JUR' and f.sw2 is null and (x.cancelado is false or x.cancelado is null)       
        union
        SELECT concat(f.tipo_documento,'_',f.tipo) as asiento,f.id as origen,f.tipo as documentoTipo,f.fecha,f.documento
        ,f.moneda,f.tipo_de_cambio as tc,f.subtotal,f.impuesto,f.total,c.nombre referencia2,s.nombre as sucursal,f.cliente_id as cliente
        ,concat('107-0003-',(SELECT x.cuenta_operativa FROM cuenta_operativa_proveedor x join proveedor p on(x.proveedor_id=p.id) join cliente z on(z.rfc=p.rfc) where z.id=c.id ),'-0000') as cta_cliente, c.rfc, x.uuid    
        FROM cuenta_por_cobrar f join cliente c on(f.cliente_id=c.id) join nota_de_cargo n on(n.cuenta_por_cobrar_id=f.id)
        LEFT join sucursal s on(f.sucursal_id=s.id) LEFT join cfdi x on(n.cfdi_id=x.id)
        where  f.fecha='@FECHA' and f.tipo_documento in('NOTA_DE_CARGO') and f.tipo='CHO' and f.sw2 is null and (x.cancelado is false or x.cancelado is null)
        ) as x
        """
    }
}
