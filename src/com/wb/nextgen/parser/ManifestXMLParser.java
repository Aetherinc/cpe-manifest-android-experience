package com.wb.nextgen.parser;

import android.content.res.AssetManager;
import android.util.Xml;

import com.wb.nextgen.NextGenApplication;
import com.wb.nextgen.parser.manifest.schema.v1_4.MediaManifestType;
import com.wb.nextgen.util.utils.StringHelper;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Created by gzcheng on 3/16/16.
 */
public class ManifestXMLParser {
    private static final String ns = null;

    private static String MANIFEST_HEADER = "manifest:";

    public MediaManifestType startParsing(){
        MediaManifestType manifest = null;
        try{
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            AssetManager am = NextGenApplication.getContext().getAssets();
            parser.setInput(am.open("mos_hls_manifest_v3.xml"), null);
            parser.nextTag();
            //result = readFeed(parser);

            manifest = parseManifest(parser);
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
        return manifest;
    }

    private Annotation[] getAllInheritedAnnotations(Class classObj){
        List<Annotation> allAnnotations = new ArrayList<Annotation>();
        Class currentClass = classObj;
        while(!currentClass.equals(Object.class)){
            allAnnotations.addAll(Arrays.asList(currentClass.getAnnotations()) );
            currentClass = currentClass.getSuperclass();
        }

        if (allAnnotations.size() > 0){
            return allAnnotations.toArray(new Annotation[allAnnotations.size()]);
        }else
            return new Annotation[0];
    }

    private class FieldClassObject{
        public final Field field;
        public final Class fieldClass;
        public FieldClassObject(Field field, Class fieldClass){
            this.field = field;
            this.fieldClass = fieldClass;
        }
    }

    private <T extends Object> T parseElement(XmlPullParser parser, Class<T> classObj, String tagName){
        T retObj = null;
        HashMap<String, FieldClassObject> classXmlNameToFieldMap = new HashMap<String, FieldClassObject>();

        String currentInClass, currentFieldName = "";

        try {
            retObj = classObj.getConstructor().newInstance();

            Class currentClass = classObj;
            boolean bIsValue = false;
            while(!currentClass.equals(Object.class)){                  //handle XmlElement

                Field declaredFields[] = currentClass.getDeclaredFields();
                for (Field field: declaredFields) {

                    Annotation annotations[] = field.getAnnotations();
                    for (Annotation annotation : annotations) {
                        if (annotation instanceof XmlElement) {
                            classXmlNameToFieldMap.put(((XmlElement) annotation).name(), new FieldClassObject(field, currentClass));
                        } else if (annotation instanceof XmlValue) {
                            bIsValue = true;
                        } else if (annotation instanceof XmlAttribute) {
                            String attributValue = parser.getAttributeValue("", ((XmlAttribute) annotation).name());
                            if (!StringHelper.isEmpty(attributValue)) {
                                Object obj = toObject(field.getType(), attributValue);
                                Method setter = currentClass.getDeclaredMethod("set" + StringHelper.capitalize(((XmlAttribute) annotation).name()), field.getType());
                                setter.invoke(retObj, obj);
                            }
                        }
                    }
                }
                currentClass = currentClass.getSuperclass();
            }

            if (bIsValue){
                try {
                    Method setter = classObj.getDeclaredMethod("setValue", String.class);
                    setter.invoke(retObj, readText(parser));

                } catch (Exception ex) {

                }

            }


            // Handle XmlAttribute
            // Handle XmlVlaue

            //Parse the element
            parser.require(XmlPullParser.START_TAG, ns, tagName);
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String xmlElementName = parser.getName();
                if (xmlElementName.startsWith(MANIFEST_HEADER)){
                    xmlElementName = xmlElementName.replaceFirst(MANIFEST_HEADER, "");
                }
                currentFieldName = xmlElementName;
                if (classXmlNameToFieldMap.containsKey(xmlElementName)){
                    FieldClassObject thisFieldClass = classXmlNameToFieldMap.get(xmlElementName);

                    try {

                        if (thisFieldClass.field.getType().isAnnotationPresent(XmlType.class)) {
                            Object fieldObj = parseElement(parser, thisFieldClass.field.getType(), parser.getName());
                            //thisField.set(thisField.getType(), fieldObj);
                            Method setter = thisFieldClass.fieldClass.getDeclaredMethod("set" + xmlElementName, thisFieldClass.field.getType());
                            setter.invoke(retObj, fieldObj);
                        } else if (thisFieldClass.field.getType().equals(List.class)) {             // no setter for list, should use getter and add


                            if (thisFieldClass.field.getGenericType() instanceof ParameterizedType) {
                                Type elementType = ((ParameterizedType) thisFieldClass.field.getGenericType()).getActualTypeArguments()[0];        // get the List <Type>
                                Object listItmeObj = null;
                                Method getter = thisFieldClass.fieldClass.getDeclaredMethod("get" + xmlElementName);
                                List listObj = (List) getter.invoke(retObj);

                                if (((Class) elementType).isAnnotationPresent(XmlType.class)) {
                                    listItmeObj = parseElement(parser, (Class) elementType, parser.getName());

                                } else {
                                    listItmeObj = toObject(thisFieldClass.field.getType(), readText(parser));
                                }


                                listObj.add(listItmeObj);

                            }


                            //setter.invoke(retObj, listObj);
                        } else {
                            //String stringValue = readText(parser, parser.getName());
                            Object obj = toObject(thisFieldClass.field.getType(), readText(parser));
                            Method setter = thisFieldClass.fieldClass.getDeclaredMethod("set" + xmlElementName, thisFieldClass.field.getType());
                            setter.invoke(retObj, obj);
                        }
                    }catch (IllegalArgumentException iex){
                        System.out.println("PARSER VALUE ERROR " + iex.getLocalizedMessage());
                    }
                }else
                    skip(parser);


            }



        }catch (Exception ex){
            System.out.println(ex.getLocalizedMessage() + "Current injecting class: " + classObj.getName() + " Field: " + currentFieldName );
            ex.printStackTrace();
        }

        return retObj;
    }

