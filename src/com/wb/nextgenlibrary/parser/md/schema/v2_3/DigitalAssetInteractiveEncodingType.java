//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.02.08 at 03:02:05 PM PST 
//


package com.wb.nextgenlibrary.parser.md.schema.v2_3;

import java.util.ArrayList;
import java.util.List;
import com.wb.nextgenlibrary.parser.XmlAccessType;
import com.wb.nextgenlibrary.parser.XmlAccessorType;
import com.wb.nextgenlibrary.parser.XmlAnyElement;
import com.wb.nextgenlibrary.parser.XmlElement;
import com.wb.nextgenlibrary.parser.XmlType;


/**
 * <p>Java class for DigitalAssetInteractiveEncoding-type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DigitalAssetInteractiveEncoding-type"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="RuntimeEnvironment" type="{http://www.movielabs.com/schema/md/v2.3/md}string-Interactive-Enc-RuntimeEnvironment"/&gt;
 *         &lt;element name="FirstVersion" type="{http://www.movielabs.com/schema/md/v2.3/md}string-Interactive-Enc-Version" minOccurs="0"/&gt;
 *         &lt;element name="LastVersion" type="{http://www.movielabs.com/schema/md/v2.3/md}string-Interactive-Enc-Version" minOccurs="0"/&gt;
 *         &lt;any namespace='##other' maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DigitalAssetInteractiveEncoding-type", propOrder = {
    "runtimeEnvironment",
    "firstVersion",
    "lastVersion",
    "any"
})
public class DigitalAssetInteractiveEncodingType {

    @XmlElement(name = "RuntimeEnvironment", required = true)
    protected String runtimeEnvironment;
    @XmlElement(name = "FirstVersion")
    protected String firstVersion;
    @XmlElement(name = "LastVersion")
    protected String lastVersion;
    @XmlElement(name = "EnvironmentAttribute")
    protected List<String> environmentAttribute;
    @XmlAnyElement(lax = true)
    protected List<Object> any;

    /**
     * Gets the value of the runtimeEnvironment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRuntimeEnvironment() {
        return runtimeEnvironment;
    }

    /**
     * Sets the value of the runtimeEnvironment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRuntimeEnvironment(String value) {
        this.runtimeEnvironment = value;
    }

    /**
     * Gets the value of the firstVersion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFirstVersion() {
        return firstVersion;
    }

    /**
     * Sets the value of the firstVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFirstVersion(String value) {
        this.firstVersion = value;
    }

    /**
     * Gets the value of the lastVersion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLastVersion() {
        return lastVersion;
    }

    /**
     * Sets the value of the lastVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLastVersion(String value) {
        this.lastVersion = value;
    }

    /**
     * Gets the value of the environmentAttribute property.
     *
     * @return
     *     possible object is
     *     {@link List<String> }
     *
     */
    public List<String> getEnvironmentAttribute() {
        if (environmentAttribute == null) {
            environmentAttribute = new ArrayList<String>();
        }
        return this.environmentAttribute;
    }

    /**
     * Gets the value of the any property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the any property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAny().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getAny() {
        if (any == null) {
            any = new ArrayList<Object>();
        }
        return this.any;
    }

}
