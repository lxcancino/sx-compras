package sx.tesoreria

import grails.gorm.services.Service

import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import sx.contabilidad.SaldoPorCuentaContable
import sx.core.LogUser
import sx.cxc.CobroDeposito
import sx.cxc.CobroTransferencia
import sx.utils.Periodo


@CompileDynamic
@Slf4j
@Service(SaldoPorCuentaDeBanco)
abstract class SaldoPorCuentaDeBancoService implements  LogUser{

    abstract SaldoPorCuentaDeBanco save(SaldoPorCuentaDeBanco saldoPorCuentaDeBanco)

    SaldoPorCuentaDeBanco actualizarSaldo(String id, Integer ejercicio = Periodo.currentYear(), Integer mes = Periodo.currentMes()) {
        SaldoPorCuentaDeBanco saldo = findOrCreate(CuentaDeBanco.get(id), ejercicio, mes)
        if(!saldo.cierre) {
            saldoInicial(saldo)
            ingresos(saldo)
            egresos(saldo)
            saldo.saldoFinal = saldo.saldoInicial + saldo.ingresos - saldo.egresos
            logEntity(saldo)
            saldo = save(saldo)
            log.debug('Saldo actualizado: {}', saldo)
        }
        return saldo
    }


    SaldoPorCuentaDeBanco findOrCreate(CuentaDeBanco cuenta, Integer ejercicio, Integer mes) {
        SaldoPorCuentaDeBanco saldo = find(cuenta, ejercicio, mes)
        if(!saldo){
            saldo = new SaldoPorCuentaDeBanco(ejercicio: ejercicio, mes: mes, cuenta: cuenta, numero: cuenta.numero, descripcion: cuenta.descripcion)
        }
        return saldo
    }

    SaldoPorCuentaDeBanco find(CuentaDeBanco cuenta, Integer ejercicio, Integer mes) {
        return SaldoPorCuentaDeBanco.where{ cuenta == cuenta && ejercicio == ejercicio &&  mes == mes}.find()
    }

    void saldoInicial(SaldoPorCuentaDeBanco saldo) {
        Integer lastEjercicio = saldo.ejercicio
        Integer lastMes = saldo.mes - 1
        if(lastMes <= 0) {
            lastEjercicio = lastEjercicio - 1
            lastMes = 1
        }
        SaldoPorCuentaDeBanco anterior = SaldoPorCuentaDeBanco
                .where{ cuenta == saldo.cuenta && ejercicio == lastEjercicio &&  mes == lastMes}.find()
        saldo.saldoInicial = anterior ? anterior.saldoFinal : 0.0
        log.info('Saldo Inicial: {}', saldo.saldoInicial)
    }

    @CompileDynamic
    void  ingresos(SaldoPorCuentaDeBanco saldo) {
        def c = MovimientoDeCuenta.createCriteria()
        def res = c.get {
            eq("cuenta", saldo.cuenta)
            gt("importe", 0.0)
            sqlRestriction "year(fecha) = ? and month(fecha) = ? ", [saldo.ejercicio, saldo.mes]
            projections {
                sum('importe')
            }
        }
        saldo.ingresos = res?: 0.0
    }

    @CompileDynamic
    void egresos(SaldoPorCuentaDeBanco saldo) {
        def c = MovimientoDeCuenta.createCriteria()
        def res = c.get {
            eq("cuenta", saldo.cuenta)
            lt("importe", 0.0)
            sqlRestriction "year(fecha) = ? and month(fecha) = ? ", [saldo.ejercicio, saldo.mes]
            projections {
                sum('importe')
            }
        }
        saldo.egresos = res?: 0.0

    }

