package sx.contabilidad

import groovy.util.logging.Slf4j
import org.springframework.stereotype.Component
import sx.core.Cliente
import sx.core.Sucursal
import sx.tesoreria.CorteDeTarjeta
import sx.tesoreria.CorteDeTarjetaAplicacion
import static sx.contabilidad.Mapeo.*



@Slf4j
@Component
class CobranzaConProc implements  ProcesadorDePoliza{

       static String IVA_NO_TRASLADADO = "209-0001-0000-0000"

    String QUERY = """
                SELECT          
        'DEBE_BANCO' tipo,
        round(x.total/1.16,2) subtotal,
        x.total-round(x.total/1.16,2) impuesto,
        x.total,
        x.fecha,
        x.moneda,
        x.tc,
        x.documento,
        x.sucursal,
        x.suc,
        x.documentoTipo,
        x.asiento,
        x.referencia2,
        x.origen,
        x.cliente,
        x.cta_contable,
        x.cta_iva
        FROM (                                                       
        SELECT concat('COB_','FICHA_',f.origen) as asiento,f.id origen,f.origen documentoTipo,f.fecha,f.folio documento,'MXN' moneda,1 tc 
        ,f.total,f.tipo_de_ficha referencia2,s.nombre sucursal, s.clave as suc,z.descripcion cliente,concat('102-0001-',z.sub_cuenta_operativa,'-0000') as cta_contable
        ,(case when m.por_identificar is true then '208-0003-0000-0000' else '000-0000-0000-0000' end) cta_iva
        FROM ficha f join movimiento_de_cuenta m on(f.ingreso_id=m.id) join cuenta_de_banco z on(m.cuenta_id=z.id)
        join sucursal s on(f.sucursal_id=s.id) left join cobro_cheque x on(x.ficha_id=f.id) left join cobro b on(x.cobro_id=b.id)  
        where f.fecha='2018-02-01' and f.origen in('CON')                       
        UNION                        
        SELECT concat('COB_','DEP_',f.tipo,(case when m.por_identificar is true then '_xIDENT' else '' end)) as asiento,f.id origen,f.tipo documentoTipo,f.primera_aplicacion,x.folio documento,f.moneda,f.tipo_de_cambio tc 
        ,f.importe,c.nombre referencia2,s.nombre sucursal, s.clave as suc,z.descripcion banco,concat((case when M.por_identificar is true then '205-0002-' else '102-0001-' end),z.sub_cuenta_operativa,'-0000') as cta_contable
        ,(case when m.por_identificar is true then '208-0003-0000-0000' else '000-0000-0000-0000' end) cta_iva
        FROM cobro f join cobro_deposito x on(x.cobro_id=f.id) join movimiento_de_cuenta m on(x.ingreso_id=m.id) join cuenta_de_banco z on(m.cuenta_id=z.id)
        join sucursal s on(f.sucursal_id=s.id) join cliente c on(f.cliente_id=c.id)
        where f.primera_aplicacion='2018-02-01' and f.tipo in('CON')                
        UNION        
        SELECT concat('COB_','TRANSF_',f.tipo,(case when m.por_identificar is true then '_xIDENT' else '' end)) as asiento,f.id origen,f.tipo documentoTipo,f.primera_aplicacion,x.folio documento,f.moneda,f.tipo_de_cambio tc 
        ,f.importe,c.nombre referencia2,s.nombre sucursal, s.clave as suc,z.descripcion banco,concat((case when M.por_identificar is true then '205-0002-' else '102-0001-' end),z.sub_cuenta_operativa,'-0000') as cta_contable
        ,(case when m.por_identificar is true then '208-0003-0000-0000' else '000-0000-0000-0000' end) cta_iva
        FROM cobro f join cobro_transferencia x on(x.cobro_id=f.id) join movimiento_de_cuenta m on(x.ingreso_id=m.id) join cuenta_de_banco z on(m.cuenta_id=z.id)
        join sucursal s on(f.sucursal_id=s.id) join cliente c on(f.cliente_id=c.id)
        where f.primera_aplicacion='2018-02-01' and f.tipo in('CON')                  
        UNION	   
	   SELECT concat('COB_','TARJ_CON') as asiento,t.id origen,f.tipo documentoTipo,t.corte,t.folio documento,'MXN'moneda,1.00 tc 
        ,f.importe,a.tipo referencia2,s.nombre sucursal, s.clave as suc,z.descripcion banco,concat('102-0001-',z.sub_cuenta_operativa,'-0000') as cta_contable 
        ,(case when f.por_identificar is true then '208-0003-0000-0000' else '000-0000-0000-0000' end) cta_iva
        from corte_de_tarjeta t join corte_de_tarjeta_aplicacion a on(a.corte_id=t.id)
        join movimiento_de_cuenta f on(a.ingreso_id=f.id) join cuenta_de_banco z on(f.cuenta_id=z.id)
        join sucursal s on(f.sucursal=s.nombre) 
        where t.corte='2018-02-01'               
        ) as x     
            UNION
        SELECT 
        'HABER_VENTA' tipo,
        x.subtotal,
        x.impuesto,
        x.total,
        x.fecha,
        x.moneda,
        x.tc,
        x.documento,
        x.sucursal,
        x.suc,
        x.documentoTipo,
        x.asiento,
        x.referencia2,
        x.origen,
        x.cliente,
        x.cta_contable,
        x.cta_iva
        FROM (                       
        SELECT concat('COB_',(case 	when b.forma_de_pago in('EFECTIVO','CHEQUE') then 'FICHA'when b.forma_de_pago like 'TARJETA%' then 'TARJ'        				
        				when b.forma_de_pago in('TRANSFERENCIA') then 'TRANSF' else substr(b.forma_de_pago,1,3) end),'_',f.tipo,
        				(case when  b.forma_de_pago like 'DEPOSIT%' and (SELECT m.por_identificar FROM cobro_deposito xx join movimiento_de_cuenta m on(xx.ingreso_id=m.id) where xx.cobro_id=b.id)=true then '_xIDENT'
        				when  b.forma_de_pago in('TRANSFERENCIA') and (SELECT m.por_identificar FROM cobro_transferencia xx join movimiento_de_cuenta m on(xx.ingreso_id=m.id)  where xx.cobro_id=b.id)=true then '_xIDENT'
        				else '' end)) as asiento
        ,null as origen,f.tipo documentoTipo,f.fecha,null documento
        ,f.moneda,f.tipo_de_cambio tc,sum(f.subtotal) subtotal,sum(f.impuesto) impuesto,sum(f.total) total,s.nombre referencia2,s.nombre sucursal, s.clave as suc
        ,concat(s.nombre,"_",f.tipo_documento,"_",f.tipo) cliente
        ,concat('105-0001-',(case when s.clave>9 then '00' else '000' end),s.clave,'-0000') as cta_contable,'209-0001-0000-0000' as cta_iva     
        FROM cuenta_por_cobrar f join cliente c on(f.cliente_id=c.id)   join aplicacion_de_cobro a on(a.cuenta_por_cobrar_id=f.id)
        join sucursal s on(f.sucursal_id=s.id) join cfdi x on(f.cfdi_id=x.id) join cobro b on(a.cobro_id=b.id)
        where f.fecha='2018-02-01' and f.tipo in('CON') and f.tipo_documento='VENTA' and f.cancelada is null and f.sw2 is null
        and b.forma_de_pago not in('PAGO_DIF','DEVOLUCION','BONIFICACION') and a.fecha=b.primera_aplicacion        
        group by f.fecha,s.id,f.moneda,f.tipo_de_cambio ,
        			concat('COB_',(case 	when b.forma_de_pago in('EFECTIVO','CHEQUE') then 'FICHA'when b.forma_de_pago like 'TARJETA%' then 'TARJ'        				
        				when b.forma_de_pago in('TRANSFERENCIA') then 'TRANSF' else substr(b.forma_de_pago,1,3) end),'_',f.tipo,
        				(case when  b.forma_de_pago like 'DEPOSIT%' and (SELECT m.por_identificar FROM cobro_deposito xx join movimiento_de_cuenta m on(xx.ingreso_id=m.id) where xx.cobro_id=b.id)=true then '_xIDENT'
        				when  b.forma_de_pago in('TRANSFERENCIA') and (SELECT m.por_identificar FROM cobro_transferencia xx join movimiento_de_cuenta m on(xx.ingreso_id=m.id)  where xx.cobro_id=b.id)=true then '_xIDENT'
        				else '' end))       				        				       					    
	UNION	   
	SELECT 'COB_TARJ_CON' as asiento  ,null as origen,f.tipo documentoTipo,a.fecha,null documento
        ,f.moneda,f.tipo_de_cambio tc,sum(round(a.importe/1.16,2)) subtotal,sum(a.importe-round(a.importe/1.16,2)) impuesto,sum(a.importe) total,s.nombre referencia2,s.nombre sucursal, s.clave as suc
        ,concat(s.nombre,"_",f.tipo_documento,"_",f.tipo) cliente
        ,concat('105-0002-',(case when s.clave>9 then '00' else '000' end),s.clave,'-0000') as cta_contable,'209-0001-0000-0000' as cta_iva
        FROM cuenta_por_cobrar f join cliente c on(f.cliente_id=c.id)   join aplicacion_de_cobro a on(a.cuenta_por_cobrar_id=f.id)
        join sucursal s on(f.sucursal_id=s.id) join cfdi x on(f.cfdi_id=x.id) join cobro b on(a.cobro_id=b.id)
        where a.fecha='2018-08-04' and 
        f.tipo in('COD') and f.tipo_documento='VENTA'and f.cancelada is null and f.sw2 is null
        and b.forma_de_pago like 'TARJ%' 
        group by a.fecha,s.id,f.moneda,f.tipo_de_cambio ,
        			concat('COB_',(case when b.forma_de_pago like 'TARJETA%' then 'TARJ' else substr(b.forma_de_pago,1,3) end),'_',f.tipo)          			           
        ) as x
	   UNION
        SELECT  'HABER_SAF' tipo,
        x.total-round(x.total/1.16,2) subtotal,
        round(x.total/1.16,2) impuesto,
        x.total,
        x.fecha,
        x.moneda,
        x.tc,
        x.documento,
        x.sucursal,
        x.suc,
        x.documentoTipo,
        x.asiento,
        x.referencia2,
        x.origen,
        x.cliente,
        x.cta_contable,
        x.cta_iva
        FROM (        
        SELECT concat('COB_',(case 	when b.forma_de_pago in('EFECTIVO','CHEQUE') then 'FICHA'when b.forma_de_pago like 'TARJETA%' then 'TARJ'        				
        				when b.forma_de_pago in('TRANSFERENCIA') then 'TRANSF' else substr(b.forma_de_pago,1,3) end),'_',f.tipo,
        				(case when  b.forma_de_pago like 'DEPOSIT%' and (SELECT m.por_identificar FROM cobro_deposito xx join movimiento_de_cuenta m on(xx.ingreso_id=m.id) where xx.cobro_id=b.id)=true then '_xIDENT'
        				when  b.forma_de_pago in('TRANSFERENCIA') and (SELECT m.por_identificar FROM cobro_transferencia xx join movimiento_de_cuenta m on(xx.ingreso_id=m.id)  where xx.cobro_id=b.id)=true then '_xIDENT'
        				else '' end),'_SAF') as asiento
        ,b.id as origen,f.tipo documentoTipo,f.fecha,referencia documento
        ,f.moneda,f.tipo_de_cambio tc ,b.importe,b.diferencia,b.importe-b.diferencia-sum( ifnull((SELECT sum(y.importe) FROM aplicacion_de_cobro y where y.cobro_id=b.id),0)) total
        ,s.nombre referencia2,s.nombre sucursal, s.clave as suc,concat(s.nombre,"_",f.tipo_documento,"_",f.tipo) cliente
        ,'205-0001-0001-0000' as cta_contable,'209-0004-0000-0000' as cta_iva        
        FROM cuenta_por_cobrar f join cliente c on(f.cliente_id=c.id)   join aplicacion_de_cobro a on(a.cuenta_por_cobrar_id=f.id)
        join sucursal s on(f.sucursal_id=s.id) join cfdi x on(f.cfdi_id=x.id) join cobro b on(a.cobro_id=b.id)
        where f.fecha='2018-02-01' and f.tipo in('CON') and f.tipo_documento='VENTA' and f.cancelada is null and f.sw2 is null
        and b.forma_de_pago not in('PAGO_DIF','DEVOLUCION','BONIFICACION') and a.fecha=b.primera_aplicacion        
        group by b.id,s.id,f.moneda,f.tipo_de_cambio ,
        			concat('COB_',(case 	when b.forma_de_pago in('EFECTIVO','CHEQUE') then 'FICHA'when b.forma_de_pago like 'TARJETA%' then 'TARJ'        				
        				when b.forma_de_pago in('TRANSFERENCIA') then 'TRANSF' else substr(b.forma_de_pago,1,3) end),'_',f.tipo,
        				(case when  b.forma_de_pago like 'DEPOSIT%' and (SELECT m.por_identificar FROM cobro_deposito xx join movimiento_de_cuenta m on(xx.ingreso_id=m.id) where xx.cobro_id=b.id)=true then '_xIDENT'
        				when  b.forma_de_pago in('TRANSFERENCIA') and (SELECT m.por_identificar FROM cobro_transferencia xx join movimiento_de_cuenta m on(xx.ingreso_id=m.id)  where xx.cobro_id=b.id)=true then '_xIDENT'
        				else '' end),'_SAF') having (b.importe-b.diferencia-sum( ifnull((SELECT sum(y.importe) FROM aplicacion_de_cobro y where y.cobro_id=b.id),0)))<>0        
        ) as x   
	   UNION
	   SELECT  'HABER_OPRD' tipo,
        x.subtotal,
        x.impuesto,
        x.total,
        x.fecha,
        x.moneda,
        x.tc,
        x.documento,
        x.sucursal,
        x.suc,
        x.documentoTipo,
        x.asiento,
        x.referencia2,
        x.origen,
        x.cliente,
        x.cta_contable,
        x.cta_iva
        FROM (          
        SELECT b.id,concat('COB_',(case 	when b.forma_de_pago in('EFECTIVO','CHEQUE') then 'FICHA'when b.forma_de_pago like 'TARJETA%' then 'TARJ'        				
        				when b.forma_de_pago in('TRANSFERENCIA') then 'TRANSF' else substr(b.forma_de_pago,1,3) end),'_',f.tipo,
        				(case when  b.forma_de_pago like 'DEPOSIT%' and (SELECT m.por_identificar FROM cobro_deposito xx join movimiento_de_cuenta m on(xx.ingreso_id=m.id) where xx.cobro_id=b.id)=true then '_xIDENT'
        				when  b.forma_de_pago in('TRANSFERENCIA') and (SELECT m.por_identificar FROM cobro_transferencia xx join movimiento_de_cuenta m on(xx.ingreso_id=m.id)  where xx.cobro_id=b.id)=true then '_xIDENT'
        				else '' end),'_OPRD') as asiento
        ,b.id as origen,f.tipo documentoTipo,f.fecha,referencia documento
        ,f.moneda,f.tipo_de_cambio tc ,round(b.diferencia/1.16,2) subtotal,b.diferencia-round(b.diferencia/1.16,2) impuesto,b.diferencia total
        ,s.nombre referencia2,s.nombre sucursal, s.clave as suc,concat(s.nombre,"_",f.tipo_documento,"_",f.tipo) cliente
        ,'704-0001-0000-0000' as cta_contable,'209-0004-0000-0000' as cta_iva   
        FROM cuenta_por_cobrar f join cliente c on(f.cliente_id=c.id)   join aplicacion_de_cobro a on(a.cuenta_por_cobrar_id=f.id)
        join sucursal s on(f.sucursal_id=s.id) join cfdi x on(f.cfdi_id=x.id) join cobro b on(a.cobro_id=b.id)
        where b.diferencia_fecha='2018-02-01' and f.tipo in('CON') and f.tipo_documento='VENTA' and f.cancelada is null and f.sw2 is null
        and b.forma_de_pago not in('PAGO_DIF') and b.diferencia<>0     
        group by b.id,s.id,f.moneda,f.tipo_de_cambio ,
        			concat('COB_',(case 	when b.forma_de_pago in('EFECTIVO','CHEQUE') then 'FICHA'when b.forma_de_pago like 'TARJETA%' then 'TARJ'        				
        				when b.forma_de_pago in('TRANSFERENCIA') then 'TRANSF' else substr(b.forma_de_pago,1,3) end),'_',f.tipo,
        				(case when  b.forma_de_pago like 'DEPOSIT%' and (SELECT m.por_identificar FROM cobro_deposito xx join movimiento_de_cuenta m on(xx.ingreso_id=m.id) where xx.cobro_id=b.id)=true then '_xIDENT'
        				when  b.forma_de_pago in('TRANSFERENCIA') and (SELECT m.por_identificar FROM cobro_transferencia xx join movimiento_de_cuenta m on(xx.ingreso_id=m.id)  where xx.cobro_id=b.id)=true then '_xIDENT'
        				else '' end),'_OPRD') 
        ) as x 
	   UNION
        SELECT 'DEBE_OGST' tipo,
        x.subtotal,
        x.impuesto,
        x.total,
        x.fecha,
        x.moneda,
        x.tc,
        x.documento,
        x.sucursal,
        x.suc,
        x.documentoTipo,
        x.asiento,
        x.referencia2,
        x.origen,
        x.cliente,
        x.cta_contable,
        x.cta_iva
        FROM (                 
        SELECT concat('COB_','PAGO_DIF_',f.tipo) as asiento,f.id origen,f.tipo documentoTipo,a.fecha,f.documento,f.moneda,f.tipo_de_cambio tc 
        ,round(a.importe/1.16,2) subtotal,a.importe-round(a.importe/1.16,2) impuesto,a.importe total,c.nombre referencia2,s.nombre sucursal, s.clave as suc
        ,concat(s.nombre,"_",f.tipo_documento,"_",f.tipo) cliente,c.nombre,'703-0001-0000-0000' as cta_contable,'209-0001-0000-0000' as cta_iva
        FROM cuenta_por_cobrar f join cliente c on(f.cliente_id=c.id)   join aplicacion_de_cobro a on(a.cuenta_por_cobrar_id=f.id)
        join sucursal s on(f.sucursal_id=s.id) join cfdi x on(f.cfdi_id=x.id) join cobro b on(a.cobro_id=b.id)
        where b.forma_de_pago='PAGO_DIF'  and b.fecha='2018-02-21' and f.tipo in('CON')        
        ) as x
    """

