package oracle.summit.model.views;

import java.sql.Date;

import java.util.List;

import oracle.jbo.domain.DBSequence;
import oracle.jbo.domain.Number;
import oracle.jbo.server.AttributeDefImpl;

import oracle.summit.base.SummitEntityImpl;
import oracle.summit.base.SummitViewRowImpl;
import oracle.summit.model.entities.OrdEOImpl;
// ---------------------------------------------------------------------
// ---    File generated by Oracle ADF Business Components Design Time.
// ---    Fri Mar 25 11:45:11 GMT 2011
// ---    Custom code may be added to this class.
// ---    Warning: Do not modify method signatures of generated methods.
// ---------------------------------------------------------------------
public class OrdVORowImpl extends SummitViewRowImpl {
    /**
     * This is the default constructor (do not remove).
     */
    public OrdVORowImpl() {
    }

    /**
     * Gets OrdEO entity object.
     * @return the OrdEO
     */
    public OrdEOImpl getOrdEO() {
        return (OrdEOImpl)getEntity(0);
    }

    /**
     * Gets EmpEO entity object.
     * @return the EmpEO
     */
    public SummitEntityImpl getEmpEO() {
        return (SummitEntityImpl)getEntity(1);
    }

    /**
     * Gets CustomerEO entity object.
     * @return the CustomerEO
     */
    public SummitEntityImpl getCustomerEO() {
        return (SummitEntityImpl)getEntity(2);
    }


    /**
     * Sample exportable method.
     */
    public void sampleOrdVORowImplExportable() {
    }

    /**
     * Sample exportable method.
     */
    public void sampleOrdVORowImplExportable2(String testParam1) {
    }

    /**
     * Sample exportable method.
     */
    public List<String> sampleOrdVORowImplExportable3(List<String> listParam,
                                                      String testParam1) {
        return listParam;
    }

    public oracle.jbo.domain.Number calculateTimeToShip(Date ordered,
                                                        Date shipped) {

        if (null != shipped) {
            long days =
                (shipped.getTime() - ordered.getTime()) / (1000 * 60 * 60 *
                                                           24);

            return new Number(days);
        } else
            return new Number(0);

    }
}