    BigDecimal calcularSaldoInicial(CuentaDeBanco cuenta, Date fechaInicial) {
        Integer ejercicio = Periodo.obtenerYear(fechaInicial)
        Integer mes = Periodo.obtenerMes(fechaInicial) + 1
        if(ejercicio > 2018) {
            // Utilizar la entidad
            SaldoPorCuentaDeBanco saldo = find(cuenta, ejercicio, mes)
            if(!saldo){
                throw new RuntimeException(
                        "No se ha generado el saldo del periodo ${ejercicio}/${mes} para la cuenta ${cuenta}  ")
            }
            // log.info('SaldoPorCuenta: {}', saldo)
            Date inicio = Periodo.inicioDeMes(fechaInicial)
            // Importe entre el inicio del periodo y la fechaInicial
            BigDecimal movimientos = MovimientoDeCuenta
                    .findAll("""
                    select sum(m.importe) from MovimientoDeCuenta m
                     where date(m.fecha) >= ?  
                       and date(m.fecha) < ?
                       and m.cuenta.id = ?
                       and m.porIdentificar = false
                """,
                    [inicio, fechaInicial, cuenta.id],)[0]?: 0.0 as BigDecimal
            log.debug("Movimientos ({}) >= {} y < {} : {}", cuenta, inicio.format('dd/MM/yyyy'),
                    fechaInicial.format('dd/MM/yyyy'), movimientos)
            BigDecimal saldoInicial = saldo.saldoInicial + movimientos
            log.debug("Saldo Inicial = Saldo al inicio del mes: {} + {} = {}", saldo.saldoInicial, movimientos, saldoInicial)
            return saldoInicial
        } else {
            log.debug('Calculando el saldo inicial considerando todos los movimientos anteriores al {}', fechaInicial)
            Date inicioOperativo = Date.parse('dd/MM/yyyy', '31/12/2017')
            BigDecimal saldoInicial = MovimientoDeCuenta
                    .findAll("""
                    select sum(m.importe) from MovimientoDeCuenta m
                     where date(m.fecha) < ?  
                       and date(m.fecha) >= ?
                       and m.cuenta.id = ?
                       and m.porIdentificar = false
                """,
                    [fechaInicial, inicioOperativo, cuenta.id],)[0]?: 0.0 as BigDecimal
            log.info('Saldo inicial: {}', saldoInicial)
            return saldoInicial
        }
    }

    List<MovimientoDeCuenta> movimientos(CuentaDeBanco cuenta, Date fechaIni, Date fechaFin, boolean cancelados = true) {
        List<MovimientoDeCuenta> movimientos = MovimientoDeCuenta
                .findAll("""
                    from MovimientoDeCuenta m 
                      where date(m.fecha) between ? and ?
                        and m.cuenta.id = ? 
                      order by fecha 
                """,
                [fechaIni, fechaFin, cuenta.id])
        if(cancelados) {
            List<Cheque> chequesCancelados = Cheque
                    .findAll(
                    "from Cheque c where c.fecha between ? and ? and c.cuenta = ? and c.cancelado != null",
                    [fechaIni, fechaFin, cuenta])
            chequesCancelados.each {
                MovimientoDeCuenta mv = new MovimientoDeCuenta()
                mv.cuenta = cuenta
                mv.fecha = it.fecha
                mv.referencia = it.folio.toString()
                mv.comentario = it.nombre
                mv.afavor = it.nombre
                mv.cheque = it
                mv.formaDePago = 'CHEQUE'
                mv.tipo = 'CHE_CANCELADO'
                mv.sucursal = 'OFICINAS'
                mv.importe = 0.0
                mv.conceptoReporte = "CHEQUE CANCELADO: ${it.folio}"
                mv.concepto = 'CHE_CANCELADO'
                mv.dateCreated = it.dateCreated
                mv.lastUpdated = it.lastUpdated
                mv.createUser = it.createUser
                mv.updateUser = it.updateUser
                movimientos << mv
            }
        }
        return movimientos

    }

    def actulizarFechaDeposito(MovimientoDeCuenta ingreso) {
        if(ingreso.formaDePago == 'TRANSFERENCIA') {
            CobroTransferencia transferencia = CobroTransferencia.where{ingreso == ingreso}.find()
            if(transferencia) {
                ingreso.fechaDeposito = transferencia.fechaDeposito
            }

        } else if(ingreso.formaDePago.startsWith('DEPOSITO') ){
            CobroDeposito deposito = CobroDeposito.where{ingreso == ingreso}.find()
            ingreso.fechaDeposito = deposito?.fechaDeposito
        }
    }

    void correrSaldos(String id, Integer ejercicio = Periodo.currentYear()) {
        Integer mes = Periodo.currentMes()
        (1..mes).each { Integer it ->
            actualizarSaldo(id, ejercicio, it)
        }
    }
}
