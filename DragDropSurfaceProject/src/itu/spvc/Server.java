package itu.spvc;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.mt4j.sceneManagement.IPreDrawAction;

public class Server extends Thread {
	ServerSocket serverSocket = null;
	Socket sock = null;
	
	DataOutputStream outputStream;	
	DataInputStream inputStream;
	
	DragDropScene scene;

	/**
	 *  Create the server and start the thread listening
	 * @param ddscene the scene that will receive image and events
	 */
	public Server(DragDropScene ddscene) {
		super("socketListener");
		this.scene = ddscene;
		this.start();
	}

	public void run () {
		try {
			// we open the connection and 
			// get ready to listen to incoming information
			serverSocket = new ServerSocket(55555);
			sock = serverSocket.accept();
			inputStream = new DataInputStream(sock.getInputStream());
			outputStream = new DataOutputStream(sock.getOutputStream());
			
			System.out.println("Accepted connection : " + sock);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			while (true) {
				//System.out.println("Waiting...");

				// listening for messages
				if (inputStream.available() >0) {
					byte head = inputStream.readByte();
					System.out.println("[operation] " + head);
					// We check the message header 
					// 1 means image coming
					// 2 means coordinates
					// anything else means stop communication
					if (head==1) {
						// We get the size of the image, 
						// to know how much we have to read from the socket
						int size = inputStream.readInt();
						System.out.println("[size] " + size);
						System.out.println(size);
						byte[] imgBytes = new byte[size];
						int soFar = 0;

						// "read" doesn't make sure that all the "size" bytes are read!
						// So while we did not get the full image we keep reading.
						while(soFar < size) {
							int readNow = inputStream.read(imgBytes, soFar, (size-soFar));
							if(readNow > 0) {
								soFar += readNow;
							}
							System.out.println("read " + readNow + ", missing: " + (size-soFar));
						}

						// Once we got the all the byte of the image we rebuild it.
						final Image awtImage = Toolkit.getDefaultToolkit().createImage(imgBytes);
						// And add it to the scene
						scene.registerPreDrawAction(new IPreDrawAction() {
							
							@Override
							public void processAction() {
								scene.addImage(awtImage);
							}
							
							@Override
							public boolean isLoop() {
								// TODO Auto-generated method stub
								return false;
							}
						});
					} 
					else if (head==2) {
						// We get x and y coordinates from the move commands
						// We move the latest image added to the server
						float x = inputStream.readFloat();
						float y = inputStream.readFloat();
						
						System.out.println("[x, y] = " + " [ " + x + ", " + y + " ]");
						
						scene.moveImage(x,y);						
					} 
					else break;
				}
			}
			System.out.println("Closing");

			outputStream.close();
			serverSocket.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void sendImage(String img) throws IOException {
		System.out.println("%%%% server starting sending sequence !");
		
		byte[] bytes = getBytesFromFile(new File(img));
		outputStream.writeByte(1);
		System.out.println("server send file > " + bytes.length);
		outputStream.writeInt(bytes.length);
		outputStream.write(bytes);
		outputStream.flush();
		System.out.println("%%%% server ended sending sequence !");
	}
	
	public static byte[] getBytesFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);

		// Get the size of the file
		long length = file.length();

		if (length > Integer.MAX_VALUE) {
			// File is too large
		}

		// Create the byte array to hold the data
		byte[] bytes = new byte[(int) length];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length
				&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}

		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file "
					+ file.getName());
		}

		// Close the input stream and return bytes
		is.close();
		return bytes;
	}
}
