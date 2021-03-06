package sx.contabilidad.ingresos

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

import sx.contabilidad.*

@Slf4j
@Component
class IngresosCodProc implements  ProcesadorDePoliza{

   @Autowired
    @Qualifier('ingresosTask')
    IngresosTask ingresosTask

    @Autowired
    @Qualifier('ventasTask')
    VentasTask  ventasTask 


    @Override
    String definirConcepto(Poliza poliza) {
        return "Ingresos COD ${poliza.fecha.format('dd/MM/yyyy')}"
    }

    @Override
    Poliza recalcular(Poliza poliza) {
        println poliza
        poliza.partidas.clear()
        ingresosTask.generarAsientos(poliza, [tipo: 'COD'])
        poliza = poliza.save flush: true, failOnError:true
        return poliza
    }

}
