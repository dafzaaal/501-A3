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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class Deserializer {

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

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Object deserialize(Document document) throws Exception {
        List objList = document.getRootElement().getChildren();
        Map table = new HashMap();
        createInstances(table, objList);
        assignFieldValues(table, objList);

        for(Object object : table.values()) {
            printObject(object);
        }

        return table.get("0");
    }

    public void createInstances(Map table, List objList)
            throws Exception {
        for (int i = 0; i < objList.size(); i++) {
            Element oElt = (Element) objList.get(i);
            Class cls = Class.forName(oElt.getAttributeValue("class"));
            Object instance = null;
            if (!cls.isArray()) {
                Constructor c = cls.getDeclaredConstructor(null);
                if (!Modifier.isPublic(c.getModifiers())) {
                    c.setAccessible(true);
                }
                instance = c.newInstance(null);
            } else {
                instance = Array.newInstance(
                        cls.getComponentType(),
                        Integer.parseInt(oElt.getAttributeValue("length")));
            }
            table.put(oElt.getAttributeValue("id"), instance);
        }
    }

    public void assignFieldValues(Map table, List objList)
            throws Exception {
        for (int i = 0; i < objList.size(); i++) {
            Element oElt = (Element) objList.get(i);
            Object instance = table.get(oElt.getAttributeValue("id"));
            List fElts = oElt.getChildren();
            if (!instance.getClass().isArray()) {
                for (int j = 0; j < fElts.size(); j++) {
                    Element fElt = (Element) fElts.get(j);
                    String className = fElt.getAttributeValue("declaringclass");
                    Class fieldDC = Class.forName(className);
                    String fieldName = fElt.getAttributeValue("name");
                    Field f = fieldDC.getDeclaredField(fieldName);
                    if (!Modifier.isPublic(f.getModifiers())) {
                        f.setAccessible(true);
                    }
                    Element vElt = (Element) fElt.getChildren().get(0);
                    f.set(instance,
                            deserializeValue(vElt, f.getType(), table));
                }
            } else {
                Class comptype = instance.getClass().getComponentType();
                for (int j = 0; j < fElts.size(); j++) {
                    Array.set(instance, j,
                            deserializeValue((Element) fElts.get(j),
                                    comptype, table));
                }
            }
        }
    }

    public Object deserializeValue(Element vElt,
            Class fieldType,
            Map table)
            throws ClassNotFoundException {
        String valtype = vElt.getName();
        if (valtype.equals("null")) {
            return null;
        } else if (valtype.equals("reference")) {
            return table.get(vElt.getText());
        } else {
            if (fieldType.equals(boolean.class)) {
                if (vElt.getText().equals("true")) {
                    return Boolean.TRUE;
                } else {
                    return Boolean.FALSE;
                }
            } else if (fieldType.equals(byte.class)) {
                return Byte.valueOf(vElt.getText());
            } else if (fieldType.equals(short.class)) {
                return Short.valueOf(vElt.getText());
            } else if (fieldType.equals(int.class)) {
                return Integer.valueOf(vElt.getText());
            } else if (fieldType.equals(long.class)) {
                return Long.valueOf(vElt.getText());
            } else if (fieldType.equals(float.class)) {
                return Float.valueOf(vElt.getText());
            } else if (fieldType.equals(double.class)) {
                return Double.valueOf(vElt.getText());
            } else if (fieldType.equals(char.class)) {
                return new Character(vElt.getText().charAt(0));
            } else {
                return vElt.getText();
            }
        }
    }

    public void printObject(Object classObject) {
        System.out.println();
        System.out.println("-------- Printing Object Information --------");

        System.out.println();
        System.out.println("Class Name: " + classObject.getClass().getName());

        Field[] fields = classObject.getClass().getDeclaredFields();

        if(fields.length == 0) {
            System.out.println("No fields to print");
        }
        else {
            System.out.println();
            System.out.println("--- Printing Field Information ---");
        }

        for(Field field : fields) {
            field.setAccessible(true);
            try {
                System.out.println();
                System.out.println("Field Name: " + field.getName() + ", Field Type: " + field.getType() + ", Field Value: " + field.get(classObject));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(fields.length != 0) {
            System.out.println();
            System.out.println("--- End of Printing Field Information ---");
        }

        System.out.println();
        System.out.println("-------- End of Printing Object Information --------");


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
        Deserializer deserializer = new Deserializer();
        deserializer.listeningForConnections();


    }
}
