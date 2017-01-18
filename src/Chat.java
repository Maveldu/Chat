import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

/**	Classe principale de l'application Chat
 * 	Auteur : Mavel DUSSARTRE
 * 	
 * 	Elle initialise le serveur, contrôle les différents composants de l'application et gère les Threads
 *	
 *	Elle répertorie et instancie :
 *	- Les interfaces client
 *	- Les salles de chat
 *	- Un module d'envoi de message
 *	- Un module de traitement des entrées utilisateurs
 * 
 * 	Elle répertorie également les messages globaux (boite aux lettres)
 **/

public class Chat {
	//attribut permettant de stopper le serveur
	static private boolean stop=false;
	
	//définition des tailles maximales des listes d'users, de salles de chat et de messages
	static private int maxUsers;
	static private int maxRooms;
	static private int maxMsg;
	
	//listes d'utilisateurs, de salles de chat et boite aux lettres globale
	static private ArrayList<Client> clientList;
	static private ArrayList<ChatRoom> roomList;
	static private ArrayList<String> msgList;
	
	//module permettant la diffusion des nouveaux messages aux clients
	static private Sender sender;
	
	//module permettant de déchiffrer ce qu'envoie le client
	static private MessageHandler msgHandler;
	
	//mot de passe administrateur
	static private String password;
	
	//nom de la salle de chat principale
	static private String defaultRoom;
	
	//socket de ce serveur
	static private ServerSocket srvSck;

	//format d'heure pour les conversations
	static private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
	
	//group des threads des clients
	static private ThreadGroup clientGroup;
	
	public static void main (String args[]) {
		Scanner scanner = new Scanner(System.in);
		
		//Définition du nom de la salle de chat principale
		System.out.println("Nom de la salle de chat par défaut : ");
		defaultRoom=scanner.nextLine();
		
		//Création du socket serveur
		try{
		    srvSck=new ServerSocket(2016);
		    System.out.println("Le serveur écoute sur le port 2016");
		}catch (IOException e){
			System.out.println("Le serveur ne peut pas acceder au port 2016");
			Chat.stop=true;
		}
		
		//Choix du mot de passe administrateur
		System.out.println("Mot de passe administrateur : ");
		password=scanner.nextLine();
		
		//Création de la liste d'utilisateurs
		clientList=new ArrayList<Client>();
		System.out.println("Nombre maximum d'utilisateurs : ");
		maxUsers=scanner.nextInt();
		
		//Création de la liste de salles de chat
		roomList=new ArrayList<ChatRoom>();
		System.out.println("Nombre maximum de salles de chat : ");
		maxRooms=scanner.nextInt();
		
		//Création de la liste de messages
		msgList=new ArrayList<String>();
		System.out.println("Nombre maximum de messages stockés : ");
		maxMsg=scanner.nextInt();
		scanner.close();
		
		//Création de la salle de chat par défaut
		newRoom(defaultRoom,maxMsg);
		
		//Création de l'instance qui envoie les messages aux threads client
		sender = new Sender();
		
		//Création de l'instance qui interprete les entrées du client
		msgHandler = new MessageHandler();
		
		//Création du groupe de Threads pour les clients
		clientGroup = new ThreadGroup("Clients");
		
		//Acceptation des clients
		while(!stop){
		    try{
		    	Socket s = srvSck.accept();
		    	if(stop)break;
		    	newClient(s);
			}catch (SocketException e){
				System.out.println("Dans IOException de accept");
			}catch (IOException e){}
		}
		
		//Nettoyage de tous les threads lancés
		shutdown();
		
		System.out.println("Sortie du serveur");
	}

	//Getters
	protected static int getMaxUsers(){
		return maxUsers;
	}
	
	protected static int getMaxRooms(){
		return maxRooms;
	}
	
	protected static int getMaxMsg(){
		return maxMsg;
	}
	
	protected static String getDefRoom(){
		return defaultRoom;
	}
	
	protected static ArrayList<Client> getClientList(){
		return clientList;
	}
	
	protected static ArrayList<ChatRoom> getRoomList(){
		return roomList;
	}
	
	protected static ArrayList<String> getMsgList(){
		return msgList;
	}
	
