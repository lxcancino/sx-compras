package sx.cxp

import org.springframework.stereotype.Component
import grails.compiler.GrailsCompileStatic
import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import groovy.util.slurpersupport.GPathResult
import org.apache.commons.lang3.exception.ExceptionUtils
import org.springframework.web.multipart.MultipartFile
import sx.core.LogUser
import sx.core.Proveedor
import sx.cxp.ComprobanteFiscal



@Component
@Slf4j
class ImportadorCfdi32{

    

    ComprobanteFiscal buildFromXml32(xml, xmlData,fileName, String origen){

       
        String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"

        def data = xml.attributes()
        log.info('Building CFDI 3.2 con File: {} Tipo: {}', fileName, origen)

        def receptorNode = xml.breadthFirst().find { it.name() == 'Receptor'}
        def receptorNombre = receptorNode.attributes()['nombre']
        def receptorRfc = receptorNode.attributes()['rfc']
        

        def emisorNode = xml.breadthFirst().find { it.name() == 'Emisor'}
        def emisorNombre = emisorNode.attributes()['nombre']
        def emisorRfc = emisorNode.attributes()['rfc']


          

        def proveedor = Proveedor.findByRfc(emisorRfc)
        if(!proveedor){
            if(origen == 'GASTOS') {
                proveedor = new Proveedor(nombre: emisorNombre, rfc: emisorRfc, tipo: origen)
                proveedor.clave = "GS${emisorRfc[0..-4]}"
                proveedor.save(failOnError: true, flush: true)
                log.info('Nuevo proveedor registrado {}  ', emisorRfc)
            } else {
                throw new ComprobanteFiscalException(
                        message:"El proveedor de RFC: ${emisorRfc} / ${emisorNombre} no esta dado de alta en el sistema")

            }
        }



        def serie = xml.attributes()['serie']
        def folio = xml.attributes()['folio']

        def fecha = Date.parse(DATE_FORMAT, xml.attributes()['fecha'])
        def timbre = xml.breadthFirst().find { it.name() == 'TimbreFiscalDigital'}
        def uuid = timbre.attributes()['UUID']

       

        def total = data['total'] as BigDecimal
        def subTotal = data['subTotal'] as BigDecimal
        def descuento = data['descuento'] as BigDecimal

        def formaDePago = data['formaDePago']

        String metodoDePago = data['metodoDePago'] as String
        if(metodoDePago && metodoDePago.length() > 2) {
            metodoDePago = '99'
        }
        def tipoDeComp =  data['tipoDeComprobante']
        def tipo='I'
        switch(tipoDeComp) {
            case 'ingreso':
                tipo='I'
            break
            case 'egreso':
                tipo='E'
            break
        }

        def moneda = data['Moneda'] ?: 'MXN'

        if(moneda != 'MXN' || moneda != 'USD') {
            moneda = 'MXN'
        }


        def tipoDeCamio = data['TipoCambio'] as BigDecimal ?: 0.0
        def comprobanteFiscal = ComprobanteFiscal.findByUuid(uuid)

        if(comprobanteFiscal){
            return comprobanteFiscal
           
        }
            
        comprobanteFiscal=new ComprobanteFiscal(
            xml:xmlData,
            proveedor: proveedor,
            fileName: fileName,
            uuid: uuid,
            serie: serie,
            folio: folio,
            emisorNombre: emisorNombre,
            emisorRfc: emisorRfc,
            receptorRfc: receptorRfc,
            receptorNombre: receptorNombre,
            subTotal: subTotal,
            descuento: descuento,
            total: total,
            fecha: fecha,
            formaDePago: formaDePago,
            metodoDePago: metodoDePago,
            tipoDeComprobante: tipo,
            moneda: moneda,
            versionCfdi: '3.2',
            tipoDeCambio: tipoDeCamio?: 1.0,
            tipo: origen)
        comprobanteFiscal = comprobanteFiscal.save failOnError: true, flush: true


        return comprobanteFiscal

    }
}