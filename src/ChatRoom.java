import java.util.ArrayList;

/**	Classe définissant les salles de chat
 * 
 *	Les salles de chat répertorient :
 *	- Les utilisateurs qui y sont connectés
 *	- Les messages qui y sont envoyés (boite aux lettres)
 **/

public class ChatRoom {
	
	//nom de la salle de chat
	private String name;
	
	//taille maximal du stockage des messages
	private int msgSize;
	
	//boite aux lettres de salle de chat
	private ArrayList<String> msgList;
	
	//répertorie les clients connectés à cette salle
	private ArrayList<Client> clientList;
	
	public ChatRoom(String name,int msgSize){
		System.out.println("Création d'une salle de chat");
		
		//Attribution du nom de la salle de chat
		this.name=name;
		
		//Création du stockage des messages
		this.msgList=new ArrayList<String>();
		this.msgSize=msgSize;
		
		//Création de la liste de clients de cette salle de chat
		this.clientList=new ArrayList<Client>(Chat.getMaxUsers());
	}
	
	//Getters
	protected String getName(){
		return this.name;
	}
	
    protected ArrayList<Client> getClientList(){
    	return this.clientList;
    }
    
    protected ArrayList<String> getMsgList(){
    	return this.msgList;
    }
	
    //Ajoute un client à la salle de chat
	protected void addClient(Client client){
		clientList.add(client);
	}
	
	//Supprime un client de la salle de chat
    protected void delClient(Client client){
    	this.clientList.remove(client);
    }

    //Ajoute un message dans la boite aux lettres
    protected void addMsg(String msg){
		if(this.msgList.size()<this.msgSize){
			this.msgList.add(msg);
		}else{
			this.msgList.remove(0);
			this.msgList.add(msg);
		}
    }
}