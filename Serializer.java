import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

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
                Element field = new Element("field");
                field.setAttribute("name", f.getName());
                field.setAttribute("declaringclass", f.getDeclaringClass().toString());
                object.addContent(field);
            }
        }

        rootElement.addContent(object);

        printDocument(document);

        
        return document;
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