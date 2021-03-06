package sx.cfdi.v33

import groovy.util.slurpersupport.GPathResult
import groovy.xml.XmlUtil
import lx.cfdi.v33.CMoneda
import lx.cfdi.v33.CfdiUtils
import lx.cfdi.v33.Comprobante
import sx.cfdi.Cfdi
import sx.utils.MonedaUtils

class V33CfdiUtils {

	static Comprobante toComprobante(Cfdi cfdi, Byte[] xmlFile){
        Comprobante comprobante = CfdiUtils.read(xmlFile)
        return comprobante
	}

    static List getPartidas(Cfdi cfdi, Byte[] xmlFile) {
        Comprobante comprobante = CfdiUtils.read(xmlFile)
        comprobante.getConceptos().concepto
    }

    /**
     * Pritty XML del comprobante
     *
     * @param xmlData
     * @return
     */
    static String parse(byte[] xmlData){
        ByteArrayInputStream is=new ByteArrayInputStream(xmlData)
        GPathResult xmlResult = new XmlSlurper().parse(is)
        return XmlUtil.serialize(xmlResult)
    }

	static getMonedaCode(Currency moneda){
		if(moneda == MonedaUtils.PESOS)
			return CMoneda.MXN
		else 
			return CMoneda.USD
	}

}