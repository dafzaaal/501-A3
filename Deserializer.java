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

    HashMap<Integer, List<Integer>> referenceMap = new HashMap<>();

    public void mappingReferences(Element rootElement) {
        List<Element> objectElements = rootElement.getChildren();
        
        for(Element objectElement : objectElements) {
            int objectID = Integer.valueOf(objectElement.getAttributeValue("id"));
            
            List<Integer> referenceIDs = new ArrayList<>();
            for (Element child : objectElement.getChildren("reference")) {
                int referenceID = Integer.valueOf(child.getText());
                referenceIDs.add(referenceID);
            }
            
            if (!referenceIDs.isEmpty()) {
                referenceMap.put(objectID, referenceIDs);
            }
        }
        printReferenceMap(referenceMap);
    }
    
    
    private Document convertXmlToDocument(String xml) {
        SAXBuilder saxBuilder = new SAXBuilder();
        try {
            return saxBuilder.build(new StringReader(xml));
        } 
        catch (Exception e) {
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

                } 
                catch (IOException e) {
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
            if(className.equals("PrimitiveArrayObject")) {
                visualizeObjectWithArray(objectElement);
            }
            else if(className.equals("int[]")) {
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
                        } 
                        else if (fType.equals(double.class)) {
                            field.setDouble(classInstance, Double.parseDouble(valueStr));
                        } 
                        else if (fType.equals(char.class)) {
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


    public void visualizeObjectWithArray(Element  objectElement) {
        String className = objectElement.getAttributeValue("class");

        try {
            Class<?> objClass = Class.forName(className);
            Object classInstance = objClass.newInstance();

            List<Element> fieldElements = objectElement.getChildren("field");

            for(Element element : fieldElements) {
                String fieldName = element.getAttributeValue("name");
                Field field = objClass.getDeclaredField(fieldName);
                field.setAccessible(true);
            }
        }

        catch (Exception e) {
            e.printStackTrace();
        }
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

    public void printReferenceMap(HashMap<Integer, List<Integer>> referenceMap) {
        for (Map.Entry<Integer, List<Integer>> entry : referenceMap.entrySet()) {
            int objectID = entry.getKey();
            List<Integer> references = entry.getValue();
            
            System.out.print("Object ID: " + objectID + " -> References: ");
            for (int i = 0; i < references.size(); i++) {
                System.out.print(references.get(i));
                if (i < references.size() - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println();
        }
    }
    


    public static void main(String[] args) {
        Deserializer deserializer = new Deserializer();
        deserializer.listeningForConnections();
    }
}