	protected static Client getClient(String name){
		ArrayList<Client> tmp = new ArrayList<Client>(clientList);
		Iterator<Client> iterator = tmp.iterator();
		while(iterator.hasNext()){
		    Client client = iterator.next();
		    if(client.getName()!=null && client.getName().contentEquals(name)){
		    	return client;
		    }
		    iterator.remove();
		}
		return null;
	}
	
	protected static ChatRoom getRoom(String name){
		ArrayList<ChatRoom> tmp = new ArrayList<ChatRoom>(roomList);
		Iterator<ChatRoom> iterator = tmp.iterator();
		while(iterator.hasNext()){
		    ChatRoom room = iterator.next();
		    if(room.getName()!=null && room.getName().contentEquals(name)){
		    	return room;
		    }
		    iterator.remove();
		}
		return null;
	}

	protected static MessageHandler getMsgHandler(){
		return msgHandler;
	}
	
	protected static Sender getSender(){
		return sender;
	}
	
	//Crée une nouvelle interface utilisateur 
	private static void newClient(Socket sock){
		Client client;
		if(clientList.size()==maxUsers){
			client = new Client(sock,maxMsg,true);
			System.out.println("Nombre maximal de client atteint");
			new Thread(client).start();
		}else{
			client = new Client(sock,maxMsg,false);
			clientList.add(client);
			assignClient(client,getRoom(defaultRoom));
			new Thread(clientGroup,client).start();
		}
	}
	
	//Déconnecte un client et supprime son interface
	protected static void delClient(Client client){
		clientList.remove(client);
		client.stop();
	}

	//Assigne un utilisateur à une salle de chat
	protected static void assignClient(Client client, ChatRoom room){
		room.addClient(client);
		if(client.getRoom()!=null)
			client.getRoom().delClient(client);
		client.setRoom(room);
	}
	
	//Crée une nouvelle salle de chat
	protected static String newRoom(String name,int msgSize){
		ArrayList<ChatRoom> tmp = new ArrayList<ChatRoom>(roomList);
		Iterator<ChatRoom> iterator = tmp.iterator();
		while(iterator.hasNext()){
		    ChatRoom room = iterator.next();
		    if(room.getName()!=null && room.getName().contentEquals(name))
		    	return "Une salle de chat possède déjà ce nom";
		    iterator.remove();
		}
		ChatRoom room = new ChatRoom(name,msgSize);
		if(roomList.size()<maxRooms){
			roomList.add(room);
			return "Salle ajoutée";
		}else{
			room=null;
			return "Nombre maximal de salles de chat atteint";
		}
	}
	
	//Supprime une salle de chat
	protected static void delRoom(ChatRoom room){
		Iterator<Client> iterator = room.getClientList().iterator();
		while(iterator.hasNext()){
		    Client client = iterator.next();
		    Chat.delClient(client);
		    iterator.remove();
		}
		roomList.remove(room);
		room=null;
	}
	
	//Ajoute un message global à la liste de message globaux
	protected static void addMsg(String msg){
		if(msgList.size()<maxMsg){
			msgList.add(msg);
		}else{
			msgList.remove(0);
			msgList.add(msg);
		}
	}
	
	//Renvoie le temps actuel au format HH:mm:ss
	protected static String getTime(){
		LocalDateTime date = LocalDateTime.now();
	    return date.format(formatter);
	}
	
	//Vérifie si le mot de passe est le bon
    protected static boolean isPwd(String pwd){
    	if(pwd.equals(password))
    		return true;
    	else
    		return false;			
    }
    
    //Stoppe l'attente d'utilisateurs
    protected static void stop(){
    	//On arrête de boucler dans le main
    	stop=true;
    	
		//On brise le "accept" dans le main en refermant la socket
		try{
		    new Socket(srvSck.getInetAddress(),srvSck.getLocalPort()).close();
		}catch(SocketException e){
		    System.out.println("SocketException");}
		catch(IOException e){
		    System.out.println("IOException");}
		finally{
		    System.out.println("Finally");}
    }
	
    //Eteint les clients
	protected static void shutdown(){
		Iterator<Client> iterator = clientList.iterator();
		while(iterator.hasNext()){
		    Client client = iterator.next();
		    iterator.remove();
		    delClient(client);
		}
		System.out.println("Sortie de shutdown");
	}
}