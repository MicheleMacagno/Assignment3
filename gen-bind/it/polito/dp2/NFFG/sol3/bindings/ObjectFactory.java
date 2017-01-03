//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.01.03 at 04:41:33 PM CET 
//


package it.polito.dp2.NFFG.sol3.bindings;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the it.polito.dp2.NFFG.sol3.bindings package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Nffgs_QNAME = new QName("", "nffgs");
    private final static QName _Policies_QNAME = new QName("", "policies");
    private final static QName _Nffg_QNAME = new QName("", "nffg");
    private final static QName _Policy_QNAME = new QName("", "policy");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: it.polito.dp2.NFFG.sol3.bindings
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link XNffgs }
     * 
     */
    public XNffgs createXNffgs() {
        return new XNffgs();
    }

    /**
     * Create an instance of {@link XPolicies }
     * 
     */
    public XPolicies createXPolicies() {
        return new XPolicies();
    }

    /**
     * Create an instance of {@link XNffg }
     * 
     */
    public XNffg createXNffg() {
        return new XNffg();
    }

    /**
     * Create an instance of {@link XPolicy }
     * 
     */
    public XPolicy createXPolicy() {
        return new XPolicy();
    }

    /**
     * Create an instance of {@link XNodes }
     * 
     */
    public XNodes createXNodes() {
        return new XNodes();
    }

    /**
     * Create an instance of {@link XTraversal }
     * 
     */
    public XTraversal createXTraversal() {
        return new XTraversal();
    }

    /**
     * Create an instance of {@link XNode }
     * 
     */
    public XNode createXNode() {
        return new XNode();
    }

    /**
     * Create an instance of {@link XVerification }
     * 
     */
    public XVerification createXVerification() {
        return new XVerification();
    }

    /**
     * Create an instance of {@link XLinks }
     * 
     */
    public XLinks createXLinks() {
        return new XLinks();
    }

    /**
     * Create an instance of {@link XLink }
     * 
     */
    public XLink createXLink() {
        return new XLink();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XNffgs }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "nffgs")
    public JAXBElement<XNffgs> createNffgs(XNffgs value) {
        return new JAXBElement<XNffgs>(_Nffgs_QNAME, XNffgs.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XPolicies }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "policies")
    public JAXBElement<XPolicies> createPolicies(XPolicies value) {
        return new JAXBElement<XPolicies>(_Policies_QNAME, XPolicies.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XNffg }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "nffg")
    public JAXBElement<XNffg> createNffg(XNffg value) {
        return new JAXBElement<XNffg>(_Nffg_QNAME, XNffg.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XPolicy }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "policy")
    public JAXBElement<XPolicy> createPolicy(XPolicy value) {
        return new JAXBElement<XPolicy>(_Policy_QNAME, XPolicy.class, null, value);
    }

}
