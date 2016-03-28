package com.sdis1516t1g02;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;

public class Interface {

    //final static Integer RANGE = 10;
    //final static Integer SIZE = 10;
    final static String REMOTE = "06111995";

    public static void main(String[] args) {

        try {
            if (args.length < 3 || args.length > 4) {
                System.out.println("Numero de argumentos incorreto.");
                return;
            }
            String[] argumentos = args.toString().split(" ");
            String peer_ap = argumentos[0];
            String sub_protocol = argumentos[1];
            String file = argumentos[2];

            Registry registry = null;
            registry = LocateRegistry.getRegistry(null); // iniciar o rmi

            //String random = randomString(SIZE, RANGE);
            RMI_Interface rmiInterface = null;
            rmiInterface = (RMI_Interface) registry.lookup(REMOTE);
            String response = null;

            switch(sub_protocol) {
                case "BACKUP":
                    if (argumentos.length != 4) {
                        System.out.println("Numero de argumentos insuficiente (BACKUP).");
                        return;
                    }
                    String repDegree = argumentos[3];
                    response = rmiInterface.backup(file, Integer.parseInt(repDegree));
                    break;
                case "RESTORE":
                    response = rmiInterface.restore(file);
                    break;
                case "DELETE":
                    response = rmiInterface.delete(file);
                    break;
                case "REMOVE":
                    response = rmiInterface.remove(file);
                    break;
                default:
                    System.out.println("Nome do protocolo incorreto");
                    break;
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }
    /*
    public static String randomString(Integer size, Integer range) {
        String retorno = "";

        for (int i = 0; i < size; i++) {
            Random rn = new Random();
            Integer answer = rn.nextInt(range);
            retorno.concat(answer.toString());
        }
        return retorno;
    }
    */
}
