import java.io.*;
import java.net.*;
import java.util.Scanner;

public class TCPClient
{
	public static void main(String[] args) throws IOException
	{
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
		Scanner scan = new Scanner(System.in);

		System.out.println("Please enter your file's path: ");
		String path = scan.nextLine();
		out.println(path);

		System.out.println("Do you want to send a text file? (Y/N)");
		String input = scan.nextLine();
		out.println(input);

		//Text file
		if(input.toUpperCase().equals("Y"))
		{
			// Variables for message passing
			Reader reader = new FileReader("file.txt");
			System.out.println("Please input file path:");
			scan.close();

			BufferedReader fromFile =  new BufferedReader(reader); // reader for the string file
			String fromServer; // messages received from ServerRouter
			String fromUser; // messages sent to ServerRouter
			String address = "192.168.0.101"; // destination IP (Server) - laptop
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
			}

			// closing connections
			fromFile.close();
			out.close();
			in.close();
			Socket.close();
		}
		//Video/audio file
		else
		{
			OutputStream outFile = null;
			InputStream inFile = null;

			try
			{
				outFile = Socket.getOutputStream();
				//inFile = new FileInputStream(f);
			}
			catch(IOException e)
			{
				System.out.println("Couldn't get I/O for the connection to: " + routerName);
			}

			try 
			{
				inFile = new FileInputStream(path);
				//outFile = Socket.getOutputStream();
			} 
			catch (FileNotFoundException ex) 
			{
				System.out.println("File not found. ");
			}

			byte[] bytes = new byte[16 * 1024];

			int count;
			while((count = inFile.read(bytes)) > 0)
			{
				outFile.write(bytes, 0, count);
			}

			inFile.close();
			outFile.close();
			Socket.close();
		}
	}
}