    @Override
    String definirConcepto(Poliza poliza) {
        return "COBRANZA CONTADO"
    }

    @Override
    Poliza recalcular(Poliza poliza) {

        poliza.partidas.clear() 
        String select = QUERY.replaceAll('@FECHA', toSqlDate(poliza.fecha))
        def rows = getAllRows(select, [])

        
        rows.each{ row ->
            if(row.tipo == 'DEBE_BANCO'){

                if(row.referencia2.contains('AMEX_INGRESO')){
                    cargoAbonoBancoAmex(poliza,row,true)
                    cargoAbonoBancoAmex(poliza,row,false)
                }

                if(!row.referencia2.contains('AMEX_COMISION')){
                    cargoBanco(poliza,row)
                }
                
                if(row.asiento.contains('xIDENT')){
                    cargoIvaxIdent(poliza,row)
                } 

                if(row.referencia2.endsWith('COMISION')){
                    cargoComisionBancaria(poliza,row)
                }
                if(row.referencia2.endsWith('COMISION_IVA')){
                    cargoIvaComisionBancaria(poliza,row)
                }
                
            }

            if(row.tipo == 'HABER_VENTA'){
                abonoProvision(poliza,row)

            }

            if(row.tipo == 'HABER_SAF'){
                abonoSaldoAFavor(poliza, row)
            }

            if(row.tipo == 'HABER_OPRD'){
                abonoOtrosProductos(poliza, row)
            }
              if(row.tipo == 'DEBE_OGST'){
                cargoOtrosGastos(poliza, row)
            }
                
            
            

        }
        return poliza
    }

