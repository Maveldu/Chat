import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

/**	Classe définissant les interfaces clients
 * 	(Threads lancés par le main "Chat")
 * 
 * 	Les interfaces permettent d'intéragir avec un client, d'initialiser la connexion 
 * 	et de définir le client aux yeux du serveur
 * 
 * 	Elles répertorient :
 *	- Le socket du client
 *	- Le nom de l'utilisateur
 *	- Le statut de l'utilisateur (administrateur ou non)
 *	- Le dernier utilisateur qui lui a envoyé un message privé
 *	- Les messages privés de l'utilisateur (boite aux lettres)
 *	- La salle de chat où se situe l'utilisateur
 *
 *	Elle répertorient et instancient également les gestionnaires de flux entrants et sortants
 **/

public class Client implements Runnable{

	//socket du client associé
    private Socket socket;
    
    //nom de l'utilisateur
    private String name=null;
    
    //buffers entrants et sortants (gestionnaires de flux)
    private BufferedWriter outputStream;
    private BufferedReader inputStream;
    
    //dernière personne à avoir envoyé un message à cet utilisateur
    private Client lastWhisp=null;
    
    //boîte aux lettres
    private int msgSize;
	private ArrayList<String> msgList;
	
	//définit si le client est de trop
	private boolean excess;
	
	//sert à stopper le Thread
    private boolean stop=false;
    
    //statut de l'utilisateur
    private boolean admin=false;
    
    //salle de chat où se trouve le client
    private ChatRoom room;

    public Client(Socket s, int msgSize, boolean excess){
		System.out.println("Création d'un client");
		this.socket=s;
		
		//Création de la boîte aux lettres
		this.msgList=new ArrayList<String>();
		this.msgSize=msgSize;
		
		//Si le client est de trop (le serveur de chat est déjà plein)
		this.excess=excess;
    }

    //Fil d'exécution du thread
    public void run(){
    	//vérification de la possibilité d'accepter le client
    	if(this.excess){
    		//le serveur ne peut pas accepter le client, on informe celui-ci et le thread se termine
    		try {
				this.outputStream=new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
			} catch (IOException e) {
				e.printStackTrace();
			}
    		this.print("Le nombre max d'utilisateurs est atteint");this.br();
        	try{
        		outputStream.close();
        	}catch (IOException e){}
    	}else{
    		//le client est accepté
    		
    		//l'échange client/interface est initialisé
			while(!this.init()){}
			
			//gestion de l'arrêt du thread
			while(!stop){
				//le client est invité à envoyer un message
				String message=this.scan();
				//ce message est traîté grâce au module de traitement de message
			    if(message!=null && !message.isEmpty()){
			    	Chat.getMsgHandler().handle(this,message);
			    }
			}
			System.out.println("Sortie du thread client "+this.name);
    	}
    }//fin du thread

    //Initialisation de l'échange
    private boolean init(){
    	//création des flux entrants et sortants pour communiquer avec le client
    	try{
    		this.outputStream=new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
    		this.inputStream=new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    	}catch(IOException e){};
	    
    	//choix du nom d'utilisateur par le client
	    this.print("Bienvenue ! Quel est votre nom (15 caractères max) ? ");
	    String enteredName=this.scan();
	    if(enteredName.length()<=15){
	    	if(Chat.getClient(enteredName)!=null || Chat.getRoom(enteredName)!=null){
	    		this.print("Ce nom est déjà utilisé");
	    		this.br();
	    		return false;
	    	}else{
	    		name=enteredName;
	    	}
	    }else{
	    	if(Chat.getClient(enteredName.substring(0,15))!=null || Chat.getRoom(enteredName.substring(0,15))!=null){
	    		this.print("Ce nom est déjà utilisé");
	    		this.br();
	    		return false;
	    	}else{
	    		name=enteredName.substring(0,15);
	    	}
	    }
	    
	    Chat.getSender().broadcast("(a)System["+Chat.getTime()+"]>"+this.name+" s'est connecté");
	    
	    this.print("Bonjour "+name+", entrez '/bye' pour quitter et '/?' pour plus d'infos.");
	    this.br();
	    return true;
	}

    //Fonction d'envoi de message au client
    protected void print(String str) {
    	try{
    		this.outputStream.write(str);this.outputStream.flush();
		}catch(IOException e){}
    }
    
    //Fonction d'envoi de retour à la ligne
    protected void br(){
    	try{
    		this.outputStream.newLine();this.outputStream.flush();
    		this.outputStream.write(">");this.outputStream.flush();
		}catch(IOException e){}
    }
    
    //Fonction de réception de message du client
    protected String scan(){
    	try{
    		String message = this.inputStream.readLine();
    		this.outputStream.write(">");this.outputStream.flush();
    		return message;
		}catch(IOException e){
			return null;
		}
    }
    
    //Setters
    protected void setName(String name){
    	this.name=name;
    }
    
    protected void setLastWhisp(Client client){
    	this.lastWhisp=client;
    }
    
    protected void setAdmin(){
    	this.admin=true;
    }
    
    protected void setRoom(ChatRoom room){
    	this.room=room;
    }
    
    //Getters
    protected String getName(){
    	return this.name;
    }
    
    protected Client getLastWhisp(){
    	return this.lastWhisp;
    }

    protected ArrayList<String> getMsgList(){
    	return this.msgList;
    }

    protected ChatRoom getRoom(){
    	return this.room;
    }
    
    //Vérifie le statut de l'utilisateur
    protected boolean isAdmin(){
    	return admin;
    }
    
    //Ajoute un message à la boîte aux lettres
    protected void addMsg(String msg){
		if(this.msgList.size()<this.msgSize){
			this.msgList.add(msg);
		}else{
			this.msgList.remove(0);
			this.msgList.add(msg);
		}
    }
    
    //Gestion de l'arrêt du thread
    protected void stop(){
    	this.stop=true;
    	try{
    		//On ferme les flux, ce qui stoppe le readLine dans la boucle du run
    		outputStream.close();
    		inputStream.close();
    	}catch (IOException e){}
    }
}
