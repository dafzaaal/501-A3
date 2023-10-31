import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class Serializer {

    public Element getReferenceElement(Field field, int id) {
        Element fieldInfo = new Element("field");

        fieldInfo.setAttribute("name", field.getName().toString());
        fieldInfo.setAttribute("declaringclass", field.getDeclaringClass().toString());

        Element reference = new Element("reference");
        reference.setText(String.valueOf(id + 1));

        return fieldInfo.addContent(reference);
    }

    public Element getSimpleObjectField(Field field, Object value) {
        Element fieldInfo = new Element("field");

        fieldInfo.setAttribute("name", field.getName().toString());
        fieldInfo.setAttribute("declaringclass", field.getDeclaringClass().toString());

        Element fieldValue = new Element("value");
        fieldValue.setText(String.valueOf(value));

        return fieldInfo.addContent(fieldValue);
    }


    public Element getArrayElement(Object arrayObject, int id, Field field) {

        int length = Array.getLength(arrayObject);
        String type = arrayObject.getClass().getTypeName();

        Element object = new Element("object");

        object.setAttribute("class", type);
        object.setAttribute("length", String.valueOf(length));
        object.setAttribute("id", String.valueOf(id + 1));

        for(int i = 0; i < length; i++) {
            Element arrayValue = new Element("value");
            Object arrayValueAtIndice = Array.get(arrayObject, i);
            arrayValue.setText(arrayValueAtIndice.toString());
            object.addContent(arrayValue);
        }

        return object;

    }

    public Element getArrayReference(Object arrObject, int id, Field field) {
        Element fieldInfo = new Element("field");
        fieldInfo.setAttribute("name", field.getName());
        fieldInfo .setAttribute("delcaringclass", field.getDeclaringClass().toString());
        
        Element reference = new Element("reference");

        // id of the refering array is always id + 1
        int refID = id + 1;
        reference.setText(String.valueOf(refID));

        fieldInfo.addContent(reference);

        return fieldInfo;
    }


    public org.jdom2.Document serialize (Map<Integer, Object> objects) {

        System.out.println("--- Inside the Serializer ---");
        System.out.println("\n");

        Element rootElement = new Element("serialized");
        Document document = new Document(rootElement);

        if(objects.size() == 0) {
            System.out.println("Object map has size of zero.");
            return document;
        }

        for(Map.Entry<Integer, Object> entry : objects.entrySet()) {

            if(entry.getValue() == null) {
                continue;
            }

            Class<?> classObj = entry.getValue().getClass();
            Element objElement = new Element("object");
            String id = String.valueOf(entry.getKey());
            Integer intID = entry.getKey();
            objElement.setAttribute("class", classObj.getName());
            objElement.setAttribute("id", id);
            
            Field[] fields = classObj.getDeclaredFields();


            if(fields.length == 0) {
                System.out.println("Object has no declared fields");
            }
            else {
                for(Field f : fields) {
                    Class<?> type = f.getType();
                    Object value = null;


                    try {
                        if (type == int.class) {
                            value = f.getInt(entry.getValue());
                            Element fieldElement = getSimpleObjectField(f, value);
                            objElement.addContent(fieldElement);
                        } 
                        else if (type == double.class) {
                            value = f.getDouble(entry.getValue());
                            Element fieldElement = getSimpleObjectField(f, value);
                            objElement.addContent(fieldElement);
                        } 
                        else if (type == char.class) {
                            value = f.getChar(entry.getValue());
                            Element fieldElement = getSimpleObjectField(f, value);
                            objElement.addContent(fieldElement);
                        }
                        else if(type.isArray()) {

                            Object arrayObj = f.get(entry.getValue());
                            Element arrElement = getArrayElement(arrayObj, intID, f);
                            Element arrRefElement = getArrayReference(arrayObj, intID, f);

                            objElement.addContent(arrRefElement);
                            rootElement.addContent(arrElement);

                        }
                        else if(!(type.isPrimitive())) {
                            Element fieldInfo = getReferenceElement(f, intID);
                            objElement.addContent(fieldInfo);
                        }
                    }
                    catch (IllegalAccessException e) {
                        e.printStackTrace(); 
                    }
                }
            }

        
            rootElement.addContent(objElement);

        }

        printDocument(document);


        // NOW SEND TO DESERIAlIZER
        sendToDeserializer(document);


        

        
        return document;
    }

    public void sendToDeserializer(Document document) {
        final String HOST = "localhost";
        final int PORT = 8000;

        try (Socket socket = new Socket(HOST, PORT); PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                XMLOutputter xmlOutputter = new XMLOutputter();
                String xmlString = xmlOutputter.outputString(document);
                out.println(xmlString);

            }

        catch(IOException e){
            e.printStackTrace();;
        }
    }


    public void printDocument(Document document) {
        XMLOutputter xmlOutputter = new XMLOutputter();
        xmlOutputter.setFormat(Format.getPrettyFormat());
        try {
            xmlOutputter.output(document, System.out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        System.out.println("Inside Serializer");
    }
}