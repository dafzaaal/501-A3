import java.io.*;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Scanner;


public class ObjectCreator {

    public static void printMap(Map<?, ?> map) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        int option = 0;
        Map<String, Integer> map = Map.of("one", 1, "two", 2, "three", 3, "four", 4);
        Map<Integer, SimpleObject> createdObjects = new IdentityHashMap<>();
        System.out.println("Create a type of Object. \n");

        while(option != 3) {
            System.out.println("---- Object Creator Menu ----");
            System.out.println("1. Simple Object \n" + "2. An object that contains an array of primitives. \n" +  "3. Finish and send to Serializer \n" + "4. Exit \n");
            option = scanner.nextInt();

            if(!(map.containsValue(option))){
                System.out.println("Invalid choice, exiting program.");
                scanner.close();
                System.exit(1);
            }
            else if(option == 1) {
                SimpleObject simpleObj = new SimpleObject();

                System.out.print("Enter int value for int field in object: ");
                simpleObj.intValue = scanner.nextInt();
                
                System.out.print("Enter double value for double field in object: ");
                simpleObj.doubleValue = scanner.nextDouble();
                
                System.out.print("Enter char value for char value in object: ");
                simpleObj.charValue = scanner.next().charAt(0);
                
                System.out.println("Created Object: " + simpleObj);
                System.out.println("Adding newly created object to the hashmap");

                createdObjects.put(createdObjects.size() + 1, simpleObj);

                printMap(createdObjects);

            }
            else if(option == 3) {
                System.out.println("Now sending objects to serializer");
            }
            else if(option == 4){
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
