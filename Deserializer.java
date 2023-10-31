import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.input.SAXBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;


public class Deserializer {

    Map<Integer, int[]> refArrays = new IdentityHashMap<>();

    private Document convertXmlToDocument(String xml) {
        SAXBuilder saxBuilder = new SAXBuilder();
        try {
            return saxBuilder.build(new StringReader(xml));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static final int PORT = 8000;

    public void listeningForConnections() {
        System.out.println("Listening for requests...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                    StringBuilder xmlBuilder = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        xmlBuilder.append(line);
                    }

                    String receivedXml = xmlBuilder.toString();
                    System.out.println("Received XML:\n" + receivedXml);

                    Document document = convertXmlToDocument(receivedXml);
                    printDocument(document);
                    deserialize(document);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Object deserialize(Document document) {
        Element root = document.getRootElement();
        List<Element> objectElements = root.getChildren();

        for (Element objectElement : objectElements) {
            String className = objectElement.getAttributeValue("class");

            if(className.equals("int")) {
                continue;
            }

            try {
                Class<?> objClass = Class.forName(className);
                Object classInstance = objClass.newInstance();

                List<Element> fieldElements = objectElement.getChildren("field");
                if (fieldElements.isEmpty()) {
                    continue;
                }
                for (Element fieldElement : fieldElements) {
                    String fieldName = fieldElement.getAttributeValue("name");
                    Field field = objClass.getDeclaredField(fieldName);
                    field.setAccessible(true);


                    Element valueElement = fieldElement.getChild("value");
                    String valueStr = valueElement.getText();

                    if (valueStr != null && !valueStr.trim().isEmpty()) {
                        Class<?> fType = field.getType();
                        if (fType.equals(int.class)) {
                            field.setInt(classInstance, Integer.parseInt(valueStr));
                        } else if (fType.equals(double.class)) {
                            field.setDouble(classInstance, Double.parseDouble(valueStr));
                        } else if (fType.equals(char.class)) {
                            field.setChar(classInstance, valueStr.charAt(0));
                        }
                    }
                }
                visualizeObject(classInstance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public void visualizeObjectWithArray(int[] arrayValues, Object classObject) {
        Class<?> classObj = classObject.getClass();
        boolean flag = false;

        System.out.println();
        System.out.println("---------- Object w/ Primitive Array ----------"); 

        System.out.println();
        System.out.println("Class Name: " + classObj.getName());

        Field[] fields = classObj.getFields();

        if(fields.length != 0) {
            System.out.println();
            System.out.println("--- Printing Fields ---");
            flag = true;
        }

        for(Field field : fields) {

            field.setAccessible(true);

            System.out.println();
            System.out.println("Field Name: " + field.getName());
            System.out.println("Field Type: " + field.getType());

            if(field.getType().isArray()) {
                System.out.println("Array: " + arrayValues);
            }
            System.out.println();
            
        }

      if(flag) {
            System.out.println();
            System.out.println("--- End of Printing Fields ---");
        }


        System.out.println();
        System.out.println("---------- End of Printing Object w/ Primitive Array ----------");

    }

    public void visualizeObject(Object classObject) {

        Class<?> classObj = classObject.getClass();

        System.out.println();
        System.out.println("---------- Printing Object Information ----------");

        System.out.println();
        System.out.println("Class Name: " + classObj.getName());

        Field[] fields = classObj.getDeclaredFields();

        if(fields.length != 0) {
            System.out.println();
            System.out.println("--- Printing Fields ---");
        }

        for(Field field : fields) {

            field.setAccessible(true);

            System.out.println();
            System.out.println("Field Name: " + field.getName());
            System.out.println("Field Type: " + field.getType());

            try {
                Object fieldValue = field.get(classObject);
                System.out.println("Field Value: " + fieldValue);
            }
            catch (IllegalAccessException e) {
                System.out.println(e.getMessage());
            }
            System.out.println();
            
        }
        System.out.println();
        System.out.println("--- End of Printing Fields ---");

        System.out.println();
        System.out.println("---------- End of Printing Object Information ----------");

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

    public static void printMap(Map<?, ?> map) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }


    public static void main(String[] args) {
        Deserializer deserializer = new Deserializer();
        deserializer.listeningForConnections();
    }
}

