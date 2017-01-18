import java.util.ArrayList;
import java.util.Iterator;

/**	Classe définissant le module de d'envoi de messages
 * 	
 * 	Le module d'envoi de message fournit des fonctions pour envoyer un message privé, global ou de groupe
 **/

public class Sender {
	public Sender(){}
	
	//envoie un message privé
	protected synchronized void whisper(Client sender,Client receiver,String message){
		//stocke le message dans les boîtes aux lettres de la source et du destinataire
		sender.addMsg(message);
		receiver.addMsg(message);
		//affiche le message chez le client source et chez le client destinataire
		sender.print(message);sender.br();
		receiver.print(message);receiver.br();
		//met la source comme dernier contact au destinataire
		receiver.setLastWhisp(sender);
	}
	
	//envoie un message de groupe
	protected synchronized void multicast(ChatRoom room,String message){
		//stocke le message dans la boîte au lettre de la salle de chat
		room.addMsg(message);
		//récupère les clients connectés à la salle de chat
		ArrayList<Client> clientList = new ArrayList<Client>(room.getClientList());
		//envoie le message à chaque utilisateur connecté à la salle de chat
		Iterator<Client> iterator = clientList.iterator();
		while(iterator.hasNext()){
		    Client client = iterator.next();
		    client.print(message);client.br();
		    iterator.remove();
		}
	}
	
	//envoie un message global
	protected synchronized void broadcast(String message){
		//stocke le message dans la boîte au lettre globale
		Chat.addMsg(message);
		//récupère les clients connectés au serveur
		ArrayList<Client> clientList = new ArrayList<Client>(Chat.getClientList());
		//envoie le message à chaque utilisateur
		Iterator<Client> iterator = clientList.iterator();
		while(iterator.hasNext()){
		    Client client = iterator.next();
		    client.print(message);client.br();
		    iterator.remove();
		}
	}
}
