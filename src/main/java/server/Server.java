package server;
import org.json.simple.parser.ParseException;
import util.ID;
import util.IOThread;
import util.ServerReceiveSockets;

import java.net.InetAddress;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingDeque;

public class Server {

    // Declare the port number, username. ip is not defined.
    private static int port = 4321;
    private static String username = "admin";
    private static InetAddress serverIP;
    private static int timeout = 1000;


    public static void main(String[] args) throws IOException, ParseException {
        System.out.println("Main len: "+args.length);
        try {
            username = args[0];
            port = Integer.parseInt(args[1]);
            serverIP = InetAddress.getByName(args[2]);
        } catch (ArrayIndexOutOfBoundsException | UnknownHostException e){
            System.out.println(e.getStackTrace());
        }
        LinkedBlockingDeque<Socket> socketQueue = new LinkedBlockingDeque<Socket>();
        LinkedBlockingDeque<ID> userList = new LinkedBlockingDeque<ID>();
        IOThread ioThread = null; // used to receive all sockets and store in a queue
        try {
            ioThread = new IOThread(port, socketQueue, timeout);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ioThread.start();

        while (true){
            while (!socketQueue.isEmpty()){
                System.out.println("Server: received a connection!!!");
                Socket socket = socketQueue.pop();
                new ServerReceiveSockets(socket, userList);
                System.out.println("Server: after execution, userList size: "+userList.size());
                socket.close();
            }
        }


//        ServerSocketFactory factory = ServerSocketFactory.getDefault();
//        try(ServerSocket server = factory.createServerSocket(port)){
//            System.out.println(server.getInetAddress());
//            System.out.println("Waiting for client connection..");
//            // Wait for connections.
//            while(true){
//                Socket client = server.accept();
//                counter++;
//                System.out.println("Client "+counter+": Applying for connection!");
//                // Start a new thread for a connection
//                Thread t = new Thread(() -> serveClient(client));
//                t.start();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
//    private static void serveClient(Socket client)
//    {
//        try(Socket clientSocket = client)
//        {
//            // The JSON Parser
//            JSONParser parser = new JSONParser();
//            // Input stream
//            DataInputStream input = new DataInputStream(clientSocket.getInputStream());
//            // Output Stream
//            DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
//            System.out.println("CLIENT: "+input.readUTF());
//            output.writeUTF("Server: Hi Client "+counter+" !!!");
//            // Receive more data..
//            while(true){
//                if(input.available() > 0){
//                    // Attempt to convert read data to JSON
//                    JSONObject command = (JSONObject) parser.parse(input.readUTF());
//                    System.out.println("COMMAND RECEIVED: "+command.toJSONString());
//                    Integer result = parseCommand(command);
//                    JSONObject resObj = new JSONObject();
//                    resObj.put("result", result);
//                    output.writeUTF(resObj.toJSONString());
//                }
//            }
//        } catch (IOException | ParseException e) {
//            e.printStackTrace();
//        }
//    }
//    private static Integer parseCommand(JSONObject command) {
//        int result = 0;
//        if(command.containsKey("command_name")){
//            System.out.println("IT HAS A COMMAND NAME");
//        }
//        if (command.get("command_name").equals("Math"))
//        {
//            Math math = new Math();
//            Integer firstInt = Integer.parseInt(command.get("first_integer").toString());
//            Integer secondInt = Integer.parseInt(command.get("second_integer").toString());
//            switch((String) command.get("method_name"))
//            {
//                case "add":
//                    result = math.add(firstInt,secondInt);
//                    break;
//                case "multiply":
//                    result = math.multiply(firstInt,secondInt);
//                    break;
//                case "subtract":
//                    result = math.subtract(firstInt,secondInt);
//                    break;
//                default:
//                    try
//                    {
//                        throw new Exception();
//                    } catch (Exception e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//            }
//        }
//        // TODO Auto-generated method stub
//        return result;
//    }
}