    def cargoAbonoBancoAmex(Poliza poliza,def row,boolean cargo){


        CuentaContable cuenta = buscarCuenta("107-0001-0001-0000")

        if(!cuenta)
            throw new RuntimeException("No existe cuenta contable para el abono a ventas del reg: ${row}")

        String descripcion  = !row.origen ?
                "${row.asiento}":
                "F: ${row.documento} ${row.fecha.format('dd/MM/yyyy')} ${row.documentoTipo} ${row.sucursal}"

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
                debe: Math.abs(row.total)
        )

        if(!cargo){
            det.haber = Math.abs(row.total)
            det.debe = 0.0
        }

        poliza.addToPartidas(det)


    }

    def cargoBanco(Poliza poliza,def row){

    CuentaContable cuenta = buscarCuenta(row.cta_contable)

        if(!cuenta)
            throw new RuntimeException("No existe cuenta contable para el abono a ventas del reg: ${row}")

        String descripcion  = !row.origen ?
                "${row.asiento}":
                "F: ${row.documento} ${row.fecha.format('dd/MM/yyyy')} ${row.documentoTipo} ${row.sucursal}"

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
                debe: Math.abs(row.total)
        )
            if(row.asiento.contains('xIDENT')) 
                det.debe = row.subtotal
            
            if(!row.referencia2.contains('AMEX') && (row.referencia2.contains('COMISION') || row.referencia2.contains('COMISION_IVA') )){
                det.haber = Math.abs(row.total)
                det.debe = 0.0
            }

             if(row.referencia2.contains('AMEX_INGRESO') ){

                CorteDeTarjeta corte = CorteDeTarjeta.get(row.origen) 

                CorteDeTarjetaAplicacion aplicacionComision= CorteDeTarjetaAplicacion.findByCorteAndTipo(corte,'AMEX_COMISION')
                CorteDeTarjetaAplicacion aplicacionIva= CorteDeTarjetaAplicacion.findByCorteAndTipo(corte,'AMEX_COMISION_IVA')

                det.haber = 0.0
                det.debe = Math.abs(row.total)-aplicacionComision.importe-aplicacionIva.importe
            }
            

        
        poliza.addToPartidas(det)
    }



    def cargoComisionBancaria(Poliza poliza,def row){

        def claveSuc = ''
        if( new Integer(row.suc).intValue() < 10){
            claveSuc = "000"+row.suc
        }else{
            claveSuc = "00"+row.suc
        }

        CuentaContable cuenta = buscarCuenta("600-0014-${claveSuc}-0000")
    

        if(!cuenta)
            throw new RuntimeException("No existe cuenta contable para el abono a ventas del reg: ${row}")

        String descripcion  = !row.origen ?
                "${row.asiento}":
                "F: ${row.documento} ${row.fecha.format('dd/MM/yyyy')} ${row.documentoTipo} ${row.sucursal}"

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
                debe: Math.abs(row.total)
        )
        println det
        poliza.addToPartidas(det)
    }

    def cargoIvaComisionBancaria(Poliza poliza,def row){

        CuentaContable cuenta = buscarCuenta("118-0002-0000-0000")
    

        if(!cuenta)
            throw new RuntimeException("No existe cuenta contable para el abono a ventas del reg: ${row}")

        String descripcion  = !row.origen ?
                "${row.asiento}":
                "F: ${row.documento} ${row.fecha.format('dd/MM/yyyy')} ${row.documentoTipo} ${row.sucursal}"

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
                debe: Math.abs(row.total)
        )
        println det
        poliza.addToPartidas(det)
    }

    def cargoIvaxIdent(Poliza poliza,def row){

        CuentaContable cuenta = buscarCuenta(row.cta_iva)

           if(!cuenta)
            throw new RuntimeException("No existe cuenta contable para el abono a ventas del reg: ${row}")

        String descripcion  = !row.origen ?
                "${row.asiento}":
                "F: ${row.documento} ${row.fecha.format('dd/MM/yyyy')} ${row.documentoTipo} ${row.sucursal}"

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
    
        println det
        poliza.addToPartidas(det)

    }

    def abonoOtrosProductos(Poliza poliza, def row){
 
          CuentaContable cuenta = buscarCuenta(row.cta_contable)

        String descripcion  = !row.origen ?
                "${row.asiento}":
                "F: ${row.documento} ${row.fecha.format('dd/MM/yyyy')} ${row.documentoTipo} ${row.sucursal}"

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

      def cargoOtrosGastos(Poliza poliza, def row){
 
        CuentaContable cuenta = buscarCuenta(row.cta_contable)

        String descripcion  = !row.origen ?
                "${row.asiento}":
                "F: ${row.documento} ${row.fecha.format('dd/MM/yyyy')} ${row.documentoTipo} ${row.sucursal}"

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

    def abonoProvision(Poliza poliza,def row){
          if(row.cta_contable == null) {
            throw new RuntimeException("No existe cuenta contable:  ${row.cta_contable}")
        }
        CuentaContable cuenta = CuentaContable.findByClave(row.cta_contable)

        String descripcion  = !row.origen ?
                "${row.asiento}":
                "F: ${row.documento} ${row.fecha.format('dd/MM/yyyy')} ${row.documentoTipo} ${row.sucursal}"

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

    def abonoSaldoAFavor(Poliza poliza, def row){

        CuentaContable cuenta = buscarCuenta(row.cta_contable)

        if(!cuenta)
            throw new RuntimeException("No existe cuenta contable para el abono a ventas del reg: ${row}")

        String descripcion  = !row.origen ?
                "${row.asiento}":
                "F: ${row.documento} ${row.fecha.format('dd/MM/yyyy')} ${row.documentoTipo} ${row.sucursal}"

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

    def abonoIvaVenta(Poliza poliza,def row){

        CuentaContable cuenta = buscarCuenta(IvaNoTrasladadoVentas.clave)
        String descripcion  = !row.origen ?
                "${row.asiento}":
                "F: ${row.documento} ${row.fecha.format('dd/MM/yyyy')} ${row.documentoTipo} ${row.sucursal}"
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

    





}