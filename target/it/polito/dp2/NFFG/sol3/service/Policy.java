//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.12.30 at 03:22:11 PM CET 
//


package it.polito.dp2.NFFG.sol3.service;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Policy complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Policy">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{}NameType"/>
 *         &lt;element name="positivity" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="src" type="{}NameType"/>
 *         &lt;element name="dst" type="{}NameType"/>
 *         &lt;element name="traversal" type="{}Traversal" minOccurs="0"/>
 *         &lt;element name="verification" type="{}Verification" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="nffg" type="{}NameType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Policy", propOrder = {
    "name",
    "positivity",
    "src",
    "dst",
    "traversal",
    "verification"
})
public class Policy {

    @XmlElement(required = true)
    protected String name;
    protected boolean positivity;
    @XmlElement(required = true)
    protected String src;
    @XmlElement(required = true)
    protected String dst;
    protected Traversal traversal;
    protected Verification verification;
    @XmlAttribute(name = "nffg")
    protected String nffg;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the positivity property.
     * 
     */
    public boolean isPositivity() {
        return positivity;
    }

    /**
     * Sets the value of the positivity property.
     * 
     */
    public void setPositivity(boolean value) {
        this.positivity = value;
    }

    /**
     * Gets the value of the src property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSrc() {
        return src;
    }

    /**
     * Sets the value of the src property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSrc(String value) {
        this.src = value;
    }

    /**
     * Gets the value of the dst property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDst() {
        return dst;
    }

    /**
     * Sets the value of the dst property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDst(String value) {
        this.dst = value;
    }

    /**
     * Gets the value of the traversal property.
     * 
     * @return
     *     possible object is
     *     {@link Traversal }
     *     
     */
    public Traversal getTraversal() {
        return traversal;
    }

    /**
     * Sets the value of the traversal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Traversal }
     *     
     */
    public void setTraversal(Traversal value) {
        this.traversal = value;
    }

    /**
     * Gets the value of the verification property.
     * 
     * @return
     *     possible object is
     *     {@link Verification }
     *     
     */
    public Verification getVerification() {
        return verification;
    }

    /**
     * Sets the value of the verification property.
     * 
     * @param value
     *     allowed object is
     *     {@link Verification }
     *     
     */
    public void setVerification(Verification value) {
        this.verification = value;
    }

    /**
     * Gets the value of the nffg property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNffg() {
        return nffg;
    }

    /**
     * Sets the value of the nffg property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNffg(String value) {
        this.nffg = value;
    }

}