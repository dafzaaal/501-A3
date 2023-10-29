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

            Class<?> classObj = entry.getValue().getClass();

            Element objElement = new Element("object");
            String id = String.valueOf(entry.getKey());
            objElement.setAttribute("class", classObj.getName());
            objElement.setAttribute("id", id);

            
            Field[] fields = classObj.getDeclaredFields();


            if(fields.length == 0) {
                System.out.println("Object has no declared fields");
            }
            else {
                for(Field f : fields) {
                    Element fieldInfo = new Element("field");
                    Element primitiveValue = new Element("value");

                    Class<?> type = f.getType();
                    Object value = null;

                    try {
                        if (type == int.class) {
                            value = f.getInt(entry.getValue());
                        } else if (type == double.class) {
                            value = f.getDouble(entry.getValue());
                        } else if (type == char.class) {
                            value = f.getChar(entry.getValue());
                        }
                        else if(type.isArray()) {
                            Object arrayObj = f.get(entry.getValue());
                            int length = Array.getLength(arrayObj);
                            fieldInfo.setAttribute("name", f.getName());
                            fieldInfo.setAttribute("declaringclass", f.getDeclaringClass().toString());

                            for(int i = 0; i < length; i++) {
                                Element arrayValue = new Element("value");
                                Object arrElement = Array.get(arrayObj, i);
                                arrayValue.setText(arrElement.toString());
                                fieldInfo.addContent(arrayValue);
                            }
                            objElement.addContent(fieldInfo);
                            continue;
                        }
                    }
                    catch (IllegalAccessException e) {
                        e.printStackTrace(); 
                    }

                    fieldInfo.setAttribute("name", f.getName());
                    fieldInfo.setAttribute("declaringclass", f.getDeclaringClass().toString());
                    primitiveValue.setText(value.toString());
                    if(primitiveValue != null) {
                        fieldInfo.addContent(primitiveValue);
                    }
                    objElement.addContent(fieldInfo);

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