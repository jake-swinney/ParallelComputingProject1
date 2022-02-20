import java.io.*;
import java.net.*;

public class TCPClient
{
    public static void main(String[] args) throws IOException
    {
        if (args.length == 0)
        {
            System.out.println("One argument required: fileName");
            System.exit(1);
        }
        String fileName = args[0];
        File f = new File(fileName);
        if (!f.exists() || f.isDirectory())
        {
            System.out.println("File could not be opened");
            System.exit(1);
        }
        
        // Variables for setting up connection and communication
        Socket Socket = null; // socket to connect with ServerRouter
        PrintWriter out = null; // for writing to ServerRouter
        BufferedReader in = null; // for reading form ServerRouter
        InetAddress addr = InetAddress.getLocalHost();
        String host = addr.getHostAddress(); // Client machine's IP
        String routerName = "192.168.0.100"; // ServerRouter host name - Raspberry Pi
        int SockNum = 5555; // port number

        // Tries to connect to the ServerRouter
        try
        {
            Socket = new Socket(routerName, SockNum);
            out = new PrintWriter(Socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(Socket.getInputStream()));
        }
        catch (UnknownHostException e)
        {
            System.err.println("Don't know about router: " + routerName);
            System.exit(1);
        }
        catch (IOException e)
        {
            System.err.println("Couldn't get I/O for the connection to: " + routerName);
            System.exit(1);
        }

        String fromServer; // messages received from ServerRouter
        String fromUser; // messages sent to ServerRouter
        String address = "192.168.0.101"; // destination IP (Server) - laptop
        
        if (fileName.endsWith(".txt"))
        {
            // Variables for message passing
            Reader reader = new FileReader(fileName);
            BufferedReader fromFile =  new BufferedReader(reader); // reader for the string file
            //String fromServer; // messages received from ServerRouter
            //String fromUser; // messages sent to ServerRouter
            //String address = "192.168.0.101"; // destination IP (Server) - laptop
            long t0, t1, t;

            // Communication process (initial sends/receives)
            out.println(address); // initial send (IP of the destination Server)
            fromServer = in.readLine(); // initial receive from router (verification of connection)
            System.out.println("ServerRouter: " + fromServer);
            out.println(host); // Client sends the IP of its machine as initial send
            t0 = System.currentTimeMillis();

            // Communication while loop
            while ((fromServer = in.readLine()) != null)
            {
                System.out.println("Server: " + fromServer);
                t1 = System.currentTimeMillis();
                if (fromServer.equals("Bye.")) // exit statement
                    break;
                t = t1 - t0;
                System.out.println("Cycle time: " + t);

                fromUser = fromFile.readLine(); // reading strings from a file
                if (fromUser != null)
                {
                    System.out.println("Client: " + fromUser);
                    out.println(fromUser); // sending the strings to the Server via ServerRouter
                    t0 = System.currentTimeMillis();
                }
                else
                {
                    System.out.println("File read finished. Sending 'Bye.'.");
                    out.println("Bye.");
                    break;
                }
            }
        }
        else
        {
            out.println(address);
            fromServer = in.readLine();
            System.out.println("ServerRouter: " + fromServer);
            out.println(host);
            
            byte[] data = new byte[(int) f.length()];
            //FileInputStream fis = new FileInputStream(f);
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
            bis.read(data, 0, data.length);
            
            out.println("!BYTES:" + data.length);
            OutputStream os = Socket.getOutputStream();
            os.write(data, 0, data.length);
            os.flush();
            
            System.out.println("Transmitted " + data.length + " bytes.");
            
        }

        System.out.println("Closing connection.");
        // closing connections
        out.close();
        in.close();
        Socket.close();
    }
}