    private Object toObject(Class targetClass, String stringValue) throws IOException, XmlPullParserException{

        if (targetClass.equals(String.class)) {
            return stringValue;
        }else if (targetClass.equals(int.class)){
            return Integer.parseInt(stringValue);
        }else if (targetClass.equals(boolean.class)){
            return Boolean.parseBoolean(stringValue);
        }else if (targetClass.equals(BigInteger.class)) {
            return new BigInteger(stringValue);
        }else if (targetClass.equals(Integer.class)){
            return new Integer(stringValue);
        }else if (targetClass.equals(Boolean.class)){
            return new Boolean(stringValue);
        }else if (targetClass.equals(XMLGregorianCalendar.class)){
            try {

                return DatatypeFactory.newInstance().newXMLGregorianCalendar(stringValue);
            }catch (Exception ex){
                return null;
            }
        }else if (targetClass.equals(Duration.class)){
            return new Duration() {
                @Override
                public int getSign() {
                    return 0;
                }

                @Override
                public Number getField(DatatypeConstants.Field field) {
                    return null;
                }

                @Override
                public boolean isSet(DatatypeConstants.Field field) {
                    return false;
                }

                @Override
                public Duration add(Duration rhs) {
                    return null;
                }

                @Override
                public void addTo(Calendar calendar) {

                }

                @Override
                public Duration multiply(BigDecimal factor) {
                    return null;
                }

                @Override
                public Duration negate() {
                    return null;
                }

                @Override
                public Duration normalizeWith(Calendar startTimeInstant) {
                    return null;
                }

                @Override
                public int compare(Duration duration) {
                    return 0;
                }

                @Override
                public int hashCode() {
                    return 0;
                }
            };
        }else {
            return  stringValue;
        }
    }


    private MediaManifestType parseManifest(XmlPullParser parser) throws XmlPullParserException, IOException {
        return parseElement(parser, MediaManifestType.class, "manifest:MediaManifest");

    }

    // For the tags title and summary, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}


