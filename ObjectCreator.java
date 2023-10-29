import java.io.*;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Scanner;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;



public class ObjectCreator {

    public static void printMap(Map<?, ?> map) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    public static Object createSimpleObject() {
        Scanner scanner = new Scanner(System.in);
        SimpleObject simpleObj = new SimpleObject();

        System.out.print("Enter int value for int field in object: ");
        simpleObj.intValue = scanner.nextInt();
        
        System.out.print("Enter double value for double field in object: ");
        simpleObj.doubleValue = scanner.nextDouble();
        
        System.out.print("Enter char value for char value in object: ");
        simpleObj.charValue = scanner.next().charAt(0);
        
        System.out.println("Created Object: " + simpleObj);
        System.out.println("Adding newly created object to the hashmap");

        return simpleObj;
    }

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        int option = 0;
        Map<String, Integer> mapOfOptions = Map.of("one", 1, "two", 2, 
        "three", 3, "four", 4, "five", 5);
        Map<Integer, Object> createdObjects = new IdentityHashMap<>();
        System.out.println("Create a type of Object. \n");

        while(option != 3) {
            System.out.println("---- Object Creator Menu ----");
            System.out.println( "1. Simple Object \n" + 
                                "2. An object that contains an array of primitives. \n" +  
                                "3. Create a complex object with references to other objects \n" + 
                                "4. Finish and send to Serializer \n" + 
                                "5. Exit \n");
            option = scanner.nextInt();

            if(!(mapOfOptions.containsValue(option))){
                System.out.println("Invalid choice, exiting program.");
                scanner.close();
                System.exit(1);
            }
            else if(option == 1) {

                Object simpleObj = createSimpleObject();


                createdObjects.put(createdObjects.size() + 1, simpleObj);

                printMap(createdObjects);

            }
            else if(option == 2) {

                PrimitiveArrayObject primitiveArrayObj = new PrimitiveArrayObject();

                System.out.print("Enter the length of the int array: ");
                int length = scanner.nextInt();

                primitiveArrayObj.integerArray = new int[length];

                for(int i = 0; i < length; i++) {
                    System.out.print("Enter the value for the " +  "#" + (i  + 1) + " position" + ": ");
                    primitiveArrayObj.integerArray[i] = scanner.nextInt();
                }
                
                createdObjects.put(createdObjects.size() + 1, primitiveArrayObj);
                printMap(createdObjects);
            }
            else if(option == 3) {
                System.out.println("Now sending objects to serializer");
                System.out.println("\n");
                Serializer serializer = new Serializer();
                serializer.serialize(createdObjects);
            }
            else if(option == 5){
                System.out.println("Exiting Program.");
                scanner.close();
                System.exit(0);
            }

            System.out.println("You Entered: " + option);
        }

        scanner.close();
        System.exit(0);

        

        
    }
}
