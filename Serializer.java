import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class Serializer {

    public org.jdom2.Document serialize (Object obj) {

        Class<?> classObj = obj.getClass();

        Element rootElement = new Element("serialized");
        Document document = new Document(rootElement);

        Element object = new Element("object");

        
        object.setAttribute("class", obj.getClass().getName());
        object.setAttribute("id", "0");

        Field[] fields = classObj.getDeclaredFields();

        if(fields.length == 0) {
            System.out.println("Object has no declared fields.");
        }
        else {
            for(Field f : fields) {

                f.setAccessible(true);

                Element fieldInfo = new Element("field");
                Element primitiveValue = new Element("value");

                Class<?> type = f.getType();
                Object value = null;

                //Refactor this, you can put finding the value into a separate method

                try {
                    if (type == int.class) {
                        value = f.getInt(obj);
                    } else if (type == double.class) {
                        value = f.getDouble(obj);
                    } else if (type == char.class) {
                        value = f.getChar(obj);
                    }   
                }
                catch (IllegalAccessException e) {
                    e.printStackTrace(); 
                }
                
  

                primitiveValue.setText(value.toString());
                fieldInfo.addContent(primitiveValue);

                fieldInfo.setAttribute("name", f.getName());
                fieldInfo.setAttribute("declaringclass", f.getDeclaringClass().toString());
                object.addContent(fieldInfo);
            }
        }

        rootElement.addContent(object);

        printDocument(document);


        // NOW SEND TO DESERIAlIZER


        

        
        return document;
    }

    public void sendToDeserializer(Document document) {
        final String HOST = "localhost";
        final int PORT = 8000;

        try (Socket socket = new Socket(HOST, PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                out.println(document);

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