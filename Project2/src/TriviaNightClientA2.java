import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.DecimalFormat;


/**
 * NtpClient - an NTP client for Java.  This program connects to an NTP server
 * and prints the response to the console.
 * 
 * The local clock offset calculation is implemented according to the SNTP
 * algorithm specified in RFC 2030.  
 * 
 * Note that on windows platforms, the curent time-of-day timestamp is limited
 * to an resolution of 10ms and adversely affects the accuracy of the results.
 * 
 * 
 * This code is copyright (c) Adam Buckley 2004
 *
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the Free 
 * Software Foundation; either version 2 of the License, or (at your option) 
 * any later version.  A HTML version of the GNU General Public License can be
 * seen at http://www.gnu.org/licenses/gpl.html
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for 
 * more details.
 *  
 * @author Adam Buckley
 */

public class TriviaNightClientA2
{
	public static void main(String[] args) throws IOException
	{
                // Server name
		String serverName;
                
                // Long values used to keep track of delay between the message being sent and recieved
                long start;
                long end;
		
                // Reading in the server info from client
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Enter in the server address (use this: 0.north-america.pool.ntp.org): ");
		serverName = inFromUser.readLine();

		// Send request
		DatagramSocket socket = new DatagramSocket();
		InetAddress address = InetAddress.getByName(serverName);
		byte[] buf = new NtpMessage().toByteArray();
		DatagramPacket packet =
			new DatagramPacket(buf, buf.length, address, 123);
		
		// Set the transmit timestamp *just* before sending the packet
		// ToDo: Does this actually improve performance or not?
		NtpMessage.encodeTimestamp(packet.getData(), 40,
			(System.currentTimeMillis()/1000.0) + 2208988800.0);
		
                start = System.currentTimeMillis();
                
		socket.send(packet);
		
		
		// Get response
		System.out.println("NTP request sent, waiting for response...\n");
		packet = new DatagramPacket(buf, buf.length);
		socket.receive(packet);
		
                end = System.currentTimeMillis();
                
		// Immediately record the incoming timestamp
		double destinationTimestamp =
			(System.currentTimeMillis()/1000.0) + 2208988800.0;
		
		
		// Process response
		NtpMessage msg = new NtpMessage(packet.getData());
		
		// Corrected, according to RFC2030 errata
		double roundTripDelay = (destinationTimestamp-msg.originateTimestamp) -
			(msg.transmitTimestamp-msg.receiveTimestamp);
			
		double localClockOffset =
			((msg.receiveTimestamp - msg.originateTimestamp) +
			(msg.transmitTimestamp - destinationTimestamp)) / 2;
		
		
		// Display response
		System.out.println("NTP server IP: " + serverName);
		
                // Below are added output lines
                
                System.out.println("Client IP and Port: ");
                
                System.out.println("Recieve Size: ");
                
                System.out.println("Delay: " + (end - start) + "ms"); // T2 - T1
                
                System.out.println("Response data rate: ");
		
                // Below are original output lines
                
		System.out.println("Dest. timestamp:     " +
			NtpMessage.timestampToString(destinationTimestamp));
		
		System.out.println("Round-trip delay: " +
			new DecimalFormat("0.00").format(roundTripDelay*1000) + " ms");
		
		System.out.println("Local clock offset: " +
			new DecimalFormat("0.00").format(localClockOffset*1000) + " ms");
		
		socket.close();
	}
}
